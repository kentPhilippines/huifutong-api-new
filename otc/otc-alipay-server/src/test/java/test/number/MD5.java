package test.number;

import alipay.manage.util.CheckUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpUtil;
import otc.common.PayApiConstant;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MD5 {
	public static void main(String[] args) {
      String url = "https://api.doudoupays.com/gateway/notify/deposit/zhongbang";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("apporderid", "TEST3980c2fb43a1461b");
		map.put("tradesno", "BA20211115152205401896670");
		map.put("status", 2);
		map.put("amount", 2661.0);
		map.put("appid", "doudoululu");
		map.put("statusdesc", "成功");
		String sign = CheckUtils.getSign(map,  "A0803B14DB804DF2ACBFB05A34073BEF");
		map.put("sign", sign);
		System.out.println(map.toString());
		send(url,  "BA20211115152205401896670", map, true);















    }
	private static void send(String url, String orderId, Map<String, Object> msg, boolean flag) {
		String result = "";
		try {
			if (url.contains("https")) {
				msg.put("url", url);
				//result = HttpUtil.post(PayApiConstant.Notfiy.OTHER_URL + "/forword", msg);
			} else {
				result = HttpUtil.post(url, msg, 2000);
			}
		} catch (Exception e) {
			//加入定时任务推送
		}
		String isNotify = "NO";
		System.out.println(result);
	}
	private static final String UTF_8 = "utf-8";
	private static final String ENCODE_TYPE = "md5";
    /**
     * md5加密
     *
     * @param
     * @return
     */
    public static String md5(String a) {
		String c = "";
		MessageDigest md5;
		String result = "";
		try {
			md5 = MessageDigest.getInstance(ENCODE_TYPE);
			md5.update(a.getBytes(UTF_8));
			byte[] temp;
			temp = md5.digest(c.getBytes(UTF_8));
			for (int i = 0; i < temp.length; i++)
				result += Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {


		}
		return result;
    }
}
