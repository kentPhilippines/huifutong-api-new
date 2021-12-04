package alipay.manage.service.impl;

import alipay.manage.mapper.WithdrawMapper;
import alipay.manage.service.WithdrawService;
import org.springframework.stereotype.Component;
import otc.api.alipay.Common;
import otc.bean.dealpay.Withdraw;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Component
public class WithdrawServiceImpl implements WithdrawService {
	@Resource
	private WithdrawMapper withdrawDao;

	@Override
	public boolean addOrder(Withdraw witb) {
		int insertSelective = withdrawDao.insertSelective(witb);
		return insertSelective > 0 && insertSelective < 2;
	}

	@Override
	public Withdraw findOrderId(String orderId) {
		return withdrawDao.findWitOrder(orderId);
	}

    @Override
    public Withdraw findOrderByApp(String appId, String appOrderId) {
        return withdrawDao.findOrderByApp(appId, appOrderId);
    }

    @Override
    public void updateWitError(String orderId) {
        withdrawDao.updataOrderStatusEr(orderId, Common.Order.Wit.ORDER_STATUS_ER, "账户扣减失败，订单失败");
    }

    @Override
    public Withdraw findEthFee() {
        return withdrawDao.findEthFee();
    }

    @Override
    public void updateEthFee(String orderId, String hash) {
        withdrawDao.updateEthFee(orderId, hash);
    }

    @Override
    public Withdraw findOrderByAppSum(String appId) {
        Withdraw ordemaprByAppSum = withdrawDao.findOrderByAppSum(appId);
        if (null == ordemaprByAppSum) {
            ordemaprByAppSum = new Withdraw();
            ordemaprByAppSum.setAmount(new BigDecimal("0"));
        }
        return ordemaprByAppSum;
    }


    /**
     * 查询订单成功但是未结算统计数据 与 代理数据的订单
     *
     * @return
     */
    @Override
    public List<Withdraw> findSuccessAndNotAmount() {

        return withdrawDao.findSuccessAndNotAmount();
    }


    /**
     * 将订单修改为以结算
     *
     * @param orderId
     */
    @Override
    public void updateSuccessAndAmount(String orderId) {
        withdrawDao.updateSuccessAndAmount(orderId);
    }

    @Override
    public boolean updatePush(String orderId) {
        return withdrawDao.updatePush(orderId);
    }

    @Override
    public List<Withdraw> findNotPush() {
        return withdrawDao.findNotPush();
    }

    @Override
    public void updateMsg(String orderId, String msg) {
        withdrawDao.updateMsg(orderId, msg);
    }

    @Override
    public boolean updatePushAgent(String orderId) {
        boolean b = withdrawDao.updatePushAgent(orderId) > 0;
        return b;
    }

}
