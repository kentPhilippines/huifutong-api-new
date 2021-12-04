package alipay.manage.api.channel.deal.zongbang;

import alipay.manage.api.channel.util.ChannelInfo;
import alipay.manage.api.channel.util.shenfu.PayUtil;
import alipay.manage.api.config.PayOrderService;
import alipay.manage.bean.UserInfo;
import alipay.manage.service.UserInfoService;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
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
import otc.util.encode.XRsa;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 惠付通手动代付
 */
@Component("ZongbangDpayToBank")
public class ZongbangDpayToBank extends PayOrderService {
    private static final Log log = LogFactory.get();
    private static final String WIT_RESULT = "SUCCESS";
    @Autowired
    private UserInfoService userInfoServiceImpl;

    private static String getNowDateStr() {
        return DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT);
    }


    @Override
    public Result withdraw(Withdraw wit) {
        log.info("【进入惠付通手动代付代付】");
        try {
            log.info("【本地订单创建成功，开始请求远程三方代付接口】");
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
            UserInfo userInfo = userInfoServiceImpl.findDealUrl(wit.getUserId());
            if (StrUtil.isBlank(userInfo.getDealUrl())) {
                withdrawEr(wit,"url未设置",wit.getRetain2());
                return Result.buildFailMessage("请联系运营为您的商户好设置交易url");
            }



            String createDpay = createDpay(userInfo.getDealUrl()+
                            PayApiConstant.Notfiy.NOTFIY_API_WAI + "/zongbang-wit-noyfit",
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

    static  final  String PUBLIC_KEY  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCJA3kkGVMP3lTsWR6PtBSWFOtP+RmEEv4yWS3E4rIKG07rzX2f7sgQnm2CGld25s4lL9bWT8Hw9ulTpi1vNACHLXko0O/YyNuIfeUvfaXirBgWlErDlQ+hOFdhLle+vdITu+5JW08i+G9Z1gZkcdtk/UeomBuY0FNaLxx/dRCNyQIDAQAB";
    SimpleDateFormat d = new SimpleDateFormat("yyyyMMddHHmmss");
    String createDpay(String notify, Withdraw wit, ChannelInfo channelInfo) {
        try {
            String userid = channelInfo.getChannelAppId();
            String key = channelInfo.getChannelPassword();//交易密钥
            Map<String, Object> objectToMap = new HashMap<>();
            objectToMap.put("appid", userid);
            objectToMap.put("apporderid", wit.getOrderId());
            objectToMap.put("ordertime", d.format(new Date()) + "");
            objectToMap.put("amount", wit.getAmount());
            objectToMap.put("acctno", wit.getBankNo());
            objectToMap.put("acctname",  wit.getAccname());
            objectToMap.put("bankcode", wit.getBankcode());
            objectToMap.put("notifyurl",notify);
            String createParam = createParam(objectToMap);
            log.info("签名前请求串：" + createParam);
            String md5 =  PayUtil.md5(createParam + key);
            log.info("签名：" + md5);
            objectToMap.put("sign", md5);
            String createParam2 = createParam(objectToMap);
            log.info("加密前字符串：" + createParam2);
            XRsa rsa = new XRsa(PUBLIC_KEY);
            String publicEncrypt = rsa.publicEncrypt(createParam2);
            log.info("加密后字符串：" + publicEncrypt);
            Map<String, Object> postMap = new HashMap<String, Object>();
            postMap.put("cipherText", publicEncrypt);
            postMap.put("userId", userid);
            log.info("请求参数：" + postMap.toString());
            long l1 = System.currentTimeMillis();
            String post = HttpUtil.post(channelInfo.getWitUrl(), postMap);
            long l = System.currentTimeMillis();
            long a = l - l1;
            log.info("相应结果集：" + post + " 处理时间：" + a);
            JSONObject parseObj = JSONUtil.parseObj(post);
            String success = parseObj.getStr("success");
            if ("true".equals(success)) {
                witComment(wit.getOrderId());
                return WIT_RESULT;
            } else {
                withdrawEr(wit, parseObj.getStr("message"), wit.getRetain2());
            }
            return "";
        } catch (Exception e) {
            log.error("请求刚盾代付异常", e);
            withdrawErMsg(wit, "代付异常,网络异常", wit.getRetain2());
            return "";
        }
    }
    public static String createParam(Map<String, Object> map) {
        try {
            if (map == null || map.isEmpty()) {
                return null;
            }
            Object[] key = map.keySet().toArray();
            Arrays.sort(key);
            StringBuffer res = new StringBuffer(128);
            for (int i = 0; i < key.length; i++) {
                if (ObjectUtil.isNotNull(map.get(key[i]))) {
                    res.append(key[i] + "=" + map.get(key[i]) + "&");
                }
            }
            String rStr = res.substring(0, res.length() - 1);
            return rStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
