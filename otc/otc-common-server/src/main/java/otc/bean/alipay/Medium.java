package otc.bean.alipay;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>收款媒介</p>
 *
 * @author K
 */
public class Medium implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;                            //数据id
    private String mediumNumber;                //媒介账号       银行卡号
    private String mediumId;                    //系统媒介编号    系统编号
    private String mediumHolder;                //媒介所属人      开户人
    private String mediumPhone;                    //媒介绑定手机号   手机号
    private String bankcode;                    //  R 为 入款    W  为出款
    private String account;                        //   银行账户    如中国工商银行
    private String mountNow;                    // 当前媒介实际金额
    private String mountSystem;                 //当前媒介系统金额
    private String mountLimit;                  //当前媒介限制金额   系统默认一万
    private String qrcodeId;                    //媒介所属人
    private String code;                        //媒介code
    private Date createTime;
    private Date submitTime;
    private Integer status;
    private String isDeal;
    private String mediumNote;                    //媒介备注
    private String attr;                        //收款媒介供应链标识
    private String fixation;                //当前媒介下所有金额
    private String notfiyMask;                //当前媒介下所有金额

    private Integer version;
    private String isQueue;

    private BigDecimal toDayDeal;//当如入款
    private BigDecimal sumDayDeal;//累计入款
    private BigDecimal toDayWit;//当日出款
    private BigDecimal sumDayWit;//累计出款
    private BigDecimal startAmount;//起始收款金额
    private BigDecimal sumAmounlimit;//总金额

    public BigDecimal getSumAmounlimit() {
        return sumAmounlimit;
    }

    public void setSumAmounlimit(BigDecimal sumAmounlimit) {
        this.sumAmounlimit = sumAmounlimit;
    }

    private BigDecimal yseToday;//昨日余额
    private String startTime;//接单开始时间  格式    hh:mm:ss
    private String endTime;//接单结束 时间
    private Integer isClickPay;// 收款户名验证是否验证户名   1 验证    0 不验证
    private Long sc;//接单间隔秒数
    private String payInfo;//支付宝扫码绑定的时候 上传二维码



    private Integer todayCount;//当日交易笔数
    private Integer sumCount;//累计交易笔数
    private Integer countLimit;//日交易限制笔数  默认  50


    private Integer todayCountWit;//当日出款笔数
    private Integer sumCountWit;//累计出款笔数




    public Integer getSumCountWit() {
        return sumCountWit;
    }

    public void setSumCountWit(Integer sumCountWit) {
        this.sumCountWit = sumCountWit;
    }

    public Integer getTodayCountWit() {
        return todayCountWit;
    }

    public void setTodayCountWit(Integer todayCountWit) {
        this.todayCountWit = todayCountWit;
    }

    public Integer getCountLimit() {
        return countLimit;
    }

    public void setCountLimit(Integer countLimit) {
        this.countLimit = countLimit;
    }

    public Integer getSumCount() {
        return sumCount;
    }

    public void setSumCount(Integer sumCount) {
        this.sumCount = sumCount;
    }

    public Integer getTodayCount() {
        return todayCount;
    }

    public void setTodayCount(Integer todayCount) {
        this.todayCount = todayCount;
    }

    private Integer isRed;//是否支付宝红包

    public Integer getIsRed() {
        return isRed;
    }

    public void setIsRed(Integer isRed) {
        this.isRed = isRed;
    }
    public String getPayInfo() {
        return payInfo;
    }

    public void setPayInfo(String payInfo) {
        this.payInfo = payInfo;
    }

    public BigDecimal getYseToday() {
        return yseToday;
    }

    public void setYseToday(BigDecimal yseToday) {
        this.yseToday = yseToday;
    }

    public Long getSc() {
        return sc;
    }

    public void setSc(Long sc) {
        this.sc = sc;
    }

    public Integer getIsClickPay() {
        return isClickPay;
    }

    public void setIsClickPay(Integer isClickPay) {
        this.isClickPay = isClickPay;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public BigDecimal getStartAmount() {
        return startAmount;
    }

    public void setStartAmount(BigDecimal startAmount) {
        this.startAmount = startAmount;
    }

    public BigDecimal getSumDayWit() {
        return sumDayWit;
    }

    public void setSumDayWit(BigDecimal sumDayWit) {
        this.sumDayWit = sumDayWit;
    }

    public BigDecimal getToDayWit() {
        return toDayWit;
    }

    public void setToDayWit(BigDecimal toDayWit) {
        this.toDayWit = toDayWit;
    }

    public BigDecimal getSumDayDeal() {
        return sumDayDeal;
    }

    public void setSumDayDeal(BigDecimal sumDayDeal) {
        this.sumDayDeal = sumDayDeal;
    }

    public BigDecimal getToDayDeal() {
        return toDayDeal;
    }

    public void setToDayDeal(BigDecimal toDayDeal) {
        this.toDayDeal = toDayDeal;
    }
    private  String witAmount;


    public String getWitAmount() {
        return witAmount;
    }

    public void setWitAmount(String witAmount) {
        this.witAmount = witAmount;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getIsQueue() {
        return isQueue;
    }

    public void setIsQueue(String isQueue) {
        this.isQueue = isQueue;
    }

    public String getNotfiyMask() {
        return notfiyMask;
    }

    public void setNotfiyMask(String notfiyMask) {
        this.notfiyMask = notfiyMask;
    }

    public String getMountLimit() {
        return mountLimit;
    }

    public void setMountLimit(String mountLimit) {
        this.mountLimit = mountLimit;
    }

    public String getMountSystem() {
        return mountSystem;
    }

    public void setMountSystem(String mountSystem) {
        this.mountSystem = mountSystem;
    }

    public String getMountNow() {
        return mountNow;
    }

    public void setMountNow(String mountNow) {
        this.mountNow = mountNow;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getBankcode() {
        return bankcode;
    }

    public void setBankcode(String bankcode) {
        this.bankcode = bankcode;
    }

    public String getFixation() {
        return fixation;
    }

    public void setFixation(String fixation) {
        this.fixation = fixation;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMediumNumber() {
        return mediumNumber;
    }

    public void setMediumNumber(String mediumNumber) {
        this.mediumNumber = mediumNumber == null ? null : mediumNumber.trim();
    }

    public String getMediumId() {
        return mediumId;
    }

    public void setMediumId(String mediumId) {
        this.mediumId = mediumId == null ? null : mediumId.trim();
    }

    public String getMediumHolder() {
        return mediumHolder;
    }

    public void setMediumHolder(String mediumHolder) {
        this.mediumHolder = mediumHolder == null ? null : mediumHolder.trim();
    }

    public String getMediumPhone() {
        return mediumPhone;
    }

    public void setMediumPhone(String mediumPhone) {
        this.mediumPhone = mediumPhone == null ? null : mediumPhone.trim();
    }

    public String getQrcodeId() {
        return qrcodeId;
    }

    public void setQrcodeId(String qrcodeId) {
        this.qrcodeId = qrcodeId == null ? null : qrcodeId.trim();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
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

    public String getIsDeal() {
        return isDeal;
    }

    public void setIsDeal(String isDeal) {
        this.isDeal = isDeal == null ? null : isDeal.trim();
    }

    public String getMediumNote() {
        return mediumNote;
    }

    public void setMediumNote(String mediumNote) {
        this.mediumNote = mediumNote == null ? null : mediumNote.trim();
    }

	@Override
	public String toString() {
		return "Medium [id=" + id + ", mediumNumber=" + mediumNumber + ", mediumId=" + mediumId + ", mediumHolder="
				+ mediumHolder + ", mediumPhone=" + mediumPhone + ", qrcodeId=" + qrcodeId + ", code=" + code
				+ ", createTime=" + createTime + ", submitTime=" + submitTime + ", status=" + status + ", isDeal="
				+ isDeal + ", mediumNote=" + mediumNote + "]";
	}
}