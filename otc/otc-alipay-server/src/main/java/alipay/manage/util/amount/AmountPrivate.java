package alipay.manage.util.amount;

import alipay.manage.bean.UserFund;
import alipay.manage.bean.UserInfo;
import alipay.manage.service.RunOrderService;
import alipay.manage.service.UserInfoService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.api.alipay.Common;
import otc.exception.user.UserException;
import otc.result.Result;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>资金处理方法</p>
 *
 * @author K
 */
@Component
public class AmountPrivate extends Util {
	/**
	 * <p>账户减少</p>
	 *
	 * @param userFund
	 * @param balance
	 * @param addType
	 * @return
	 */
	protected static final String DELETE_KEY = "deleteAmountBalance";
	@Autowired
	static Lock lock = new ReentrantLock();
	Logger log = LoggerFactory.getLogger(AmountPrivate.class);
	@Autowired
	UserInfoService userInfoServiceImpl;
	/**
	 * #####################################################################
	 * 以下方法暂时不对外界开放
	 * #####################################################################
	 */
	@Autowired
	RunOrderService runorderServiceImpl;

	/**
	 * 增加授权额度
	 *
	 * @param userFund
	 * @param balance
	 * @return
	 */
	protected Result addQuota(UserFund userFund, BigDecimal balance) {
		log.info("【当前方法为 【增加账户授权度】 ，当前操作金额为：" + balance + "】");
		UserFund fund = new UserFund();
		fund.setId(userFund.getId());
		BigDecimal quota = userFund.getQuota();
		log.info("【当前授权额度为：" + quota + "，增加后的授权额度为：" + quota.add(balance) + "】");
		BigDecimal add = quota.add(balance);
		fund.setQuota(add);
		fund.setUserId(userFund.getUserId());
		fund.setVersion(userFund.getVersion());
		Boolean aBoolean = userInfoServiceImpl.updataAmount(fund);
		if (aBoolean) {
			log.info("【当前增加授权额度执行成功，操作的金额为：" + balance + "】");
			log.info("【增加授权额度后的账户金额为：账户总余额：" + userFund.getAccountBalance() + "，当前利润账户【现金账户】：" + userFund.getCashBalance() + "，当前冻结账户：" + userFund.getFreezeBalance() + "当前操作金额为：" + balance + "】");
			return Result.buildSuccessMessage("当前授权额度增加执行成功");
		} else {
			return Result.buildFailMessage("当前授权额度增加执行失败");
		}


	}

	static List accountList = new LinkedList();//迸发过高不关闭账户的例外

	static {
	}

	protected Result addFreezeAmount(UserFund userFund, BigDecimal amount) {
		log.info("【当前方法为 【码商或者商户资金解冻】 ，当前操作金额为：" + amount + "】");
		BigDecimal accountBalance = userFund.getAccountBalance();
		BigDecimal rechargeNumber = userFund.getRechargeNumber();
		BigDecimal cashBalance = userFund.getCashBalance();
		BigDecimal freezeBalance = userFund.getFreezeBalance();


		freezeBalance = freezeBalance.subtract(amount);//冻结余额 扣减
		accountBalance = rechargeNumber.add(cashBalance).subtract(freezeBalance);//余额账户新增
		userFund.setAccountBalance(accountBalance);
		userFund.setFreezeBalance(freezeBalance);
		userFund.setCashBalance(cashBalance);
		userFund.setRechargeNumber(rechargeNumber);
		Boolean updataAmount = userInfoServiceImpl.updataAmount(userFund);
		if (updataAmount) {
			log.info("【当前解冻资金执行成功，操作的金额为：" + amount + "】");
			log.info("【解冻资金后的账户金额为：账户总余额：" + accountBalance + "，当前利润账户【现金账户】：" + cashBalance + "，当前冻结账户：" + freezeBalance + "当前操作金额为：" + amount + "】");
			return Result.buildSuccessMessage("当前金额解冻方法执行成功");
		} else {
			return Result.buildFailMessage("当前金额解冻方法执行失败");
		}

	}

	protected Result addAmountDealApp(UserFund userFund, BigDecimal balance) {
		BigDecimal accountBalance = userFund.getAccountBalance();//当前账户比较金额
		BigDecimal cashBalance = userFund.getCashBalance();//当前利润账户
		BigDecimal freezeBalance = userFund.getFreezeBalance();//当前冻结账户
		BigDecimal rechargeNumber = userFund.getRechargeNumber();//当前充值点数
		//	BigDecimal sumAgentProfit = userFund.getSumAgentProfit();//当前代理商分润  【当前订单为自己接单，不需要该字段】
		BigDecimal sumDealAmount = userFund.getSumDealAmount();//当前交易分润
		//	BigDecimal sumProfit = userFund.getSumProfit();//当前当前总的利润
		//	BigDecimal todayAgentProfit = userFund.getTodayAgentProfit();//今日代理分润  【当前订单为自己接单，无需统计该字段】
		BigDecimal todayDealAmount = userFund.getTodayDealAmount();//今日交易金额
		Integer sumOrderCount = userFund.getSumOrderCount();//总订单笔数
		Integer todayOrderCount = userFund.getTodayOrderCount();//今日订单笔数
		//	BigDecimal todayProfit = userFund.getTodayProfit();//今日总利润  =  今日代理分润 + 今日接单分润
		log.info("【金额修改后账户情况：当前账户总比较金额：" + accountBalance + "，当前账户充值点数：" + rechargeNumber + "，当前账户利润金额：" + cashBalance + "，当前账户冻结金额：" + freezeBalance + "，当前账户：" + userFund.getUserId() + "】");
		//以上为当前账户情况
		rechargeNumber = rechargeNumber.add(balance);
		accountBalance = rechargeNumber.add(cashBalance).subtract(freezeBalance);
		sumDealAmount = sumDealAmount.add(balance);
		todayDealAmount = todayDealAmount.add(balance);
		sumOrderCount += 1;
        todayOrderCount += 1;
        userFund.setAccountBalance(accountBalance);
        userFund.setCashBalance(cashBalance);
        userFund.setFreezeBalance(freezeBalance);
        userFund.setRechargeNumber(rechargeNumber);
        userFund.setSumDealAmount(sumDealAmount);
        userFund.setSumOrderCount(sumOrderCount);
        userFund.setTodayDealAmount(todayDealAmount);
        userFund.setTodayOrderCount(todayOrderCount);
        Boolean updataAmount = userInfoServiceImpl.updataAmount(userFund);
        if (updataAmount) {
            log.info("【金额修改后账户情况：当前账户总比较金额：" + accountBalance + "，当前账户充值点数：" + rechargeNumber + "，当前账户利润金额：" + cashBalance + "，当前账户冻结金额：" + freezeBalance + "，当前账户：" + userFund.getUserId() + "】");
        } else {
            log.info("【账户修改失败】");
			Boolean updataStatusEr = updateAccountEr(userFund.getUserId());
            if (updataStatusEr) {
                log.info("【账户已修改为不可使用，当前账号为：" + userFund.getUserId() + "】");
            } else {
                throw new UserException("账户修改异常", null);
            }
            return Result.buildFail();
        }
		return Result.buildSuccessResult();
	}

