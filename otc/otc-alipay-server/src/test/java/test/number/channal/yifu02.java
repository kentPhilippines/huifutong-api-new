package test.number.channal;

import alipay.manage.api.channel.util.shenfu.PayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class yifu02 {

    public static void main(String[] args) {
        notifytest();
    }

    private static SimpleDateFormat d = new SimpleDateFormat("yyyyMMddHHmmss");

    private static void notifytest() {
        Map<String, Object> map = new HashMap();
        map.put("oid_partner", "202102101152580034");
        map.put("notify_url", "www.baidu.com");
        map.put("sign_type", "MD5");
        map.put("user_id", IdUtil.objectId());
        map.put("no_order", IdUtil.objectId());
        map.put("time_order", d.format(new Date()));
        map.put("money_order", "1000");
        map.put("name_goods", "huafei");
        map.put("pay_type", "205");//PDD PDD 插件通道
        map.put("info_order", "info_order");
        String createParam = PayUtil.createParam(map);
        System.out.println("【绅付请求参数：" + createParam + "】");
        String md5 = PayUtil.md5(createParam + "789okjhy789okjuy7890plkju890olkju789okjhy789ok");
        map.put("sign", md5);
        String post = HttpUtil.post("http://ggsfapi.bbsixsix.com/gateway/bankgateway/pay", map);
        System.out.println("【绅付返回数据：" + post + "】");
        System.out.println(post);
    }

    /**
     *      支付宝扫码: alipay
     *          微信扫码: wechat
     *          云闪付: quickpass
     *          支付宝H5: alipayh5
     *          微信H5: wxh5
     *          支付宝转卡: alipaytb
     *          微信转卡: wxtb
     *          支付宝转支付宝: alipayta
     *          银联支付: unionpay
     */
    private static void test() {
        /**
         *  商户号         merchant_id         是       String(32)      商户号，由平台分配
            商户订单号     order_id            是       String(32)      商户系统内部的订单号
            下单金额        amount              是       String(16)      单位: 分，不支持小数点
            支付类型        pay_type            是       String(32)
            通知地址        notify_url          是       String(255)     接收通知的URL，需给绝对路径，255字符内格式，确保平台能通过互联网访问该地址
            商户用户ID      user_id             是       String(32)      商户系统内部用户ID
            实际支付用户IP   user_ip         是       String(32)      实际支付用户IP地址
            订单名称        order_name      否       String(128)     订单名称
            附加信息        attach          否       String(128)     商户附加信息，可做扩展参数，255字符内，原样返回
            签名              sign            是       String(32)          MD5签名结果
         */
        String key = "a07261026f369734b81a276a26b3cc9c";
        String merchant_id = "10174";
        String order_id = System.currentTimeMillis()/1000 +"";
        String amount = "50000.00";
        String pay_type = "alipaytb";
        String notify_url = "www.baidu.com";
        String user_id = "aaaa";
        String user_ip = "126.0.292.98";
        Map<String,Object> map = new HashMap();
        map.put("merchant_id",merchant_id);
        map.put("order_id",order_id);
        map.put("amount",amount);
        map.put("pay_type",pay_type);
        map.put("notify_url",notify_url);
        map.put("user_id",user_id);
        map.put("user_ip",user_ip);
        String param =  createParam(map);
        System.out.println("【易付2号加签前的参数："+param+"】");
        String s = md5(param + "key=" + key);
        map.put("sign",s);
        System.out.println("【易付2号请求前的参数："+map.toString()+"】");
        String post = HttpUtil.post("http://apl.wuchengdu.com:12306/api/v1/createorder", map);
        HttpUtil.decode(post, "utf-8");
        System.out.println(post);//{"bank_name":"建设银行","card_no":"6217003110033402130","card_user":"农洪共","money_order":"1000.00","no_order":"C20210212151359978932599","oid_partner":"202102101152580034","ret_code":"0000","ret_msg":"SUCCESS","sign":"146b48c9d84e757afb09616cda259ee6"}

    }
    public static String md5(String a) {
        String c = "";
        MessageDigest md5;
        String result="";
        try {
            md5 = MessageDigest.getInstance("md5");
            md5.update(a.getBytes("utf-8"));
            byte[] temp;
            temp=md5.digest(c.getBytes("utf-8"));
            for (int i=0; i<temp.length; i++)
                result+=Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
        }
        return result;
    }
    public static String createParam(Map<String, Object> map) {
        try {
            if (map == null || map.isEmpty())
                return null;
            Object[] key = map.keySet().toArray();
            Arrays.sort(key);
            StringBuffer res = new StringBuffer(128);
            for (int i = 0; i < key.length; i++)
                if(ObjectUtil.isNotNull(map.get(key[i])))
                    res.append(key[i] + "=" + map.get(key[i]) + "&");
            return res.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
