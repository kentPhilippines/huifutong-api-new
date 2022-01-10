package alipay.manage.api.deal;

import alipay.config.redis.RedisUtil;
import alipay.manage.api.AccountApiService;
import alipay.manage.api.VendorRequestApi;
import alipay.manage.api.config.FactoryForStrategy;
import alipay.manage.bean.*;
import alipay.manage.bean.util.DealBean;
import alipay.manage.mapper.ChannelFeeMapper;
import alipay.manage.service.ExceptionOrderService;
import alipay.manage.service.OrderAppService;
import alipay.manage.service.UserInfoService;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.api.alipay.Common;
import otc.result.Result;
import otc.util.MapUtil;
import otc.util.number.Number;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

@Component
public class DealPay {
    @Autowired
    VendorRequestApi vendorRequestApi;
    Logger log = LoggerFactory.getLogger(DealPay.class);
    @Autowired
    private FactoryForStrategy factoryForStrategy;
    @Autowired
    private AccountApiService accountApiServiceImpl;
    @Autowired
    private OrderAppService orderAppServiceImpl;
    @Autowired
    private UserInfoService userInfoServiceImpl;
    @Resource
    private ChannelFeeMapper channelFeeDao;
    @Autowired
    private ExceptionOrderService exceptionOrderServiceImpl;
    @Autowired
    private RedisUtil redis;

    public Result deal(HttpServletRequest request) {
        if (ObjectUtil.isNull(request.getParameter("userId"))) {
            log.info("当前传参，参数格式错误");
            return Result.buildFailMessage("当前传参，参数格式错误，请使用[application/x-www-form-urlencoded]表单格式传参");
        }
        Result pay = vendorRequestApi.pay(request);
        if (!pay.isSuccess()) {
            return pay;
        }
        Object result = pay.getResult();
        DealBean mapToBean = MapUtil.mapToBean((Map<String, Object>) result, DealBean.class);
        if (ObjectUtil.isNull(mapToBean)) {
            return Result.buildFailMessage("加密前格式错误，参数为空");
        }
        String clientIP = HttpUtil.getClientIP(request);
        if (StrUtil.isNotBlank(clientIP)) {
            mapToBean.setIp(clientIP);
        }
        log.info("【当前请求交易实体类：" + mapToBean.toString() + "】");
        String passcode = mapToBean.getPassCode(); //通道支付编码
        if (StrUtil.isBlank(passcode)) {
            return Result.buildFailMessage("通道编码为空");
        }
        log.info("【当前通道编码：" + passcode + "】");
        UserRate userRate = null;
        ChannelFee channelFee = null;
        try {
            userRate = accountApiServiceImpl.findUserRateByUserId(mapToBean.getAppId(), passcode, mapToBean.getAmount());
            channelFee = channelFeeDao.findImpl(userRate.getChannelId(), userRate.getPayTypr());
        } catch (Exception e) {
            exceptionOrderServiceImpl.addDealOrder(mapToBean, "用户报错：当前通道编码有误，产品类型设置重复；处理方法：当前配置用户产品的时候配置用户产品重复", clientIP);
            log.info("【当前通道编码设置有误，产品类型设置重复：" + e.getMessage() + "】");
            return Result.buildFailMessage("当前通道编码设置有误，产品类型设置重复");
        }
        if (ObjectUtil.isNull(channelFee)) {
            log.info("【通道实体不存在，当前商户订单号：" + mapToBean.getOrderId() + "】");
            log.info("【通道实体不存在，费率配置错误】");
            exceptionOrderServiceImpl.addDealOrder(mapToBean, "用户报错：通道实体不存在；处理方法：渠道费率未设置", clientIP);
            return Result.buildFailMessage("通道实体不存在，费率配置错误");
        }
        UserInfo userInfoByUserId = userInfoServiceImpl.findUserInfoByUserId(userRate.getChannelId());
        if (Common.Order.DEAL_OFF.equals(userInfoByUserId.getReceiveOrderState())) {
            log.info("【渠道关闭】");
            exceptionOrderServiceImpl.addDealOrder(mapToBean, "用户报错：渠道关闭，联系运营处理；处理方法：开启渠道充值状态", HttpUtil.getClientIP(request));
            return Result.buildFailMessage("渠道关闭，联系运营处理");
        }
        Object o = redis.get(mapToBean.getOrderId() + userRate.getUserId());
        if (null != o) {
            if (o.toString().equals(mapToBean.getOrderId() + userRate.getUserId())) {
                log.info("【当前商户订单号重复：" + mapToBean.getOrderId() + "】");
                exceptionOrderServiceImpl.addDealOrder(mapToBean, "用户报错：商户订单号重复；处理方法：提醒用户换一个订单号提交支付请求", clientIP);
                return Result.buildFailMessage("商户订单号重复");
            }
        }
        DealOrderApp dealBean = createDealAppOrder(mapToBean, userRate);
        if (ObjectUtil.isNull(dealBean)) {
            exceptionOrderServiceImpl.addDealOrder(mapToBean, "用户报错：交易预订单生成出错；处理方法：让商户重新发起支付提交请求，或联系技术人员处理", clientIP);
            return Result.buildFailMessage("交易预订单生成出错");
        }
        Result deal = null;
        try {
            deal = factoryForStrategy.getStrategy(channelFee.getImpl()).deal(dealBean, channelFee.getChannelId());
        } catch (Exception e) {
            log.info("【当前通道编码对于的实体类不存在：" + e.getMessage() + "】", e);
            exceptionOrderServiceImpl.addDealOrder(mapToBean, "用户报错：当前通道编码不存在；处理方法：生成交易订单时候出现错误，或者请求三方渠道支付请求的时候出现异常返回，或联系技术人员处理," +
                    "三方渠道报错信息：" + e.getMessage(), clientIP);
            return Result.buildFailMessage("当前通道编码不存在");
        } finally {
            userRate = null;
            channelFee = null;
            mapToBean = null;
            clientIP = null;
            pay = null;
            result = null;
            o = null;
            dealBean = null;
        }
		/*if (deal.isSuccess())
			deal.setResult(new ResultDeal(true, 0, deal.getCode(), deal.getResult()));*/
        return deal;
    }

