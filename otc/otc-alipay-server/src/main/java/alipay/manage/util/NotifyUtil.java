package alipay.manage.util;

import alipay.config.redis.RedisUtil;
import alipay.manage.bean.DealOrder;
import alipay.manage.bean.DealOrderApp;
import alipay.manage.bean.UserInfo;
import alipay.manage.mapper.DealOrderAppMapper;
import alipay.manage.service.OrderService;
import alipay.manage.service.UserInfoService;
import alipay.manage.service.WithdrawService;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import otc.api.alipay.Common;
import otc.bean.dealpay.Withdraw;
import otc.common.PayApiConstant;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class NotifyUtil {
    @Value("${otc.payInfo.url}")
    public   String url;
    Logger log = LoggerFactory.getLogger(NotifyUtil.class);
    @Autowired
    OrderService orderSerciceImpl;
    @Autowired
    SettingFile settingFile;
    public static ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();//单台服务器推送30次   20秒推送一次    也就是一个订单最多推送 一台机器30次  分布式部署 1台 * 30
    @Autowired
    private CheckUtils checkUtils;
    @Autowired
    private WithdrawService withdrawServiceImpl;
    @Autowired
    UserInfoService userInfoServiceImpl;
    static final String SC5 = "*/20 * * * * ? ";
    /**
     * @param url
     * @param orderId
     * @param msg
     */
    static Lock lock = new ReentrantLock();
    private static final String SU_MSG = "成功";
    private static final String ER_MSG = "失败";
    private static final String YU_MSG = "处理中";
    @Resource
    DealOrderAppMapper dealOrderAppDao;

    static {
        //开启单机版定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    @Autowired
    RedisUtil redis;

    public void wit(String orderId) {
        log.info("【代付订单修改成功，现在开始通知下游，代付订单号：" + orderId + "】");
        Map<String, Object> map = new HashMap<String, Object>();
        Withdraw wit = withdrawServiceImpl.findOrderId(orderId);
        UserInfo userInfo = userInfoServiceImpl.findPassword(wit.getUserId());
        map.put("apporderid", wit.getAppOrderId());
        map.put("tradesno", wit.getOrderId());
        map.put("status", wit.getOrderStatus());//0 预下单    1处理中  2 成功  3失败
        map.put("amount", wit.getAmount());
        map.put("appid", wit.getUserId());
        String sign = CheckUtils.getSign(map, userInfo.getPayPasword());
        map.put("sign", sign);
        ThreadUtil.execute(() -> {
            send(wit.getNotify(), orderId, map);
        });

    }

    /**
     * <p>发送通知</p>
     *
     * @param url     发送通知的地址
     * @param orderId 发送订单ID
     * @param msg     发送通知的内容
     */
    private void send(String url, String orderId, Map<String, Object> msg) {
        String result = "";
        if (url.contains("https")) {
            msg.put("url", url);
            result = HttpUtil.post(url+ "/witForword", msg);
        } else {
            result = HttpUtil.post(url, msg, 2000);
        }
        log.info("服务器返回结果为: " + result.toString()+":"+msg.toString());
        log.info("【下游商户返回信息为成功,成功收到回调信息】");
        //更新订单是否通知成功状态
    }

    /**
     * <p>根据下游交易订单号,和订单状态 发送通知</p>
     *
     * @param orderId 下游交易订单号
     */
    public void sendMsg(String orderId) {
        log.info("=======[准备向下游商户发送通知]=======");
        DealOrder order = orderSerciceImpl.findOrderNotify(orderId);
        DealOrderApp orderApp = dealOrderAppDao.findOrderByOrderId(order.getAssociatedId());
        UserInfo userInfo = userInfoServiceImpl.findPassword(orderApp.getOrderAccount());
        /**
         * 		tradesno			M(5)				网关订单号
         status				M(32)				状态，2-成功，3-失败，1-处理中
         realamount			M(20)				实际交易金额（元）
         timeend				M(32)				完成时间,格式: yyyyMMddHHmmss
         appid				M(20)				平台id
         apporderid			M(14)				请求订单号
         statusdesc			M(20)				状态描述
         hmac				M(32)				签名数据
         */
        String apporderid = order.getExternalOrderId();
        String status = order.getOrderStatus();
        BigDecimal amount = order.getDealAmount();
        String appid = order.getOrderAccount();
        String statusdesc = getOrderMsg(status);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("apporderid", apporderid);
        map.put("tradesno", order.getOrderId());
        map.put("status", status);
        map.put("amount", amount);
        map.put("appid", appid);
        map.put("statusdesc", statusdesc);
        String sign = CheckUtils.getSign(map, userInfo.getPayPasword());
        map.put("sign", sign);
        ThreadUtil.execute(() -> {
            send(order.getNotify(), order.getOrderId(), map, true);
        });
    }


    String getOrderMsg(String status) {
        switch (status) {
            case Common.Order.DealOrder.ORDER_STATUS_DISPOSE:
                return YU_MSG;
            case Common.Order.DealOrder.ORDER_STATUS_ER:
                return ER_MSG;
            case Common.Order.DealOrder.ORDER_STATUS_SU:
                return SU_MSG;
        }
        return SU_MSG;
    }

    /**
     * <p>发送通知</p>
     *
     * @param url     发送通知的地址
     * @param orderId 发送订单ID
     * @param msg     发送通知的内容
     * @param flag    该线程是否为首次调用该方法  true   首次将会对通知失败订单重复推送，  false    非首次将不会对通知失败订单重复推送
     */
    private void send(String url, String orderId, Map<String, Object> msg, boolean flag) {
        log.info("【通知参数为：" + msg.toString() + "】  ");
        log.info("【通知URL为 ：" + url + "】  ");
        String result = "";
        try {
            if (url.contains("https")) {
                msg.put("url", url);
                result = HttpUtil.post(url + "/forword", msg);
            } else {
                result = HttpUtil.post(url, msg, 2000);
            }
        } catch (Exception e) {
            //加入定时任务推送
            if (flag) {
                push(url, orderId, msg);
            }
        }
        log.info("服务器返回结果为: " + result.toString());
        String isNotify = "NO";
        if ("success".equalsIgnoreCase(result)) {
            isNotify = "YES";
            isSuccess(result, orderId, isNotify);
            log.info("【下游商户返回信息为成功,成功收到回调信息】");
        } else {
            log.info("【下游商户未收到回调信息，或回调信息下游未成功返回】");
            if (flag) {
                push(url, orderId, msg);
            }
        }

    }

    void isSuccess(String result, String orderId, String isNotify) {
        //更新订单是否通知成功状态
        CronUtil.remove(orderId);//若存在定时任务则删除，若不存在则删除无效
        boolean flag = orderSerciceImpl.updataOrderisNotifyByOrderId(orderId, isNotify);
        if (!flag) {
            log.info("【更新是否通知状态失败！】");
        }
    }

    private void push(String url, String orderId, Map<String, Object> msg) {
        lock.lock();
        try {
            String schedule = CronUtil.schedule(orderId, SC5, (Task) () -> {
                log.info("【" + orderId + "   ：   开启重新通知任务.通知url为：" + url + "】");
                DealOrder order = orderSerciceImpl.findOrderNotify(orderId);
                if ("YES".equals(order.getIsNotify())) {
                    CronUtil.remove(orderId);
                }
                send(url, orderId, msg, false);
                log.info("【" + DatePattern.NORM_DATETIME_FORMAT.format(new Date()) + "】");
                if (null == map.get(orderId)) {
                    map.put(orderId, 1);
                } else {
                    Integer o = (Integer) map.get(orderId);
                    o++;
                    if (o > 5) {
                        log.info("【" + " 删除定时任务id  ：    " + orderId + "】");
                        CronUtil.remove(orderId);
                        map.remove(orderId);
                    } else {
                        map.put(orderId, o);
                    }
                }
            });
        } finally {
            lock.unlock();
        }
    }

}
