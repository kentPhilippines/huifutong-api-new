package alipay.manage.api.channel.amount.withdraw;

import alipay.manage.api.config.PayOrderService;
import alipay.manage.util.OrderUtil;
import alipay.manage.util.bankcardUtil.CreateOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.bean.dealpay.Withdraw;
import otc.result.Result;

/**
 * <p>自己的代付渠道</p>
 *
 * @author hx08
 */
@Component("MyChannelWithdraws")
public class MyChannelS extends PayOrderService {
	@Autowired
	OrderUtil orderUtil;
	@Autowired
	private CreateOrder createOrder;

	@Override
	public Result withdraw(Withdraw wit) {
		//Result withrawOrder = orderUtil.withrawOrder(wit.getOrderId(), wit.getRetain2(), false);


		Result result = createOrder.witAddOrder(wit, null,true);
		if (!result.isSuccess()) {
			withdrawEr(wit, result.getMessage(), wit.getRetain2());
		}
		return result;

		// TODO 商户或 码商 代付接口


		/**    字段					数据类型 		是否必传					字段解释
		 * appkey复制 			[int]				是						商户id	 
		 kyc	 				[int]				是						用户实名认证	展开
		 username	 		[string]			是						用户姓名	展开
		 area_code	 		[string]									国际区号(不传默认86，香港852/台湾886/澳门853)	展开
		 phone	 			[string]			是						手机号	展开
			id_card_type	 	[int]										证件类型	展开
			id_card_num	 		[string]									证件号码	展开
			email	 			[string]									邮箱	展开
			pay_card_num	 	[string]			是						银行卡号	展开
			pay_card_bank	 	[string]			是						开户银行	展开
			pay_card_branch	 	[string]									开户支行	展开
			company_order_num	[string]			是						商户订单号	展开
		 coin_sign	 		[string]			是						数字货币标识	展开
		 coin_sum	 		[string]			是						交易总额CNY 与coin_amount二选一	展开
		 coin_amount	 		[string]			是						实际成交数量 与coin_sum二选一	展开
		 order_amount	 	[string]									客户展示数量（最多接收四位小数）	展开
		 goods_name	 		[string]									商品名称	展开
		 order_time	 		[string]			是						订单时间戳(北京时间)	展开
		 sync_url	 		[string]			是						同步回调地址	展开
		 async_url	 		[string]			是						异步回调地址	展开
		 sign	 			[string]			是						签名比对
		 */
		//	return withrawOrder;
	}
	
	

}
