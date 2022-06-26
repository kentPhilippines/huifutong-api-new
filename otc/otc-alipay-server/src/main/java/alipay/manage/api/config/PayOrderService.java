package alipay.manage.api.config;

import alipay.manage.api.channel.util.ChannelInfo;
import alipay.manage.bean.*;
import alipay.manage.bean.util.ResultDeal;
import alipay.manage.mapper.ChannelFeeMapper;
import alipay.manage.mapper.WithdrawMapper;
import alipay.manage.service.*;
import alipay.manage.util.NotifyUtil;
import alipay.manage.util.OrderUtil;
import alipay.manage.util.amount.AmountPublic;
import alipay.manage.util.amount.AmountRunUtil;
import alipay.manage.util.bankcardUtil.BankUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.support.TransactionTemplate;
import otc.api.alipay.Common;
import otc.bean.alipay.Medium;
import otc.bean.dealpay.Withdraw;
import otc.common.PayApiConstant;
import otc.common.SystemConstants;
import otc.result.Result;
import otc.util.RSAUtils;
import otc.util.number.GenerateOrderNo;
import otc.util.number.Number;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

/**
 * <p>请求交易抽象【交易】【代付】</p>
 *
 * @author kent
 */
public abstract class PayOrderService implements PayService {
	@Value("${otc.payInfo.url}")
	public   String url;
	public static final Log log = LogFactory.get();
	private static final String ORDER = "orderid";
	@Autowired
	private BankUtil qrUtil;
	@Autowired
	private WithdrawService withdrawServiceImpl;
	@Autowired
	private AmountPublic amountPublic;
	@Autowired
	private AmountRunUtil amountRunUtil;
	@Autowired
	private UserInfoService userInfoServiceImpl;
	@Autowired
	NotifyUtil notifyUtil;
	@Autowired
	private OrderService orderServiceImpl;
	@Autowired
	private OrderAppService OrderAppServiceImpl;
	@Autowired
	private CorrelationService correlationServiceImpl;
	@Autowired
	private UserRateService userRateServiceImpl;
	@Resource
	private ChannelFeeMapper channelFeeDao;
    @Autowired
    private OrderUtil orderUtilImpl;

    @Override
    public Result deal(DealOrderApp dealOrderApp, String channel) throws Exception {
		if (Common.Deal.PRODUCT_ALIPAY_SCAN.equals(channel)) {
			return dealAlipayScan(dealOrderApp);
		} else if (Common.Deal.PRODUCT_ALIPAY_H5.equals(channel)) {
			return dealAlipayH5(dealOrderApp);
		}
		return null;
	}

	public boolean orderEr(DealOrderApp orderApp, String msg) {
		log.info("【将当前订单置为失败，当前交易订单号：" + orderApp.getOrderId() + "】");
		DealOrder dealOrder = orderServiceImpl.findAssOrder(orderApp.getOrderId());
		if (ObjectUtil.isNotNull(dealOrder)) {
			boolean updateOrderStatus = orderServiceImpl.updateOrderStatus(dealOrder.getOrderId(), Common.Order.DealOrder.ORDER_STATUS_ER, msg);
			if (updateOrderStatus) {
				OrderAppServiceImpl.updateOrderEr(orderApp.getOrderId(), msg);
				return true;
			}
		}
		return false;
	}

	public boolean orderAppEr(DealOrderApp orderApp, String msg) {
		log.info("【将当前订单置为失败，当前交易订单号：" + orderApp.getOrderId() + "】");
		OrderAppServiceImpl.updateOrderEr(orderApp.getOrderId(), msg);
		return true;
	}

	public boolean orderEr(DealOrderApp orderApp) {
		return orderEr(orderApp, "暂无支付渠道");
	}

	public String create(DealOrderApp orderApp, String channeId) {
		log.info("【开始创建本地订单，当前创建订单的商户订单为：" + orderApp.toString() + "】");
		log.info("【当前交易的渠道账号为：" + channeId + "】");
		DealOrder order = new DealOrder();
		UserInfo userinfo = userInfoServiceImpl.findDealUrl(channeId);//查询渠道账户
		UserRate rate = userRateServiceImpl.findRateFeeType(orderApp.getFeeId());//长久缓存
		ChannelFee channelFee = channelFeeDao.findChannelFee(rate.getChannelId(), rate.getPayTypr());
		log.info("【当前交易的产品类型为：" + userinfo.getUserNode() + "】");
		order.setAssociatedId(orderApp.getOrderId());
		order.setDealDescribe("正常交易订单");
		order.setActualAmount(orderApp.getOrderAmount().subtract(new BigDecimal(orderApp.getRetain3())));
		order.setDealAmount(orderApp.getOrderAmount());
		order.setDealFee(new BigDecimal(orderApp.getRetain3()));
		order.setExternalOrderId(orderApp.getAppOrderId());
		order.setOrderAccount(orderApp.getOrderAccount());
		order.setNotify(orderApp.getNotify());
		String orderQrCh = GenerateOrderNo.Generate("J");
        order.setOrderId(orderQrCh);
        order.setOrderQrUser(userinfo.getUserId());
        order.setOrderStatus(Common.Order.DealOrder.ORDER_STATUS_DISPOSE.toString());
        order.setOrderType(Common.Order.ORDER_TYPE_DEAL.toString());
        order.setRetain1(rate.getPayTypr());
        order.setBack(orderApp.getBack());
        order.setCurrency(orderApp.getCurrency());
        String channelRFee = channelFee.getChannelRFee();
        BigDecimal orderAmount = orderApp.getOrderAmount();
        BigDecimal fee = new BigDecimal(channelRFee);
        log.info("【当前渠道费率：" + fee + "】");
        BigDecimal multiply = orderAmount.multiply(fee);
        log.info("【当前渠道收取手续费：" + multiply + "】");
        log.info("【当前收取商户手续费：" + orderApp.getRetain3() + "】");
        BigDecimal subtract = new BigDecimal(orderApp.getRetain3()).subtract(multiply);
        log.info("【当前订单系统盈利：" + subtract + "】");
		order.setRetain3(subtract.toString());
		orderServiceImpl.addOrder(order);
		return orderQrCh;
	};


