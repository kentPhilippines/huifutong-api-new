package otc.apk.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis:
 * port: 6379
 * host: 10.14.180.68
 * timeout : 500
 * pool :
 * max-idle : 8
 * min-idle : 0
 * max-active : 8
 * max-wait : -1
 * session:
 * store-type: redis
 * timeout: 7200s
 *
 * @author ADMIN
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {
	Logger log = LoggerFactory.getLogger(RedisConfig.class);
	@Value("${spring.redis.host}")
	private String host;

	@Value("${spring.redis.port}")
	private int port;

	@Value("${spring.redis.timeout}")
	private int timeout;
	@Value("${spring.redis.pool.max-idle}")
	private int maxIdle;
	@Value("${spring.redis.password}")
	private String pasword;
	@Value("${spring.redis.pool.max-wait}")
	private long maxWaitMillis;

	@Bean
	public JedisPool redisPoolFactory() {
        log.debug("JedisPool注入成功！！");
        log.debug("redis地址：" + host + ":" + port);
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout,pasword);
        return jedisPool;
    }

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		log.debug("redisTemplate注入成功！！");
		RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
		template.setConnectionFactory(factory);
		Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
		ObjectMapper om = new ObjectMapper();
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
		template.setKeySerializer(stringRedisSerializer);
		template.setHashKeySerializer(stringRedisSerializer);
		template.setValueSerializer(jackson2JsonRedisSerializer);
		template.setHashValueSerializer(jackson2JsonRedisSerializer);
		template.afterPropertiesSet();
		return template;
	}

	@Bean
	@Override
	public KeyGenerator keyGenerator() {
		return (target, method, params) -> {
			StringBuilder sb = new StringBuilder();
			sb.append(target.getClass().getName());
			sb.append(":");
			sb.append(method.getName());
			for (Object obj : params) {
				sb.append(":" + String.valueOf(obj));
			}
			String rsToUse = String.valueOf(sb);
			log.info("自动生成Redis Key -> [{}]", rsToUse);
			return rsToUse;
		};
	}


	@Override
	@Bean
	public CacheErrorHandler errorHandler() {
		// 异常处理，当Redis发生异常时，打印日志，但是程序正常走
		log.info("初始化 -> [{}]", "Redis CacheErrorHandler");
		CacheErrorHandler cacheErrorHandler = new CacheErrorHandler() {
			@Override
			public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
				log.error("Redis occur handleCacheGetError：key -> [{}]", key, e);
			}

			@Override
			public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
				log.error("Redis occur handleCachePutError：key -> [{}]；value -> [{}]", key, value, e);
			}

			@Override
			public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
				log.error("Redis occur handleCacheEvictError：key -> [{}]", key, e);
			}

			@Override
			public void handleCacheClearError(RuntimeException e, Cache cache) {
				log.error("Redis occur handleCacheClearError：", e);
			}
		};
		return cacheErrorHandler;
	}
}

