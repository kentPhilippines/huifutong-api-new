package alipay.manage.bean.util;

import java.io.Serializable;

public class OnlineVO implements Serializable {

    private static final long serialVersionUID = -4273487630583486431L;

    private int loginOnlineCount = 0;
    private int bizOnlineCount = 0;
    private String onlineList;
    private String bizList;
    private String isAgent;

    public int getLoginOnlineCount() {
        return loginOnlineCount;
    }

    public void setLoginOnlineCount(int loginOnlineCount) {
        this.loginOnlineCount = loginOnlineCount;
    }

    public int getBizOnlineCount() {
        return bizOnlineCount;
    }

    public void setBizOnlineCount(int bizOnlineCount) {
        this.bizOnlineCount = bizOnlineCount;
    }

    public String getOnlineList() {
        return onlineList;
    }

    public void setOnlineList(String onlineList) {
        this.onlineList = onlineList;
    }

    public String getBizList() {
        return bizList;
    }

    public void setBizList(String bizList) {
        this.bizList = bizList;
    }

    public String getIsAgent() {
        return isAgent;
    }

    public void setIsAgent(String isAgent) {
        this.isAgent = isAgent;
    }
}
