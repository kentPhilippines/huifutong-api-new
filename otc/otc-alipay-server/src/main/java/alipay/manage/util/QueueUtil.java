package alipay.manage.util;

import alipay.config.redis.RedisUtil;
import alipay.manage.api.feign.QueueServiceClien;
import alipay.manage.service.MediumService;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.bean.alipay.Medium;
import otc.common.RedisConstant;
import otc.result.Result;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**<p>远程队列操作</p>
 * @author hx070
 *
 */
@Component
public class QueueUtil {
	private static final Log log = LogFactory.get();
	@Autowired QueueServiceClien queueServiceClienImpl;
	@Autowired RedisUtil redisUtil;
	@Autowired MediumService mediumServiceImpl;
	private static final String MEDIUM_QUEUE = RedisConstant.Queue.MEDIUM_QUEUE;
	static Lock lock = new  ReentrantLock();
	/**
	 * <p>收款媒介入列操作</p>
	 * @param medium	必传参数<strong>收款媒介数据id</strong>
	 * @return
	 */
	public Result addNode(Medium medium) {
		log.info("【进入支付媒介入列操作】");
			Result addNode  = Result.buildFail();
				Medium mediumId = mediumServiceImpl.findMediumId(medium.getId().toString());
				String mediumNo = mediumId.getMediumId();
				log.info("【当前操作的媒介id为："+mediumNo+"】");
				addNode = queueServiceClienImpl.addNode(mediumId);	// addNode - queue
					redisUtil.set(MEDIUM_QUEUE + mediumNo, mediumNo);
					boolean flag = mediumServiceImpl.updataMediumStatusSu(medium.getId().toString());// open Medium
					if (!flag) {
						return Result.buildFail();
					}
			return addNode;
	}
	/**
	 * <p>收款媒介出列操作</p>
	 * @param medium			必传参数<strong>收款媒介数据id</strong>
	 * @return
	 */
	public Result pop(Medium medium) {
		log.info("【进入支付媒介出列操作】");
			Result deleteNode  = Result.buildFail();
			Medium mediumId = mediumServiceImpl.findMediumId(medium.getId().toString());
			String mediumNo = mediumId.getMediumId();
			deleteNode = queueServiceClienImpl.deleteNode(mediumId);   // delete medium  -  queue
				log.info("【当前操作的媒介id为：" + mediumNo + "】");
				redisUtil.del(MEDIUM_QUEUE + mediumNo);
				boolean flag = mediumServiceImpl.updataMediumStatusEr(medium.getId().toString());   // off medium
				if (!flag) {
					return Result.buildFail();
				}
		return deleteNode;
	}
}
