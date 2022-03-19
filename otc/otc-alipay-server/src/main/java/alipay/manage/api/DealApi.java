package alipay.manage.api;

import alipay.manage.api.config.NotfiyChannel;
import alipay.manage.bean.*;
import alipay.manage.mapper.DealOrderAppMapper;
import alipay.manage.service.*;
import alipay.manage.util.LogUtil;
import alipay.manage.util.NotifyUtil;
import alipay.manage.util.QrUtil;
import alipay.manage.util.bankcardUtil.BankUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import otc.api.alipay.Common;
import otc.bean.alipay.FileList;
import otc.bean.alipay.Medium;
import otc.common.SystemConstants;
import otc.result.Result;
import otc.util.RSAUtils;
import otc.util.number.Number;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
@Controller
@RequestMapping("/pay")
public class DealApi extends NotfiyChannel {
	private static final String ORDER = "orderid";
	@Autowired OrderAppService orderAppServiceImpl;
	@Autowired OrderService orderServiceImpl;
	@Autowired UserInfoService userInfoServiceImpl;
	@Autowired
	private BankUtil qrUtil;
	@Autowired LogUtil logUtil;
	@Autowired NotifyUtil notifyUtil;
	@Autowired FileListService fileListServiceImpl;
	@Autowired MediumService mediumServiceImpl;
	@Autowired CorrelationService correlationServiceImpl;
	@Autowired  private UserRateService userRateServiceImpl;

	static Lock lock = new ReentrantLock();
	private static final String tinyurl =  "http://tinyurl.com/api-create.php";
	private static final Log log = LogFactory.get();
	@RequestMapping("/alipayScan")
	@ResponseBody
	public Result alipayScan(HttpServletRequest request) {
		String orderId = request.getParameter("order_id");
		String ip = request.getParameter("ip");
		if(StrUtil.isEmpty(orderId) ||StrUtil.isEmpty(ip)   ){
			log.info("【关联订单号为空】");
		}
		log.info("【请求交易的终端用户交易请求参数为：order_id=" + orderId + "】");
/*
		log.info("【请求交易的终端用户交易请求参数为：" + param + "】");
		Map<String, Object> stringObjectMap = RSAUtils.retMapDecode(param, SystemConstants.INNER_PLATFORM_PRIVATE_KEY);
		if (CollUtil.isEmpty(stringObjectMap)) {
			log.info("【参数解密为空】");
		}*/
		log.info("【当前请求交易订单号为：" + orderId + "】");
		DealOrder order = orderServiceImpl.findAssOrder(orderId);
		if (ObjectUtil.isNotNull(order)) {
			//return "toFixationPay";
			return  Result.buildSuccessMessage("订单成功,请拉起扫码页面获取详细扫码信息");
		}
		DealOrderApp orderApp = orderAppServiceImpl.findOrderByOrderId(orderId);
		log.info(orderApp.toString());
		boolean flag = addOrder(orderApp, request,ip);
		if (!flag) {
			log.info("【订单生成有误，或者当前武可用渠道】");
			ThreadUtil.execute(() -> {
				orderAppServiceImpl.updateOrderEr(orderId, "当前无可用渠道");
			});
			return Result.buildFailMessage("当前暂无二维码");
		//	return "payEr";
		}
	//	return "toFixationPay";
		return Result.buildSuccessMessage("订单成功,请拉起扫码页面获取详细扫码信息");
	}
	private boolean addOrder(DealOrderApp orderApp, HttpServletRequest request,String ip ) {
		if (!orderApp.getOrderStatus().toString().equals(Common.Order.DealOrder.ORDER_STATUS_DISPOSE.toString())
		) {
			log.info("【订单状态有误】");
			return false;
		}
		String orderId = Number.alipayDeal();

		DealOrder order = new DealOrder();
		String orderAccount = orderApp.getOrderAccount();//交易商户号
	//	UserInfo accountInfo = userInfoServiceImpl.findUserInfoByUserId(orderAccount);//这里有为商户配置的 供应队列属性
	//	 String[] split = {"huifutong2"};
		String[] split = {"zbzfb999"};
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
		if (addOrder) {
			corr(order.getOrderId());
		}
		return addOrder;
	}
	/**
	 * <p>数据数据统计</p>
	 */
	void corr(String orderId){
		ThreadUtil.execute(()->{
			DealOrder order = orderServiceImpl.findOrderByOrderId(orderId);
			FileList findQrByNo = fileListServiceImpl.findQrByNo(order.getOrderQr());
			Medium medium = mediumServiceImpl.findMediumById(findQrByNo.getConcealId());
			CorrelationData corr = new CorrelationData();
			corr.setAmount(order.getDealAmount());
			corr.setMediumId(medium.getId());
			corr.setOrderId(order.getOrderId());
			corr.setQrId(findQrByNo.getId().toString());
			corr.setOrderStatus(Integer.valueOf(order.getOrderStatus()));
			corr.setUserId(order.getOrderQrUser());
			corr.setAppId(order.getOrderAccount());
			boolean addCorrelationDate = correlationServiceImpl.addCorrelationDate(corr);
			if (addCorrelationDate) {
				log.info("【订单号：" + order.getOrderId() + "，添加数据统计成功】");
			} else {
				log.info("【订单号：" + order.getOrderId() + "，添加数据统计失败】");
			}
		});
	}
	
	
	@GetMapping("/getOrderGatheringCode")
	@ResponseBody
	public Result findOrder(String orderNo) {
		log.info("【查询订单号为："+orderNo+"】");
		String[] split = orderNo.split("/");
		List<String> asList = Arrays.asList(split);
		String last = CollUtil.getLast(asList);
		log.info("【当前元素为："+last+"】");
		Map<String, Object> stringObjectMap = RSAUtils.retMapDecode(last, SystemConstants.INNER_PLATFORM_PRIVATE_KEY);
		DealOrder order2 = orderServiceImpl.findAssOrder(stringObjectMap.get(ORDER).toString());
		return Result.buildSuccessResult(order2);
	}
	@Resource
	DealOrderAppMapper dealOrderAppDao;
	
	@GetMapping("/getOrderGatheringCode1")
	@ResponseBody
	public Result findOrder1(String orderNo) {
		log.info("【查询订单号为："+orderNo+"】");
		if(StrUtil.isEmpty(orderNo)){
			 return Result.buildFailMessage("必传订单号为空");
		}
		DealOrder orderByAssociatedId = orderServiceImpl.findOrderByAssociatedId(orderNo);
		String orderQr = orderByAssociatedId.getOrderQr();//支付宝数据
		Medium mediumById = mediumServiceImpl.findMediumById(orderQr);
		Map  map = new HashMap();
		map.put("order",orderByAssociatedId);
		map.put("medium",mediumById);
		return Result.buildSuccessResult(map);
	}
	
	@GetMapping("/testWit")
	@ResponseBody
	public Result testWit(String orderNo) {
		log.info("【查询订单号为："+orderNo+"】");
		return witNotfy(orderNo,"123.2.2.2");
	}


	
}
