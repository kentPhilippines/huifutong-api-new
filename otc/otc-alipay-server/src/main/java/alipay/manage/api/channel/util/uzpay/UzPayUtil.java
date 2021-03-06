package alipay.manage.api.channel.util.uzpay;

import cn.hutool.core.util.ObjectUtil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

public class UzPayUtil {
	public static final String KEY = "83e63494c7abb22b87d397155c04d413";
	public static final String QUERY_URL ="https://www.uz-pay.com/Api/collection/query";
	public static final String URL ="https://www.uz-pay.com/Api/collection";
	public static final String UID =  "55589";
	public static final String USERID = "JX888";
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
	 public static String md5(String a) {
	    	String c = "";
	    	MessageDigest md5;
		   	String result="";
			try {
				md5 = MessageDigest.getInstance("md5");
				md5.update(a.getBytes("utf-8"));
				byte[] temp;
				temp = md5.digest(c.getBytes("utf-8"));
				for (int i = 0; i < temp.length; i++) {
					result += Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
				}
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			}
			return result;
	    }
}
