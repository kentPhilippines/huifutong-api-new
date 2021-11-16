package alipay.manage.contorller;

import alipay.config.annotion.LogMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexContorller {
	Logger log= LoggerFactory.getLogger(IndexContorller.class);
	/**
	 *	 首页
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/")
	public String index() {
		return "receive-order";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	@GetMapping("/otc365")
	public String otc365() {
		return "rechargeChannel/otc365";
	}
	/**
	 * <p>用户注册</p>
	 * @return
	 */

	@GetMapping("/register")
	public String register(Model m,HttpServletRequest request) {
		return "register";
	}

	/**
	 *	 我的主页
	 *
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/my-home-page")
	public String myHomePage() {
		log.info("我的主页面****************");
		return "my-home-page";
	}

	/**
	 *	 个人信息
	 *
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/personal-info")
	public String personalInfo() {
		return "personal-info";
	}

	/**
	 * 	个人帐变
	 *
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/personal-account-change")
	public String personalAccountChange() {
		return "personal-account-change";
	}
	/**
	 *	 充值
	 *
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/recharge")
	public String recharge() {
		return "recharge";
	}

	/**
	 * 	提现
	 *
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/withdraw")
	public String withdraw() {
		return "withdraw";
	}
	/**
	 *   	个人充提
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/recharge-withdraw-log")
	public String rechargeWithdrawLog() {
		return "recharge-withdraw-log";
	}
	/**
	 * 收款码
	 *
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/gathering-code")
	public String gatheringCode() {
		return "gathering-code";
	}
	/**
	 * 接单
	 *
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/receive-order")
	public String receiveOrder() {
		return "receive-order";
	}
	@LogMonitor(required = true)
	@GetMapping("/audit-order-re")
	public String auditOrderRe() {
		return "audit-order-re";
	}
	/**
	 *	 审核订单
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/audit-order-wit")
	public String auditOrderWit() {
		return "audit-order-wit";
	}

	/**
	 * 	接单记录
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/receive-order-record")
	public String receiveOrderRecord() {
		return "receive-order-record";
	}
	/**
	 * 	申诉记录
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/appeal-record")
	public String appealRecord() {
		return "appeal-record";
	}
	/**
	 * 	申诉详情
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/appeal-details")
	public String appealDetails() {
		return "appeal-details";
	}
	/**
	 *	 在线客服
	 * @return
	 */
	@GetMapping("/online-customer")
	public String onlineCustomer() {
		return "online-customer";
	}
	/**
	 * 	代理中心
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/agent-center")
	public String agentCenter() {
		return "agent-center";
	}
	/**
	 * 	代理开户
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/agent-open-an-account")
	public String agentOpenAnAccount() {
		return "agent-open-an-account";
	}
	/**
	 * 	下级开户
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/lower-level-open-an-account")
	public String lowerLevelOpenAnAccount() {
		return "lower-level-open-an-account";
	}
	/**
	 *	 下级账号管理
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/lower-level-account-manage")
	public String LowerLevelAccountManage() {
		return "lower-level-account-manage";
	}
	@GetMapping("/lower-level-account-change")
	public String LowerLevelAccountChange() {
		return "lower-level-account-change";
	}
	/**
	 *	 团队接单明细
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/lower-level-account-receive-order-record")
	public String LowerLevelAccountReceiveOrderRecord() {
		return "lower-level-account-receive-order-record";
	}
	/**
	 *	 团队充值明细
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/lower-level-recharge-details")
	public String lowerLevelRechargeDetails() {
		return "lower-level-recharge-details";
	}
	/**
	 *	 团队提现明细
	 * @return
	 */
	@LogMonitor(required = true)
	@GetMapping("/lower-level-withdraw-details")
	public String lowerLevelWithdrawDetails() {
		return "lower-level-withdraw-details";
	}

	@GetMapping("/pay")
	public String pay() {
		log.info("访问付款页面----------->");
		return "pay";
	}

	@GetMapping("/payEr")
	public String payEr() {
		return "payEr";
	}

	@GetMapping("/order-loadPayImg")
	@LogMonitor(required = true)
	public String loadPayImg() {
		return "order-loadPayImg";
	}

	@ResponseBody
	@GetMapping("/paySuccessNotice")
	public String paySuccessNotice() {
		return "success";
	}

	@GetMapping("/google")
	public String google() {
		return "google";
	}

	@GetMapping("/account_manage")
	public String accountManage() {
		return "account-manage";
	}
    @GetMapping("/onlineStatMembers")
	public String onlineMemberList(){return "online-member-list";}

}

