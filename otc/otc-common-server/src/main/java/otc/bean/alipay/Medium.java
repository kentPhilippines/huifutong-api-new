package otc.bean.alipay;

import java.io.Serializable;
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