package alipay.manage.api.channel.deal.shenfu;

import alipay.config.redis.RedisUtil;
import alipay.manage.api.channel.util.ChannelInfo;
import alipay.manage.api.channel.util.shenfu.PayUtil;
import alipay.manage.api.config.PayOrderService;
import alipay.manage.bean.DealOrderApp;
import alipay.manage.bean.UserInfo;
import alipay.manage.bean.util.ResultDeal;
import alipay.manage.service.OrderService;
import alipay.manage.service.UserInfoService;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import otc.common.PayApiConstant;
import otc.result.Result;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component("ShenFuSourcePay")
public class ShenFuSourcePay extends PayOrderService {
    @Value("${otc.payInfo.url}")
    public   String url;
    private static final String MARS = "SHENFU";
    private static final String PAY_URL = "http://";

    private static SimpleDateFormat d = new SimpleDateFormat("yyyyMMddHHmmss");
    @Autowired
    private UserInfoService userInfoServiceImpl;
    @Autowired
    RedisUtil redis;
    @Autowired
    private OrderService orderServiceImpl;

    @Override
    public Result deal(DealOrderApp dealOrderApp, String channel) throws Exception {
        log.info("【进入申付支付，当前请求产品：" + dealOrderApp.getRetain1() + "，当前请求渠道：" + channel + "】");
        String orderId = create(dealOrderApp, channel);
        UserInfo userInfo = userInfoServiceImpl.findDealUrl(dealOrderApp.getOrderAccount());
        if (StrUtil.isBlank(userInfo.getDealUrl())) {
            orderEr(dealOrderApp, "当前商户交易url未设置");
            return Result.buildFailMessage("请联系运营为您的商户好设置交易url");
        }
        Result result = createOrder(
                userInfo.getDealUrl() +
                        PayApiConstant.Notfiy.NOTFIY_API_WAI + "/shenfu-source-notify",
                dealOrderApp.getOrderAmount(),
                orderId,
                getChannelInfo(channel, dealOrderApp.getRetain1()));
        if (result.isSuccess()) {
            return Result.buildSuccessResult("支付处理中", ResultDeal.sendUrl(result.getResult()));
        } else {
            orderEr(dealOrderApp, "错误消息：" + result.getMessage());
            return result;
        }
    }

    private Result createOrder(String notify, BigDecimal orderAmount, String orderId, ChannelInfo channelInfo) {
        Map<String, Object> map = new HashMap();
        map.put("oid_partner", channelInfo.getChannelAppId());
        map.put("notify_url", notify);
        map.put("sign_type", "MD5");
        map.put("user_id", IdUtil.objectId());
        map.put("no_order", orderId);
        map.put("time_order", d.format(new Date()));
        map.put("money_order", orderAmount);
        map.put("name_goods", "huafei");
        map.put("pay_type", channelInfo.getChannelType());//PDD PDD 插件通道
        map.put("info_order", "info_order");
        String createParam = PayUtil.createParam(map);
        log.info("【绅付请求参数：" + createParam + "】");
        String md5 = PayUtil.md5(createParam + channelInfo.getChannelPassword());
        map.put("sign", md5);
        String post = HttpUtil.post(channelInfo.getDealurl(), map);
        log.info("【绅付返回数据：" + post + "】");
        log.info(post);////{
        // "bank_name":"建设银行",
        // "card_no":"6217003110033402130",
        // "card_user":"农洪共",
        // "money_order":"1000.00",
        // "no_order":"C20210212151359978932599",
        // "oid_partner":"202102101152580034",
        // "ret_code":"0000",
        // "ret_msg":"SUCCESS",
        // "sign":"146b48c9d84e757afb09616cda259ee6"
        // }
        //  PddBean bean = JSONUtil.toBean(post, PddBean.class);
        JSONObject jsonObject = JSONUtil.parseObj(post);

        if (ObjectUtil.isNotNull(jsonObject)) {
            if ("0000".equals(jsonObject.getStr("ret_code"))) {
                Map cardmap = new HashMap();
                cardmap.put("bank_name", jsonObject.getStr("bank_name"));
                cardmap.put("card_no", jsonObject.getStr("card_no"));
                cardmap.put("card_user", jsonObject.getStr("card_user"));
                cardmap.put("money_order", jsonObject.getStr("money_order"));
                cardmap.put("no_order", jsonObject.getStr("no_order"));
                cardmap.put("oid_partner", jsonObject.getStr("oid_partner"));
                orderServiceImpl.updateBankInfoByOrderId(jsonObject.getStr("card_user") + ":" + jsonObject.getStr("bank_name") + ":" + jsonObject.getStr("card_no"), orderId);
                redis.hmset(MARS + orderId, cardmap, 600000);
                return Result.buildSuccessResult(url + "/pay?orderId=" + orderId + "&type=" + channelInfo.getChannelType());
            } else {
                return Result.buildFailMessage(jsonObject.getStr("ret_msg"));
            }
        }
        return Result.buildFail();
    }
}
