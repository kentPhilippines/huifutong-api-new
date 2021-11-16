package otc.apk.util;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;
import otc.apk.feign.AlipayServiceClien;
import otc.apk.redis.RedisUtil;
import otc.bean.alipay.Medium;
import otc.common.RedisConstant;
import otc.result.Result;
import otc.util.RSAUtils;

import java.util.LinkedHashSet;
import java.util.Set;
@Component
public class HeartUtil {
	@Autowired
	RedisUtil redisUtil;
	private static final String REDISKEY_QUEUE = RedisConstant.Queue.QUEUE_REDIS;//队列储存数据
	private static final String DATA_QUEUE_HASH = RedisConstant.Queue.MEDIUM_HASH;//本地数据储存键
	private static final String HEARTBEAT = RedisConstant.Queue.HEARTBEAT;//心跳数据标记
	private static final String REDISKEY_QUEUE_CLECK = RedisConstant.Queue.QUEUE_CLECK;//所有队列
	private static final Log log = LogFactory.get();
	@Autowired
	AlipayServiceClien AlipayServiceClienImpl;
	public void clickHeart() {
		/**
		 * ####################################
		 * 1,拿到所有队列数据
		 */
		log.info("【当前模糊匹配键值：" + REDISKEY_QUEUE + "】");
		Set<Object> keys = redisUtil.sGet(REDISKEY_QUEUE_CLECK);
		log.info("【当前收款媒介个数：" + keys.size() + "】");
		for (Object key : keys) {
			log.info("【当前检查key ：" + key + "】");
			LinkedHashSet<TypedTuple<Object>> withScores = redisUtil.zRangeWithScores(key.toString(), 0, -1);
			for (TypedTuple tuple : withScores) {
                Object value = tuple.getValue();
                Double score = tuple.getScore();
                log.info("【当前队列元素：value：" + value + "，score：" + score + "】");
                String md52 = RSAUtils.md5(value.toString());//获取心跳数据加密值
				log.info("【当前检查key ：" + md52 + "】");
				boolean hasKey = redisUtil.hasKey(HEARTBEAT + md52);//验证媒介是否在心跳数据中
                if (!hasKey) {//不在的时候 讲该媒介踢出队列
                    Result offMediumQueue = AlipayServiceClienImpl.offMediumQueue(value.toString());
                    if (offMediumQueue.isSuccess()) {
                        log.info("【已将收款媒介：" + value.toString() + "，成功踢出队列】");
                    }
                }
            }
		}
		
	}
	
	
	
	
	
	
	
}
