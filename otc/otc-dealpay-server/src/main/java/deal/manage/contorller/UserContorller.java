package deal.manage.contorller;


import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import deal.manage.api.AccountApiService;
import deal.manage.bean.*;
import deal.manage.service.*;
import deal.manage.util.CardBankOrderUtil;
import deal.manage.util.QrUtil;
import deal.manage.util.SessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import otc.api.dealpay.Common;
import otc.exception.other.OtherException;
import otc.result.Result;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Controller
@RequestMapping("/userAccount")
public class UserContorller {
	Logger log = LoggerFactory.getLogger(UserContorller.class);
	@Autowired
	UserInfoService userInfoServiceImpl;
	@Autowired
	SessionUtil sessionUtil;
	@Autowired
	BankListService bankCardServiceImpl;
	@Autowired
	InviteCodeService inviteCodeServiceImpl;
	@Autowired
	CardBankOrderUtil bankUtil;
	@Autowired QrUtil qrUtil;
	@Autowired AccountApiService accountApiServiceImpl;
	@Autowired UserRateService userRateServiceImpl;
	@Autowired UserFundService userFundServiceImpl;
	/**
	 * <p>获取账号登录情况</p>
	 * @return
	 */
	@GetMapping("/getUserAccountInfo")
	@ResponseBody
	public Result getUserAccountInfo(HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}

