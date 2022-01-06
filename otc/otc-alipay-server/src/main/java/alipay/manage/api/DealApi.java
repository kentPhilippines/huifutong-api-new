package alipay.manage.api;

import alipay.manage.api.config.NotfiyChannel;
import alipay.manage.bean.*;
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

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
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
	static Lock lock = new ReentrantLock();
	private static final String tinyurl =  "http://tinyurl.com/api-create.php";
	private static final Log log = LogFactory.get();
	@RequestMapping("/alipayScan")
	public String alipayScan(HttpServletRequest request) {
		String orderId = request.getParameter("order_id");
		if(StrUtil.isEmpty(orderId)){
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
			return "toFixationPay";
		}
		DealOrderApp orderApp = orderAppServiceImpl.findOrderByOrderId(orderId);
		boolean flag = addOrder(orderApp, request);
		if (!flag) {
			log.info("【订单生成有误，或者当前武可用渠道】");
			ThreadUtil.execute(() -> {
				orderAppServiceImpl.updateOrderEr(orderId, "当前无可用渠道");
			});
			return "payEr";
		}
		return "toFixationPay";
	}
	private boolean addOrder(DealOrderApp orderApp, HttpServletRequest request) {
		if (!orderApp.getOrderStatus().toString().equals(Common.Order.DealOrder.ORDER_STATUS_DISPOSE.toString())) {
			return false;
		}
		DealOrder order = new DealOrder();
		String orderAccount = orderApp.getOrderAccount();//交易商户号
		UserInfo accountInfo = userInfoServiceImpl.findUserInfoByUserId(orderAccount);//这里有为商户配置的 供应队列属性
		String[] split = {};
		if (StrUtil.isNotBlank(accountInfo.getQueueList())) {
			split = accountInfo.getQueueList().split(",");//队列供应标识数组
		}
		order.setAssociatedId(orderApp.getOrderId());
		order.setDealDescribe("正常交易订单");
		order.setActualAmount(orderApp.getOrderAmount());
		order.setDealAmount(orderApp.getOrderAmount());
		order.setDealFee(new BigDecimal("0"));
		order.setExternalOrderId(orderApp.getAppOrderId());
		order.setGenerationIp(HttpUtil.getClientIP(request));
		order.setOrderAccount(orderApp.getOrderAccount());
		order.setNotify(orderApp.getNotify());
		Medium qr = null;

			 qr = qrUtil.findQr(orderApp.getOrderId(), orderApp.getOrderAmount(), Arrays.asList(split), true, "");

		if (ObjectUtil.isNull(qr)) {
			return false;
		}
		order.setOrderQrUser(qr.getMediumHolder());
		order.setOrderQr(qr.getMediumId());
		order.setOrderStatus(Common.Order.DealOrder.ORDER_STATUS_DISPOSE.toString());
		order.setOrderType(Common.Order.ORDER_TYPE_DEAL.toString());
		UserRate rate = userInfoServiceImpl.findUserRate(qr.getMediumHolder(), Common.Deal.PRODUCT_ALIPAY_SCAN);
		order.setOrderId(Number.getOrderQr());
		order.setFeeId(rate.getId());
		order.setRetain1(rate.getPayTypr());
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
	
	
	@GetMapping("/getOrderGatheringCode1")
	@ResponseBody
	public Result findOrder1(String orderNo) {
		log.info("【查询订单号为："+orderNo+"】");
		DealOrder order2 = orderServiceImpl.findOrderByOrderId(orderNo);
		return Result.buildSuccessResult(order2);
	}
	
	@GetMapping("/testWit")
	@ResponseBody
	public Result testWit(String orderNo) {
		log.info("【查询订单号为："+orderNo+"】");
		return witNotfy(orderNo,"123.2.2.2");
	}


	
}
