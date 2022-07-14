package alipay.manage.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>用户资金账户表</p>
 * @author K
 */
public class UserFund implements Serializable{
	private static final long serialVersionUID = 1L;
    private Integer id;                        //数据id
    private String userId;                    //用户id
    private String userName;                //用户姓名
    private BigDecimal cashBalance;            //现金账户【分润】
    private BigDecimal rechargeNumber;        //充值点数
    private BigDecimal freezeBalance;        //冻结账户
    private BigDecimal accountBalance;        //可取现账户金额【码商账户余额=冻结金额+现金账户+充值点数】
    private BigDecimal quota;                //授权额度
    private BigDecimal sumDealAmount;        //累计交易额
    private BigDecimal sumWitAmount;        //累计交易额
    private BigDecimal sumOtherWitAmount;        //累计出款交易额
    private BigDecimal todayOtherWitAmount;        //当日出款交易额
    private BigDecimal sumRechargeAmount;    //累计充值金额【充值成功时统计记录】
    private BigDecimal sumProfit;            //累计利润金额
    private BigDecimal sumAgentProfit;        //累计代理商利润【如果当前账户为商户则该数据为0】
    private Integer sumOrderCount;            //累计接单笔数
    private BigDecimal todayDealAmount;        //当日接单金额
    private BigDecimal todayWitAmount;        //当日接单金额
    private BigDecimal todayProfit;            //当日接单利润【代理利润+接单利润=当日利润】
    private Integer todayOrderCount;        //当日接单笔数
    private BigDecimal todayAgentProfit;    //当日代理商利润【如果当前账户为商户则该数据为0】
    private BigDecimal deposit;    //设置卡商押金
    private String userType;                //用户类型,商户1 码商2
    private String agent;                    //代理商id【如果存在代理商则存在数据,如果不存在代理商则为null】
    private String isAgent;                    //是否为代理商:1代理商2普通码商【分润结算类型看用户类型userType】
    private Date createTime;
    private Date submitTime;
    private Integer status;
    private Integer version;                //版本号
    private String currency;
    private BigDecimal credit;			//信用等级为默认为100,掉单一次,或者出现故障一次减分0.001分,可人为加分

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
    }

    public BigDecimal getTodayOtherWitAmount() {
        return todayOtherWitAmount;
    }

    public void setTodayOtherWitAmount(BigDecimal todayOtherWitAmount) {
        this.todayOtherWitAmount = todayOtherWitAmount;
    }

    public BigDecimal getSumOtherWitAmount() {

        return sumOtherWitAmount;
    }

    public void setSumOtherWitAmount(BigDecimal sumOtherWitAmount) {
        this.sumOtherWitAmount = sumOtherWitAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getQuota() {
        return quota;
    }

    public void setQuota(BigDecimal quota) {
        this.quota = quota;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public BigDecimal getRechargeNumber() {
        return rechargeNumber;
    }

    public void setRechargeNumber(BigDecimal rechargeNumber) {
        this.rechargeNumber = rechargeNumber;
    }

    public BigDecimal getFreezeBalance() {
        return freezeBalance;
    }

    public void setFreezeBalance(BigDecimal freezeBalance) {
        this.freezeBalance = freezeBalance;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public BigDecimal getSumDealAmount() {
        return sumDealAmount;
    }

    public void setSumDealAmount(BigDecimal sumDealAmount) {
        this.sumDealAmount = sumDealAmount;
    }

    public BigDecimal getSumRechargeAmount() {
        return sumRechargeAmount;
    }

    public void setSumRechargeAmount(BigDecimal sumRechargeAmount) {
        this.sumRechargeAmount = sumRechargeAmount;
    }

    public BigDecimal getSumProfit() {
        return sumProfit;
    }

    public void setSumProfit(BigDecimal sumProfit) {
        this.sumProfit = sumProfit;
    }

    public BigDecimal getSumAgentProfit() {
        return sumAgentProfit;
    }

    public void setSumAgentProfit(BigDecimal sumAgentProfit) {
        this.sumAgentProfit = sumAgentProfit;
    }

    public Integer getSumOrderCount() {
        return sumOrderCount;
    }

    public void setSumOrderCount(Integer sumOrderCount) {
        this.sumOrderCount = sumOrderCount;
    }

    public BigDecimal getTodayDealAmount() {
        return todayDealAmount;
    }

    public void setTodayDealAmount(BigDecimal todayDealAmount) {
        this.todayDealAmount = todayDealAmount;
    }

    public BigDecimal getTodayProfit() {
        return todayProfit;
    }

    public void setTodayProfit(BigDecimal todayProfit) {
        this.todayProfit = todayProfit;
    }

    public Integer getTodayOrderCount() {
        return todayOrderCount;
    }

    public void setTodayOrderCount(Integer todayOrderCount) {
        this.todayOrderCount = todayOrderCount;
    }

    public BigDecimal getTodayAgentProfit() {
        return todayAgentProfit;
    }

    public void setTodayAgentProfit(BigDecimal todayAgentProfit) {
        this.todayAgentProfit = todayAgentProfit;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent == null ? null : agent.trim();
    }

    public String getIsAgent() {
        return isAgent;
    }

    public void setIsAgent(String isAgent) {
        this.isAgent = isAgent == null ? null : isAgent.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Integer getStatus() {
        return status;
    }

    public BigDecimal getSumWitAmount() {
        return sumWitAmount;
    }

    public void setSumWitAmount(BigDecimal sumWitAmount) {
        this.sumWitAmount = sumWitAmount;
    }

    public BigDecimal getTodayWitAmount() {
        return todayWitAmount;
    }

    public void setTodayWitAmount(BigDecimal todayWitAmount) {
        this.todayWitAmount = todayWitAmount;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
	@Override
	public String toString() {
		return "UserFund [id=" + id + ", userId=" + userId + ", userName=" + userName + ", cashBalance=" + cashBalance
				+ ", rechargeNumber=" + rechargeNumber + ", freezeBalance=" + freezeBalance + ", accountBalance="
				+ accountBalance + ", sumDealAmount=" + sumDealAmount + ", sumRechargeAmount=" + sumRechargeAmount
				+ ", sumProfit=" + sumProfit + ", sumAgentProfit=" + sumAgentProfit + ", sumOrderCount=" + sumOrderCount
				+ ", todayDealAmount=" + todayDealAmount + ", todayProfit=" + todayProfit + ", todayOrderCount="
				+ todayOrderCount + ", todayAgentProfit=" + todayAgentProfit + ", userType=" + userType + ", agent="
				+ agent + ", isAgent=" + isAgent + ", createTime=" + createTime + ", submitTime=" + submitTime
				+ ", status=" + status + ", version=" + version + "]";
	}
}