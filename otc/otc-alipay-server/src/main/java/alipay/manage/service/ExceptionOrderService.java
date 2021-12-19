package alipay.manage.service;

import alipay.manage.bean.ExceptionOrder;
import alipay.manage.bean.util.DealBean;
import alipay.manage.bean.util.WithdrawalBean;

import java.math.BigDecimal;

public interface ExceptionOrderService {


    /**
     * 一般异常订单生成，一般用于系统异常等情况
     * @param order
     * @return
     */
    boolean addOrder(ExceptionOrder order);

    /**
     * 交易异常订单生成，一般用于用户交易拦截
     * @param deal
     * @param msg
     * @return
     */
    void addDealOrder(DealBean deal ,String msg,String ip);
    void addDealOrderOthen(String msg , String user , String ip );
    /**
     * 代付异常订单生成一般用于代付异常拦截
     * @param wit
     * @param msg
     * @return
     */
    void addWitOrder(WithdrawalBean wit , String msg,String ip);






    void addWitEx(String user,String amount,String msg,String ip ,String orderId  );
    void addDealEx(String user,String amount,String msg,String ip ,String orderId  );

    void addBankInfo(String orderId, String userId, String msg, Boolean status, String ip, BigDecimal dealAmount);
    void addFindBankInfo(String orderId, String userId, String msg, Boolean status, String ip, BigDecimal dealAmount);

    void unGrabOrder(String orderId, String userId, String 卡商放弃出款, Boolean aFalse, String clientIP, BigDecimal dealAmount);


    void enterBankOrder(String orderId, String userId, String s, Boolean aTrue, String clientIP, BigDecimal dealAmount);


    
}