	protected Result deleteQuota(UserFund userFund, BigDecimal balance) {
		log.info("【当前方法为 【减少账户授权度】 ，当前操作金额为：" + balance + "】");
		UserFund fund = new UserFund();
		fund.setId(userFund.getId());
		BigDecimal quota = userFund.getQuota();
		log.info("【当前授权额度为：" + quota + "，减少后的授权额度为：" + quota.subtract(balance) + "】");
		BigDecimal add = quota.subtract(balance);
		fund.setQuota(add);
		fund.setUserId(userFund.getUserId());
		fund.setVersion(userFund.getVersion());
		Boolean aBoolean = userInfoServiceImpl.updataAmount(fund);
		if (aBoolean) {
			log.info("【当前减少授权额度执行成功，操作的金额为：" + balance + "】");
			log.info("【减少授权额度后的账户金额为：账户总余额：" + userFund.getAccountBalance() + "，当前利润账户【现金账户】：" + userFund.getCashBalance() + "，当前冻结账户：" + userFund.getFreezeBalance() + "当前操作金额为：" + balance + "】");
			return Result.buildSuccessMessage("当前授权额度减少执行成功");
		} else {
			log.info("【账户修改失败】");
			ThreadUtil.execute(() -> {
                //TODO 新建线程提交，该线程不受主线程事务控制
				Boolean updataStatusEr = updateAccountEr(userFund.getUserId());
                if (updataStatusEr) {
                    log.info("【账户已修改为不可使用，当前账号为：" + userFund.getUserId() + "】");
                }
            });
			return Result.buildFailMessage("当前授权额度减少执行失败");
		}

	}

	/**
	 * <p>账户冻结</p>
	 *
	 * @param userFund
	 * @param amount
	 * @return
	 */
	protected Result freezeBalance(UserFund userFund, BigDecimal amount) {
		log.info("【当前方法为 【码商或者商户资金冻结】 ，当前操作金额为：" + amount + "】");
		BigDecimal accountBalance = userFund.getAccountBalance();
		BigDecimal rechargeNumber = userFund.getRechargeNumber();
		BigDecimal cashBalance = userFund.getCashBalance();
		BigDecimal freezeBalance = userFund.getFreezeBalance();
		freezeBalance = freezeBalance.add(amount);
		accountBalance = rechargeNumber.add(cashBalance).subtract(freezeBalance);
		userFund.setAccountBalance(accountBalance);
		userFund.setFreezeBalance(freezeBalance);
		userFund.setCashBalance(cashBalance);
		userFund.setRechargeNumber(rechargeNumber);
		Boolean updataAmount = userInfoServiceImpl.updataAmount(userFund);
		if (updataAmount) {
			log.info("【当前冻结资金执行成功，操作的金额为：" + amount + "】");
			log.info("【冻结资金后的账户金额为：账户总余额：" + accountBalance + "，当前利润账户【现金账户】：" + cashBalance + "，当前冻结账户：" + freezeBalance + "当前操作金额为：" + amount + "】");
			return Result.buildSuccessMessage("当前金额取款方法执行成功");
		} else {
			log.info("【账户修改失败】");
			ThreadUtil.execute(() -> {
                //TODO 新建线程提交，该线程不受主线程事务控制
				Boolean updataStatusEr = updateAccountEr(userFund.getUserId());
                if (updataStatusEr) {
                    log.info("【账户已修改为不可使用，当前账号为：" + userFund.getUserId() + "】");
                }
            });
			return Result.buildFailMessage("取款扣除金额方法执行失败");
		}
	}

	/**
	 * <p>取款</p>
	 *
	 * @return
	 */
	protected Result withdrawBalance(UserFund userFund, BigDecimal amount) {
		log.info("【当前方法为 【码商或者商户发起提现】 ，当前操作金额为：" + amount + "】");
		BigDecimal accountBalance = userFund.getAccountBalance();
		BigDecimal rechargeNumber = userFund.getRechargeNumber();
		BigDecimal cashBalance = userFund.getCashBalance();
		BigDecimal freezeBalance = userFund.getFreezeBalance();
		accountBalance = accountBalance.subtract(amount);
		rechargeNumber = rechargeNumber.subtract(amount);
		if (rechargeNumber.compareTo(new BigDecimal("0")) == -1) {//交易点数小于0 的时候
			cashBalance = cashBalance.add(rechargeNumber);
			rechargeNumber = new BigDecimal("0");
		}
		userFund.setAccountBalance(accountBalance);
		userFund.setCashBalance(cashBalance);
		userFund.setFreezeBalance(freezeBalance);
		userFund.setRechargeNumber(rechargeNumber);
		Boolean updataAmount = userInfoServiceImpl.updataAmount(userFund);
		if (updataAmount) {
			log.info("【当前金额扣除执行成功，操作的金额为：" + amount + "】");
			log.info("【金额修改后账户情况：当前账户总比较金额：" + accountBalance + "，当前账户充值点数：" + rechargeNumber + "，当前账户利润金额：" + cashBalance + "，当前账户冻结金额：" + freezeBalance + "，当前账户：" + userFund.getUserId() + "】");
			return Result.buildSuccessMessage("当前金额取款方法执行成功");
		} else {
			log.info("【账户修改失败】");
			ThreadUtil.execute(() -> {
                //TODO 新建线程提交，该线程不受主线程事务控制
				Boolean updataStatusEr = updateAccountEr(userFund.getUserId());
                if (updataStatusEr) {
                    log.info("【账户已修改为不可使用，当前账号为：" + userFund.getUserId() + "】");
                }
            });
			return Result.buildFailMessage("取款扣除金额方法执行失败");
		}
	}

