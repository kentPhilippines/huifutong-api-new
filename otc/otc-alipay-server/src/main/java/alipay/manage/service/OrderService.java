package alipay.manage.service;

import alipay.manage.api.channel.amount.recharge.usdt.USDTOrder;
import alipay.manage.bean.DealOrder;
import alipay.manage.bean.RunOrder;
import otc.bean.dealpay.Recharge;
import otc.bean.dealpay.Withdraw;

import java.util.List;

public interface OrderService {

	/**
	 * <p>码商查询自己的订单</p>
	 *
	 * @param id
	 * @param userId     账户号
	 * @param createTime 时间
	 * @return
	 */
	List<DealOrder> findOrderByUser( String userId, String createTime, String orderStatus,String orderType);

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
	List<RunOrder> findOrderRunByPage(RunOrder order);
	
	/**
	 * 分页查询流水订单，根据自己的子账号[参数为null]查询所有订单流水
	 * @param orderRun
	 * @return
	 */
	List<RunOrder> findAllOrderRunByPage(RunOrder order);
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

	/**
	 * <p>通过关联订单号查询订单</p>
	 * @param orderId            订单号
	 * @return
	 */
	DealOrder findAssOrder(String orderId);

	/**
	 * <p>咸鱼对接专属接口</p>
	 *
	 * @param orderId
	 * @param id
	 */
	void updataXianyYu(String orderId, String id);


	/**
	 * 根据订单号修改支付信息数据
	 *
	 * @param bank
	 * @param orderId
	 * @return
	 */
	boolean updateBankInfoByOrderId(String bank, String orderId);


	/**
	 * 订单回调专用查询接口
	 *
	 * @param orderId
	 * @return
	 */
    DealOrder findOrderNotify(String orderId);


	/**
	 * 订单状态订单号查询
	 *
	 * @param orderId
	 * @return
	 */
	DealOrder findOrderStatus(String orderId);


	int addUsdtOrder(USDTOrder order);


	void updateUsdtTxHash(String orderId, String hash);

	/**
	 * 根据外部订单查询
	 *
	 * @param externalOrderId
	 * @return
	 */
    List<DealOrder> findExternalOrderId(String externalOrderId);


    /**
     * 根据订单号查询充值订单
     *
     * @param rechargeId
     * @return
     */
    Recharge findrecharge(String rechargeId);


    /**
     * 修改出款订单的入款凭证
     *
     * @param orderId
     * @param qrcodeId
     * @return
     */
    boolean updatePayImg(String orderId, String qrcodeId);


	/**
	 * 修改订单 标记银行卡余额
	 * @param bankAccount
	 * @param orderId
	 * @return
	 */
	boolean updateBankAmount(String bankAccount, String orderId);

	boolean enterOrderLock(String orderId);

	List<DealOrder> findWitOrderByUserId(String qrcodeId);

	boolean updateWitQr(DealOrder order);
}