	/**
	 * <p>支付宝扫码支付实体</p>
	 */
	public Result dealAlipayScan(DealOrderApp dealOrderApp) {
		/**
		 * #############################
		 * 生成预订单病返回支付连接
		 */
		//String encryptPublicKey = RSAUtils.getEncryptPublicKey(param, SystemConstants.INNER_PLATFORM_PUBLIC_KEY);
		String URL = url;//configServiceClientImpl.getConfig(ConfigFile.ALIPAY, ConfigFile.Alipay.SERVER_IP).getResult().toString();补充链接即可成为三方支付服务
		return Result.buildSuccessResult("支付处理中",ResultDeal.sendUrl(URL + "/pay/alipayScan?order_id="+dealOrderApp.getOrderId()));
	}
	/**
	 * <p>支付宝H5</p>
	 */
	public Result dealAlipayH5(DealOrderApp dealOrderApp) {
		/**
		 * ############################
		 * 生成预订单并返回支付连接
		 */
		return null;
	}
	@Autowired
	private TransactionTemplate transactionTemplate;
	/**
	 * <p>代付</p>
	 */
	@Override
	public Result withdraw(Withdraw wit) {
		/**
		 * #####################################
		 * 代付扣款操作
		 */
		//	lock.lock();
		try {
			UserFund userFund = new UserFund();// userInfoServiceImpl.findUserFundByAccount(wit.getUserId());
			userFund.setUserId(wit.getUserId());
			Result deleteTransaction = transactionTemplate.execute((Result) -> {
				Result deleteWithdraw = amountPublic.deleteWithdraw(userFund, wit.getActualAmount(), wit.getOrderId());
				if (!deleteWithdraw.isSuccess()) {
					return deleteWithdraw;
				}
				Result deleteAmount = amountRunUtil.deleteAmount(wit, wit.getRetain2(), false);
				if (!deleteAmount.isSuccess()) {
					return deleteAmount;
				}
				return deleteAmount;
			});
			if (!deleteTransaction.isSuccess()) {
				return Result.buildFailMessage("账户扣减失败,请联系技术人员处理");
			}
			Result deleteFeeTransaction = transactionTemplate.execute((Result) -> {
			Result deleteWithdraw2 = amountPublic.deleteWithdraw(userFund, wit.getFee(), wit.getOrderId());
			if (!deleteWithdraw2.isSuccess()) {
				return deleteWithdraw2;
			}
			Result deleteAmountFee = amountRunUtil.deleteAmountFee(wit, wit.getRetain2(), false);
			if (!deleteAmountFee.isSuccess()) {
				return deleteAmountFee;
			}
			return deleteAmountFee;
			});
			if (!deleteFeeTransaction.isSuccess()) {
				return Result.buildFailMessage("账户扣减失败,请联系技术人员处理");
			}
		}  finally {
			//	lock.unlock();
		}
		return Result.buildSuccess();

	}
	/**
	 * <p>代付失败</p>
	 * @param wit
	 * @param msg
	 * @param ip
	 * @return
	 */
	public Result withdrawEr(Withdraw wit, String msg, String ip) {
		Result withrawOrderErBySystem = orderUtilImpl.withrawOrderErBySystem(wit.getOrderId(), ip, msg);
		if (withrawOrderErBySystem.isSuccess()) {
				notifyUtil.wit(wit.getOrderId());
		}
		return withrawOrderErBySystem;
	}

	@Resource
	private WithdrawMapper withdrawDao;

	public void witComment(String orderId) {
		withdrawDao.updateComment(orderId, "已提交三方处理");
	}


