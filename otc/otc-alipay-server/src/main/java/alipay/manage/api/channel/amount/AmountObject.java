package alipay.manage.api.channel.amount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import alipay.manage.api.StorageApi;
import alipay.manage.api.feign.DealpayServiceClien;
import alipay.manage.mapper.RechargeMapper;
import alipay.manage.util.OrderUtil;
import otc.api.alipay.Common;
import otc.bean.dealpay.Recharge;
import otc.bean.dealpay.Withdraw;
import otc.result.Result;
/**
 * <p>充值代付接口渠道分发</p>
 * @author hx08
 *
 */
public abstract class AmountObject  implements AmountChannel{
	Logger log = LoggerFactory.getLogger(AmountObject.class);
	@Autowired RechargeMapper rechargeDao;
	@Autowired OrderUtil orderUtil;
	@Autowired DealpayServiceClien dealpayServiceClienImpl;
		@Override
		public Result recharge(Recharge rechaege) {
			//  返回充值接口
			
			
			return null;
		}
	
	
		
		
		
		@Override
		public Result withdraw(Withdraw wit) {
		// 返回代付成功 当找不到代付渠道的时候代付失败
		return null;
		}
		/**
		 * <p>充值失败时候，将充值订单修改为失败</p>
		 * @param rechaege				充值订单·
		 * @return
		 */
		Result updateRechargeEr(Recharge rechaege){
			log.info("【充值失败，可能原因，暂无充值渠道】");
			int a = rechargeDao.updateOrderStatus(rechaege.getOrderId(),Common.Order.Recharge.ORDER_STATUS_ER);
			if(a > 0  && a < 2)
				return Result.buildFailMessage("充值失败，可能原因，暂无充值渠道");
			return Result.buildFail();
		}
}
