package alipay.manage.service.impl;

import alipay.manage.bean.DealOrderApp;
import alipay.manage.mapper.DealOrderAppMapper;
import alipay.manage.service.OrderAppService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Component
public class orderAppServiceImpl implements OrderAppService {
	@Resource
	private DealOrderAppMapper dealOrderAppDao;

	@Override
	public boolean add(DealOrderApp dealApp) {
		int insertSelective = dealOrderAppDao.insertSelective(dealApp);
		return insertSelective > 0 && insertSelective < 2;
	}

	@Override
	public DealOrderApp findOrderByOrderId(String orderId) {

		return dealOrderAppDao.findOrderByOrderId(orderId);
	}

	@Override
	public DealOrderApp findAssOrder(String orderId) {
		return null;
	}

	@Override
	public void updateOrderEr(String orderId, String msg) {
		dealOrderAppDao.updateOrderEr(orderId,msg);
	}

	@Override
	public void updateOrderSu(String orderId, String orderStatus) {
		dealOrderAppDao.updateOrderSu(orderId, orderStatus);
	}

	@Override
	public DealOrderApp findOrderByApp(String appId, String appOrderId) {
		return dealOrderAppDao.findOrderByApp(appId, appOrderId);
	}

	@Override
	public DealOrderApp findOrderByAppSum(String appId) {
		DealOrderApp orderByAppSum = dealOrderAppDao.findOrderByAppSum(appId);
		if (null == orderByAppSum) {
			orderByAppSum = new DealOrderApp();
			orderByAppSum.setOrderAmount(new BigDecimal("0"));
		}

		return orderByAppSum;
	}

}
