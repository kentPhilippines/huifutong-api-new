package alipay.manage.api.channel.deal;

import alipay.manage.api.channel.util.shenlaifu.ShenlaifuUtil;
import alipay.manage.api.config.PayOrderService;
import alipay.manage.bean.DealOrderApp;
import alipay.manage.bean.UserInfo;
import alipay.manage.service.UserInfoService;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.common.PayApiConstant;
import otc.result.Result;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component("ShenLaiFuAlipayToCard")
public class ShenLaiFuDeal extends PayOrderService {
    private static final Log log = LogFactory.get();
    @Autowired
    private UserInfoService userInfoServiceImpl;
    @Override
    public Result deal(DealOrderApp dealOrderApp, String channel) {
        log.info("【进入神来付转卡支付】");
        String orderId = create(dealOrderApp, channel);
        if(StrUtil.isNotBlank(orderId)) {
            log.info("【本地订单创建成功，开始请求远程三方支付】");
            UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(dealOrderApp.getOrderAccount());
            if (StrUtil.isBlank(userInfo.getDealUrl())) {
                orderEr(dealOrderApp, "当前商户交易url未设置");
                return Result.buildFailMessage("请联系运营为您的商户号设置交易url");
            }
            log.info("【回调地址ip为：" + userInfo.getDealUrl() + "】");
            String url = createOrder(userInfo.getDealUrl() + PayApiConstant.Notfiy.NOTFIY_API_WAI + "/shenlaifu-notfiy", dealOrderApp.getOrderAmount(), orderId);
            if (StrUtil.isBlank(url)) {
                boolean orderEr = orderEr(dealOrderApp);
                if (orderEr) {
                    return Result.buildFailMessage("支付失败");
                }
            } else {
                return Result.buildSuccessResultCode("支付处理中", url, 1);
            }
        }
        return  Result.buildFailMessage("支付错误");
    }
    private static final SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHMMSS");
    private String createOrder(String notify, BigDecimal orderAmount, String orderId) {
        Map map = new HashMap();
        String key = ShenlaifuUtil.KEY;
        String memberId = ShenlaifuUtil.APPID;
        String amount = orderAmount.intValue()+"";
        String applyDate = sdf.format(new Date());
        String channelCode = "IMA";
        String userId = "GP233554";
        String notifyUrl = notify;
        String sgin = "memberId^"+memberId+"&orderId^"+orderId+"&amount^"+amount+"&applyDate^"+applyDate
                +"&channelCode^"+channelCode+"&userId^"+userId+"&notifyUrl^"+notifyUrl+"&key="+key;
        log.info("【神来付加签参数："+sgin+"】");
        String md5Sign = ShenlaifuUtil.md5(sgin).toUpperCase();
        log.info("【神来付我方签名："+md5Sign+"】");
        map.put("memberId",memberId);
        map.put("orderId",orderId);
        map.put("amount",amount);
        map.put("applyDate",applyDate);
        map.put("channelCode",channelCode);
        map.put("userId",userId);
        map.put("realName","jinxin");
        map.put("notifyUrl",notifyUrl);
        map.put("md5Sign",md5Sign);
        String s = HttpUtil.toParams(map);
        log.info("【神来付GET请求参数："+s+"】");
        String url = ShenlaifuUtil.URL;
        url += ("?"+ s);
        log.info("【神来付GET请求连接："+url+"】");
        return url;
    }

}
