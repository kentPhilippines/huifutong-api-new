package alipay.manage.util;

import alipay.config.redis.RedisUtil;
import alipay.manage.api.feign.ConfigServiceClient;
import alipay.manage.bean.DealOrder;
import alipay.manage.bean.UserFund;
import alipay.manage.service.CorrelationService;
import alipay.manage.util.bankcardUtil.BankUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.api.alipay.Common;
import otc.bean.config.ConfigFile;
import otc.util.date.DateUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>选码风控工具类</p>
 *
 * @author kent
 * @date 2020-3-31 21:17:35
 */
@Component
public class RiskUtil {
    private static final Log log = LogFactory.get();
    @Resource
    RedisUtil redisUtil;
    @Autowired
    ConfigServiceClient configServiceClientImpl;
    @Autowired
    CorrelationService correlationServiceImpl;
    DateFormat formatter = new SimpleDateFormat(Common.Order.DATE_TYPE);
    private static final Integer LOCK_TIME = 600;
    @Autowired
    private BankUtil bankUtil;

    /**
     * <p> 更新缓存中的账户余额 </p>
     *
     * @param user 资金账户
     * @param flag 是否发起顶代扣款冻结
     * @throws ParseException 时间转换异常
     */
    public void updataUserAmountRedis(UserFund user, boolean flag) {
        log.info("【进入账户金额虚拟冻结更新，当前账户：" + user.getUserId() + "】");
        if (flag) {
            log.info("【顶代账户余额冻结模式】");
            String findAgent = correlationServiceImpl.findAgent(user.getUserId());
            log.info("【当前顶代账号为】");
            Map<Object, Object> hmget = redisUtil.hmget(findAgent);
            Set<Object> keySet = hmget.keySet();
            for (Object obj : keySet) {
                String accountId = user.getUserId();
                int length = accountId.length();
                String subSuf = StrUtil.subSuf(obj.toString(), length);// 时间戳
				Date parse = null;
				try {
					parse = formatter.parse(subSuf);
				} catch (ParseException e) {
					e.printStackTrace();
                }
                Object object = hmget.get(obj.toString());// 当前金额
                if (!DateUtil.isExpired(parse, DateField.SECOND,
                        Integer.valueOf(LOCK_TIME), new Date())) {
                    redisUtil.hdel(user.getUserId(), obj.toString());
                }
            }
            return;
        }
        Map<Object, Object> hmget = redisUtil.hmget(user.getUserId());
        log.info("【当前账户冻结金额 ：" + hmget.toString() + "】");
        Set<Object> keySet = hmget.keySet();
        for (Object obj : keySet) {
            String accountId = user.getUserId();
            int length = accountId.length();
            String subSuf = StrUtil.subSuf(obj.toString(), length);// 时间戳
            Date parse = null;
            try {
                parse = formatter.parse(subSuf);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Object object = hmget.get(obj.toString());// 当前金额
            if (!DateUtil.isExpired(parse, DateField.SECOND, Integer.valueOf(LOCK_TIME), new Date())) {
                redisUtil.hdel(user.getUserId(), obj.toString());
            }
        }
	}


	/**
	 * <p>验证当前用户账户余额是否可以接单</p>
	 *
	 * @param userId      用户id
	 * @param amount2     交易金额
	 * @param usercollect 用户集合
	 * @param flag        true 顶代结算模式   false  非顶代结算模式
	 * @return
	 */
	public boolean isClickAmount(String userId, BigDecimal amount2, ConcurrentHashMap<String, UserFund> usercollect, boolean flag) {
        log.info("【检查当前账户金额是否满足需求，当前账户：" + userId + "，当前交易金额：" + amount2 + "】");
        if (flag) {
            //获取顶代账号
            //发起账户冻结比对
            log.info("【顶代账户余额冻结模式】");
            String findAgent = correlationServiceImpl.findAgent(userId);
            log.info("【当前顶代账号为】");
            Map<Object, Object> hmget = redisUtil.hmget(findAgent);
            Set<Object> keySet = hmget.keySet();
            BigDecimal amount = amount2;
            for (Object obj : keySet) {
				Object object = hmget.get(obj);
                if (ObjectUtil.isNull(object)) {
                    object = "0";
                }
                BigDecimal bigDecimal = new BigDecimal(object.toString());
                amount = amount.add(bigDecimal);
            }
            UserFund user2 = usercollect.get(userId);
            return amount.compareTo(user2.getAccountBalance()) == -1;
        }
        Map<Object, Object> hmget = redisUtil.hmget(userId);
        log.info("【当前账户冻结金额 ：" + hmget.toString() + "】");
        Set<Object> keySet = hmget.keySet();
        BigDecimal amount = amount2;
        for (Object obj : keySet) {
            Object object = hmget.get(obj);
            if (ObjectUtil.isNull(object)) {
                object = "0";
            }
            BigDecimal bigDecimal = new BigDecimal(object.toString());
            amount = amount.add(bigDecimal);
        }
        UserFund user2 = usercollect.get(userId);
        log.info("【当前入款订单缓存冻结金额为：" + amount + "】");
        return amount.compareTo(user2.getAccountBalance().subtract(user2.getSumProfit()).add(user2.getQuota()).subtract(user2.getFreezeBalance())) == -1;
    }
	
	
	/**
	 * <p>订单成功更新风控规则</p>
	 * @param order
	 */
	public void orderSu(DealOrder order) {
        try {
            boolean b = updataRedisOrDate(order);
            log.info("当前订单风控解锁数据为 : " + b + "，当前订单为：" + order.getOrderId() + "");
        } catch (Exception c) {
            log.info("更新风控数据异常");
            log.error(c);
        }

    }

    /**
     * <p>清除缓存值</p>
     *
     * @param qrcodeDealOrder
     * @return
     */
    boolean updataRedisOrDate(DealOrder qrcodeDealOrder) {
        clearAmount(qrcodeDealOrder);
        //	updataQr(qrcodeDealOrder);
        updateCorrelation(qrcodeDealOrder);
        //	try {
        //		deleteRedisAmount(qrcodeDealOrder);
        //	} catch (ParseException e) {
        //		log.info("解锁订单当前码商订单金额发生异常，当前码商改订单金额解锁失败，解锁时间误差时间为20秒");
        //	}
        //  unLockAmount(qrcodeDealOrder);
        if ("4".equals(qrcodeDealOrder.getOrderType())) {//卡商代付订单成功，则解锁卡商代付缓存锁定
            bankUtil.openWit(qrcodeDealOrder.getOrderQrUser());
        }
        return true;
    };
	/**
     * <p>更新数据统计</p>
     */
    private void updateCorrelation(DealOrder qrcodeDealOrder) {
        log.info("更新数据统计服务");
        correlationServiceImpl.updateCorrelationDate(qrcodeDealOrder.getOrderId());
    }

    /**
     * <p>对卡商账户金额进行解冻</p>
     *
     * @param order
     */
    private void clearAmount(DealOrder order) {
        log.info("【对商户账户金额进行解冻】");
        try {
            Object o = redisUtil.get("AMOUNT:LOCK:" + order.getOrderId());//金额标记
            String orderQrUser = order.getOrderQrUser();
            redisUtil.hdel(orderQrUser, o);//删除锁定金额
        } catch (Exception e) {
            log.info("删除锁定金额失败，当前订单号：" + order.getOrderId());
        }
        redisUtil.del(order.getOrderQr() + order.getDealAmount());
        String orderMark = "ORDER:" + order.getOrderQrUser() + ":AUTO";
        redisUtil.hdel(orderMark, order.getOrderQrUser());
    }

    private void updataQr(DealOrder qrcodeDealOrder) {
        Map<Object, Object> hmget = redisUtil.hmget(qrcodeDealOrder.getOrderQr());
        if (hmget.size() > 0) {//两次锁定二维码   成功解锁
            Set<Object> keySet = hmget.keySet();
            for (Object obj : keySet) {
                redisUtil.hdel(qrcodeDealOrder.getOrderQr(), obj.toString());//二维码三次未收到回调锁定一小时
            }
        }
    }


    /**
     * 解锁金额的老方法
     *
     * @param qrcodeDealOrder
     * @throws ParseException
     */
    private void unLockAmount(DealOrder qrcodeDealOrder) {
        Map<Object, Object> hmget2 = redisUtil.hmget(qrcodeDealOrder.getOrderQrUser());
        if (hmget2.size() <= 0) //成功订单提前解锁金额
        {
            return;
        }
        try {
            Set<Object> keySet = hmget2.keySet();
            for (Object obj : keySet) {
                String accountId = qrcodeDealOrder.getOrderQrUser();
                int length = accountId.length();
                String subSuf = StrUtil.subSuf(obj.toString(), length);//时间戳
                Date parse = formatter.parse(subSuf);
                if (qrcodeDealOrder.getDealAmount().compareTo(new BigDecimal(hmget2.get(obj.toString()).toString())) == 0 && DateUtils.isTimeScope(20, qrcodeDealOrder.getCreateTime(), parse)) {
                    redisUtil.hdel(qrcodeDealOrder.getOrderQrUser(), obj.toString());
                    break;
                }
            }
        } catch (Exception e) {
            return;
        }
	}
	private  void addDealSu(DealOrder qrcodeDealOrder) {
		Map<Object, Object> hmget = redisUtil.hmget(qrcodeDealOrder.getOrderQr());
		if (hmget.size() > 0) {
			redisUtil.hset(qrcodeDealOrder.getOrderQr() + qrcodeDealOrder.getOrderQrUser(), qrcodeDealOrder.getOrderQr() + qrcodeDealOrder.getOrderId(), qrcodeDealOrder.getOrderId());
        } else {
            redisUtil.hset(qrcodeDealOrder.getOrderQr() + qrcodeDealOrder.getOrderQrUser(), qrcodeDealOrder.getOrderQr() + qrcodeDealOrder.getOrderId(), qrcodeDealOrder.getOrderId(), LOCK_TIME);
		}
	}
}