	/**
	 * <p>扣款【或者扣点数】</p>
	 *
	 * @param amount
	 * @return 当前扣款为系统或者后台人员操作扣款，所以针对账户详情的资金开关，针对后台和系统管理人员无效
	 */
	protected Result deductBalance(UserFund userFund, BigDecimal amount) {
		log.info("【当前方法为 【人工扣除账户金额方法】 ，当前操作金额为：" + amount + "】");
		BigDecimal accountBalance = userFund.getAccountBalance();
		BigDecimal rechargeNumber = userFund.getRechargeNumber();
		BigDecimal cashBalance = userFund.getCashBalance();
		BigDecimal freezeBalance = userFund.getFreezeBalance();
		accountBalance = accountBalance.subtract(amount);
		rechargeNumber = rechargeNumber.subtract(amount);
		if (rechargeNumber.compareTo(new BigDecimal("0")) == -1) {//交易点数小于0 的时候
			cashBalance = cashBalance.add(rechargeNumber);
			rechargeNumber = new BigDecimal("0");
		}
		userFund.setAccountBalance(accountBalance);
		userFund.setCashBalance(cashBalance);
		userFund.setFreezeBalance(freezeBalance);
		userFund.setRechargeNumber(rechargeNumber);
		Boolean updataAmount = userInfoServiceImpl.updataAmount(userFund);
		if (updataAmount) {
			log.info("【当前金额扣除执行成功，操作的金额为：" + amount + "】");
			log.info("【金额修改后账户情况：当前账户总比较金额：" + accountBalance + "，当前账户充值点数：" + rechargeNumber + "，当前账户利润金额：" + cashBalance + "，当前账户冻结金额：" + freezeBalance + "，当前账户：" + userFund.getUserId() + "】");
			return Result.buildSuccessMessage("充值点数扣除方法执行成功");
		} else {
			log.info("【账户修改失败】");
			ThreadUtil.execute(() -> {
                //TODO 新建线程提交，该线程不受主线程事务控制
				Boolean updataStatusEr = updateAccountEr(userFund.getUserId());
                if (updataStatusEr) {
                    log.info("【账户已修改为不可使用，当前账号为：" + userFund.getUserId() + "】");
                }
            });
			return Result.buildFailMessage("金额扣除方法执行失败");
		}
	}

	/**
	 * <p>交易减充值点数</p>
	 *
	 * @param amount
	 * @return
	 */
	protected Result deductRecharge(UserFund userFund, BigDecimal amount) {
		log.info("【当前方法为 【交易扣除充值点数】 ，当前操作金额为：" + amount + "】");
		BigDecimal accountBalance = userFund.getAccountBalance();
		BigDecimal rechargeNumber = userFund.getRechargeNumber();
		BigDecimal cashBalance = userFund.getCashBalance();
		BigDecimal freezeBalance = userFund.getFreezeBalance();
		accountBalance = accountBalance.subtract(amount);
		rechargeNumber = rechargeNumber.subtract(amount);
		if (rechargeNumber.compareTo(new BigDecimal("0")) == -1) {//交易点数小于0 的时候
			cashBalance = cashBalance.add(rechargeNumber);
			rechargeNumber = new BigDecimal("0");
		}
		userFund.setAccountBalance(accountBalance);
		userFund.setCashBalance(cashBalance);
		userFund.setFreezeBalance(freezeBalance);
		userFund.setRechargeNumber(rechargeNumber);
		Boolean updataAmount = userInfoServiceImpl.updataAmount(userFund);
		if (updataAmount) {
			log.info("【当前金额扣除执行成功，操作的金额为：" + amount + "】");
			log.info("【金额修改后账户情况：当前账户总比较金额：" + accountBalance + "，当前账户充值点数：" + rechargeNumber + "，当前账户利润金额：" + cashBalance + "，当前账户冻结金额：" + freezeBalance + "，当前账户：" + userFund.getUserId() + "】");
			return Result.buildSuccessMessage("充值点数扣除方法执行成功");
		} else {
			log.info("【账户修改失败】");
			ThreadUtil.execute(() -> {
                //TODO 新建线程提交，该线程不受主线程事务控制
				Boolean updataStatusEr = updateAccountEr(userFund.getUserId());
                if (updataStatusEr) {
                    log.info("【账户已修改为不可使用，当前账号为：" + userFund.getUserId() + "】");
                }
            });
			return Result.buildFailMessage("金额扣除方法执行失败");
		}
	}

