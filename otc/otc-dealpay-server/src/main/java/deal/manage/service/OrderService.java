package deal.manage.service;

import java.util.List;
import java.util.Map;

import deal.manage.bean.DealOrder;
import deal.manage.bean.Recharge;
import deal.manage.bean.Runorder;
import deal.manage.bean.Withdraw;


public interface OrderService {

	/**
	 * <p>码商查询自己的订单</p>
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
	 * <p>分页查询流水订单，根据自己的子账号</p>
	 * @param order
	 * @return
	 */
	List<Runorder> findOrderRunByPage(Runorder order);
	
	/**
	 * <p>分页查询订单，根据自己的所有子账号</p>
	 * @param order
	 * @return
	 */
	List<DealOrder> findOrderByPage(DealOrder order);

	/**
	 * <p>查询充值订单</p>
	 * @param bean
	 * @return
	 */

	List<Recharge> findRechargeOrder(Recharge bean);

	/**
	 * <p>查询代付订单</p>
	 * @param bean
	 * @return
	 */
	List<Withdraw> findWithdrawOrder(Withdraw bean);

	/**
	 * <p>根据订单号查询订单【码商交易订单】</p>
	 * @param orderId
	 * @return
	 */
	DealOrder findOrderByOrderId(String orderId);
	/**
	 * <p>修改订单状态【码商交易订单】</p>
	 * @param orderId
	 * @param mag 
	 * @param status
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
	 * <p>创建充值订单</p>
	 * @param order
	 * @return
	 */
	boolean addRechargeOrder(Recharge order);

}