	/**
	 * 请求渠道时,获取渠道详情
	 *
	 * @param channelId
	 * @param payType
	 * @return
	 */
	protected ChannelInfo getChannelInfo(String channelId, String payType) {
		ChannelInfo channelInfo = new ChannelInfo();
		UserInfo userInfo = userInfoServiceImpl.findNotifyChannel(channelId);
		channelInfo.setChannelAppId(userInfo.getUserNode());
		channelInfo.setChannelPassword(userInfo.getPayPasword());
		channelInfo.setDealurl(userInfo.getDealUrl());
		ChannelFee channelFee = channelFeeDao.findChannelFee(channelId, payType);
		channelInfo.setChannelType(channelFee.getChannelNo());
		if (StrUtil.isNotBlank(userInfo.getWitip())) {
			channelInfo.setWitUrl(userInfo.getWitip());
		}
		return channelInfo;
	}

	/**
	 * 修改代付订单为失败，只是单纯的失败
	 *
	 * @param orderId
	 * @return
	 */
	protected Result withdrawErByAmount(String orderId, String msg) {
		int a = withdrawDao.updataOrderStatusEr(orderId,
				Common.Order.Wit.ORDER_STATUS_ER, msg);
		if (a > 0) {
				notifyUtil.wit(orderId);
			return Result.buildSuccessMessage("修改成功");
		} else {
			return Result.buildFailMessage("修改失败");
		}
	}
	protected static boolean isNumber(String str) {
		BigDecimal a = new BigDecimal(str);
		double dInput = a.doubleValue();
		long longPart = (long) dInput;
		BigDecimal bigDecimal = new BigDecimal(Double.toString(dInput));
		BigDecimal bigDecimalLongPart = new BigDecimal(Double.toString(longPart));
		double dPoint = bigDecimal.subtract(bigDecimalLongPart).doubleValue();
		System.out.println("整数部分为:" + longPart + "\n" + "小数部分为: " + dPoint);
		return dPoint > 0;
	}
	public Result withdrawErMsg(Withdraw wit, String msg, String ip) {
		withdrawServiceImpl.updateMsg(wit.getOrderId(),msg);
		return  Result.buildSuccess();
	}


	public boolean addOrder(DealOrderApp orderApp , String ip ) {
		if (!orderApp.getOrderStatus().toString().equals(Common.Order.DealOrder.ORDER_STATUS_DISPOSE.toString())) {
			log.info("【订单状态有误】");
			return false;
		}
		String orderId = Number.alipayDeal();

		DealOrder order = new DealOrder();
		String orderAccount = orderApp.getOrderAccount();//交易商户号
		//	UserInfo accountInfo = userInfoServiceImpl.findUserInfoByUserId(orderAccount);//这里有为商户配置的 供应队列属性
	//	String[] split = {"zbzfb999"};
		String[] split = {"huifutong2"};
		UserRate rateFeeType = userRateServiceImpl.findRateFeeType(orderApp.getFeeId());//商户入款费率
		BigDecimal fee1 = rateFeeType.getFee();//商户交易订单费率
		order.setAssociatedId(orderApp.getOrderId());
		order.setDealDescribe("正常交易订单");
		order.setActualAmount(orderApp.getOrderAmount().subtract(fee1.multiply(orderApp.getOrderAmount())));
		order.setDealAmount(orderApp.getOrderAmount());
		order.setDealFee(fee1.multiply(orderApp.getOrderAmount()));
		order.setExternalOrderId(orderApp.getAppOrderId());
		order.setGenerationIp(ip);//终端玩家拉起ip
		order.setOrderAccount(orderApp.getOrderAccount());
		order.setNotify(orderApp.getNotify());
		Medium qr = null;
		qr = qrUtil.findQr(orderId, orderApp.getOrderAmount(), Arrays.asList(split), false, "alipay","");
		if (ObjectUtil.isNull(qr)) {
			return false;
		}
		order.setOrderQrUser(qr.getQrcodeId());
		order.setOrderQr(qr.getMediumId());
		order.setOrderStatus(Common.Order.DealOrder.ORDER_STATUS_DISPOSE.toString());
		order.setOrderType(Common.Order.ORDER_TYPE_DEAL.toString());
		UserRate userRateR = userRateServiceImpl.findUserRateR(qr.getQrcodeId());
		//	UserRate rate = userInfoServiceImpl.findUserRate(qr.getMediumHolder(), Common.Deal.PRODUCT_ALIPAY_SCAN);
		order.setOrderId(orderId);
		order.setFeeId(userRateR.getId());
		order.setRetain1(userRateR.getPayTypr());
		BigDecimal fee = userRateR.getFee();//卡商入款订单手续费率
		BigDecimal multiply = fee.multiply(orderApp.getOrderAmount());//码商接单成本
		log.info("码商接单成本："+multiply);
		order.setRetain2(multiply.toString());//渠道成本
		BigDecimal multiply1 = fee1.multiply(orderApp.getOrderAmount());
		log.info("商户此单交易手续费："+multiply);
		BigDecimal subtract = multiply1.subtract(multiply);//商户交易手续费 - 渠道成本 =  渠道利润
		log.info("渠道此单利润："+subtract);
		order.setRetain3(subtract.toString());// 渠道利润
		boolean addOrder = orderServiceImpl.addOrder(order);
		return addOrder;
	}







}