	/**
	 * <p>当前账户交易利润加钱</p>
	 *
	 * @param userFund   资金账户表
	 * @param balance    金额					该笔交易较小【分润】
	 * @param dealAmount
	 * @return <p>交易利润加钱需要怎加交易利润字段</p>
	 */
	@SuppressWarnings("unlikely-arg-type")
	protected Result addAmountDeal(UserFund userFund, BigDecimal balance, BigDecimal dealAmount) {
		log.info("【当前方法为 【交易利润加钱】，当前交易金额为：" + dealAmount + "，当前操作金额为：" + balance + "】");
		UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(userFund.getUserId());
		if (Common.User.USER_INFO_OFF.equals(userInfo.getSwitchs())) {
			log.info("【===========【当前账户被标记为禁止使用资金账户功能，请检查该账户存在的交易异常，当前账户为：" + userInfo.getUserId() + "】===========】");
			//TODO 当前位置除了做运行日志记录和数据日志记录，还应将该处存在的问题推送到系统醒目的地方
			return Result.buildFailMessage("当前账户被禁止使用资金流水功能，请检查账户是否存在异常");
		}
		BigDecimal accountBalance = userFund.getAccountBalance();//当前账户比较金额
		BigDecimal cashBalance = userFund.getCashBalance();//当前利润账户
		BigDecimal freezeBalance = userFund.getFreezeBalance();//当前冻结账户
		BigDecimal rechargeNumber = userFund.getRechargeNumber();//当前充值点数
		//	BigDecimal sumAgentProfit = userFund.getSumAgentProfit();//当前代理商分润  【当前订单为自己接单，不需要该字段】
		BigDecimal sumDealAmount = userFund.getSumDealAmount();//当前交易分润
		BigDecimal sumProfit = userFund.getSumProfit();//当前当前总的利润
		//	BigDecimal todayAgentProfit = userFund.getTodayAgentProfit();//今日代理分润  【当前订单为自己接单，无需统计该字段】
		BigDecimal todayDealAmount = userFund.getTodayDealAmount();//今日交易分润
		Integer sumOrderCount = userFund.getSumOrderCount();//总订单笔数
		Integer todayOrderCount = userFund.getTodayOrderCount();//今日订单笔数
		BigDecimal todayProfit = userFund.getTodayProfit();//今日总利润  =  今日代理分润 + 今日接单分润
		log.info("【金额修改后账户情况：当前账户总比较金额：" + accountBalance + "，当前账户充值点数：" + rechargeNumber + "，当前账户利润金额：" + cashBalance + "，当前账户冻结金额：" + freezeBalance + "，当前账户：" + userFund.getUserId() + "】");
		//以上为当前账户情况
		cashBalance = cashBalance.add(balance);
		accountBalance = cashBalance.add(rechargeNumber).subtract(freezeBalance);
		sumDealAmount = sumDealAmount.add(dealAmount);
		sumProfit = sumProfit.add(balance);
		todayDealAmount = todayDealAmount.add(dealAmount);
		sumOrderCount += 1;
		todayOrderCount += 1;
		todayProfit = todayProfit.add(balance);
		userFund.setAccountBalance(accountBalance);
		userFund.setCashBalance(cashBalance);
		userFund.setFreezeBalance(freezeBalance);
		userFund.setRechargeNumber(rechargeNumber);
		userFund.setSumDealAmount(sumDealAmount);
		userFund.setSumOrderCount(sumOrderCount);
		userFund.setSumProfit(sumProfit);
		userFund.setTodayDealAmount(todayDealAmount);
		userFund.setTodayOrderCount(todayOrderCount);
		userFund.setTodayProfit(todayProfit);
		Boolean updataAmount = userInfoServiceImpl.updataAmount(userFund);
		if (updataAmount) {
			log.info("【金额修改后账户情况：当前账户总比较金额：" + accountBalance + "，当前账户充值点数：" + rechargeNumber + "，当前账户利润金额：" + cashBalance + "，当前账户冻结金额：" + freezeBalance + "，当前账户：" + userFund.getUserId() + "】");
		} else {
			log.info("【账户修改失败】");
			ThreadUtil.execute(() -> {

				//TODO 新建线程提交，该线程不受主线程事务控制
				Boolean updataStatusEr = updateAccountEr(userFund.getUserId());
				if (updataStatusEr) {
					log.info("【账户已修改为不可使用，当前账号为：" + userFund.getUserId() + "】");
				}
			});
			throw new UserException("账户修改异常", null);
		}
		return Result.buildSuccessResult();
	}

	protected Result addAmountAgentProfit(UserFund userFund, BigDecimal balance) {
		log.info("【当前方法为 【增加代理商点数】 ，当前操作金额为：" + balance + "】");
		UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(userFund.getUserId());
		if (Common.User.USER_INFO_OFF.equals(userInfo.getSwitchs())) {
			log.info("【===========【当前账户被标记为禁止使用资金账户功能，请检查该账户存在的交易异常，当前账户为：" + userInfo.getUserId() + "】===========】");
			//TODO 当前位置除了做运行日志记录和数据日志记录，还应将该处存在的问题推送到系统醒目的地方
			return Result.buildFailMessage("当前账户被禁止使用资金流水功能，请检查账户是否存在异常");
		}
		BigDecimal accountBalance = userFund.getAccountBalance();//当前账户比较金额
		BigDecimal cashBalance = userFund.getCashBalance();//当前利润账户
		BigDecimal freezeBalance = userFund.getFreezeBalance();//当前冻结账户
		BigDecimal rechargeNumber = userFund.getRechargeNumber();//当前充值点数
		BigDecimal sumAgentProfit = userFund.getSumAgentProfit();//当前代理商分润
		BigDecimal sumDealAmount = userFund.getSumDealAmount();//当前交易分润
		BigDecimal sumProfit = userFund.getSumProfit();//当前总的利润  =  交易利润 + 代理利润
		BigDecimal todayAgentProfit = userFund.getTodayAgentProfit();//今日代理分润
		BigDecimal todayDealAmount = userFund.getTodayDealAmount();//今日交易分润
		Integer sumOrderCount = userFund.getSumOrderCount();//总订单笔数
		Integer todayOrderCount = userFund.getTodayOrderCount();//今日订单笔数
		BigDecimal todayProfit = userFund.getTodayProfit();//今日总利润  =  今日代理分润 + 今日接单分润
		log.info("【金额修改后账户情况：当前账户总比较金额：" + accountBalance + "，当前账户充值点数：" + rechargeNumber + "，当前账户利润金额：" + cashBalance + "，当前账户冻结金额：" + freezeBalance + "，当前账户：" + userFund.getUserId() + "】");
		//以上为当前账户情况
		cashBalance = cashBalance.add(balance);//增加 分润账户
		accountBalance = cashBalance.add(rechargeNumber).subtract(freezeBalance);
		sumAgentProfit = sumAgentProfit.add(balance);
		todayAgentProfit = todayAgentProfit.add(balance);
        todayProfit = todayProfit.add(balance);
        sumProfit = sumProfit.add(balance);
        userFund.setCashBalance(cashBalance);
        userFund.setAccountBalance(accountBalance);
        userFund.setTodayAgentProfit(todayAgentProfit);
        userFund.setTodayProfit(todayProfit);
        userFund.setSumAgentProfit(sumAgentProfit);
        userFund.setSumProfit(sumProfit);
        Boolean updataAmount = userInfoServiceImpl.updataAmount(userFund);
        if (updataAmount) {
            log.info("【金额修改后账户情况：当前账户总比较金额：" + accountBalance + "，当前账户充值点数：" + rechargeNumber + "，当前账户利润金额：" + cashBalance + "，当前账户冻结金额：" + freezeBalance + "，当前账户：" + userFund.getUserId() + "】");
        } else {
            log.info("【账户修改失败】");
            ThreadUtil.execute(() -> {
                //TODO 新建线程提交，该线程不受主线程事务控制
				Boolean updataStatusEr = updateAccountEr(userFund.getUserId());
                if (updataStatusEr) {
                    log.info("【账户已修改为不可使用，当前账号为：" + userFund.getUserId() + "】");
                }
            });
            throw new UserException("账户修改异常", null);
        }
		return Result.buildSuccessResult();
	}

