package deal.manage.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import deal.manage.bean.DealOrder;
import deal.manage.bean.DealOrderExample;
import deal.manage.mapper.DealOrderMapper;
import deal.manage.service.OrderService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
public class OrderServiceImpl implements OrderService {
	@Resource
	DealOrderMapper dealOrderDao;

	@Override
	public List<DealOrder> findOrderByUser(String userId, String createTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DealOrder findOrderByAssociatedId(String orderId) {
		return dealOrderDao.findOrderByAssociatedId(orderId);
	}

	@Override
	public List<DealOrder> findOrderByPage(DealOrder order) {
		DealOrderExample example = new DealOrderExample();
		DealOrderExample.Criteria criteria = example.createCriteria();
		if (StrUtil.isNotBlank(order.getOrderQrUser())) {
			criteria.andOrderQrUserEqualTo(order.getOrderQrUser());
		}
		if (CollUtil.isNotEmpty(order.getOrderQrUserList())) {
			criteria.andOrderQrUserListEqualTo(order.getOrderQrUserList());
		}
		if (StrUtil.isNotBlank(order.getTime())) {
			Date date = getDate(order.getTime());
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			System.out.println("开始时间：" + calendar.getTime());
			Date time = calendar.getTime();
			calendar.set(Calendar.HOUR, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			calendar.set(Calendar.MILLISECOND, 999);
			System.out.println("结束时间：" + calendar.getTime());
			criteria.andCreateTimeBetween(time, calendar.getTime());
		}
		if (StrUtil.isNotBlank(order.getAssociatedId())) {
			criteria.andAssociatedIdEqualTo(order.getAssociatedId());
		}
		if (StrUtil.isNotBlank(order.getOrderAccount())) {
			criteria.andOrderAccountEqualTo(order.getOrderAccount());
		}
		if (StrUtil.isNotBlank(order.getOrderStatus())) {
			criteria.andOrderStatusEqualTo(order.getOrderStatus());
		}
		if (StrUtil.isNotBlank(order.getOrderType())) {
			criteria.andOrderTypeEqualTo(order.getOrderType());
		}
		if (StrUtil.isNotBlank(order.getOrderId())) {
			criteria.andOrderIdEqualTo(order.getOrderId());
		}
		example.setOrderByClause("createTime desc");
		return dealOrderDao.selectByExample(example);
	}
	Date getDate(String time){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date dateTime = null;
		try {
			dateTime = simpleDateFormat.parse(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateTime;
	}

	@Override
	public DealOrder findOrderByOrderId(String orderId) {
		return 	dealOrderDao.findOrderByOrderId(orderId);
	}

	@Override
	public boolean updateOrderStatus(String orderId, String status, String mag) {
		int a = dealOrderDao.updateOrderStatus(orderId,status,mag);
		return a > 0 && a < 2;
	}

	@Override
	public List<DealOrder> findMyOrder(DealOrder order) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addOrder(DealOrder orderApp) {
		int insertSelective = dealOrderDao.insertSelective(orderApp);
		return insertSelective > 0 && insertSelective < 2;
	}

	@Override
	public boolean updataOrderStatusByOrderId(String orderId, String s) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updataOrderisNotifyByOrderId(String orderId, String isNotify) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updataOrderStatusByOrderId(String orderId, String string, boolean b) {
		// TODO Auto-generated method stub
		return false;
	}

	/*@Override
	public List<DealOrder> findOrderByUser(String userId, String orderType, String startTime,
			String endTime) {
		List<DealOrder> selectByExample = dealOrderDao.findOrderByUser(userId,orderType,startTime,endTime);
		return selectByExample;
	}*/
	@Override
	public List<DealOrder> findOrderByUser(String userId, String productType, String formatDateTime,
										   String formatDateTime2) {
		DealOrderExample example = new DealOrderExample();
		DealOrderExample.Criteria criteria = example.createCriteria();
		if (StrUtil.isNotBlank(userId)) {
			criteria.andOrderAccountEqualTo(userId);
		}
		if (StrUtil.isNotBlank(productType)) {
			criteria.andProductTypeEqualTo(productType);
		}
		if (StrUtil.isNotBlank(formatDateTime) && StrUtil.isNotBlank(formatDateTime2)) {
			criteria.andCreateTimeBetween(getDate2(formatDateTime), getDate2(formatDateTime2));
		}
		return dealOrderDao.selectByExample(example);
	}
	Date getDate2(String time){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateTime = null;
		try {
			dateTime = simpleDateFormat.parse(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateTime;
	}
	@Override
	public boolean updateOrderStatus(String orderId, String orderStatusSu) {
		int a = dealOrderDao.updateOrder(orderId,orderStatusSu);
		return a  > 0 && a  < 2;
	}

	@Override
	public List<DealOrder> grabAnOrderListFind(String orderType, Date dayEndTime) {





		return null;
	}

}
