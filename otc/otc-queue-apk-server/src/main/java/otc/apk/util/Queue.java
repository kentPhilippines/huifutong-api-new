package otc.apk.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateBetween;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;
import otc.api.alipay.Common;
import otc.apk.feign.AlipayServiceClien;
import otc.apk.feign.ConfigServiceClient;
import otc.apk.redis.RedisUtil;
import otc.bean.alipay.FileList;
import otc.bean.alipay.Medium;
import otc.bean.config.ConfigFile;
import otc.common.RedisConstant;
import otc.result.Result;
import otc.util.RSAUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Queue {
    private static final Log log = LogFactory.get();
    @Autowired
    AlipayServiceClien alipayServiceClienFeignImpl;
    @Autowired
    ConfigServiceClient configServiceClientFeignImpl;
    @Autowired
    RedisUtil redisUtil;
    private static final String DATA_QUEUE_HASH = RedisConstant.Queue.MEDIUM_HASH;
    private static final String REDISKEY_QUEUE = RedisConstant.Queue.QUEUE_REDIS;
    private static final String REDISKEY_QUEUE_CLECK = RedisConstant.Queue.QUEUE_CLECK;

    private static boolean checkNotNull(Object v) {
        if (v == null || "" == v) {
            return false;
        }
        return true;
    }

    public Set<Object> getList(List<String> codes) {
        if (codes.size() == 0) {
            if (!redisUtil.hasKey(REDISKEY_QUEUE)) {  //当无 队列标识时   初始化所有当前媒介
                List<Medium> findIsDealMedium = alipayServiceClienFeignImpl.findIsDealMedium(Common.Medium.MEDIUM_BANK);
                log.info("findIsDealMedium 获取的值是：" + findIsDealMedium);
                if (CollUtil.isNotEmpty(findIsDealMedium)) {
                    for (Medium medium : findIsDealMedium) {
                        boolean addNode = addNode(medium.getMediumNumber(), "");
                        if (addNode) {
                            String md5 = RSAUtils.md5(DATA_QUEUE_HASH + medium.getMediumNumber());
                            redisUtil.hset(DATA_QUEUE_HASH, md5, medium, 259200);//本地收款媒介缓存数据会缓存三天
                        }
                    }
                }
            }
            return redisUtil.zRange(REDISKEY_QUEUE, 0, -1);
        }

        /**
         * 验证当前队列标识
         */
        for (String code : codes) {
            log.info("【code 队列标识为  ：" + code + "】");
            if (!redisUtil.hasKey(REDISKEY_QUEUE + code)) {  //如果当前队列标识不存在  则 初始化当前队列标识
                List<Medium> findIsDealMedium = alipayServiceClienFeignImpl.findIsDealMedium(Common.Medium.MEDIUM_BANK, code);
                log.info("findIsDealMedium 获取的值是：" + findIsDealMedium);
                if (CollUtil.isNotEmpty(findIsDealMedium)) {
                    for (Medium medium : findIsDealMedium) {
                        boolean addNode = addNode(medium.getMediumNumber(), code);
                        if (addNode) {
                            String md5 = RSAUtils.md5(DATA_QUEUE_HASH + medium.getMediumNumber());
                            redisUtil.hset(DATA_QUEUE_HASH, md5, medium, 259200);//本地收款媒介缓存数据会缓存三天
                        }
                    }
                }
            }
        }

    	Set<Object> zRange = null;
    	for(String code : codes) {//这里需要解决 多个卡池 顺序配排队的问题
            Set<Object> zRange2 = redisUtil.zRange(REDISKEY_QUEUE + code, 0, -1);
            if (CollUtil.isEmpty(zRange)) {
                zRange = zRange2;
            } else {
                for (Object obj : zRange2) {
                    zRange.add(obj);
                }
            }
        }
        return zRange;
    }

    public boolean addNode(Object alipayAccount, String code) {
        return addNode(alipayAccount, null, code);
    }

    /**
     * <p>出列</p>
     *
     * @return K
     */
    public Object pop() {
        if (!redisUtil.hasKey(REDISKEY_QUEUE)) {
            return null;
        }
        Set<Object> zRange = redisUtil.zRange(REDISKEY_QUEUE, 0, 1);
        redisUtil.zRemoveRange(REDISKEY_QUEUE, 0, 1);
        return CollUtil.getFirst(zRange);
    }
    /**
     * <p>删除队列元素</p>
     * @param alipayAccount
     * @param code 
     * @return
     */
    public boolean deleteNode(Object alipayAccount, String code) {
        log.info("【删除队列操作：" + alipayAccount + "，code ： " + code + "】");
        redisUtil.del(alipayAccount.toString() + RedisConstant.User.QUEUEQRNODE);
        boolean a = redisUtil.zRemove(REDISKEY_QUEUE + code, alipayAccount) > 0;
        if (a) {
            log.info("剔除元素成功，队列元素：" + alipayAccount + ",code：" + code);
        } else {
            log.info("剔除元素[失败]，队列元素：" + alipayAccount + ",code：" + code);
        }
        return a;
    }

    /**
     * <p>更新队列</p>
     * @param alipayAccount
     * @param qr
     * @return
     */
    public boolean updataNode(Object alipayAccount, FileList qr,String code) {
        if (deleteNode(alipayAccount, code)) {
            if (addNode(alipayAccount, qr, code)) {
                return true;
            }
        }
        return false;
    }
    public boolean updataNode(Object alipayAccount,String code) {
        if (deleteNode(alipayAccount, code)) {
            if (addNode(alipayAccount, code)) {
                return true;
            }
        }
        return false;
    }
    public boolean addNodeRightToLeft(Object alipayAccount) {
        return addNodeRightToLeft(alipayAccount, null);
    }
    /**
     * 	<p>根据score值从右往左入列</p>
     * @param alipayAccount
     * @return
     */
    public boolean addNodeRightToLeft(Object alipayAccount, Object index) {
        if (!checkNotNull(alipayAccount)) {
            return false;
        }
        LinkedHashSet<TypedTuple<Object>> zRangeWithScores = redisUtil.zRangeWithScores(REDISKEY_QUEUE, 0, -1);
        if (CollUtil.isEmpty(zRangeWithScores)) {
            redisUtil.zAdd(REDISKEY_QUEUE, alipayAccount.toString(), 10);
        }
        Double score = 0.00;
        if (checkNotNull(index)) {
            Object[] str = zRangeWithScores.toArray();
            List<Object> list = CollUtil.toList(str);
            @SuppressWarnings("unchecked")
            TypedTuple<Object> member = (TypedTuple<Object>) list.get(Integer.parseInt(index.toString()));
            score = member.getScore() - 0.1;
        } else {
            //获取队列首个元素的score值
            TypedTuple<Object> typedTuple = CollUtil.getFirst(zRangeWithScores);
            Double score2 = typedTuple.getScore();
            score = score2 - 0.1;//预留10个操作空间
        }
        if (score == 0.00) {
            return false;
        }
        return redisUtil.zAdd(REDISKEY_QUEUE, alipayAccount.toString(), score);
    }

    /**
     * <p>支付宝入列</p>
     *
     * @param alipayAccount 支付宝账户号
     * @param qr            二维码具体参数                 当前参数是用于对于某个排列顺序做出优先的具体动作的
     * @param code          队列code值
     * @return
     */
    public boolean addNode(Object alipayAccount, FileList qr,String code) {
        log.info("【当前元素入列操作，元素标签：" + alipayAccount + "，添加元素code：" + code + "】");
        if (!checkNotNull(alipayAccount)) {
            return false;
        }
        String queueKey = REDISKEY_QUEUE + code;
        redisUtil.sSet(REDISKEY_QUEUE_CLECK, queueKey);
        log.info("添加队列key = " + queueKey);
        String onlineLocationKey = alipayAccount.toString() + RedisConstant.User.QUEUEQRNODE;
        log.info("是否在线key = " + onlineLocationKey);
        Double score = 10.0;
        LinkedHashSet<TypedTuple<Object>> zRangeWithScores = redisUtil.zRangeWithScores(queueKey, 0, -1);//linkedhashset 保证set集合查询最快
        if (CollUtil.isEmpty(zRangeWithScores)) {
            Object o = redisUtil.get(onlineLocationKey);//如果当前媒介已存在数据，则取出当前排列顺序，增加
            if (null != o) {
                score = Double.valueOf(o.toString());
            }
            Boolean zAdd = redisUtil.zAdd(queueKey, alipayAccount.toString(), score);
            if (zAdd) {
                redisUtil.set(onlineLocationKey, score);
            }
            return zAdd;
        }
        Optional<TypedTuple<Object>> reduce = zRangeWithScores.stream().reduce((first, second) -> second);
        TypedTuple<Object> typedTuple = null;
        if (reduce.isPresent()) {
            typedTuple = reduce.get();
        }
        if (ObjectUtil.isNull(typedTuple)) {
            return false;
        }
        score = typedTuple.getScore() + 10;//预留10个操作空间
        if (ObjectUtil.isNotNull(qr)) {
            Timestamp createTime = (Timestamp) qr.getCreateTime();//创建时间   多少天之前 .
            Result dayCount = configServiceClientFeignImpl.getConfig(ConfigFile.ALIPAY, ConfigFile.Alipay.NEW_QRCODE_PRIORITY);
            Integer day = Integer.valueOf(dayCount.getResult().toString());//天数
            DateBetween create = DateBetween.create(createTime, new Date(), true);
            long between = create.between(DateUnit.DAY);
            if (between <= day) {// 置为队列中间
                List<TypedTuple<Object>> collect = zRangeWithScores.stream().collect(Collectors.toList());
                TypedTuple<Object> typedTuple2 = collect.get(zRangeWithScores.size() / 2);
                score = typedTuple2.getScore();
                score -= 1;
            }
        }
        Boolean zAdd = redisUtil.zAdd(queueKey, alipayAccount.toString(), score);
        redisUtil.set(onlineLocationKey, score);
        return zAdd;
    }

    public void updataBankNode(String mediumNumber, Object o, String attr) {
        if (deleteNode(mediumNumber, attr)) {
            if (addNode(mediumNumber, attr)) {
            }
        }
    }
}
