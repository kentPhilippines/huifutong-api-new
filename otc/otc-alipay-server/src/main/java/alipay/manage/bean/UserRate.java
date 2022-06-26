package alipay.manage.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * <p>码商费率表</p>
 * @author K
 */
public class UserRate implements Serializable{
	private static final long serialVersionUID = 1L;
    private Integer id;						//数据id
    private String userId;					//用户id
    private Integer userType;				//用户类型,商户1 码商2
    private Integer switchs;				//当前用户总开关 1开启0关闭【码商商户后台控制,该数据只能在后台显示】
    private String payTypr;					//产品类型			
    private BigDecimal fee;					//费率数值
    private Integer feeType;				//费率类型:1交易费率，2代付费率
    private String channelId;				
    private Date createTime;
    private Date submitTime;
    private Integer status;
    private String retain1;
    private String retain2;
    private String retain3;
    private String retain4;
    private String queueList;
    public String getQueueList() {
        return queueList;
    }

    public void setQueueList(String queueList) {
        this.queueList = queueList;
    }

    public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
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

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Integer getSwitchs() {
        return switchs;
    }

    public void setSwitchs(Integer switchs) {
        this.switchs = switchs;
    }

    public String getPayTypr() {
        return payTypr;
    }

    public void setPayTypr(String payTypr) {
        this.payTypr = payTypr == null ? null : payTypr.trim();
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public Integer getFeeType() {
        return feeType;
    }

    public void setFeeType(Integer feeType) {
        this.feeType = feeType;
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

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRetain1() {
        return retain1;
    }

    public void setRetain1(String retain1) {
        this.retain1 = retain1 == null ? null : retain1.trim();
    }

    public String getRetain2() {
        return retain2;
    }

    public void setRetain2(String retain2) {
        this.retain2 = retain2 == null ? null : retain2.trim();
    }

    public String getRetain3() {
        return retain3;
    }

    public void setRetain3(String retain3) {
        this.retain3 = retain3 == null ? null : retain3.trim();
    }

    public String getRetain4() {
        return retain4;
    }

    public void setRetain4(String retain4) {
        this.retain4 = retain4 == null ? null : retain4.trim();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}