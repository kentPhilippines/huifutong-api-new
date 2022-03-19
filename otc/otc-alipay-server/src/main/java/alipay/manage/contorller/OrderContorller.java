package alipay.manage.contorller;

import alipay.config.redis.RedisUtil;
import alipay.manage.bean.DealOrder;
import alipay.manage.bean.RunOrder;
import alipay.manage.bean.UserInfo;
import alipay.manage.bean.UserRate;
import alipay.manage.bean.util.PageResult;
import alipay.manage.service.ExceptionOrderService;
import alipay.manage.service.OrderService;
import alipay.manage.service.UserInfoService;
import alipay.manage.service.UserRateService;
import alipay.manage.util.LogUtil;
import alipay.manage.util.OrderUtil;
import alipay.manage.util.SessionUtil;
import alipay.manage.util.SettingFile;
import alipay.manage.util.bankcardUtil.ReWit;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import otc.bean.dealpay.Recharge;
import otc.bean.dealpay.Withdraw;
import otc.exception.user.UserException;
import otc.result.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Controller
@RequestMapping("/order")
public class OrderContorller {
	Logger log = LoggerFactory.getLogger(OrderContorller.class);
	@Autowired SessionUtil sessionUtil;
	@Autowired OrderService orderServiceImpl;
	@Autowired OrderUtil orderUtil;
	@Autowired LogUtil logUtil;
	@Autowired SettingFile	settingFile;
	@Autowired UserInfoService accountServiceImpl;
	@Autowired UserRateService userRateService;
	private Lock lock = new ReentrantLock();
	/**
	 * <p>获取当前我，正在交易的订单</p>
	 * <p>若查询量过大，这里可能会造成服务器宕机</p>
	 * <p>查询量查询:  最大查询量为 ：  当前 可接单人数  如 当前可接单人数为  100 人 则可能造成  100个线程并发查询</p>
	 * @return
	 */
	@GetMapping("/findMyWaitReceivingOrder")
	@ResponseBody
	public Result findMyWaitReceivingOrder(HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		List<DealOrder> list = new ArrayList();
		/**
		 * <p>接口请求参数</p>
		 * 		accountId  : 码商 账户id
		 * <p>成功返回参数</p>
		 * JsonResult :
		 * 			code : 200
		 * 			success : true
		 * 			result : List<QrcodeDealOrder>
		 * 				<p>查询成功必传参数</p>
		 * 						orderId : 订单号
		 * 						dealAmount : 订单交易金额
		 * 						createTime : 订单创建时间
		 * 						dealType : 订单类型  (中文 ced:UTF-8)   例  alipay_qr:支付宝扫码
		 */
		return null;
	}

