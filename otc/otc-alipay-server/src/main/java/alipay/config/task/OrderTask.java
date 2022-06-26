package alipay.config.task;

import alipay.config.redis.RedisUtil;
import alipay.manage.api.AccountApiService;
import alipay.manage.api.deal.WitPay;
import alipay.manage.bean.DealOrder;
import alipay.manage.bean.RunOrder;
import alipay.manage.bean.UserInfo;
import alipay.manage.mapper.DealOrderMapper;
import alipay.manage.service.RunOrderService;
import alipay.manage.service.WithdrawService;
import alipay.manage.util.OrderUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.bean.dealpay.Withdraw;
import otc.result.Result;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Component
public class OrderTask {
	static final String KEY = "TASK:ORDER:ST:";
	static final String KEY_WIT = "TASK:ORDER:STWIT:";
	static final String KEY_WIT_PUSH = "TASK:ORDER:PUSH:";
	/**
	 * <p>定时任务修改订单状态</p>
	 */
	private static final Log log = LogFactory.get();
	@Autowired
	RunOrderService RunOrderServiceimpl;
	@Resource
	private DealOrderMapper dealOrderDao;
	@Autowired
	private OrderUtil orderUtilImpl;
	@Autowired
	private RedisUtil redis;
	@Autowired
	private alipay.config.redis.RedisLockUtil RedisLockUtil;
	@Autowired
	private WithdrawService withdrawServiceImpl;
	@Autowired
	private WitPay WitPayImpl;
	@Autowired BankOpen bankOpen;
	private List<Withdraw> orderList;

	/**
	 * 每十秒结算一次
	 */
	public void orderTask() {
        ThreadUtil.execute(() -> {
            /*
             * 主动修改10分钟未确认的单子为 未收到回调状态
             *  商户充值订单
             * 	卡商充值订单
             */
            int a = dealOrderDao.updateUnNotify();
        });
        RedisLockUtil.redisLock(KEY + "lock");
        List<DealOrder> orderList = dealOrderDao.findSuccessAndNotAmount();
        for (DealOrder order : orderList) {
            if (redis.hasKey(KEY + order.getOrderId())) {
                log.info("当前订单已处理，当前订单号：" + order.getOrderId());
                continue;
            }
            ;
            redis.set(KEY + order.getOrderId(), order.getOrderId(), 2000); //防止多个任务同时获取一个订单发起结算
            try {
				List<RunOrder> runOrderList = RunOrderServiceimpl.findAssOrder(order.getOrderId());
				List<RunOrder> runOrderList1 = new ArrayList<>();
				if (!order.getAssociatedId().contains("W")) {
					runOrderList1 = RunOrderServiceimpl.findAssOrder(order.getAssociatedId());
				}
				if (CollUtil.isNotEmpty(runOrderList) || CollUtil.isNotEmpty(runOrderList1)) {
					dealOrderDao.updateSuccessAndAmount(order.getOrderId());
					continue;
				}
				Result settlement = orderUtilImpl.settlement(order);
				if (settlement.isSuccess()) {
					//更新银行卡余额,并生成银行卡卡流水   暂时先更新银行卡余额  余额生成简单流水
					ThreadUtil.execute(() -> {
					bankOpen.updateBnakAmount(order);
					bankOpen.nightBankFee(order);//夜间浮动结算
					});
					ThreadUtil.execute(() -> {
						dealOrderDao.updateSuccessAndAmount(order.getOrderId());
					});
				}
			} catch (Exception e) {
				log.info("【异步结算发生异常：" + order.getOrderId() + "】");
				log.info("【异步结算发生异常：" + e.getMessage() + "】");
				push("【异步结算发生异常：" + order.getOrderId() + "】");
			} finally {
				RedisLockUtil.unLock(KEY + "lock");
			}
		}





		RedisLockUtil.redisLock(KEY_WIT + "lock");
		List<Withdraw> orderWitList = withdrawServiceImpl.findSuccessAndNotAmount();
		for (Withdraw order : orderWitList) {
			try {
				if (redis.hasKey(KEY_WIT + order.getOrderId())) {
					log.info("当前订单已处理");
					continue;
				}
				redis.set(KEY_WIT + order.getOrderId(), order.getOrderId(), 2000); //防止多个任务同时获取一个订单发起结算
				List<RunOrder> runOrderList = RunOrderServiceimpl.findAssOrder(order.getOrderId());
				if (CollUtil.isEmpty(runOrderList)) {
					log.info("当前订单存在异常，请核实");
					String msg = "当前订单存在结算异常问题， 请与技术核实，当前订单号为：" + order.getOrderId();
					push(msg);
					withdrawServiceImpl.updateSuccessAndAmount(order.getOrderId());//修改为已处理
					continue;
				}
				Result result = orderUtilImpl.settlementWit(order);
				if (result.isSuccess()) {
					ThreadUtil.execute(() -> {
						withdrawServiceImpl.updateSuccessAndAmount(order.getOrderId());
					});

				}
			} catch (Exception e) {
                log.info("【异步结算发生异常：" + order.getOrderId() + "】");
                log.info("【异步结算发生异常：" + e.getMessage() + "】");
                push("【异步结算发生异常：" + order.getOrderId() + "】");
            } finally {
                RedisLockUtil.unLock(KEY_WIT + "lock");
            }
        }


    }

    @Autowired
    private AccountApiService accountApiServiceImpl;

    /**
     * 异步推送订单
     */
    public void orderWitTask() {
        RedisLockUtil.redisLock(KEY_WIT_PUSH + "lock");
        List<Withdraw> orderList = withdrawServiceImpl.findNotPush();
        for (Withdraw order : orderList) {
            if (redis.hasKey(KEY_WIT_PUSH + order.getOrderId())) {
                log.info("当前订单已处理");
                continue;
            }
            ;
            UserInfo userInfo = accountApiServiceImpl.findautoWit(order.getUserId());
            redis.set(KEY_WIT_PUSH + order.getOrderId(), order.getOrderId(), 200); //防止多个任务同时获取一个订单发起结算
            try {
                List<RunOrder> runOrderList = RunOrderServiceimpl.findAssOrder(order.getOrderId());
                if (CollUtil.isNotEmpty(runOrderList)) {
                    log.info("当前订单已已经结算，且推送，存在异常，请核实");
                    String msg = "当前订单已已经结算，且推送，存在异常，请核实，当前订单号为：" + order.getOrderId();
                    push(msg);
                    withdrawServiceImpl.updatePush(order.getOrderId());//修改为已推送
                    continue;
				}
				Result settlement = WitPayImpl.witAutoPush(order);
			} catch (Exception e) {
				log.info("【推送代付订单异常：" + order.getOrderId() + "】");
				log.info("【推送代付订单异常：" + e.getMessage() + "】");
			} finally {
				RedisLockUtil.unLock(KEY_WIT_PUSH + "lock");
			}

		}

	}

	void push(String msg) {
		ThreadUtil.execute(() -> {
            String url = "http://10.170.0.4:8888/api/send?text=";
            String test = msg + "，触发时间：" + DatePattern.NORM_DATETIME_FORMAT.format(new Date());
            test = HttpUtil.encode(test, "UTF-8");
            String id = "&id=";
            String ids = "-414940159";
            id += ids;
            String s = url + test + id;
            HttpUtil.get(s, 1000);
        });
	}

}
