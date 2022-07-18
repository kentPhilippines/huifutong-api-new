
package test.number;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import otc.util.MapUtil;
import otc.util.encode.XRsa;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class witTest {
	public static void main(String[] args) {
        for (int a = 0; a <= 0; a++) {
//            new witTest().deal();
        }
        //find();
/*
        String userid2 = "2u7rMduh";
        String key2 = "1CA9B6463CBE1809531432D0210D8346";
        String publickey2 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCOUFLo5X4Iehvx6+8jfXnf6sGFaQhQoKZivhg5m47sFAVPORMeMOEtuOOBzgjYUAD4U1YAKIhHvZ9MBnMRJcz3ZvqIrTDdop1JQcP3C3lGj3sfDegHfW7yww3m3lRdQFsAGq/SUtOf+nUMkqrX+d4d+xEeQFthCD4d/w90RU56/wIDAQAB";

		// new witTest().wit(userid2, key2, publickey2);
		//  new witTest().deal();
		String userid2 = "facai123";
		String key2 = "52148D59F72532316ED398A1F3281D73";//交易密钥
		String publickey2 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCXu03qJm8Ib87EU1lsbpHzpz9/i1zrAZSLPkqQSMU1O0XvGDUO/l8WSPmzLcV1ZeBoRKaWHBIzZ5lNKX/G1Y6GdMPRVECf7wVn7MPJ8x95RavNafdQGAc+J46mKTDHw+xA34B8S1BJ503xSumDWFzJfOlBJp8EELQxJIxqDySzNQIDAQAB";
		//	new witTest().wit(userid, key, publicKey);
*/

		String userid1 = "sx978";
		String key1 = "D0FBE6229950990760223703DEEEC4F1";
        String publickey1 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCLIjBF1o1rJECZyD8a5qr+vwcdd1EPl+zCpZqhXV8Vn/zNNbAYguIKPkuCIIdp6bG2knsO0qjVJ0kaVRFoa72Vztku8zSQP+4huijDIBofX5FtXgjS91OMx5tNV9f3FaHd9X9BoVvUp8/BiPIYuD231s8F5PcpGHBP0fRdLI7HmwIDAQAB";
        //	new witTest().wit(userid1, key1, publickey1);
        //USDT测试账号
        //     String userid2 = "kentTest";
        //    String key2 = "F71915AEE539D5B34EC913E9DA124821";
        //    String publickey2 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCqDnm1INpS68yAZ7l/Bx/7axG8qWWcwlGaDOaxXCHa8jCsjlmEcekLU/62rfH+iOAtb3z2ZcVT0AiBSpLn/dJe/9+UmP64o7EvzLPoFZPfCNMeIELbP7QGTBmqSaqXZ3TR/DyCbG0rdcBAR1wwam6NfIckNfGw72sPTQNE70wo+wIDAQAB";

        String userid3 = "632QP888";
        String key3 = "004475525F277F44BA4CDE4670B8E727";
        String publickey3 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCZhADxkdiJFcFaqamlhcxE+bzekfJJFH/qm6sSXg88J+L5q1uboF7LVhtx7t0oz855PED77GsuVbwYhx42ztr4DsU4+5YJEJ/OQL0In3zOkU58mCeTGBbdeoR3DxjBBIkrmC8p6FjdeGw75Gf2YAnImYji+OOv9SX7+kWYBm/yZwIDAQAB";
/*

        String userid = "ceshishanghu";
        String key = "4BEB102F91F34C93A0489FE35F51A4E1";
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkoAaE1PLJ640+p2KOHUFsoolMCnCW+YwuMwI7ZDgeaix6RrkhN3FBAYJsCm2FiKGsPUcyVm8eLonT+dxj7wClIjfQlmWufQ8ST5y35KXm8gjEqr5Kt5K7r+Oy5UM5shcmkEbNP5t6y4UO7Ey+dKjm08FYR0ugiDAL6WxFbKwBAQIDAQAB";
*/

//		String userid = "jy001";
//		String key = "60757EE7C101438CB6D47398284FEFFD";
//		String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCNyChkb8qJfOdbTlLZfZnGX4rqzVHzAIzcHp31nmvVoBNe15WTHvvLQN8OEzyVx6UWPuAQqhheAJ1Cp8L/cqBbGVZshk4lc/bYww0ywvLkGST96+yxd5QRCu2RJJEbmZ0f+7gafkirYbyPegStm+hWk1yh4JhrIAuNOFpo7lvc1QIDAQAB";

		String userid = "tanqiuba";
		String key = "A0CA646839A94CCD8FE87C3ABF04D786";
		String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCuLY9ct4YYdmgYRwb0A+NUfWGIo+LX/jtEI4GirBjTqNpwsXbjj7/HmuklkgoMMhEcgMq2oEhOsKkdlG0KEeHG8yXFJvq4rHaMCvOZzXJCCB8mjb94LLAO/OxXXdvwgOQdXjz7tLBM/JuvWhpplpxK6qcPivHfgYN6V0rkp5BdywIDAQAB";





		for (int a = 0; a <= 0; a++) {
            ThreadUtil.execute(() -> {
             new witTest().wit(userid, key, publicKey);
             //new witTest().deal();
            });
		/*	ThreadUtil.execute(() -> {
				new witTest().wit(userid2, key2, publickey2);
			});
          ThreadUtil.execute(() -> {
                new witTest().wit(userid3, key3, publickey3);*/
			// });
		}
	}

		//相应结果集：{"success":true,"message":"支付处理中","result":{"sussess":true,"cod":0,"openType":1,"returnUrl":"http://api.tjzfcy.com/gateway/bankgateway/payorder/order/60326816340490956.html"},"code":null}
		//相应结果集：{"success":true,"message":"支付处理中","result":{"sussess":true,"cod":0,"openType":1,"returnUrl":"http://api.tjzfcy.com/gateway/bankgateway/payorder/order/60326822046537022.html"},"code":1}


		//	HttpUtil.get("127.0.0.1:9010/pay/testWit?orderNo=W1598355653114710115");



	public static String getKeyedDigestUTF8(String strSrc) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(strSrc.getBytes("UTF8"));
			String result = "";
			byte[] temp;
			temp = md5.digest("".getBytes("UTF8"));
			for (int i = 0; i < temp.length; i++)
				result += Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
			return result;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String createParam(Map<String, Object> map) {
		try {
			if (map == null || map.isEmpty())
				return null;
			Object[] key = map.keySet().toArray();
			Arrays.sort(key);
			StringBuffer res = new StringBuffer(128);
			for (int i = 0; i < key.length; i++)
				if (ObjectUtil.isNotNull(map.get(key[i])))
					res.append(key[i] + "=" + map.get(key[i]) + "&");
			String rStr = res.substring(0, res.length() - 1);
			return rStr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static void find() {
		String userid2 = "facai123";
		String key2 = "52148D59F72532316ED398A1F3281D73";//交易密钥
		Map<String, Object> map = new HashMap<>();
		map.put("appId", userid2);
		String createParam = createParam(map);
		System.out.println("签名前请求串：" + createParam);
		String md5 = getKeyedDigestUTF8(createParam + key2);
		map.put("sign", md5);
		String post = HttpUtil.post("127.0.0.1:9010/deal/findOrderSum", map);
		System.out.println(post);
	}

	void wit(String userid1, String key1, String publickey) {
		SimpleDateFormat d = new SimpleDateFormat("yyyyMMddHHmmss");
		String userid = userid1;
		String key = key1;//交易密钥
		long amount = RandomUtil.randomLong(400, 500);
		Map<String, Object> objectToMap = new HashMap<>();
		objectToMap.put("appid", userid);
		objectToMap.put("apporderid", StrUtil.uuid());
		objectToMap.put("ordertime", d.format(new Date()) + "");
		objectToMap.put("amount", 150);
		objectToMap.put("acctno", "63398293847548938" + RandomUtil.randomLong(1, 10));
		objectToMap.put("acctname", "张三");
		objectToMap.put("bankcode", "ICBC");
		objectToMap.put("notifyurl", "http://www.baodu.com");
		String createParam = createParam(objectToMap);
		System.out.println("签名前请求串：" + createParam);
		String md5 = getKeyedDigestUTF8(createParam + key);
		System.out.println("签名：" + md5);
		objectToMap.put("sign", md5);
        String createParam2 = createParam(objectToMap);
        System.out.println("加密前字符串：" + createParam2);
        XRsa rsa = new XRsa(publickey);
        String publicEncrypt = rsa.publicEncrypt(createParam2);
        System.out.println("加密后字符串：" + publicEncrypt);
        Map<String, Object> postMap = new HashMap<String, Object>();
        postMap.put("cipherText", publicEncrypt);
        postMap.put("userId", userid);
        System.out.println("请求参数：" + postMap.toString());
        long l1 = System.currentTimeMillis();
		String post = HttpUtil.post("http://localhost:9010/deal/wit", postMap);
        long l = System.currentTimeMillis();
        long a = l - l1;
        System.out.println("相应结果集：" + post + " 处理时间：" + a);


    }

	void deal() {
		SimpleDateFormat d = new SimpleDateFormat("yyyyMMddHHmmss");
		//	String userid = "USDTEST";
		//	String key = "91C3B668978C107E3950CD502538294C";//交易密钥
		//	String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDKtB5mlXbyZeWhpcD+3rH8RbSeHTqXvNlhf2SHRmFBlhV35kH91Yjy9daPimc1DPC925tlp1fZPL7Q8d0TVgxgRAhqFghi8vZdAafBhsgOW/wjAzDQj/l6XbE+BvNAtqKc004pMBapsS7ahN3yUHg8qEJ1aDpBTBlakaQHs8rfPQIDAQAB";
/*
		String userid = "186qp";
		String key = "D3E467DE4346429A8750EB9F26DFDF5A";
		String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCJUBzCpCxiekeXG0y1TkxWMJQW0rOTuZ6fZIDT2DNwwTUgWzAzCvEsw+6/0NiiCWTXk6z1x0JjNEaZA4rkIM6n9ixbiYL/lnYvXxEFH3CuL2v0EJvBT30xcm7UlWhusC2Ol6HwdnvZcfRqGKTbDiLmKh9TSbdalZLVFoRKniMwhwIDAQAB";
*/


/*

        String userid = "kbo88";
        String key = "7CDBBBEFCAAC44939DD629E234EEAC07";//交易密钥
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBezPAu1hAOIK625rkH5B0n3/5qIv9s5uXqebxhEIlPEP8TLaZU3OkaAizpSsyiU5ONnJUAN+6egQAFED5XdOcHq5xdw7MIt1JBP+7JRf7ejsBf/qZQTCjJVC/9jdfg9FMnfSn+bw5JpLuhVCQm3H7I5UShknNdkWKkNprL2lE/wIDAQAB";
*/

		//amount=200.00&appId=zuanshi888&applyDate=20220706182622&notifyUrl=https://api.alplaces.com/api/nations&orderId=111111111111111&pageUrl=https://www.baidu.com&passCode=CardToCard&subject=李四&userId=DstKlj&key=DF8411EF97B544EC97B6FD4588FFCBB7
        String userid = "zuanshi888";
        String key = "DF8411EF97B544EC97B6FD4588FFCBB7";
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCSIHPe0MnN+ejL8+FU+rx8vOaJQ3o8wsoFTXSC6hEM6I8rDBuhO3moc38kLkitbbnQIGmIb9W1bqiry1nqUhNuVqEsp/ymjFFzVNNM7FwzRM0Eo9LMEa5Wx05/zQEmlTQ+yFlfo3xx7Oujt4CkjGv2ed3ShrXnn6nrJRwmjGZE9QIDAQAB";
        long amount = RandomUtil.randomLong(200, 300);
        Deal deal = new Deal();
        deal.setAmount(amount + "");//金额
        deal.setAppId(userid);//商户号
        deal.setApplyDate(d.format(new Date()));
        deal.setNotifyUrl("http://starpay168.com:5055");
        deal.setPageUrl("http://starpay168.com:5055");
        deal.setOrderId(IdUtil.objectId());
        deal.setPassCode("CardToCard");
        deal.setSubject("订单交易");
        deal.setUserId("张三");  //to userid
        Map<String, Object> objectToMap = MapUtil.objectToMap(deal);
        String createParam = createParam(objectToMap);
		System.out.println("签名前请求串：" + createParam);
		String md5 = getKeyedDigestUTF8(createParam + key);
		System.out.println("签名：" + md5);
		deal.setSign(md5);
        Map<String, Object> objectToMap2 = MapUtil.objectToMap(deal);
        String createParam2 = createParam(objectToMap2);
        System.out.println("加密前字符串：" + createParam2);
        XRsa rsa = new XRsa(publicKey);
        String publicEncrypt = rsa.publicEncrypt(createParam2);
        System.out.println("加密后字符串：" + publicEncrypt);
        Map<String, Object> postMap = new HashMap<String, Object>();
        postMap.put("cipherText", publicEncrypt);
        postMap.put("userId", userid);
        System.out.println("请求参数：" + postMap.toString());
        String post = HttpUtil.post("http://47.242.42.81:44216/deal/pay", postMap);
        System.out.println("相应结果集：" + post);

    }

}

class Deal {
	private String appId;
	private String orderId;
	private String notifyUrl;
	private String amount;
	private String passCode;
	private String sign;
	private String applyDate;
	private String subject;
	private String userId;
	private String pageUrl;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getPassCode() {
		return passCode;
	}

	public void setPassCode(String passCode) {
		this.passCode = passCode;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}



	public String getPageUrl() {
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getApplyDate() {
		return applyDate;
	}

	public void setApplyDate(String applyDate) {
		this.applyDate = applyDate;
	}

	@Override
	public String toString() {
		return "Deal{" +
				"appId='" + appId + '\'' +
				", orderId='" + orderId + '\'' +
				", notifyUrl='" + notifyUrl + '\'' +
				", pageUrl='" + pageUrl + '\'' +
				", amount='" + amount + '\'' +
				", passCode='" + passCode + '\'' +
				", sign='" + sign + '\'' +
				", applyDate='" + applyDate + '\'' +
				", subject='" + subject + '\'' +
				", userid='"  + '\'' +
				'}';
	}
}
//amount=100&appId=borui123&applyDate=20200922173742&notifyUrl=http://starpay168.com:5055&orderId=5f69c5e7476ee6c666a28842&passCode=ALIPAY_SCAN&subject=订单交易&userid=freebuf002
//amount=100&appId=borui123&applyDate=20200922174347&notifyUrl=http://starpay168.com:5055&orderId=5f69c753476e6dc4e28774fe&passCode=ALIPAY_SCAN&sign=a9e5f599625ba25bf7753fce17924ef0&subject=订单交易&userid=freebuf002

//amount=100&appId=borui123&applyDate=20200922174347&notifyUrl=http://starpay168.com:5055&orderId=5f69c753476e6dc4e28774fe&passCode=ALIPAY_SCAN&subject=订单交易&userid=freebuf002
//amount=100&appId=borui123&applyDate=20200922173742&notifyUrl=http://starpay168.com:5055&orderId=5f69c5e7476ee6c666a28842&passCode=ALIPAY_SCAN&subject=订单交易&userid=freebuf002
