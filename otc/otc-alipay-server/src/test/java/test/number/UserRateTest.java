package test.number;

import alipay.AlipayApplication;
import alipay.manage.bean.UserRate;
import alipay.manage.service.UserRateService;
import alipay.manage.service.impl.OrderServiceImpl;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.google.gson.JsonArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AlipayApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserRateTest {

    @Autowired
    private UserRateService userRateService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private OrderServiceImpl orderService;
    @Test
    public void userRate() throws InterruptedException {

        String str = redisTemplate.opsForValue().get("com.ruoyi.alipay.service.impl.AlipayUserRateEntityServiceImpl:getAndRefreshAlipayMerchantRateCache:2");
        List<UserRate> list = JSONUtil.parseArray(str).toList(UserRate.class);
        //list = list.stream().filter(userRate -> userRate.getUserId().equals("feiji8")).collect(Collectors.toList());
        System.out.println(JSONUtil.toJsonStr(list.stream().filter(userRate -> StringUtils.isNotEmpty(userRate.getQueueList())).map(UserRate::getQueueList).collect(Collectors.toList())));
        ;
        System.out.println(JSONUtil.toJsonStr(orderService.getCardDealerMapToMerchant(list).get("facai588")));

        /*while(true)
        {
            Cache cache = cacheManager.getCache("UserRateCache");
           // List<UserRate> rates = cache.get("com.ruoyi.alipay.service.impl.AlipayUserRateEntityServiceImpl:getAndRefreshAlipayCache:2").get();

            //List<UserRate> list = JSONUtil.parseArray(str).toList(UserRate.class);
            List<UserRate> list = Lists.newArrayList();
            list = list.stream().filter(userRate -> userRate.getUserId().equals("feiji8")).collect(Collectors.toList());
            System.out.println(JSONUtil.toJsonStr(list.stream().map(UserRate::getQueueList).collect(Collectors.toList())));
            ;
            Thread.sleep(5000);
        }*/

    }
}