	@GetMapping("/findMyWaitConfirmOrder")
	@ResponseBody
	public Result findMyWaitConfirmOrder(HttpServletRequest request, String pageNum, String pageSize, String createTime, String orderStatus , String orderType) {
		UserInfo user2 = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user2)) {
			log.info("当前用户未登陆");
			return Result.buildFailMessage("当前用户未登陆");
		}
		log.info("获取当前用户----->" + user2.getUserId());
		PageHelper.startPage(Integer.valueOf(pageNum), Integer.valueOf(pageSize));
		List<DealOrder> listOrder = orderServiceImpl.findOrderByUser(user2.getUserId(), createTime, orderStatus,orderType);
		PageInfo<DealOrder> pageInfo = new PageInfo<DealOrder>(listOrder);
		PageResult<DealOrder> pageR = new PageResult<DealOrder>();
		pageR.setContent(pageInfo.getList());
		pageR.setPageNum(pageInfo.getPageNum());
		pageR.setTotal(pageInfo.getTotal());
		pageR.setTotalPage(pageInfo.getPages());
		return Result.buildSuccessResult(pageR);
	}
	@GetMapping("/grabAnOrderListFind")
	@ResponseBody
	public Result grabAnOrderListFind(HttpServletRequest request,String orderType) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		//按时间段查询
		log.info("当前用户进入抢单查询："+user.getUserId());
		List<DealOrder> listOrder = orderServiceImpl.grabAnOrderListFind( orderType ,islittle(user.getUserId()));
		return Result.buildSuccessResult(listOrder);
	}
	boolean islittle (String userId){
		UserInfo userInfoByUserId = accountServiceImpl.findUserInfoByUserId(userId);
		if(ObjectUtil.isNotNull(userInfoByUserId)){
			String agent = userInfoByUserId.getAgent();
			UserRate userRateW = userRateService.findUserRateW(agent);
			return userRateW.getPayTypr().equals("BANK_WIT_S");
		}
		return false;

	}
	@Autowired
	private ExceptionOrderService exceptionOrderServiceImpl;
	@Autowired
	private ReWit reWit;
	@GetMapping("/userConfirmToPaid")
	@ResponseBody
	@Transactional
	public Result userConfirmToPaid(HttpServletRequest request,String orderId) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			throw new UserException("当前用户未登录", null);
		}
		logUtil.addLog(request, "当前卡商置交易订单成功：" + orderId + "，：" + "，操作人：" + user.getUserId(), user.getUserId());
		DealOrder orderByOrderId = orderServiceImpl.findOrderByOrderId(orderId);
		if (StrUtil.isNotEmpty(orderByOrderId.getOrderQr())) {
			Result orderDealSu = orderUtil.orderDealSu(orderId, HttpUtil.getClientIP(request), user.getUserId());
			if(orderDealSu.isSuccess()){
				ThreadUtil.execute(()->{
					//重新抓一个公共切款的出款订单
				//	reWit.rewit(user.getUserId());
				});
			}
			exceptionOrderServiceImpl.enterBankOrder(orderByOrderId.getOrderId(),user.getUserId()," 卡商确认出款成功",Boolean.TRUE, HttpUtil.getClientIP(request),orderByOrderId.getDealAmount());
			return orderDealSu;
		} else {
			return Result.buildFailMessage("请绑定出款卡");
		}
	}

	@GetMapping("/findMyReceiveOrderRecordByPage")
	@ResponseBody
	@Transactional
	public Result findMyReceiveOrderRecordByPage(HttpServletRequest request,String receiveOrderTime,String pageNum,String pageSize,String productId) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			throw new UserException("当前用户未登录", null);
		}
		//通过产品的code费率产品信息		
		DealOrder order = new DealOrder();
		order.setOrderQrUser(user.getUserId());
		if (StrUtil.isNotBlank(receiveOrderTime)) {
			order.setTime(receiveOrderTime);
		}
		if (StrUtil.isNotBlank(productId)) {
			order.setRetain1(productId);
		}
		List<DealOrder> orderList = orderServiceImpl.findMyOrder(order);

		PageHelper.startPage(Integer.valueOf(pageNum), Integer.valueOf(pageSize));
		PageInfo<DealOrder> pageInfo = new PageInfo<DealOrder>(orderList);
		PageResult<DealOrder> pageR = new PageResult<DealOrder>();
		pageR.setContent(pageInfo.getList());
		pageR.setPageNum(pageInfo.getPageNum());
		pageR.setTotal(pageInfo.getTotal());
		pageR.setTotalPage(pageInfo.getPages());
		return Result.buildSuccessResult(pageR);
	}
	/**
	 * <p>获取个人流水</p>
	 * @param request
	 * @param startTime
	 * @param pageNum
	 * @param pageSize
	 * @param accountChangeTypeCode
	 * @return
	 */
	@GetMapping("/findMyAccountChangeLogByPage")
	@ResponseBody
	public Result findMyAccountChangeLogByPage(HttpServletRequest request,String startTime,
			String pageNum,String pageSize,String accountChangeTypeCode) {
		log.info("==========>" + accountChangeTypeCode);
		RunOrder order = new RunOrder();
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			log.info("当前用户未登陆");
			return Result.buildFailMessage("当前用户未登陆");
		}
		List<RunOrder> orderList = null;
		order.setOrderAccount(user.getUserId());
		if (StrUtil.isNotEmpty(startTime)) {
			order.setTime(startTime);
		}
		if (StrUtil.isNotEmpty(accountChangeTypeCode)) {
			order.setRunOrderType(Integer.valueOf(accountChangeTypeCode));
		}
		orderList = orderServiceImpl.findOrderRunByPage(order);

		PageHelper.startPage(Integer.valueOf(pageNum), Integer.valueOf(pageSize));
		PageInfo<RunOrder> pageInfo = new PageInfo<RunOrder>(orderList);
		PageResult<RunOrder> pageR = new PageResult<RunOrder>();
		pageR.setContent(pageInfo.getList());
		pageR.setPageNum(pageInfo.getPageNum());
		pageR.setTotal(pageInfo.getTotal());
		pageR.setTotalPage(pageInfo.getPages());
		return Result.buildSuccessResult(pageR);
	}
	@GetMapping("/findLowerLevelAccountChangeLogByPage")
	@ResponseBody
	@Transactional
	public Result findLowerLevelAccountChangeLogByPage(
			HttpServletRequest request,
			@RequestParam(required = false)String startTime,
			@RequestParam(required = false)String pageNum,
			@RequestParam(required = false)String pageSize,
			@RequestParam(required = true)String accountChangeTypeCode,
			@RequestParam(required = true)String userName
			) {
		log.info("startTime*****>"+startTime);
		log.info("accountChangeTypeCode*****>"+accountChangeTypeCode);
		log.info("userName*****>"+userName);
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
	        log.info("当前用户未登陆");
	        return Result.buildFailMessage("当前用户未登陆");
	    }
		List<String> userList =  accountServiceImpl.findSunAccountByUserId(user.getUserId());	
		if(StrUtil.isNotBlank(userName)) {
			if (!userList.contains(userName)) {
				return Result.buildFailMessage("输入账号有误");
			}
			userList.clear();
			userList.add(userName);
		} 
		userList.add(user.getUserId());
		log.info("子账户"+userList.toString());
		List<RunOrder> orderList =null;
		RunOrder orderRun = new RunOrder();
		orderRun.setOrderAccountList(userList);
		//所有下级的账号  带入到 流水  表里面看所有的流水[参数为null]查询所有下级账号流水
		if(StrUtil.isEmpty(startTime) || StrUtil.isEmpty(accountChangeTypeCode)) {
			orderList=orderServiceImpl.findAllOrderRunByPage(orderRun);			
		}else {
			if (StrUtil.isNotBlank(startTime)) {
				orderRun.setTime(startTime);
			}
			if (StrUtil.isNotBlank(accountChangeTypeCode)) {
				orderRun.setRunOrderType(Integer.valueOf(accountChangeTypeCode));
			}
			orderList = orderServiceImpl.findOrderRunByPage(orderRun);
		}
		PageHelper.startPage(Integer.valueOf(pageNum), Integer.valueOf(pageSize));
		PageInfo<RunOrder> pageInfo = new PageInfo<RunOrder>(orderList);
		PageResult<RunOrder> pageR = new PageResult<RunOrder>();
		pageR.setContent(pageInfo.getList());
		pageR.setPageNum(pageInfo.getPageNum());
		pageR.setTotal(pageInfo.getTotal());
		pageR.setTotalPage(pageInfo.getPages());
		return Result.buildSuccessResult(pageR);
	}
	
	@GetMapping("/findLowerLevelAccountReceiveOrderRecordByPage")
	@ResponseBody
	@Transactional
	public Result findLowerLevelAccountReceiveOrderRecordByPage(
			HttpServletRequest request,
			String startTime,
			String pageNum,
			String pageSize,
			String accountChangeTypeCode,
			String userName,
			String orderState
			) {
		log.info("accountChangeTypeCode :: " + accountChangeTypeCode);
		log.info("userName :: " + userName);
		log.info("orderState :: " + orderState);
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
	        log.info("当前用户未登陆");
	        return Result.buildFailMessage("当前用户未登陆");
	    }
		List<String> userList =  accountServiceImpl.findSunAccountByUserId(user.getUserId());
		if (StrUtil.isNotBlank(userName)) {
			if (!userList.contains(userName)) {
				return Result.buildFailMessage("输入账号有误");
			}
			userList.clear();
			userList.add(userName);
		}
		userList.add(user.getUserId());
		log.info("子账户" + userList.toString());
		DealOrder order = new DealOrder();
		if (StrUtil.isNotBlank(startTime)) {
			order.setTime(startTime);
		}
		if (StrUtil.isNotBlank(orderState)) {
			order.setOrderStatus(orderState);
		}
		order.setOrderQrUserList(userList);
		PageHelper.startPage(Integer.valueOf(pageNum), Integer.valueOf(pageSize));
		List<DealOrder> orderList = orderServiceImpl.findOrderByPage(order);
		PageInfo<DealOrder> pageInfo = new PageInfo<DealOrder>(orderList);
		PageResult<DealOrder> pageR = new PageResult<DealOrder>();
		pageR.setContent(pageInfo.getList());
		pageR.setPageNum(pageInfo.getPageNum());
		pageR.setTotal(pageInfo.getTotal());
		pageR.setTotalPage(pageInfo.getPages());
		return Result.buildSuccessResult(pageR);
	}
	/**
	 * <p>获取个人流水</p>
	 * @param request
	 * @param startTime
	 * @param pageNum
	 * @param pageSize
	 * @param orderType
	 * @return
	 */
	@GetMapping("/findMyRechargeWithdrawLogByPage")
	@ResponseBody
	public Result findMyRechargeWithdrawLogByPage(HttpServletRequest request,
			String startTime,
			String pageNum,
			String pageSize,
			String orderType
			) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			log.info("当前用户未登陆");
			return Result.buildFailMessage("当前用户未登陆");
		}
		if (StrUtil.isBlank(orderType)) {
			orderType = "1";
		}
		if ("1".equals(orderType)) {//充值
			Recharge bean = new Recharge();
			bean.setUserId(user.getUserId());
			if (StrUtil.isNotBlank(startTime)) {
				bean.setTime(startTime);
			}
			PageHelper.startPage(Integer.valueOf(pageNum), Integer.valueOf(pageSize));
			List<Recharge> witList = orderServiceImpl.findRechargeOrder(bean);
			PageInfo<Recharge> pageInfo = new PageInfo<Recharge>(witList);
			PageResult<Recharge> pageR = new PageResult<Recharge>();
			pageR.setContent(pageInfo.getList());
			pageR.setPageNum(pageInfo.getPageNum());
			pageR.setTotal(pageInfo.getTotal());
			pageR.setTotalPage(pageInfo.getPages());
			return Result.buildSuccessResult(pageR);
		}else {//提现
			Withdraw bean = new Withdraw();
			bean.setUserId(user.getUserId());
			if (StrUtil.isNotBlank(startTime)) {
				bean.setTime(startTime);
			}
			PageHelper.startPage(Integer.valueOf(pageNum), Integer.valueOf(pageSize));
			List<Withdraw> witList = orderServiceImpl.findWithdrawOrder(bean);
			PageInfo<Withdraw> pageInfo = new PageInfo<Withdraw>(witList);
			PageResult<Withdraw> pageR = new PageResult<Withdraw>();
			pageR.setContent(pageInfo.getList());
			pageR.setPageNum(pageInfo.getPageNum());
			pageR.setTotal(pageInfo.getTotal());
			pageR.setTotalPage(pageInfo.getPages());
			return Result.buildSuccessResult(pageR);
		}
	}
	/**
	 * <p>码商确认提现成功</p>
	 * @param orderId			提现订单号
	 * @return
     */
	@GetMapping("/enterOrderSu")
	@ResponseBody
	public Result enterOrderSu(String orderId,HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			log.info("当前用户未登陆");
			return Result.buildFailMessage("当前用户未登陆");
		}
		if (StrUtil.isBlank(orderId)) {
			return Result.buildFailResult("参数为空");
		}
		Map<String, Object> paramMap = new HashMap();
		paramMap.put("orderId", orderId);
		paramMap.put("userId", user.getUserId());
		String URL = settingFile.getName(SettingFile.ENTER_ORDER_SU);
		String post = HttpUtil.post(URL, paramMap);
		JSONObject parseObj = JSONUtil.parseObj(post);
		Result bean = JSONUtil.toBean(parseObj, Result.class);
		return bean;
	}

	@GetMapping("/addQrCode")
	@ResponseBody
	public Result addQrCode(String orderId, String qrcodeId, HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			log.info("当前用户未登陆");
			return Result.buildFailMessage("当前用户未登陆");
		}
		if (StrUtil.isBlank(orderId)) {
			return Result.buildFailResult("参数为空");
		}
		if (StrUtil.isBlank(qrcodeId)) {
			return Result.buildFailResult("参数为空");
		}

		boolean flag = orderServiceImpl.updatePayImg(orderId, qrcodeId);
		if (flag) {
			return Result.buildSuccess();
		} else {
			return Result.buildFail();
		}
	}

	/**
	 * <p>申诉</p>
	 *
	 * @param request
	 * @param appealType
	 * @param actualPayAmount
	 * @param userSreenshotIds
	 * @param merchantOrderId
	 * @return
	 */
	@PostMapping("/userStartAppeal")
	@ResponseBody
	public Result userStartAppeal(HttpServletRequest request, String appealType, String actualPayAmount, String userSreenshotIds, String merchantOrderId) {
		UserInfo user = sessionUtil.getUser(request);
		RunOrder order = new RunOrder();
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		return null;
	}

	private static final String ORDER_MARK = "ORDER:";
	private static final String AUTO_MARK = ":AUTO";
	@Autowired
	private RedisUtil redisUtil;

	@GetMapping("/findOrder")
	@ResponseBody
	public Result findOrder(HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		RunOrder order = new RunOrder();
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		//	log.info("查询当前是否有接单订单，当前用户："+user.getUserId());
		String orderMark = ORDER_MARK + user.getUserId() + AUTO_MARK;
		Object hget = redisUtil.get(orderMark);
		if (null != hget) {
			log.info("有订单：" + hget);
			return Result.buildSuccessMessage("有订单来了，记得处理，亲");
		}
		return Result.buildFail();
	}


}