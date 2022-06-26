package alipay.manage.contorller;

import alipay.config.exception.OtherErrors;
import alipay.manage.api.AccountApiService;
import alipay.manage.bean.InviteCode;
import alipay.manage.bean.UserFund;
import alipay.manage.bean.UserInfo;
import alipay.manage.bean.UserRate;
import alipay.manage.bean.util.PageResult;
import alipay.manage.bean.util.UserCountBean;
import alipay.manage.service.*;
import alipay.manage.util.SessionUtil;
import alipay.manage.util.SettingFile;
import alipay.manage.util.UserUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import otc.api.alipay.Common;
import otc.result.Result;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/agent")
public class AgentContorller {
	Logger log = LoggerFactory.getLogger(AgentContorller.class);
	@Autowired
	SessionUtil sessionUtil;
	@Autowired
	InviteCodeService inviteCodeServiceImpl;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	UserFundService userFundService;
	@Autowired
	UserRateService userRateService;
	    @Autowired CorrelationService correlationServiceImpl;
		@Autowired AccountApiService accountApiService;
	    @Autowired UserUtil userUtil;
	    @Autowired SettingFile settingFile;
	    /**
	     * <p>代理商开户</p>
	     *	 手机端专用
	     */
	    @RequestMapping(value = "/agentOpenAnAccount",method = RequestMethod.POST)
	    @ResponseBody
	   public Result agentOpenAnAccount(@RequestBody UserInfo user, HttpServletRequest request) {
			UserInfo user2 = sessionUtil.getUser(request);
			if (ObjectUtil.isNull(user)) {
				return Result.buildFailMessage("当前用户未登录");
			}
			user.setAgent(user2.getUserId());
			user.setUserType(Integer.valueOf(Common.User.USER_TYPE_QR));
			user.setIsAgent(Common.User.USER_IS_MEMBER);
			//	UserRate rateR = userRateService.findUserRateR(user2.getUserId());
			//	user.setFee(rateR.getFee() + "");
			List<UserRate> userFeeList = userRateService.findUserRateInfoByUserId(user2.getUserId());
			List<UserRate> rateList = new ArrayList<>();
			for (UserRate rate : userFeeList) {
				if (rate.getFeeType().equals(2)) {
					BigDecimal fee = rate.getFee();//自己的出款费率
					BigDecimal myselfFee = new BigDecimal(user.getCardFee()).divide(new BigDecimal(100));//返点汇率
					if (myselfFee.compareTo(fee) > 0) {
						return Result.buildFailMessage("结算费率开始失败");
					}
					rate.setFee(myselfFee);
					rate.setUserId(user.getUserId());
					rateList.add(rate);
					//	userRateService.add(rate);
					continue;
				}
				BigDecimal fee = rate.getFee();//自己的费率
				String rebate = user.getFee();
				BigDecimal myselfFee = new BigDecimal(rebate).divide(new BigDecimal(100));//返点汇率
				if (myselfFee.compareTo(fee) > 0) {
					return Result.buildFailMessage("开户费率设置失败");
				}
				rate.setFee(myselfFee);
				rate.setUserId(user.getUserId());
				rateList.add(rate);
				//userRateService.add(rate);
			}
			Result result = accountApiService.addAccount(user);
			if (result.isSuccess()) {
				for (UserRate rate : rateList) {
					userRateService.add(rate);
				}
			} else {
				return result;
			}

			return Result.buildSuccessMessage("开户成功");
				/*UserRate rate = new UserRate();
				rate.setUserId(user.getUserId());
				rate.setFee(new BigDecimal(user.getFee()));
				rate.setFeeType(Integer.valueOf(Common.User.ALIPAY_FEE));
				rate.setUserType(Integer.valueOf(Common.User.USER_TYPE_QR));
				rate.setPayTypr(Common.User.ALIPAY_PRODUCTID);
				boolean add = userRateService.add(rate);
				if (add) {
					return Result.buildSuccessMessage("开户成功");
				}*/
			}
	    /**
	     * <p>密码修改</p>
	     * 	手机端专用
	     * @return
	     */
	    @PostMapping("/updateAccountPasswpord")
	    @ResponseBody
	    public Result updateAccountPasswpord(@RequestBody UserInfo user, HttpServletRequest request) {
	    	UserInfo user2 = sessionUtil.getUser(request);
	        user.setUserId(user2.getUserId());
	        log.info("【登录密码修改方法】");
	        return Result.buildFail();
	    }

	    /**
	     * <p>资金密码修改</p>
	     * 	手机端专用
	     * @return
	     */
	    @PostMapping("/updateSecurityPassword")
	    @ResponseBody
	    public Result updateSecurityPassword(@RequestBody UserInfo user, HttpServletRequest request) {
	    	UserInfo user2 = sessionUtil.getUser(request);
	    	user.setUserId(user2.getUserId());
	    	  log.info("【登录资金密码修改方法】");
	    	return Result.buildFail();
	    }
	    /**
	     * <p>资金密码修改</p>
	     * 	手机端专用
	     * @return
	     */
	    @PostMapping("/generateInviteCodeAndGetInviteRegisterLink")
	    @ResponseBody
	    public Result generateInviteCodeAndGetInviteRegisterLink(
	            @RequestBody InviteCode bean,
	            HttpServletRequest request
	    ) {
			UserInfo user = sessionUtil.getUser(request);
			if (ObjectUtil.isNull(user)) {
				return Result.buildFailMessage("当前用户未登录");
			}
			if (StrUtil.isBlank(bean.getUserType())) {
				return Result.buildFailMessage("参数为空");
			}
			bean.setBelongUser(user.getUserId());
			bean.setCount(0);
			bean.setRebate(bean.getRebateR() + "_" + bean.getRebateW());
			log.info("【生成邀请码的方法，将邀请吗返回给前端】");
			String createinviteCode = createinviteCode();
			bean.setInviteCode(createinviteCode);
			bean.setCreateTime(new Date());
			bean.setSubmitTime(new Date());
			bean.setIsDeal(Common.isOk);
			boolean flag = inviteCodeServiceImpl.addinviteCode(bean);
			if (flag) {
				return Result.buildSuccessResult("操作成功", "当前功能禁止使用");
			}
			return Result.buildFail();
		}
	    
	    
	    