		UserRate rateR = null;
		UserRate rateC = null;
		UserFund userFund = null;
		UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(user.getUserId());
		Future<UserRate> execAsync3 = ThreadUtil.execAsync(() -> {
			return userRateServiceImpl.findUserRateR(userInfo.getUserId());
		});
		Future<UserRate> execAsync2 = ThreadUtil.execAsync(() -> {
			return userRateServiceImpl.findUserRateC(userInfo.getUserId());
		});
		Future<UserFund> execAsync = ThreadUtil.execAsync(()->{
			return userFundServiceImpl.findUserFundMount(user.getUserId());
		});
			try {
				rateR = execAsync3.get( );
				rateC = execAsync2.get( );
				userFund = execAsync.get( );
			} catch (InterruptedException | ExecutionException e) {
			}
		userInfo.setFee(rateR.getFee().toString());
		userInfo.setCardFee(rateC.getFee().toString());
		userInfo.setAccountBalance(userFund.getAccountBalance().toString());
		return Result.buildSuccessResult("数据获取成功",userInfo);
	}
	@GetMapping("/getUserFund")
	@ResponseBody
	public Result getUserFund(HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		UserFund userFund = userFundServiceImpl.findUserFund(user.getUserId());
		return Result.buildSuccessResult("数据获取成功", userFund);
	}
	/**
	 * <p>修改当前用户的接单状态</p>
	 * @param receiveOrderState        
	 * @return
	 */
	@PostMapping("/updateReceiveOrderState")
	@ResponseBody
	public Result updateReceiveOrderState(HttpServletRequest request,String receiveOrderState,String accountId) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		if (StrUtil.isNotBlank(accountId)) {
			user.setUserId(accountId);
		}
		boolean flag = userInfoServiceImpl.updateReceiveOrderState(user.getUserId(), Integer.valueOf(receiveOrderState));
		if (flag) {
			return Result.buildSuccessResult();
		}
		return Result.buildFailMessage("修改失败");
	}
	
	/**
	 * <p>获取用户绑定的银行卡接口</p>
	 * @param receiveOrderState
	 * @return
	 */
	@GetMapping("/getBankInfo")
	@ResponseBody
	public Result getBankInfo(String receiveOrderState ,HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		List<BankList> bank = bankCardServiceImpl.findBankCardByQr(user.getUserId());
		return Result.buildSuccessResult(bank);
	}
	/**
	 * <p>添加银行卡</p>
	 * @param receiveOrderState
	 * @return
	 */
	@PostMapping("/bindBankInfo")
	@ResponseBody
	public Result bindBankInfo(@RequestBody BankList bank ,HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		bank.setAccount(user.getUserId());
		bank.setCardType(Common.Bank.BANK_QR);
		boolean flag = false;
		bank.setStatus(1);
		bank.setIsDeal(2);
		bank.setSubmitTime(new Date());
		if (StrUtil.isNotEmpty(bank.getBankcardId())) {
			flag = bankCardServiceImpl.editBankCard(bank);
		} else {
			flag = bankCardServiceImpl.addBankCard(bank);
		}
		if (flag) {
			return Result.buildSuccessMessage("添加银行卡成功");
		}
		return Result.buildFailMessage("添加银行卡失败");
	}
	/**
	 * <p>用户通过邀请码注册</p>
	 * @param receiveOrderState
	 * @return
	 */
	@PostMapping("/register")
	@ResponseBody
	@Transactional
	public Result register(  UserInfo user ,HttpServletRequest request) {
		if (StrUtil.isBlank(user.getUserId()) || StrUtil.isBlank(user.getPassword())
				|| StrUtil.isBlank(user.getPassword())
				|| StrUtil.isBlank(user.getPayPasword())
				|| StrUtil.isBlank(user.getUserName())
				|| StrUtil.isBlank(user.getInviteCode())) {
			return Result.buildFailMessage("必传参数为空");
		}
		Invitecode code = inviteCodeServiceImpl.findInviteCode(user.getInviteCode());
		if (code.getStatus() == 0) {
			return Result.buildFailMessage("当前邀请码不可用");
		}
		user.setAgent(code.getBelongUser());//邀请码生成人
		user.setIsAgent("agent".equals(code.getUserType()) ? "1" : "2");
		user.setUserType(Common.User.USER_TYPE_CARD);
		Result addAccount = accountApiServiceImpl.addAccount(user);
		boolean flag = false;
		boolean add = false;
		boolean a = false;
		if (addAccount.isSuccess()) {
			flag = inviteCodeServiceImpl.updataInviteCode(user.getInviteCode(), user.getUserId());
			if (flag) {
				UserRate rate = new UserRate();
				rate.setUserId(user.getUserId());
				rate.setFee(code.getFee());
				rate.setFeeType(Common.User.DEAL_FEE);
				rate.setUserType(Common.User.USER_TYPE_CARD);
				add = userRateServiceImpl.add(rate);
				rate.setFeeType(Common.User.CAED_FEE);
				rate.setFee(code.getCustFee());
				a = userRateServiceImpl.add(rate);
			}
		}
		if (addAccount.isSuccess() && flag && add && a) {
			return Result.buildSuccessResult();
		}
		return Result.buildFailMessage("注册失败");
	}
	/**
	 * <p>修改密码</p>
	 * @param receiveOrderState
	 * @return
	 */
	@PostMapping("/modifyLoginPwd")
	@ResponseBody
	public Result modifyLoginPwd(String oldLoginPwd , String newLoginPwd,HttpServletRequest request) {
		if (StrUtil.isBlank(oldLoginPwd) || StrUtil.isBlank(newLoginPwd)) {
			return Result.buildFailMessage("必传参数为空");
		}
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		user.setPassword(oldLoginPwd);
		user.setNewPassword(newLoginPwd);
		Result updateAccountPassword = accountApiServiceImpl.updateLoginPassword(user);
		if (updateAccountPassword.isSuccess()) {
			return Result.buildSuccessResult();
		}
		return updateAccountPassword;
	}
	/**
	 * <p>修改密码</p>
	 * @param receiveOrderState
	 * @return
	 */
	@PostMapping("/modifyMoneyPwd")
	@ResponseBody
	public Result modifyMoneyPwd(String oldMoneyPwd , String newMoneyPwd,HttpServletRequest request) {
		if (StrUtil.isBlank(oldMoneyPwd) || StrUtil.isBlank(newMoneyPwd)) {
			return Result.buildFailMessage("必传参数为空");
		}
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		user.setNewPayPassword(newMoneyPwd);
		user.setPayPasword(oldMoneyPwd);
		Result updateAccountPassword = accountApiServiceImpl.updateLoginPassword(user);
		if (updateAccountPassword.isSuccess()) {
			return Result.buildSuccessResult();
		}
		return updateAccountPassword;
	}
	
	/**
	 * <p>修改当前二维码为不可接单</p>
	 * @return
	 */
	@GetMapping("/updataQrStatusEr")
	@ResponseBody
	public Result updataQrStatusEr(String id ,HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		boolean flag = bankCardServiceImpl.updataQrStatusEr(id);
		if (flag) {
			return Result.buildSuccessResult();
		}
		return Result.buildFailMessage("修改失败");
	}
	/**
	 * <p>修改当前二维码为可接单</p>
	 * @return
	 */
	@GetMapping("/updataQrStatusSu")
	@ResponseBody
	public Result updataQrStatusSu(String id ,HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		boolean flag = bankCardServiceImpl.updataStatusSu(id);
		if (flag) {
			return Result.buildSuccessResult();
		}
		return Result.buildFailMessage("修改失败");
	}
	/**
	 * <p>修改会员性质为代理商</p>
	 * @param request
	 * @param accountId
	 * @return
	 * @throws ParseException
	 */
	@PostMapping("/updateIsAgent")
	@ResponseBody
	public Result updateIsAgent(HttpServletRequest request,String userId ) throws ParseException {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		boolean flag = false;
		boolean flag2s = false;
		Future<Boolean> execAsync = ThreadUtil.execAsync(() -> {
			return userInfoServiceImpl.updateIsAgent(userId);
		});
		Future<Boolean> execAsync2 = ThreadUtil.execAsync(() -> {
			return userFundServiceImpl.updateIsAgent(userId);
		});
		try {
			flag = execAsync.get();
			flag2s = execAsync2.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new OtherException("修改异常", null);
		}
		if (flag && flag2s) {
			return Result.buildSuccessResult();
		}
		return Result.buildFailMessage("修改为代理商失败");
	}
	@PostMapping("/findUserByAccountId")
	@ResponseBody
	public Result findUserByAccountId(HttpServletRequest request,String userId ) throws ParseException {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		UserRate rateR = null;
		UserRate rateC = null;
		UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(userId);
		Future<UserRate> execAsync3 = ThreadUtil.execAsync(() -> {
			return userRateServiceImpl.findUserRateR(userInfo.getUserId());
		});
		Future<UserRate> execAsync2 = ThreadUtil.execAsync(() -> {
			return userRateServiceImpl.findUserRateC(userInfo.getUserId());
		});
		try {
			rateR = execAsync3.get();
			rateC = execAsync2.get();
		} catch (InterruptedException | ExecutionException e) {
			return Result.buildFailMessage("错误"); 
		}
		userInfo.setFee(rateR.getFee().toString());
		userInfo.setCardFee(rateC.getFee().toString());
		return Result.buildSuccessResult(userInfo);
	}
	@GetMapping("/updataAccountFee")
	public String updataAccountFee(HttpServletRequest request,String accountId ) throws ParseException {
		return "updataAccountFee";
	}
	/**
	 * <p>修改下级代理费率</p>
	 * @param request
	 * @param accountId			下级账户 id
	 * @param fee				下级费率
	 * @return
	 */
	@PostMapping("/updataAgentFee")
	@ResponseBody
	public Result updataAgentFee(HttpServletRequest request,String userId,String fee,String feeType ) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		log.info("【参数情况：userId = " + userId + "，fee = " + fee + " ， feeType = " + feeType + "】");
		if (StrUtil.isBlank(userId) || StrUtil.isBlank(fee) || StrUtil.isBlank(feeType)) {
			return Result.buildFailMessage("必传参数为空");
		}
		UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(user.getUserId());//自己id
		if ("fee".equals(feeType)) {//入款费率修改
			UserRate rateR = userRateServiceImpl.findUserRateR(userInfo.getUserId());
			BigDecimal fee2 = rateR.getFee();
			BigDecimal fee3 = new BigDecimal(fee);
			if (fee2.compareTo(fee3) < 0) {
				return Result.buildFailMessage("费率设置违规");
			}
			boolean a = userRateServiceImpl.updateRateR(userId, fee);
			if (a) {
				return Result.buildSuccessMessage("修改成功");
			}
			return Result.buildFailMessage("修改失败");
		} else if("cardFee".equals(feeType)) {
			UserRate rateC = userRateServiceImpl.findUserRateC(userInfo.getUserId());
			BigDecimal fee2 = rateC.getFee();
			BigDecimal fee3 = new BigDecimal(fee);
			if (fee2.compareTo(fee3) < 0) {
				return Result.buildFailMessage("费率设置违规");
			}
			boolean a = userRateServiceImpl.updateRateC(userId, fee);
			if (a) {
				return Result.buildSuccessMessage("修改成功");
			}
			return Result.buildFailMessage("修改失败");
		}
		return Result.buildFailResult("修改失败");
	}
	
	/**
	 * <p>接单开启状态</p>
	 * @param request
	 * @return
	 */
	@GetMapping("/updataReceiveOrderStateNO")
	@ResponseBody
	public Result updataReceiveOrderStateNO(HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		return Result.buildFailMessage("暂无权限");
	/*	boolean flag = userInfoServiceImpl.updataReceiveOrderStateNO(user.getUserId());
		if (flag) {
			return Result.buildSuccessResult();
		}
		return Result.buildFailResult("状态修改失败");*/
	}
	/**
	 * <p>接单入款开启状态</p>
	 * @param request
	 * @return
	 */
	@GetMapping("/updataReceiveOrderStateOFF")
	@ResponseBody
	public Result updataReceiveOrderStateOFF(HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		return Result.buildFailMessage("暂无权限");
 /*
		boolean flag = userInfoServiceImpl.updataReceiveOrderStateOFF(user.getUserId());
		if (flag) {
			return Result.buildSuccessResult();
		}
		return Result.buildFailResult("状态修改失败");*/
	}
	/**
	 * <p>收款接单开启</p>
	 * @param request
	 * @return
	 */
	@GetMapping("/updataRemitOrderStateNO")
	@ResponseBody
	public Result updataRemitOrderStateNO(HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		boolean flag = userInfoServiceImpl.updataRemitOrderStateNO(user.getUserId());
		if (flag) {
			return Result.buildSuccessResult();
		}
		return Result.buildFailResult("状态修改失败");
	}
	/**
	 * <p>收款接单关闭</p>
	 * @param request
	 * @return
	 */
	@GetMapping("/updataRemitOrderStateOFF")
	@ResponseBody
	public Result updataRemitOrderStateOFF(HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		boolean flag = userInfoServiceImpl.updataRemitOrderStateOFF(user.getUserId());
		if (flag) {
			return Result.buildSuccessResult();
		}
		return Result.buildFailResult("状态修改失败");
	}
	/**
	 * 获取用户虚拟冻结资金
	 * @param request
	 * @return
	 * @throws ParseException
	 */
	@GetMapping("/virtualAmount")
	@ResponseBody
	public Result virtualAmount(HttpServletRequest request ) throws ParseException {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		BigDecimal userAmount = qrUtil.getUserAmount(user.getUserId());
		return Result.buildSuccessResult(userAmount);
	}
	
}
