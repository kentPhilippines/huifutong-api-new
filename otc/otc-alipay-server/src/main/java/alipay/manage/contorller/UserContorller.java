package alipay.manage.contorller;

import alipay.manage.api.AccountApiService;
import alipay.manage.bean.*;
import alipay.manage.service.*;
import alipay.manage.util.QrUtil;
import alipay.manage.util.SessionUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import otc.exception.user.UserException;
import otc.result.Result;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
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
	UserFundService userFundService;
	@Autowired
	UserRateService userRateService;
	@Autowired
	SessionUtil sessionUtil;
	@Autowired
	BankListService bankListServiceImpl;
	@Autowired InviteCodeService inviteCodeServiceImpl;
	@Autowired AccountApiService accountApiServiceImpl;
	@Autowired QrUtil qrUtil;
	/**
	 * <p>获取账号登录情况</p>
	 * @return
	 */
	@GetMapping("/getUserAccountInfo")
	@ResponseBody
	public Result getUserAccountInfo(HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			log.info("当前用户未登陆");
			return Result.buildFailMessage("当前用户未登陆");
		}
		UserInfo user2 = userInfoServiceImpl.findUserInfoByUserId(user.getUserId());
		UserFund userFund = userInfoServiceImpl.findUserFundByAccount(user.getUserId());
		UserRate rateR = userRateService.findUserRateR(user.getUserId());
		UserRate rateW = userRateService.findUserRateWitByUserIdApp(user2.getUserId());
		user2.setAmount(userFund.getAccountBalance().toString());
		user2.setFee(rateR.getFee().toString());
		user2.setTodayProfit(userFund.getTodayProfit().toString());
		user2.setQQ(userFund.getSumProfit().toString());
		user2.setCardFee(rateW.getFee().toString());
		return Result.buildSuccessResult(user2);
	}

	/**
	 * 用户获取资金账户
	 * @param request
	 * @return
	 */
	@GetMapping("/getUserFundInfo")
    @ResponseBody
	public Result getUserFundInfo(HttpServletRequest request){
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			log.info("当前用户未登陆");
			return Result.buildFailMessage("当前用户未登陆");
		}
		UserFund byUserId = userFundService.findUserInfoByUserId(user.getUserId());
		return Result.buildSuccessResult(byUserId);
	}
	
	@GetMapping("/getUserRateInfo")
	@ResponseBody
	public Result getUserRateInfo(HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登陆");
		}
		List<UserRate> userRate = userRateService.findUserRateInfoByUserId(user.getUserId());
		return Result.buildSuccessResult(userRate);
	}
	/**
	 * <p>获取用户绑定的银行卡接口</p>
	 * @return
	 */
	@GetMapping("/getBankInfo")
	@ResponseBody
	public Result getBankInfo(HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
	        log.info("当前用户未登陆");
	        return Result.buildFailMessage("当前用户未登陆");
	    }
		List<BankList> bank = bankListServiceImpl.findBankCardByQr(user.getUserId());
		return Result.buildSuccessResult(CollUtil.getFirst(bank));
	}
	/**
	 * <p>添加银行卡</p>
	 * @param bank
	 * @return
	 */
	@PostMapping("/bindBankInfo")
	@ResponseBody
	public Result bindBankInfo(BankList bank , HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			log.info("当前用户未登陆");
			return Result.buildFailMessage("当前用户未登陆");
		}
		log.info("【当前添加银行卡用户：" + user.getUserId() + "】");
		bank.setAccount(user.getUserId());
		//	boolean issuer = bankListServiceImpl.cleanBankToLast(user.getUserId());
		boolean flag = bankListServiceImpl.addBankcard(bank);
		if (flag) {
			return Result.buildSuccess();
		}
		return Result.buildFail();
	}
	/**
	 * <p>用户通过邀请码注册</p>
	 * @param request
	 * @return
	 */
	@PostMapping("/register")
	@ResponseBody
	public Result register(UserInfo user ,HttpServletRequest request) {
        if (StrUtil.isBlank(user.getUserId()) || StrUtil.isBlank(user.getPassword())
                || StrUtil.isBlank(user.getPayPasword()) || StrUtil.isBlank(user.getUserName())
                || StrUtil.isBlank(user.getInviteCode())) {
            return Result.buildFailMessage("必填参数为空");
        }
        InviteCode code = inviteCodeServiceImpl.findInviteCode(user.getInviteCode());
        if (code.getStatus() == 0) {
            return Result.buildFailResult("当前邀请码不可使用");
        }
        user.setAgent(code.getBelongUser());//邀请码生成人
        if ("member".equals(code.getUserType())) {
            user.setIsAgent("2");
        } else if ("agent".equals(code.getUserType())) {
            user.setIsAgent("1");
        }
        user.setUserType(2);//卡商注册接口
        Result openAgentAccount = accountApiServiceImpl.addAccount(user);
        boolean flag = false;
        if (openAgentAccount.isSuccess()) {
			flag = inviteCodeServiceImpl.updataInviteCode(user.getInviteCode(), user.getUserId());
			List<UserRate> userFeeList = userRateService.findUserRateInfoByUserId(code.getBelongUser());
			List<UserRate> rateList = new ArrayList<>();
			for (UserRate rate : userFeeList) {
				/*BigDecimal fee = rate.getFee();
				String rebate = code.getRebate();
				BigDecimal myselfFee = new BigDecimal(rebate).multiply(new BigDecimal(100));
				BigDecimal subtract = fee.subtract(myselfFee);
				rate.setFee(subtract);
				rate.setUserId(user.getUserId());*/
				if (rate.getFeeType().equals(2)) {
					BigDecimal fee = rate.getFee();
					BigDecimal myselfFee = new BigDecimal(code.getRebate().split("|")[1]).divide(new BigDecimal(100));//返点汇率
					if (myselfFee.compareTo(fee) > 0) {
						return Result.buildFailMessage("开户费率设置失败");
					}


					rate.setFee(myselfFee);
					rate.setUserId(user.getUserId());
					rateList.add(rate);
					//	userRateService.add(rate);
					continue;
				}
				BigDecimal fee = rate.getFee();//自己的费率
				String rebate = code.getRebate().split("_")[0];
				BigDecimal myselfFee = new BigDecimal(rebate).divide(new BigDecimal(100));//返点汇率
				if (myselfFee.compareTo(fee) > 0) {
					return Result.buildFailMessage("开户费率设置失败");
				}
				rate.setFee(myselfFee);
				rate.setUserId(user.getUserId());
				rateList.add(rate);
			}
			for (UserRate rate : rateList) {
				userRateService.add(rate);
			}
		}
        if (openAgentAccount.isSuccess() && flag) {
            return Result.buildSuccessResult();
        }
        return Result.buildFailMessage("注册失败");
    }
	/**
	 * <p>修改密码</p>
	 * @param newLoginPwd
	 * @return
	 */
	@PostMapping("/modifyLoginPwd")
	@ResponseBody
	public Result modifyLoginPwd(String oldLoginPwd , String newLoginPwd,HttpServletRequest request) {
		if (StrUtil.isBlank(oldLoginPwd) || StrUtil.isBlank(newLoginPwd)) {
			return Result.buildFailResult("必填参数为空");
		}
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			log.info("当前用户未登陆");
			return Result.buildFailMessage("当前用户未登陆");
		}
		user.setPassword(oldLoginPwd);
		user.setNewPassword(newLoginPwd);
		Result updateAccountPassword = accountApiServiceImpl.updateLoginPassword(user);
		return updateAccountPassword;
	}
	/**
	 * <p>修改支付密码</p>
	 * @param newMoneyPwd
	 * @return
	 */
	@PostMapping("/modifyMoneyPwd")
	@ResponseBody
	public Result modifyMoneyPwd(String oldMoneyPwd, String newMoneyPwd, HttpServletRequest request) {
		if (StrUtil.isBlank(oldMoneyPwd) || StrUtil.isBlank(newMoneyPwd)) {
			return Result.buildFailResult("必填参数为空");
		}
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			log.info("当前用户未登陆");
			return Result.buildFailMessage("当前用户未登陆");
		}
		user.setPayPasword(oldMoneyPwd);
		user.setNewPayPassword(newMoneyPwd);
		Result updateAccountPassword = accountApiServiceImpl.updatePayPassword(user);
		return updateAccountPassword;
	}

	@GetMapping("/virtualAmount")
	@ResponseBody
	public Result virtualAmount(HttpServletRequest request ) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			throw new UserException("当前用户未登录", null);
		}
		BigDecimal userAmount = qrUtil.getUserAmount(user.getUserId());
		return Result.buildSuccessResult(userAmount);
	}

	/**
	 * <p>修改会员性质为代理商</p>
	 * @param request
	 * @param accountId
	 * @return
	 */
	@PostMapping("/updateIsAgent")
	@ResponseBody
	public Result updateIsAgent(HttpServletRequest request,Integer id,String userId) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			log.info("当前用户未登陆");
			return Result.buildFailMessage("当前用户未登陆");
		}
		boolean flag = accountApiServiceImpl.updateIsAgent(id, userId);
		if (flag) {
			return Result.buildSuccessMessage("升级代理成功");
		}
		return Result.buildFailMessage("升级代理失败");
	}
