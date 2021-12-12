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

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.util.*;

public class rsa {


    public static void main(String[] args) {
        String s2 = PayUtil.md5("6217281732001139874");
        System.out.println(s2);

        String amount = "2000.000";


        if(new BigDecimal(amount).compareTo(new BigDecimal(500))< 0 ){
            System.out.println("进入 判断");
        }

    }
}
