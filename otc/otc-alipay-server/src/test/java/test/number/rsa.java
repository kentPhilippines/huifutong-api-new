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

import java.security.PrivateKey;
import java.util.*;

public class rsa {


    public static void main(String[] args) {
        String s2 = PayUtil.md5("6230810354286164");
        System.out.println(s2);


    }
}
