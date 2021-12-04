package alipay.manage.service;

import otc.bean.dealpay.Withdraw;

import java.util.List;

public interface WithdrawService {
	/**
	 * <p>代付订单生成</p>
	 *
	 * @param witb
	 * @return
	 */
	boolean addOrder(Withdraw witb);

	/**
	 * <p>根据代付订单号查询订单</p>
	 *
	 * @param orderId
	 * @return
	 */
	Withdraw findOrderId(String orderId);

	Withdraw findOrderByApp(String appId, String appOrderId);

	/**
	 * 代付订单单纯修改为失败，兼容账户扣减失败
	 *
	 * @param orderId
	 */
	void updateWitError(String orderId);


	/**
	 * 查询当前未结算eth矿工费用代付订单
	 *
	 * @return
	 */
	Withdraw findEthFee();

	/**
	 * 标记当前代付矿工费已经结算
	 *
	 * @param orderId
	 */
	void updateEthFee(String orderId, String hash);

	Withdraw findOrderByAppSum(String appId);


	/**
	 * 查询未推送订单
	 *
	 * @return
	 */
    List<Withdraw> findSuccessAndNotAmount();

    /**
     * 修改订单 ： 已推送处理
     *
     * @param orderId
     */
    void updateSuccessAndAmount(String orderId);

    boolean updatePush(String orderId);

    /**
     * 查询未推送代付订单
     *
     * @return
     */
    List<Withdraw> findNotPush();

    void updateMsg(String orderId, String s);

    boolean updatePushAgent(String orderId);
}