	/**
	 * <p>检查当前用户资金状态是否存在问题</p>
	 *
	 * @param userFund 用户实时资金数据
	 * @return
	 */
	protected Result clickUserFund(UserFund userFund) {
        log.info("进入当前账户金额检查方法");
        BigDecimal accountBalance = userFund.getAccountBalance();//当前账户可操作余额【当前现金账户 +当前冻结账户+当前充值点数】
        BigDecimal cashBalance = userFund.getCashBalance();//当前现金账户
        BigDecimal freezeBalance = userFund.getFreezeBalance();//当前冻结账户
        BigDecimal rechargeNumber = userFund.getRechargeNumber();//当前充值点数
        BigDecimal add = cashBalance.add(rechargeNumber).subtract(freezeBalance);
        BigDecimal subtract = add.subtract(accountBalance);
        if (subtract.compareTo(new BigDecimal("0")) == -1) {
            subtract = subtract.multiply(new BigDecimal("-1"));
        }
        ;
        int i = subtract.subtract(new BigDecimal("1")).compareTo(new BigDecimal("0"));
        if (i == 1) {
            log.info("【===========【经过系统核对后，当前用户账户存在问题，请重点关照该用户的账号，检查问题出现原因】===========】");
            ThreadUtil.execute(() -> {
                //TODO 新建线程提交，该线程不受主线程事务控制
                updateAccountEr(userFund.getUserId());
            });
            return Result.buildFail();
        }
        log.info("【===========【经过系统核对后，当前用户账户不存在问题，请放心交易】===========】");
        return Result.buildSuccess();
    }

	/**
	 * <p>增加金额【充值】</p>
	 * <p>增加充值金额流水</p>
	 *
	 * @param userFund 资金账户实体
	 * @param balance  增加金额
	 * @return
	 */
	@SuppressWarnings("unlikely-arg-type")
	protected Result addAmountRecharge(UserFund userFund, BigDecimal balance) {
		UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(userFund.getUserId());
		if (Common.User.USER_INFO_OFF.equals(userInfo.getSwitchs())) {
			log.info("【===========【当前账户被标记为禁止使用资金账户功能，请检查该账户存在的交易异常，当前账户为：" + userInfo.getUserId() + "】===========】");
			//TODO 当前位置除了做运行日志记录和数据日志记录，还应将该处存在的问题推送到系统醒目的地方
			return Result.buildFailMessage("当前账户被禁止使用资金流水功能，请检查账户是否存在异常");
		}
		BigDecimal accountBalance = userFund.getAccountBalance();
		BigDecimal cashBalance = userFund.getCashBalance();
		BigDecimal freezeBalance = userFund.getFreezeBalance();
		BigDecimal rechargeNumber = userFund.getRechargeNumber();
		log.info("【当前账户余额：" + accountBalance + "】");
		log.info("【当前账户利润余额：" + cashBalance + "】");
		log.info("【当前账户冻结余额：" + freezeBalance + "】");
		log.info("【当前账户充值点数：" + rechargeNumber + "】");
		rechargeNumber = rechargeNumber.add(balance);
		accountBalance = rechargeNumber.subtract(freezeBalance).add(cashBalance);
		log.info("【当前金额加款操作。。。。。。。。。】");
		userFund.setRechargeNumber(rechargeNumber);
		userFund.setAccountBalance(accountBalance);
		Boolean flag = userInfoServiceImpl.updataAmount(userFund);
		if (flag) {
			log.info("【当前账户添加充值点数成功资金详细情况如下：】");
			log.info("【当前账户余额：" + accountBalance + "】");
			log.info("【当前账户利润余额：" + cashBalance + "】");
			log.info("【当前账户冻结余额：" + freezeBalance + "】");
			log.info("【当前账户充值点数：" + rechargeNumber + "】");
			return Result.buildSuccessMessage("当前账户充值金额添加成功");
		} else {
			log.info("【当前账户添加充值点数【失败】，请联系技术检查当前账户存在的异常情况】");
			ThreadUtil.execute(() -> {
				//TODO 新建线程提交，该线程不受主线程事务控制
				updateAccountEr(userFund.getUserId());
			});
			return Result.buildFail();
		}
	}

