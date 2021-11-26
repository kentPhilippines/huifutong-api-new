package deal.manage.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import deal.manage.bean.DealOrder;
import deal.manage.bean.Runorder;
import deal.manage.bean.Withdraw;
import otc.bean.dealpay.Recharge;


public interface OrderService {
	/**
	 * <p>卡商查询自己的订单</p>
	 * @param userId				账户号
	 * @param createTime			时间
	 * @return
	 */
	List<DealOrder> findOrderByUser(String userId, String createTime);
	/**
	 * <p>根据关联订单号查询唯一的订单信息</p>
	 * @param orderId
	 * @return
	 */
	DealOrder findOrderByAssociatedId(String orderId);
	/**
	 * <p>分页查询订单，根据自己的所有子账号</p>
	 * @param order
	 * @return
	 */
	List<DealOrder> findOrderByPage(DealOrder order);
	/**
	 * <p>根据订单号查询订单【卡商交易订单】</p>
	 * @param orderId
	 * @return
	 */
	DealOrder findOrderByOrderId(String orderId);
	/**
	 * <p>修改订单状态【卡商交易订单】</p>
	 * @param orderId				订单状态
	 * @param mag 					修改备注
	 * @param status				状态
	 * @return
	 */
	boolean updateOrderStatus(String orderId, String status, String mag);

	/**
	 * <p>获取有关于自己的所有订单</p>
	 * @param order
	 * @return
	 */
	List<DealOrder> findMyOrder(DealOrder order);
	/**
	 * <p>根据商户订单生成码商交易订单</p>
	 * @param orderApp
	 * @return
	 */
	boolean addOrder(DealOrder orderApp);

	boolean updataOrderStatusByOrderId(String orderId, String s);

	boolean updataOrderisNotifyByOrderId(String orderId, String isNotify);


	/**
	 * <p>修改订单状态</p>
	 * @param orderId
	 * @param string
	 * @param b
	 * @return
	 */
	boolean updataOrderStatusByOrderId(String orderId, String string, boolean b);

	/**
	 * <p>根据时间查询交易订单</p>
	 * @param userId
	 * @param orderType
	 * @param formatDateTime
	 * @param formatDateTime2
	 * @return
	 */
	List<DealOrder> findOrderByUser(String userId, String productType, String formatDateTime, String formatDateTime2);
	
	boolean updateOrderStatus(String orderId, String orderStatusSu);


	/**
	 *  获取所有在公共切款中的出款订单
	 * @param orderType
	 * @param dayEndTime
	 * @return
	 */
    List<DealOrder> grabAnOrderListFind(String orderType, Date dayEndTime);
}
