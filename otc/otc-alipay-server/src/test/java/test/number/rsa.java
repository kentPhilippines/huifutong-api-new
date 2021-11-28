package test.number;


import alipay.manage.api.channel.util.shenfu.PayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import org.springframework.data.redis.core.ZSetOperations;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class rsa {


    public static void main(String[] args) {
        String s = PayUtil.md5("6222623710009623260");

        System.out.println(s);

    }
}