	protected Result addAmountRechargeWit(UserFund userFund, BigDecimal balance) {
		UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(userFund.getUserId());
		if (Common.User.USER_INFO_OFF.equals(userInfo.getSwitchs())) {
			log.info("【===========【当前账户被标记为禁止使用资金账户功能，请检查该账户存在的交易异常，当前账户为：" + userInfo.getUserId() + "】===========】");
			//TODO 当前位置除了做运行日志记录和数据日志记录，还应将该处存在的问题推送到系统醒目的地方
			return Result.buildFailMessage("当前账户被禁止使用资金流水功能，请检查账户是否存在异常");
		}
		BigDecimal accountBalance = userFund.getAccountBalance();
		BigDecimal cashBalance = userFund.getCashBalance();
		BigDecimal freezeBalance = userFund.getFreezeBalance();
		BigDecimal rechargeNumber = userFund.getRechargeNumber();

		BigDecimal sumWitAmount = userFund.getSumOtherWitAmount();
		BigDecimal todayWitAmount = userFund.getTodayOtherWitAmount();


		sumWitAmount = sumWitAmount.add(balance);
		todayWitAmount = todayWitAmount.add(balance);


		log.info("【当前账户余额：" + accountBalance + "】");
		log.info("【当前账户利润余额：" + cashBalance + "】");
		log.info("【当前账户冻结余额：" + freezeBalance + "】");
		log.info("【当前账户充值点数：" + rechargeNumber + "】");
		rechargeNumber = rechargeNumber.add(balance);
		accountBalance = rechargeNumber.subtract(freezeBalance).add(cashBalance);


		log.info("【当前金额加款操作。。。。。。。。。】");
		userFund.setRechargeNumber(rechargeNumber);
		userFund.setAccountBalance(accountBalance);
		userFund.setSumOtherWitAmount(sumWitAmount);
		userFund.setTodayOtherWitAmount(todayWitAmount);
		Boolean flag = userInfoServiceImpl.updataAmount(userFund);
		if (flag) {
			log.info("【当前账户添加充值点数成功资金详细情况如下：】");
			log.info("【当前账户余额：" + accountBalance + "】");
			log.info("【当前账户利润余额：" + cashBalance + "】");
			log.info("【当前账户冻结余额：" + freezeBalance + "】");
			log.info("【当前账户充值点数：" + rechargeNumber + "】");
			return Result.buildSuccessMessage("当前账户充值金额添加成功");
		} else {
			log.info("【当前账户添加充值点数【失败】，请联系技术检查当前账户存在的异常情况】");
			ThreadUtil.execute(() -> {
				//TODO 新建线程提交，该线程不受主线程事务控制
				updateAccountEr(userFund.getUserId());
			});
			return Result.buildFail();
		}
	}

	/**
	 * 记录资金修改失败
	 *
	 * @param finalUserFund 资金账户
	 * @param type          钱款类型
	 * @param balance       金额
	 */
	protected void addExcption(UserFund finalUserFund, String type, BigDecimal balance, String orderId) {
		ThreadUtil.execute(() -> {
			String url = "http://10.170.0.4:8888/api/send?text=";
			String test = "账户" + finalUserFund.getUserId() + "被系统自动关闭，当前关闭原因：触发系统自动结算，类型：" + type + "，当前重新结算金额：" + balance.longValue() + "，关联订单：" +
					"" + orderId + "，触发时间：" + DatePattern.NORM_DATETIME_FORMAT.format(new Date());
			test = HttpUtil.encode(test, "UTF-8");
			String id = "&id=";
			String ids = "-414940159";
			id += ids;
			String s = url + test + id;
			HttpUtil.get(s, 1000);
		});

	}

	/**
	 * 代付成功增加统计数据
	 *
	 * @param userFund
	 * @param balance
	 * @return
	 */
	protected Result witSuccessStatis(UserFund userFund, BigDecimal balance) {
		log.info("当前方法为代付成功统计");
		BigDecimal sumWitAmount = userFund.getSumWitAmount();//历史代付数据
		BigDecimal todayWitAmount = userFund.getTodayWitAmount();//当日代付数据
		Integer version = userFund.getVersion();//修改版本号
		log.info("【当前账户历史累计代付金额为：" + sumWitAmount + "，当日代付累计金额为：" + todayWitAmount + "，当前代付账号为：" + userFund.getUserId() + ",当前代付金额为：" + balance + "】");
		sumWitAmount = sumWitAmount.add(balance);
		todayWitAmount = todayWitAmount.add(balance);
		userFund.setSumWitAmount(sumWitAmount);
		userFund.setTodayWitAmount(todayWitAmount);
		Boolean flag = userInfoServiceImpl.updataAmount(userFund);
		if (flag) {
			log.info("【当前账户添加充值点数成功资金详细情况如下：】");
			log.info("【当前账户余额：" + userFund.getAccountBalance() + "】");
			log.info("【当前账户利润余额：" + userFund.getCashBalance() + "】");
			log.info("【当前账户冻结余额：" + userFund.getFreezeBalance() + "】");
			log.info("【当前账户充值点数：" + userFund.getRechargeNumber() + "】");
			return Result.buildSuccessMessage("当前账户充值金额添加成功");
		} else {
			log.info("【当前账户添加充值点数【失败】，请联系技术检查当前账户存在的异常情况】");
			ThreadUtil.execute(() -> {
				//TODO 新建线程提交，该线程不受主线程事务控制
				updateAccountEr(userFund.getUserId());
			});
			return Result.buildFail();
		}
	}

