package alipay.manage.service;

import alipay.manage.bean.UserFund;
import alipay.manage.bean.UserInfo;

import java.math.BigDecimal;
import java.util.List;

public interface UserFundService {
    UserFund showTodayReceiveOrderSituation(String userId);

    /**
     * 通过userId查询用户账户
     *
     * @param userId
     * @return
     */
    UserFund findUserInfoByUserId(String userId);

    List<UserFund> findBankUserId(BigDecimal amount);

    List<UserFund> findFundAllQr();


    
}
