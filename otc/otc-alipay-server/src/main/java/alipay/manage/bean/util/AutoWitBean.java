package alipay.manage.bean.util;

public class AutoWitBean {
    private  String orderId;
    private  String bankNo;//卡号  如 62273837283972937
    private  String bankAccount;//所属人 如 张三
    private  String bankName;//银行姓名 如 中国银行
    private  String amount; // 金额 如 400

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }


    @Override
    public String toString() {
        return "AutoWitBean{" +
                "orderId='" + orderId + '\'' +
                ", bankNo='" + bankNo + '\'' +
                ", bankAccount='" + bankAccount + '\'' +
                ", bankName='" + bankName + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }
}
