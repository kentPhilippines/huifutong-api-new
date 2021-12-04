package alipay.manage.api.channel.wit;

import alipay.manage.api.channel.util.ChannelInfo;
import alipay.manage.api.channel.util.shenfu.PayUtil;
import alipay.manage.api.config.PayOrderService;
import alipay.manage.bean.UserInfo;
import alipay.manage.service.UserInfoService;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.bean.dealpay.Withdraw;
import otc.common.PayApiConstant;
import otc.result.Result;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component("Shenfu04Dpay")
public class Shenfu04Dpay extends PayOrderService {
    private static final Log log = LogFactory.get();
    private static final String SIGN_TYPE = "MD5";
    private static final String WIT_RESULT = "SUCCESS";
    @Autowired
    private UserInfoService userInfoServiceImpl;

    private static String getNowDateStr() {
        return DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT);
    }
    @Override
    public Result withdraw(Withdraw wit) {
        log.info("【进入申付代付】");
        try {
            log.info("【本地订单创建成功，开始请求远程三方代付接口】");
            UserInfo userInfo = userInfoServiceImpl.findDealUrl(wit.getUserId());
            if (StrUtil.isBlank(userInfo.getDealUrl())) {
                withdrawEr(wit, "当前商户交易url未设置", wit.getRetain2());
                return Result.buildFailMessage("请联系运营为您的商户好设置交易url");
            }
            String channel = "";
            if (StrUtil.isNotBlank(wit.getChennelId())) {//支持运营手动推送出款
                channel = wit.getChennelId();
            } else {
                channel = wit.getWitChannel();
            }

            if (isNumber(wit.getAmount().toString())) {
                Result result = withdrawEr(wit, "代付订单不支持小数提交", wit.getRetain2());
                if (result.isSuccess()) {
                    return Result.buildFailMessage("代付订单不支持小数提交");
                }
            }
            String createDpay = createDpay(userInfo.getDealUrl().toString() +
                            PayApiConstant.Notfiy.NOTFIY_API_WAI + "/ShenFuDpay-noyfit",
                    wit, getChannelInfo(channel, wit.getWitType()));
            if (StrUtil.isNotBlank(createDpay) && createDpay.equals(WIT_RESULT)) {
                return Result.buildSuccessMessage("代付成功等待处理");
            } else {
                return Result.buildFailMessage(createDpay);
            }
        } catch (Exception e) {
            log.error("【错误信息打印】" + e.getMessage());
            return Result.buildFailMessage("代付失败");
        }
    }

    String createDpay(String notify, Withdraw wit, ChannelInfo channelInfo) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            String oid_partner = channelInfo.getChannelAppId();
            String notify_url = notify;
            String no_order = wit.getOrderId();
            Integer money_order = wit.getActualAmount().intValue();
            String acct_name = wit.getAccname();
            String card_no = wit.getBankNo();
            String bank_name = wit.getBankName();
            String time_order = getNowDateStr();
            map.put("oid_partner", oid_partner);
            map.put("notify_url", notify_url);
            map.put("no_order", no_order);
            map.put("money_order", money_order);
            map.put("acct_name", acct_name);
            map.put("card_no", card_no);
            map.put("bank_name", bank_name);
            map.put("time_order", time_order);
            map.put("sign_type", SIGN_TYPE);
            String createParam = PayUtil.createParam(map);
            log.info("【绅付代付签名前参数：" + createParam + "】");
            String md5 = PayUtil.md5(createParam + channelInfo.getChannelPassword());
            map.put("sign", md5);
            log.info("【当前绅付代付请求参数为：" + map.toString() + "】");
            String post = HttpUtil.post(channelInfo.getWitUrl(), map, 2000);
            log.info("【绅付代付响应参数为：" + post + "】");

            /**
             * oid_partner			String(18)	√	商家号 商户签约时，分配给商家的唯一身份标识 例如：201411171645530813
             * no_order				String(32)	√	商家订单号 商家网站生成的订单号，由商户保证其唯一性，由字母、数字组成。
             * oid_paybill			String(32)	√	平台支付交易号
             * ret_msg				String(32)	√	SUCCESS 下单成功 如果失败返回失败信息
             * ret_code				String(32)	√	错误码 0000表示成功
             * sign					String(32)	√	参数名称：签名数据 该字段不参与签名，值如何获取，请参考提供的示例代码。
             */

            JSONObject parseObj = JSONUtil.parseObj(post);
            String object = parseObj.getStr("ret_code");
            if ("0000".equals(object)) {
                witComment(wit.getOrderId());
                return WIT_RESULT;
            } else {
                withdrawEr(wit, parseObj.getStr("ret_msg"), wit.getRetain2());
            }
            return "";
        } catch (Exception e) {
            log.error("请求申付代付异常", e);
            withdrawEr(wit, "代付异常", wit.getRetain2());
            return "";
        }
    }
}