	/**
	 * 代付扣减统计数据
	 *
	 * @param userFund
	 * @param balance
	 * @return
	 */
	protected Result witBackStatis(UserFund userFund, BigDecimal balance) {
		log.info("当前方法为代付成功统计");
		BigDecimal sumWitAmount = userFund.getSumWitAmount();//历史代付数据
		BigDecimal todayWitAmount = userFund.getTodayWitAmount();//当日代付数据
		Integer version = userFund.getVersion();//修改版本号
		log.info("【当前账户历史累计代付金额为：" + sumWitAmount + "，当日代付累计金额为：" + todayWitAmount + "，当前代付账号为：" + userFund.getUserId() + ",当前代付金额为：" + balance + "】");
		sumWitAmount = sumWitAmount.subtract(balance);
		todayWitAmount = todayWitAmount.subtract(balance);
		userFund.setSumWitAmount(sumWitAmount);
		userFund.setTodayWitAmount(todayWitAmount);
		Boolean flag = userInfoServiceImpl.updataAmount(userFund);
		if (flag) {
			log.info("【当前账户添加充值点数成功资金详细情况如下：】");
			log.info("【当前账户余额：" + userFund.getAccountBalance() + "】");
			log.info("【当前账户利润余额：" + userFund.getCashBalance() + "】");
			log.info("【当前账户冻结余额：" + userFund.getFreezeBalance() + "】");
			log.info("【当前账户充值点数：" + userFund.getRechargeNumber() + "】");
			return Result.buildSuccessMessage("当前账户充值金额添加成功");
		} else {
			log.info("【当前账户添加充值点数【失败】，请联系技术检查当前账户存在的异常情况】");
			ThreadUtil.execute(() -> {
				//TODO 新建线程提交，该线程不受主线程事务控制

				updateAccountEr(userFund.getUserId());

			});
			return Result.buildFail();
		}
	}

	Boolean updateAccountEr(String account) {
		if (!CollUtil.contains(accountList, account)) {
			return userInfoServiceImpl.updataStatusEr(account);
		}
		return Boolean.FALSE;
	}

	public Result addAmountDealWit(UserFund userFund, BigDecimal balance) {
		log.info("【当前方法为 【卡商出款交易利润加钱】， ：" + "，当前操作金额为：" + balance + "】");
		UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(userFund.getUserId());
		if (Common.User.USER_INFO_OFF.equals(userInfo.getSwitchs())) {
			log.info("【===========【当前账户被标记为禁止使用资金账户功能，请检查该账户存在的交易异常，当前账户为：" + userInfo.getUserId() + "】===========】");
			//TODO 当前位置除了做运行日志记录和数据日志记录，还应将该处存在的问题推送到系统醒目的地方
			return Result.buildFailMessage("当前账户被禁止使用资金流水功能，请检查账户是否存在异常");
		}
		BigDecimal accountBalance = userFund.getAccountBalance();//当前账户比较金额
		BigDecimal cashBalance = userFund.getCashBalance();//当前利润账户
		BigDecimal freezeBalance = userFund.getFreezeBalance();//当前冻结账户
		BigDecimal rechargeNumber = userFund.getRechargeNumber();//当前充值点数
		BigDecimal sumProfit = userFund.getSumProfit();//当前当前总的利润
		BigDecimal todayProfit = userFund.getTodayProfit();//今日总利润  =  今日代理分润 + 今日接单分润
		log.info("【金额修改后账户情况：当前账户总比较金额：" + accountBalance + "，当前账户充值点数：" + rechargeNumber + "，当前账户利润金额：" + cashBalance + "，当前账户冻结金额：" + freezeBalance + "，当前账户：" + userFund.getUserId() + "】");
		//以上为当前账户情况
		cashBalance = cashBalance.add(balance);
		accountBalance = cashBalance.add(rechargeNumber).subtract(freezeBalance);
		sumProfit = sumProfit.add(balance);
		todayProfit = todayProfit.add(balance);
		userFund.setAccountBalance(accountBalance);
		userFund.setRechargeNumber(rechargeNumber);
		userFund.setSumProfit(sumProfit);
		userFund.setTodayProfit(todayProfit);
		userFund.setCashBalance(cashBalance);
		Boolean updataAmount = userInfoServiceImpl.updataAmount(userFund);
		if (updataAmount) {
			log.info("【金额修改后账户情况：当前账户总比较金额：" + accountBalance + "，当前账户充值点数：" + rechargeNumber + "，当前账户利润金额：" + cashBalance + "，当前账户冻结金额：" + freezeBalance + "，当前账户：" + userFund.getUserId() + "】");
		} else {
			log.info("【账户修改失败】");
			ThreadUtil.execute(() -> {
				//TODO 新建线程提交，该线程不受主线程事务控制
				Boolean updataStatusEr = updateAccountEr(userFund.getUserId());
				if (updataStatusEr) {
					log.info("【账户已修改为不可使用，当前账号为：" + userFund.getUserId() + "】");
				}
			});
			throw new UserException("账户修改异常", null);
		}
		return Result.buildSuccessResult();
	}

	public Result deductBalanceProfit(UserFund userFund, BigDecimal balance) {
		log.info("【当前方法为 【卡商分润提现扣减】， ：" + "，当前操作金额为：" + balance + "】");
		UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(userFund.getUserId());
		if (Common.User.USER_INFO_OFF.equals(userInfo.getSwitchs())) {
			log.info("【===========【当前账户被标记为禁止使用资金账户功能，请检查该账户存在的交易异常，当前账户为：" + userInfo.getUserId() + "】===========】");
			//TODO 当前位置除了做运行日志记录和数据日志记录，还应将该处存在的问题推送到系统醒目的地方
			return Result.buildFailMessage("当前账户被禁止使用资金流水功能，请检查账户是否存在异常");
		}
		BigDecimal accountBalance = userFund.getSumProfit();//当前分总金额
		log.info("【当前分润总金额：" + accountBalance + "，当前扣减金额为：" + balance + "】");
		accountBalance = accountBalance.subtract(balance);
		userFund.setSumProfit(accountBalance);
		Boolean updataAmount = userInfoServiceImpl.updataAmount(userFund);
		if (updataAmount) {
		} else {
			log.info("【账户修改失败】");
			ThreadUtil.execute(() -> {
				//TODO 新建线程提交，该线程不受主线程事务控制
				Boolean updataStatusEr = updateAccountEr(userFund.getUserId());
				if (updataStatusEr) {
					log.info("【账户已修改为不可使用，当前账号为：" + userFund.getUserId() + "】");
				}
			});
			throw new UserException("账户修改异常", null);
		}
		return Result.buildSuccessResult();
	}