//	@PostMapping("/findUserByAccountId")
//	@ResponseBody
//	public Result findUserByAccountId(HttpServletRequest request,Integer id ){
//		UserInfo user = sessionUtil.getUser(request);
//		if (ObjectUtil.isNull(user)) {
//	        log.info("当前用户未登陆");
//	        return Result.buildFailMessage("当前用户未登陆");
//	    }
//		UserInfo qrUser =  accountApiServiceImpl.findUserInfo(id);
//		return Result.buildSuccessResult(qrUser);
//	}
@PostMapping("/findUserByAccountId")
@ResponseBody
public Result findUserByAccountId(HttpServletRequest request, String userId, String type) throws ParseException {
	UserInfo user = sessionUtil.getUser(request);
	if (ObjectUtil.isNull(user)) {
		return Result.buildFailMessage("当前用户未登录");
	}
	UserRate rateR = null;
	UserInfo userInfo = accountApiServiceImpl.findUserInfo(userId);
	Future<UserRate> execAsync3 = ThreadUtil.execAsync(() -> {
		if ("1".equals(type)) {
			return userRateService.findUserRateR(userInfo.getUserId());
		} else {
			return userRateService.findUserRateW(userInfo.getUserId());
		}

	});
		try {
			rateR = execAsync3.get();
		} catch (InterruptedException | ExecutionException e) {
			return Result.buildFailMessage("错误");
		}
	userInfo.setFee(rateR.getFee().toString());
	userInfo.setProductId(rateR.getPayTypr().toString());
	return Result.buildSuccessResult(userInfo);
}

	@GetMapping("/updataAccountFee")
	public String updataAccountFee(HttpServletRequest request, String accountId) {
		return "updataAccountFee";
	}

	@GetMapping("/updataAccountFeeW")
	public String updataAccountFeeW(HttpServletRequest request, String accountId) {
		return "updataAccountFeeW";
	}

	/**
	 * <p>修改下级代理费率</p>
	 *
	 * @param request
	 * @param accountId 下级账户 id
	 * @param fee       下级费率
	 * @return
	 */
	@PostMapping("/updataAgentFee")
	@ResponseBody
	public Result updataAgentFee(HttpServletRequest request,String userId,String fee,String payTypr ) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			return Result.buildFailMessage("当前用户未登录");
		}
		log.info("【参数情况：userId = " + userId + "，fee = " + fee + ",product=" + payTypr + "】");
		if (StrUtil.isBlank(userId) || StrUtil.isBlank(fee)) {
			return Result.buildFailMessage("必传参数为空");
		}
		UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(user.getUserId());//自己id
		//入款费率修改
		UserRate rateR = userRateService.findUserRateR(userInfo.getUserId());
		UserRate userRateW = userRateService.findUserRateW(userInfo.getUserId());
		if (payTypr.equals(rateR.getPayTypr())) {
			BigDecimal fee2 = rateR.getFee();
			BigDecimal fee3 = new BigDecimal(fee);
			if (fee2.compareTo(fee3) < 0) {
				return Result.buildFailMessage("费率设置违规");
			}
		}
		if (payTypr.equals(userRateW.getPayTypr())) {
			BigDecimal fee2 = userRateW.getFee();
			BigDecimal fee3 = new BigDecimal(fee);
			if (fee2.compareTo(fee3) < 0) {
				return Result.buildFailMessage("费率设置违规");
			}
		}
		boolean a = userRateService.updateRateR(userId, fee, payTypr);
		if (a) {
			return Result.buildSuccessMessage("修改成功");
		}
		return Result.buildFailMessage("修改失败");
	}

	/**
	 * <p>接单入款开启状态</p>
	 *
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
		boolean flag = Boolean.FALSE;// userInfoServiceImpl.updataReceiveOrderStateNO(user.getUserId());
		if (flag) {
			return Result.buildSuccessResult();
		}
		return Result.buildFailResult("状态修改失败");
	}

	/**
	 * <p>接单入款开启状态</p>
	 *
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
		boolean flag =   userInfoServiceImpl.updataReceiveOrderStateOFF(user.getUserId());
		if (flag) {
			return Result.buildSuccessResult();
		}
		return Result.buildFailResult("状态修改失败");
	}

	/**
	 * <p>收款接单开启</p>
	 *
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
		boolean flag =  Boolean.FALSE; //userInfoServiceImpl.updataRemitOrderStateNO(user.getUserId());
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
}