    DealOrderApp createDealAppOrder(DealBean dealBean, UserRate userRate) {
        String payTypr = userRate.getPayTypr();
        boolean alipay = payTypr.contains("ALIPAY");//如果是支付宝类型的产品金额 会有变动后面加几毛钱以作为 回调辨识
        BigDecimal amount  = new BigDecimal(dealBean.getAmount());
        if(alipay){
            int i = RandomUtil.randomInt(1, 50);
            amount = amount.add( new BigDecimal(i).divide(new BigDecimal(100))) ;
        }
        DealOrderApp dealApp = new DealOrderApp();
        dealApp.setAppOrderId(dealBean.getOrderId());
        dealApp.setOrderId(Number.getAppOreder());
        dealApp.setNotify(dealBean.getNotifyUrl());
        dealApp.setOrderAmount(amount);
        String userId = dealBean.getAppId();
        dealApp.setFeeId(userRate.getId());
        dealApp.setOrderAccount(userId);
        if (StrUtil.isNotBlank(dealBean.getIp())) {
            dealApp.setOrderIp(dealBean.getIp());
        }
        dealApp.setBack(dealBean.getPageUrl());
        dealApp.setOrderStatus(Common.Order.DealOrder.ORDER_STATUS_DISPOSE.toString());
        dealApp.setOrderType(Common.Order.ORDER_TYPE_DEAL);
        String dealDescribe  = "充值交易";
        if(StrUtil.isNotEmpty(dealBean.getUserId())){
            dealDescribe = "付款人："+ dealBean.getUserId();
        }
        dealApp.setDealDescribe(dealDescribe);
        dealApp.setRetain1(userRate.getPayTypr());
        dealApp.setRetain3(userRate.getFee().multiply(new BigDecimal(dealBean.getAmount())).toString());
        UserFund userFund = userInfoServiceImpl.findCurrency(userId);//缓存以加
        dealApp.setCurrency(userFund.getCurrency());
        boolean add = false;
        try {
            add = orderAppServiceImpl.add(dealApp);
        } catch (Exception e) {
            log.info("商户订单号重复：");
            log.info("【当前商户订单号重复：" + dealApp.getOrderId() + "】");
            exceptionOrderServiceImpl.addDealOrder(dealBean, "用户报错：商户订单号重复；处理方法：提醒用户换一个订单号提交支付请求", dealBean.getIp());
            //throw new OrderException("订单号重复", null);
        }
        if (add) {
            redis.set(dealApp.getAppOrderId() + userId, dealApp.getAppOrderId() + userId, 60 * 60);
            return dealApp;
        }
        return null;
    }
}