    public Result addBankprofit(UserFund userFund, BigDecimal balance) {
        log.info("【当前方法为 【卡商分润提现扣减】， ：" + "，当前操作金额为：" + balance + "】");
        UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(userFund.getUserId());
        if (Common.User.USER_INFO_OFF.equals(userInfo.getSwitchs())) {
            log.info("【===========【当前账户被标记为禁止使用资金账户功能，请检查该账户存在的交易异常，当前账户为：" + userInfo.getUserId() + "】===========】");
            //TODO 当前位置除了做运行日志记录和数据日志记录，还应将该处存在的问题推送到系统醒目的地方
            return Result.buildFailMessage("当前账户被禁止使用资金流水功能，请检查账户是否存在异常");
        }
        BigDecimal accountBalance = userFund.getSumProfit();//当前分总金额
        log.info("【当前分润总金额：" + accountBalance + "，当前扣减金额为：" + balance + "】");
        accountBalance = accountBalance.add(balance);
        userFund.setSumProfit(accountBalance);
        Boolean updataAmount = userInfoServiceImpl.updataAmount(userFund);
        if (updataAmount) {
        } else {
            log.info("【账户修改失败】");
            ThreadUtil.execute(() -> {
                //TODO 新建线程提交，该线程不受主线程事务控制
                Boolean updataStatusEr = updateAccountEr(userFund.getUserId());
                if (updataStatusEr) {
                    log.info("【账户已修改为不可使用，当前账号为：" + userFund.getUserId() + "】");
                }
            });
            throw new UserException("账户修改异常", null);
        }

        return Result.buildSuccessResult();
    }

    public Result deleteBankprofit(UserFund userFund, BigDecimal balance) {
        log.info("【当前方法为 【卡商分润提现扣减】， ：" + "，当前操作金额为：" + balance + "】");
        UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(userFund.getUserId());
        if (Common.User.USER_INFO_OFF.equals(userInfo.getSwitchs())) {
            log.info("【===========【当前账户被标记为禁止使用资金账户功能，请检查该账户存在的交易异常，当前账户为：" + userInfo.getUserId() + "】===========】");
            //TODO 当前位置除了做运行日志记录和数据日志记录，还应将该处存在的问题推送到系统醒目的地方
            return Result.buildFailMessage("当前账户被禁止使用资金流水功能，请检查账户是否存在异常");
        }
        BigDecimal sumProfit = userFund.getSumProfit();//当前分总金额
        BigDecimal accountBalance = userFund.getAccountBalance();
        BigDecimal rechargeNumber = userFund.getRechargeNumber();
        log.info("【当前分润总金额：" + sumProfit + "，当前扣减金额为：" + balance + "】");

        sumProfit = sumProfit.subtract(balance);
        accountBalance = accountBalance.subtract(balance);
        rechargeNumber = rechargeNumber.subtract(balance);
        userFund.setAccountBalance(accountBalance);
        userFund.setSumProfit(sumProfit);
        userFund.setRechargeNumber(rechargeNumber);
        Boolean updataAmount = userInfoServiceImpl.updataAmount(userFund);
        if (updataAmount) {
        } else {
            log.info("【账户修改失败】");
            ThreadUtil.execute(() -> {
                //TODO 新建线程提交，该线程不受主线程事务控制
                Boolean updataStatusEr = updateAccountEr(userFund.getUserId());
                if (updataStatusEr) {
                    log.info("【账户已修改为不可使用，当前账号为：" + userFund.getUserId() + "】");
                }
            });
            throw new UserException("账户修改异常", null);
        }
        return Result.buildSuccessResult();
    }

    public Result addBankprofitAmount(UserFund userFund, BigDecimal balance) {
        log.info("【当前方法为 【卡商手动增加佣金】， ：" + "，当前操作金额为：" + balance + "】");
        UserInfo userInfo = userInfoServiceImpl.findUserInfoByUserId(userFund.getUserId());
        if (Common.User.USER_INFO_OFF.equals(userInfo.getSwitchs())) {
            log.info("【===========【当前账户被标记为禁止使用资金账户功能，请检查该账户存在的交易异常，当前账户为：" + userInfo.getUserId() + "】===========】");
            //TODO 当前位置除了做运行日志记录和数据日志记录，还应将该处存在的问题推送到系统醒目的地方
            return Result.buildFailMessage("当前账户被禁止使用资金流水功能，请检查账户是否存在异常");
        }
        BigDecimal sumProfit = userFund.getSumProfit();//当前分总金额
        BigDecimal accountBalance = userFund.getAccountBalance();
        BigDecimal rechargeNumber = userFund.getRechargeNumber();
        log.info("【当前分润总金额：" + sumProfit + "，当前增加金额为：" + balance + "】");
        accountBalance = accountBalance.add(balance);
        rechargeNumber = rechargeNumber.add(balance);
        sumProfit = sumProfit.add(balance);
        userFund.setAccountBalance(accountBalance);
        userFund.setSumProfit(sumProfit);
        userFund.setRechargeNumber(rechargeNumber);
        Boolean updataAmount = userInfoServiceImpl.updataAmount(userFund);
        if (updataAmount) {
        } else {
            log.info("【账户修改失败】");
            ThreadUtil.execute(() -> {
                //TODO 新建线程提交，该线程不受主线程事务控制
                Boolean updataStatusEr = updateAccountEr(userFund.getUserId());
                if (updataStatusEr) {
                    log.info("【账户已修改为不可使用，当前账号为：" + userFund.getUserId() + "】");
                }
            });
            throw new UserException("账户修改异常", null);
        }
        return Result.buildSuccessResult();
    }
}