	    /**
	     * <p>产生随机邀请码</p>
	     * @return
	     */
	    String createinviteCode() {
			String randomString = RandomUtil.randomString(10);
			boolean flag = inviteCodeServiceImpl.findinviteCode(randomString);
			if (!flag) {
				return randomString;
			}
			return createinviteCode();
		}
	    /**
	     * <p>查询自己的子账户</p>
	     * 	手机端专用
	     * @return
	     */
	    @GetMapping("/findLowerLevelAccountDetailsInfoByPage")
	    @ResponseBody
	    public Result findLowerLevelAccountDetailsInfoByPage(@RequestParam(required = false)String pageSize,
	    		                                              @RequestParam(required = false)String pageNum,
	    		                                              @RequestParam(required = false)String userName,
	    		                                              HttpServletRequest request) {
			UserInfo user2 = sessionUtil.getUser(request);
			if (StrUtil.isBlank(user2.getUserId())) {
				return Result.buildFail();
			}
			UserInfo user = new UserInfo();
			if (StrUtil.isNotBlank(userName)) {
				user.setUserId(userName);
			}
			user.setAgent(user2.getUserId());
			PageHelper.startPage(Integer.valueOf(pageNum), Integer.valueOf(pageSize));
			List<UserInfo> userList = userInfoService.findSunAccount(user);
			UserRate userRate = null;
			UserRate userRateW = null;
			UserFund userfund = null;
			for (UserInfo qrUser : userList) {
				findOnline(qrUser);
				userRate = userRateService.findUserRateR(qrUser.getUserId());
				userRateW = userRateService.findUserRateW(qrUser.getUserId());
				qrUser.setFee(userRate.getFee() + "");
				qrUser.setCardFee(userRateW.getFee() + "");
				userfund = userFundService.findUserInfoByUserId(qrUser.getUserId());
				qrUser.setRechargeNumber(userfund.getAccountBalance());
				qrUser.setCashBalance(userfund.getTodayProfit());
			}
	        PageInfo<UserInfo> pageInfo = new PageInfo<UserInfo>(userList);
	        PageResult<UserInfo> pageR = new PageResult<UserInfo>();
	        pageR.setContent(pageInfo.getList());
	        pageR.setPageNum(pageInfo.getPageNum());
	        pageR.setTotal(pageInfo.getTotal());
	        pageR.setTotalPage(pageInfo.getPages());
	        return Result.buildSuccessResult(pageR);
	    }
	    
	    @GetMapping("/findAgentCount")
	    @ResponseBody
	    public Result findAgentCount(HttpServletRequest request) {
			UserInfo user2 = sessionUtil.getUser(request);
			if (ObjectUtil.isNull(user2)) {
				throw new OtherErrors("当前用户未登录");
			}
			UserFund findUserByAccount = userInfoService.findUserFundByAccount(user2.getUserId());
			UserCountBean findMoreCount = findMyDate(findUserByAccount.getUserId());
			findMoreCount.setMoreDealProfit(findUserByAccount.getTodayAgentProfit().toString());
			return Result.buildSuccessResult(findMoreCount);
		}
	    /**
	     * <p>根据我的账户id，查询我的个人数据情况</p>
	     * @param id
	     */
	    private UserCountBean findMyDate(@NotNull String userId) {
	    	UserCountBean bean = correlationServiceImpl.findMyDateAgen(userId);
	    	UserCountBean bean1 = correlationServiceImpl.findDealDate(userId);
	    	if(ObjectUtil.isNotNull(bean1)) {
				bean.setMoreAmountRunR(ObjectUtil.isNull(bean1.getMoreAmountRunR()) ? new BigDecimal("0") : bean1.getMoreAmountRunR());
				bean.setMoreAmountRunW(ObjectUtil.isNull(bean1.getMoreAmountRunW()) ? new BigDecimal("0") : bean1.getMoreAmountRunW());
				bean.setMoreDealCount(ObjectUtil.isNull(bean1.getMoreDealCount()) ? 0 : bean1.getMoreDealCount());
			} else {
				bean.setMoreAmountRunR(new BigDecimal("0"));
				bean.setMoreAmountRunW(new BigDecimal("0"));
				bean.setMoreDealCount(0);
			}
			return bean;
		}
	    UserInfo findOnline(UserInfo user) {
	        if (Common.User.USER_IS_AGENT.toString().equals(user.getIsAgent())) {
	           int[][] dataArray = correlationServiceImpl.findOnline(user.getUserId());
	           user.setOnline(dataArray[1][0]+"");
	           user.setToDayOrderCount(dataArray[0][0]);
	           user.setAgentCount(dataArray[2][0]+"");
	        }
	        return user;
	    }
	}
