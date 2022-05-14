package test.number;


import alipay.manage.api.channel.util.shenfu.PayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpUtil;
import org.springframework.data.redis.core.ZSetOperations;
import otc.util.MapUtil;
import otc.util.RSAUtils;
import otc.util.encode.XRsa;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.util.*;

public class rsa {


    public static void main(String[] args) {


    //    String s = HttpUtil.get("alipays://platformapi/startapp?appId=60000154&url=%2Fwww%2Findex%2Fdetail.htm%3FbatchNo%3D20220330000750021000350038484020%26token%3Denp1oAXS%26source%3DqrCode");

     //   System.out.println(s);
//https://openapi.alipay.com/gateway.do?timestamp=2013-01-01 08:08:08&method=alipay.fund.trans.aacollect.batch.query&app_id=13603&sign_type=RSA2&sign=ERITJKEIJKJHKKKKKKKHJEREEEEEEEEEEE&version=1.0&charset=GBK&biz_content=




        XRsa rsa = new XRsa("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBgveeFzs3TKSdfMH9Z5uZ5aAThUaSaCLjabXIyAzpbmz1SzbCc6YNXAEYwDvXkztTzbks6jQbt61ib1Uuy4z123wEYk3p4IyFMKfEquPAauj7yTybME0J23rmpXDgXLsX5vO2LB1P9pcv1HiJG/403wYiebnLOfB1w/20qtRnyQIDAQAB");
        String publicEncrypt = rsa.publicEncrypt("amount=1002.00&appId=duocai01&applyDate=20220423174339&notifyUrl=http://fgateway.duocai588.com/pay/notify/jinxingzhifu&orderId=t022cea1650707019300&pageUrl=http://test.com&passCode=BANK_R&sign=14037bcf5300cf44e10d1a4ecd8935a2" );
        System.out.println("加密后字符串：" + publicEncrypt);

    }
}
