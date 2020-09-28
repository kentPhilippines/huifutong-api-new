package alipay.manage.service;

import alipay.manage.bean.ExceptionOrder;
import alipay.manage.bean.util.DealBean;
import alipay.manage.bean.util.WithdrawalBean;

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
    boolean addDealOrder(DealBean deal ,String msg,String ip);
    boolean addDealOrderOthen(String msg , String user , String ip );
    /**
     * 代付异常订单生成一般用于代付异常拦截
     * @param wit
     * @param msg
     * @return
     */
    boolean addWitOrder(WithdrawalBean wit , String msg);
}
