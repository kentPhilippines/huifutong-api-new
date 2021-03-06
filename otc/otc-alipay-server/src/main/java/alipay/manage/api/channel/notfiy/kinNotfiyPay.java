package alipay.manage.api.channel.notfiy;

import alipay.manage.api.channel.util.kinpay.PayUtil;
import alipay.manage.api.config.NotfiyChannel;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import otc.common.PayApiConstant;
import otc.result.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequestMapping(PayApiConstant.Notfiy.NOTFIY_API_WAI)
@RestController
public class kinNotfiyPay extends NotfiyChannel {
    private static final Log log = LogFactory.get();

    @PostMapping("/kinPdd-notfiy")
    public KinpayNotfiyBean notify(HttpServletRequest req, HttpServletResponse res) throws IOException {
        log.info("【收到金星拼多多支付成功回调】");
        InputStream inputStream = req.getInputStream();
        String body;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
	         }
		 } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }
        body = stringBuilder.toString();
        JSON parse = JSONUtil.parse(body);
        JSONObject parseObj = JSONUtil.parseObj(parse);

        Set<String> keySet = parseObj.keySet();
        log.info("【收到金星pdd303支付成功请求，当前请求参数为：" + parseObj + "】");
        Map<String, Object> decodeParamMap = new ConcurrentHashMap();
        for (String key : keySet) {
            decodeParamMap.put(key, parseObj.getObj(key));
        }
        /**
         * 		oid_partner	String(18)		√	参数名称：商家号 商户签约时，唯一身份标识。例如201411171645530813。
         sign_type	String(10)		√	参数名称：签名方式 1.取值为：MD5
         sign		String			√	参数名称：返回签名数据 该参数用于验签，值如何使用，请参考文档提供的示例代码
         no_order	String(32)		√	参数名称：商家订单号 商家网站生成的订单号，由商户保证其唯一性，由字母、数字组成。
         oid_paybill	String(32)		√	参数名称：平台交易号。
         time_order	Date			√	参数名称：商家订单时间 时间格式：YYYYMMDDH24MISS 14 位数 字，精确到秒
         money_order	Number(13,2)	√	参数名称：客户实际支付金额 以元为单位，精确到小数点后两位.例如：12.01 ,与币种对应
         info_order	String(255)		x	参数名称：商品描述
         result_pay	String(7)	√	参数名：订单状态 取值为“SUCCESS”，代表订单交易成功
         pay_type	String(5)	√	参数名称：支付类型
         */
        String clientIP = HttpUtil.getClientIP(req);
        log.info("【当前回调ip为：" + clientIP + "】");
        if (!"47.57.147.56".equals(clientIP)) {
            log.info("【当前回调ip为：" + clientIP + "，固定IP登记为：" + "47.57.147.56" + "】");
            log.info("【当前回调ip不匹配】");
            return new KinpayNotfiyBean("9999", "ip is not here  [这个回调ip我们不接受]");
        }
        req.getQueryString();

        //map	HashMap<String, String> decodeParamMap = map//HttpUtil.decodeParamMap(queryString, "UTF-8");
        log.info("【转换为map为：" + decodeParamMap.toString() + "】");
        String sign = (String) decodeParamMap.get("sign");
        String remove = (String) decodeParamMap.remove("sign");
        String createParam = PayUtil.createParam(decodeParamMap);
        String md5 = PayUtil.md5(createParam + PayUtil.key);
        if (md5.equals(sign)) {
            log.info("【当前支付成功回调签名参数：" + sign + "，当前我方验证签名结果：" + md5 + "】");
            log.info("【签名成功】");
        } else {
            log.info("【当前支付成功回调签名参数：" + sign + "，当前我方验证签名结果：" + md5 + "】");
            log.info("【签名失败】");
            return new KinpayNotfiyBean("9999", "sign is errer  [我方验签错误]");
        }

        Result dealpayNotfiy = dealpayNotfiy((String) decodeParamMap.get("no_order"), clientIP);
        if (dealpayNotfiy.isSuccess()) {
            return new KinpayNotfiyBean("0000", "交易成功");
        }
        return new KinpayNotfiyBean("9999", "errer");
    }
}

class KinpayNotfiyBean{
	private String ret_code;
	private String ret_msg;
	public String getRet_code() {
		return ret_code;
	}
	public void setRet_code(String ret_code) {
		this.ret_code = ret_code;
	}
	public String getRet_msg() {
		return ret_msg;
	}
	public void setRet_msg(String ret_msg) {
		this.ret_msg = ret_msg;
	}
	public KinpayNotfiyBean() {
		super();
		// TODO Auto-generated constructor stub
	}
	public KinpayNotfiyBean(String ret_code, String ret_msg) {
		super();
		this.ret_code = ret_code;
		this.ret_msg = ret_msg;
	}
}
