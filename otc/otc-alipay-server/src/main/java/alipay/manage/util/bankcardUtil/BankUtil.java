package alipay.manage.util.bankcardUtil;

import alipay.config.redis.RedisUtil;
import alipay.manage.api.feign.ConfigServiceClient;
import alipay.manage.api.feign.QueueServiceClien;
import alipay.manage.bean.UserFund;
import alipay.manage.service.CorrelationService;
import alipay.manage.service.FileListService;
import alipay.manage.service.MediumService;
import alipay.manage.service.UserInfoService;
import alipay.manage.util.RiskUtil;
import alipay.manage.util.SettingFile;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.api.alipay.Common;
import otc.bean.alipay.Medium;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BankUtil {
	Logger log = LoggerFactory.getLogger(BankUtil.class);
	@Autowired
	SettingFile settingFile;
	@Resource
	RedisUtil redisUtil;
	@Autowired
	UserInfoService userInfoServiceImpl;
	@Autowired
	QueueServiceClien queueServiceClienFeignImpl;
	@Autowired
	FileListService fileListServiceImpl;
	@Autowired
	ConfigServiceClient configServiceClientImpl;
	@Autowired
	CorrelationService correlationServiceImpl;
	@Autowired
	RiskUtil riskUtil;
	DateFormat formatter = new SimpleDateFormat(Common.Order.DATE_TYPE);
	@Autowired
	private MediumService mediumService;
	private static final Long LOCK_TIME = 600L;
	private static final Long LOCK_TIME_OPEN = 300L;
	private static final String WIT_BANK_COUNT = "WIT:BANK:COUNT:";//代付出款缓存数据统计
	private static final String WIAT_BANK = "WIAT:BANK:";//代付出款缓存数据统计
	private static final String LIMIT_BANK = "LIMIT:BANK:";//代付出款缓存数据统计


	/**
	 * 推送消息给卡商提示有接单
	 *
	 * @param qrcodeId
	 */
	private void push(String qrcodeId) {


	}

	/**
	 * <p>
	 * 获取唯一的订单号,根据支付宝回调
	 * </p>
	 *
	 * @return
	 */
	public String findOrderBy(String amount, String phone, String bankNo,String payinfo) {
		log.info("【当前寻找回调参数为：amount = " + amount + "，phone = " + phone + "，bankNo = " + bankNo + "，payinfo = "+payinfo+"】");
		String notify = bankNo + amount.toString()+payinfo;
		Object object = redisUtil.get(notify);//可以找到即位当前充值订单的关联订单号
		if (ObjectUtil.isNull(object)) {
			return null;
		}
		/**
		 * 当前数据是否删除有待商榷
		 * 如果放开这个数据可能会造成保存支付的问题， 当前数据标记最大话的减少 不按照规定 支付出错的问题
		 */
		redisUtil.set(LIMIT_BANK + bankNo, LIMIT_BANK + bankNo, 10);//订单成功后这个卡无法被再次拉起
		redisUtil.deleteKey(notify);

		/**
		 * <p>
		 * 对IP解禁
		 * </p>
		 * Object IP = redisUtil.get(object.toString()); Set<Object> sGet =
		 * redisUtil.sGet(IP.toString()); Iterator<Object> iterator = sGet.iterator();
		 * while (iterator.hasNext()) { Object next = iterator.next();//这是具体值
		 * redisUtil.setRemove(IP.toString(),next.toString()); }
		 */
		return object.toString();
	}

	/**
	 * <p>
	 * 输入用户id，查询用户的虚拟冻结金额
	 * </p>
	 *
	 * @param userId 用户id
	 * @return amount 当前用户缓存冻结金额
	 */
	public BigDecimal getUserAmount(String userId) {
		BigDecimal amount = new BigDecimal("0");
		Map<Object, Object> hmget = redisUtil.hmget(userId);// 用户的虚拟hash金额缓存 key = 用户 + 时间 value = 金额
		Set<Object> keySet = hmget.keySet();
		try {
			for (Object obj : keySet) {
				String accountId = userId;
				int length = accountId.length();
				String subSuf = StrUtil.subSuf(obj.toString(), length);// 时间戳
				Date parse = formatter.parse(subSuf);
				Object object = hmget.get(obj.toString());// 当前金额
				if (!DateUtil.isExpired(parse, DateField.SECOND,
						Integer.valueOf(600 ), new Date())) {
					redisUtil.hdel(userId, obj.toString());
				}
			}
			Map<Object, Object> hmget2 = redisUtil.hmget(userId);
			Set<Object> keySet2 = hmget2.keySet();
			for (Object obj : keySet2) {
				Object object = hmget2.get(obj.toString());
				BigDecimal money = new BigDecimal(object.toString());
				amount = amount.add(money);
			}
		} catch (ParseException ex) {
			ex.printStackTrace();
		}

		return amount;
	}

	private static final String WIT_LOCK = "WIT:LOCK:";//代付出款缓存标记

	/**
	 * 针对专门的卡商限制金额
	 *
	 * @param amount
	 * @param userId
	 * @return
	 */

	public  static List<String> userList3000 = new ArrayList<>();
	public static List<String> userList1500 = new ArrayList<>();
	public static List<String> userList1000 = new ArrayList<>();
	public static List<String> userList4000 = new ArrayList<>();
	public static List<String> userList2000 = new ArrayList<>();
	public static List<String> userList5000 = new ArrayList<>();

	/**
	 * 存储当前代付缓存数据统计
	 */
	void saveWit(String userId, String orderId) {
		String hashkeyCount = userId + orderId;
		boolean hset = redisUtil.hset(WIT_BANK_COUNT + userId, hashkeyCount, orderId);
		log.info("【储存代付锁定数据：" + hashkeyCount + "，储存结果为：" + hset + "】");
	}

	/**
	 * 获取缓存代付金额
	 *
	 * @return
	 */
	boolean findAmountWit(String userId) {
		Map<Object, Object> hmget = redisUtil.hmget(WIT_BANK_COUNT + userId);
		log.info("【代付锁定数据：" + hmget.toString() + "】");
		return hmget.size() > 0;
	}

	/**
	 * 放开当前代付缓存数据
	 */
	public void openWit(String userId) {
		log.info("【删除代付锁定数据为：" + WIT_BANK_COUNT + userId + "】");
		redisUtil.del(WIT_BANK_COUNT + userId);

	}

	static {
	}

	/**
	 * <p>选码的本地方法</p>
	 *
	 * @param orderNo 订单号
	 * @param amount  金额
	 * @param code    选吗CODE值
	 * @param flag    是否为顶代结算模式  true  是     false   否
	 * @param info
	 * @param payInfo
     * @return
	 * @throws ParseException
     */
    public Medium findQr(String orderNo, BigDecimal amount, List<String> code, boolean flag, String codeMed, String payInfo) {
		Collection<String> strings = CollUtil.removeBlank(code);
		code = new ArrayList<>();
		for (String aa : strings) {
			code.add(aa);
		}
		String dealAmount = "";
		String[] split = amount.toString().split("\\.");
		if (split.length == 1) {
			String s = amount.toString();
			s += ".0";
			split = s.split("\\.");
		}
		String startAmount = split[0];
		String endAmount = split[1];
		int length = endAmount.length();
		if (length == 1) {//当交易金额为整小数的时候        补充0
			endAmount += "0";
		} else if (endAmount.length() > 2) {
			endAmount = "00";
		}
		dealAmount = startAmount + "." + endAmount;//得到正确的金额


		/**
		 * ######################################## 二维码回调逻辑,以及应该要注意的几个问题
		 * 1,防止出现同一个二维码在10分钟内同时调用 1>解决：在存入时候 先检查是否有相同二维码在缓存内使用 2,回调订单的唯一标识 1>采取策略：金额+手机号
		 * 3,当不满足任意条件的情况 1>选取 使用次数最少 2>选取 金额不一样
		 *
		 * List<String> keyS = new ArrayList<String>(); // Map<String,List<String>> map
		 * = new HashMap<String,List<String>>(); List<QrCode> qrLi = new
		 * ArrayList<QrCode>(); // qrList = shuffle(qrList); for(QrCode qc: qrList)
		 * {//两次风控规则 if(isClickQrCode(qc.getQrcodeId())) { keyS.add(qc.getQrcodeId());
         * qrLi.add(qc); } }
		 */
		// 根据金额获取符合条件的用户
		List<String> queue = queueServiceClienFeignImpl.getQueue(code);
		ThreadUtil.execute(() -> {
			for (String cod : queue) {
				log.info("【获取银行卡：" + cod + "】");
			}
		});
		List<UserFund> userList = userInfoServiceImpl.findUserByAmount(amount, flag);
		List<Medium> qcList = mediumService.findBankByAmount(amount, code,codeMed);
		log.info("【银行卡个数：" + qcList.size() + "】");



		if (CollUtil.isEmpty(userList) || CollUtil.isEmpty(qcList)) {
			return null;
		}
		ConcurrentHashMap<String, Medium> qrCollect = qcList.stream().collect(Collectors.toConcurrentMap(Medium::getMediumNumber, Function.identity(), (o1, o2) -> o1, ConcurrentHashMap::new));
		ConcurrentHashMap<String, UserFund> usercollect = userList.stream().collect(Collectors.toConcurrentMap(UserFund::getUserId, Function.identity(), (o1, o2) -> o1, ConcurrentHashMap::new));
		for (Object obj : queue) {
			String name = payInfo;

			String alipayAccount = obj.toString();
			if (StrUtil.isBlank(alipayAccount)) {
				continue;
			}
			Medium qr = qrCollect.get(alipayAccount);// 所属
			if (ObjectUtil.isNull(qr)) {
				continue;
			}
			log.info("【银行卡数据：" + qr.toString() + "】");
			UserFund qrcodeUser = usercollect.get(qr.getQrcodeId());// 所属
			if (ObjectUtil.isNull(qrcodeUser)) {
				continue;
			}

			if (!limitAmountOpen(amount, qrcodeUser.getUserId())) {
				log.info("当前银行卡只适合大额接单，卡商账号为：" + qrcodeUser.getUserId() + "，接单金额为：" + amount + "，银行卡号为：" + qr.getMediumNumber());
				continue;
			}
			Long time = 0L;
			log.info("【账户数据：" + qrcodeUser.toString() + "】");
			riskUtil.updataUserAmountRedis(qrcodeUser, flag);
			log.info("银行卡："+qr.getAccount() +" 当前付款人：" + payInfo + " 当前银行卡号 ： "+qr.getMediumNumber().trim()+ "当前手机号：" );
			/*if (openPayment(qr.getAccount())) {//是否开启姓名验证
				name = "";
			}*/
			Integer isClickPay = qr.getIsClickPay();
			if(isClickPay.equals(0)){
				name = "";
			}
			Object waitBank = null;
			String notify = qr.getMediumNumber().trim()  + dealAmount.toString().trim() + name;
			log.info("【核心回调控制数据：" + notify + " , 核心回调订单号-"+orderNo+", 核心回调时间："+ DateUtil.format(new Date(), Common.Order.DATE_TYPE)+"】");
			Object object2 = redisUtil.get(notify);//回调数据
			//	Object object = redisUtil.get(qr.getPhone());
			boolean clickAmount = riskUtil.isClickAmount(qr.getQrcodeId(), amount, usercollect, flag);
			Object o = redisUtil.get(LIMIT_BANK + qr.getMediumNumber().trim());//回调数据
			if (ObjectUtil.isNull(object2) && clickAmount && ObjectUtil.isNull(o)) {
			/*	time = LOCK_TIME_OPEN;
				if (BankOpen.BANK_LIST.contains(qr.getMediumNumber())) {
					time = LOCK_TIME_OPEN;
				}*/
				time =  600L;// qr.getSc();
				redisUtil.set(LIMIT_BANK + qr.getMediumNumber().trim(), LIMIT_BANK + qr.getMediumNumber().trim(), 30);//金额锁定时间标记     , 如果在20分钟内回调就会删除锁定金额
				redisUtil.set(notify, orderNo, time);    //核心回调数据
				//redisUtil.set(qr.getPhone(), qr.getPhone() + amount.toString(), Integer.valueOf( configServiceClientImpl.getConfig(ConfigFile.ALIPAY, ConfigFile.Alipay.QR_OUT_TIME).getResult().toString() ));
				String hashkey = qr.getQrcodeId() + DateUtil.format(new Date(), Common.Order.DATE_TYPE);    //锁定金额数据
				redisUtil.set("AMOUNT:LOCK:" + orderNo, hashkey, time);//金额锁定时间标记     , 如果在20分钟内回调就会删除锁定金额
				redisUtil.hset(qr.getQrcodeId(), hashkey, dealAmount.toString());//虚拟冻结金额
				// 该风控规则 后期有需求在加    当前媒介 如果超过  X 次未支付， 则对 当前媒介进行锁定			redisUtil.hset(qr.getFileId(), qr.getFileId() + orderNo, orderNo, Integer.valueOf( configServiceClientImpl.getConfig(ConfigFile.ALIPAY, ConfigFile.Alipay.QR_IS_CLICK).getResult().toString()));
				String orderMark = "ORDER:" + qr.getQrcodeId() + ":AUTO";
				redisUtil.set(orderMark, orderMark, 10);//金额锁定时间标记     , 如果在20分钟内回调就会删除锁定金额
				queueServiceClienFeignImpl.updataNodebank(qr);
				log.info("【获取二维码数据：" + qr.toString() + "】");
				push(qr.getQrcodeId());
				return qr;
			}
		}
		return null;
	}

	/**
	 * 针对专门的卡商限制接单金额
	 *
	 * @param amount
	 * @param userId
	 * @return
	 */
	Boolean limitAmountOpen(BigDecimal amount, String userId) {
		log.info("开放接单权限，给部分卡商限制金额，卡商id：" + userList3000.toString() + "，当前进入卡商：" + userId + "");
		if (userList3000.contains(userId)) {
			if (amount.compareTo(new BigDecimal("3000")) > -1) {
				return true;
			} else {
				return false;
			}
		}if (userList1500.contains(userId)) {
			if (amount.compareTo(new BigDecimal("1500")) > -1) {
				return true;
			} else {
				return false;
			}
		}if (userList4000.contains(userId)) {
			if (amount.compareTo(new BigDecimal("4000")) > -1) {
				return true;
			} else {
				return false;
			}
		}if (userList2000.contains(userId)) {
			if (amount.compareTo(new BigDecimal("2000")) > -1) {
				return true;
			} else {
				return false;
			}
		}if (userList1000.contains(userId)) {
			if (amount.compareTo(new BigDecimal("1000")) > -1) {
				return true;
			} else {
				return false;
			}
		}if (userList5000.contains(userId)) {
			if (amount.compareTo(new BigDecimal("5000")) > -1) {
				return true;
			} else {
				return false;
			}
		}
		return  true;
	}


	/**
	 * bank_1("平安银行"),
	 *     bank_2("中国银行"),
	 *     bank_3("广西农信"),
	 *     bank_4("长沙银行"),
	 *     bank_5("广东农信"),
	 *     bank_6("交通银行"),
	 *     bank_7("河南农信"),
	 *     bank_7("平安银行"),
	 * @param bankName  返回true 说明当前 不需要姓名验证
	 * @return
	 */
	boolean openPayment(String bankName){
		if (
				bankName.contains("中国银行")
				|| bankName.contains("平安")
				|| bankName.contains("长沙银行")
				|| bankName.contains("广东农信")
				|| bankName.contains("河南农信")
				|| bankName.contains("中信银行")
				|| bankName.contains("天津银行")
				|| bankName.contains("民生银行")
				|| bankName.contains("河北农信")
				|| bankName.contains("湖南农信")
				|| bankName.contains("山西农信")
				|| bankName.contains("山西农信")
				|| bankName.contains("北京农商")
				|| bankName.contains("海南农信社")
		) {
			return true;
		}
		return false;
	}

	Long addTime(String bankName){
		if (  bankName.contains("广西农信") ) {
			return 1500L;
		}if (  bankName.contains("交通银行") ) {
			return 1500L;
		}if (  bankName.contains("广东农信") ) {
			return 1500L;
		}
		return 0L;
	}

	public String limitAmountOpenAmount( String userId) {
		log.info("开放接单权限，给部分卡商限制金额，卡商id：" + userList3000.toString() + "，当前进入卡商：" + userId + "");
		if (userList3000.contains(userId)) {
				return "3000";
		}if (userList1500.contains(userId)) {
				return "1500";
		}if (userList4000.contains(userId)) {
				return "4000";
		}if (userList2000.contains(userId)) {
				return "2000";
		}if (userList1000.contains(userId)) {
				return "1000";
		}if (userList5000.contains(userId)) {
				return "5000";
		}
		return  "通道配额";
	}


}
