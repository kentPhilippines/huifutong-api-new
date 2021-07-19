package alipay.manage.bean;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class InviteCode implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;

    private String inviteCode;

    private String belongUser;
    private String rebate;
    private String rebateR;
    private String rebateW;

    public String getRebateW() {
        return rebateW;
    }

    public void setRebateW(String rebateW) {
        this.rebateW = rebateW;
    }

    public String getRebateR() {
        return rebateR;
    }

    public void setRebateR(String rebateR) {
        this.rebateR = rebateR;
    }

    public String getRebate() {

        return rebate;
    }

    public void setRebate(String rebate) {
        this.rebate = rebate;
    }

    private Integer count;

    private Date createTime;

    private Date submitTime;

    private Integer status;

    private String isDeal;

    private String userType;

    private String use;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode == null ? null : inviteCode.trim();
    }

    public String getBelongUser() {
        return belongUser;
    }

    public void setBelongUser(String belongUser) {
        this.belongUser = belongUser == null ? null : belongUser.trim();
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use == null ? null : use.trim();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}