package alipay.manage.bean;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
/**
 * <p>码商交易订单</p>
 * @author K
 */
public class DealOrder implements Serializable{
	private static final long serialVersionUID = 1L;
    private Integer id;						//数据id
    private String orderId;					//订单id
    private String associatedId;			//关联订单id
    private String orderStatus;				//订单状态:0预下单1处理中2成功3未收到回调4失败5超时6订单申述7人工处理
    private BigDecimal dealAmount;			//订单交易金额
    private BigDecimal dealFee;				//订单交易手续费
    private BigDecimal actualAmount;		//实际到账金额
    private String orderType;				//订单类型:1交易,2系统加款
    private String orderAccount;			//订单关联商户账号
    private String orderQrUser;				//关联码商账户
    private String orderQr;					//关联二维码
    private String externalOrderId;			//外部订单号(下游商户请求参数,用户数据回调)
    private String generationIp;			//订单生成IP(客户端ip或者是下游商户id)
    private String notify;                    //订单异步回调地址
    private String back;                    //订单异步回调地址
    private String isNotify;                //是否發送通知 // YES 已發送 NO 未發送
    private String dealDescribe;            //交易备注
    private Date createTime;
    private Date submitTime;
    private String payType;                    //产品类型
    private Integer feeId;
    private Integer status;
    private Integer grabOrder;                  // 0 默认值  1 抢单   2 没钱出款
    private String retain1;
    private String retain2;             //卡商入款   渠道  成本
    private String retain3;             //当笔预交易系统分润【未扣除代理商分润】
    private Integer retain4;
    private String Time;
    private String payImg;                 //卡商代付出款上传付款凭证查看地址
    private String payInfo;                 //入款或者出款的短信消息
    private String currency;  ///货币类型
    private List orderQrUserList;
    private String payer;
    private String bankAmountNow;//交易完成之后银行卡余额

    public Integer getGrabOrder() {
        return grabOrder;
    }

    public void setGrabOrder(Integer grabOrder) {
        this.grabOrder = grabOrder;
    }

    public String getBankAmountNow() {
        return bankAmountNow;
    }

    public void setBankAmountNow(String bankAmountNow) {
        this.bankAmountNow = bankAmountNow;
    }

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public String getPayInfo() {
        return payInfo;
    }

    public void setPayInfo(String payInfo) {
        this.payInfo = payInfo;
    }

    public String getPayImg() {
        return payImg;
    }

    public void setPayImg(String payImg) {
        this.payImg = payImg;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date usefulTime;

    public Date getUsefulTime() {
        return usefulTime;
    }

    public void setUsefulTime(Date usefulTime) {
        this.usefulTime = usefulTime;
    }

    public String getPayType() {
        return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	public List getOrderQrUserList() {
		return orderQrUserList;
	}
	public void setOrderQrUserList(List orderQrUserList) {
		this.orderQrUserList = orderQrUserList;
	}
	public String getTime() {
		return Time;
	}
	public void setTime(String time) {
		Time = time;
	}
	public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }
    public String getAssociatedId() {
        return associatedId;
    }
    public void setAssociatedId(String associatedId) {
        this.associatedId = associatedId == null ? null : associatedId.trim();
    }
    public String getOrderStatus() {
        return orderStatus;
    }
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
    public BigDecimal getDealAmount() {
        return dealAmount;
    }
    public void setDealAmount(BigDecimal dealAmount) {
        this.dealAmount = dealAmount;
    }
    public BigDecimal getDealFee() {
        return dealFee;
    }
    public void setDealFee(BigDecimal dealFee) {
        this.dealFee = dealFee;
    }
    public BigDecimal getActualAmount() {
        return actualAmount;
    }
    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }
    public String getOrderType() {
        return orderType;
    }
    public void setOrderType(String orderType) {
        this.orderType = orderType == null ? null : orderType.trim();
    }
    public String getOrderAccount() {
        return orderAccount;
    }
    public void setOrderAccount(String orderAccount) {
        this.orderAccount = orderAccount == null ? null : orderAccount.trim();
    }
    public String getOrderQrUser() {
        return orderQrUser;
    }
    public void setOrderQrUser(String orderQrUser) {
        this.orderQrUser = orderQrUser == null ? null : orderQrUser.trim();
    }
    public String getOrderQr() {
        return orderQr;
    }
    public void setOrderQr(String orderQr) {
        this.orderQr = orderQr == null ? null : orderQr.trim();
    }
    public String getExternalOrderId() {
        return externalOrderId;
    }
    public void setExternalOrderId(String externalOrderId) {
        this.externalOrderId = externalOrderId == null ? null : externalOrderId.trim();
    }
    public String getGenerationIp() {
        return generationIp;
    }
    public void setGenerationIp(String generationIp) {
        this.generationIp = generationIp == null ? null : generationIp.trim();
    }
    public String getNotify() {
        return notify;
    }
    public void setNotify(String notify) {
        this.notify = notify == null ? null : notify.trim();
    }
    public String getBack() {
        return back;
    }
    public void setBack(String back) {
        this.back = back == null ? null : back.trim();
    }
    public String getIsNotify() {
        return isNotify;
    }
    public void setIsNotify(String isNotify) {
        this.isNotify = isNotify == null ? null : isNotify.trim();
    }
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
    	if(ObjectUtil.isNotNull(createTime)){
			Date afterDate = new Date(createTime .getTime() + 300000);
			this.setUsefulTime(afterDate);
		}
        this.createTime = createTime;
    }
    public Date getSubmitTime() {
        return submitTime;
    }
    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }
    public Integer getFeeId() {
        return feeId;
    }
    public void setFeeId(Integer feeId) {
        this.feeId = feeId;
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

    public Integer getRetain4() {
        return retain4;
    }

    public void setRetain4(Integer retain4) {
        this.retain4 = retain4;
    }

    public String getDealDescribe() {
        return dealDescribe;
    }

    public void setDealDescribe(String dealDescribe) {
        this.dealDescribe = dealDescribe == null ? null : dealDescribe.trim();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}