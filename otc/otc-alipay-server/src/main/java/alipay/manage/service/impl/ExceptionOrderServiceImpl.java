package alipay.manage.service.impl;

import alipay.manage.bean.ExceptionOrder;
import alipay.manage.bean.util.DealBean;
import alipay.manage.bean.util.WithdrawalBean;
import alipay.manage.mapper.ExceptionOrderMapper;
import alipay.manage.service.ExceptionOrderService;
import cn.hutool.core.thread.ThreadUtil;
import org.springframework.stereotype.Service;
import otc.util.number.Number;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

@Service
public class ExceptionOrderServiceImpl implements ExceptionOrderService {
    //1交易,2人工加款,4人工扣款,5代付
    private final static Integer DEAL = 1;
    private final static Integer WIT = 5;
    private final static Integer ADD_ADMOUNT = 1;
    private final static Integer DELETE_AMOUNT = 1;
    private final static String OPERATION = "STSTEM";

    @Resource ExceptionOrderMapper exceptionOrderDao;
    @Override
    public boolean addOrder(ExceptionOrder order) {
        int i = exceptionOrderDao.insertSelective(order);
        return i>0 && i < 2;
    }




    @Override
    public void addDealOrder(DealBean deal, String msg,String ip) {
        ThreadUtil.execute(()->{
            ExceptionOrder exceptionOrder = new ExceptionOrder();
            exceptionOrder.setOrderExceptId(Number.getExc());
            exceptionOrder.setOrderId(deal.getOrderId());
            exceptionOrder.setOrderGenerationIp(ip);
            exceptionOrder.setExplains(msg.trim());
            exceptionOrder.setOrderAccount(deal.getAppId());
            exceptionOrder.setExceptOrderAmount(deal.getAmount());
            exceptionOrder.setExceptStatus(1);
            exceptionOrder.setExceptType(DEAL);
            exceptionOrder.setOperation(OPERATION);
            int i = exceptionOrderDao.insertSelective(exceptionOrder);
        });
    }

    @Override
    public void addDealOrderOthen(String msg, String user, String ip) {
        ThreadUtil.execute(()->{
            ExceptionOrder exceptionOrder = new ExceptionOrder();
            exceptionOrder.setOrderExceptId(Number.getExc());
            exceptionOrder.setOrderId(Number.getExc());
            exceptionOrder.setOrderGenerationIp(ip);
            exceptionOrder.setExplains(msg.trim());
            exceptionOrder.setOrderAccount(user);
            exceptionOrder.setExceptType(DEAL);
            exceptionOrder.setExceptStatus(1);
            exceptionOrder.setOperation(OPERATION);
            int i = exceptionOrderDao.insertSelective(exceptionOrder);
        });
    }

    @Override
    public void addWitOrder(WithdrawalBean wit, String msg,String i) {
        ThreadUtil.execute(()->{
            ExceptionOrder exceptionOrder = new ExceptionOrder();
            exceptionOrder.setOrderExceptId(Number.getExc());
            exceptionOrder.setOrderId(wit.getApporderid());
            exceptionOrder.setOrderGenerationIp(i);
            exceptionOrder.setExplains(msg.trim());
            exceptionOrder.setOrderAccount(wit.getAppid());
            exceptionOrder.setExceptOrderAmount(wit.getAmount());
            exceptionOrder.setExceptStatus(1);
            exceptionOrder.setExceptType(WIT);
            exceptionOrder.setOperation(OPERATION);
            exceptionOrderDao.insertSelective(exceptionOrder);
        });
    }


    @Override
    public void addWitEx(String user, String amount, String msg, String ip, String orderId) {
        ThreadUtil.execute(()->{
            ExceptionOrder exceptionOrder = new ExceptionOrder();
            exceptionOrder.setOrderExceptId(Number.getExc());
            exceptionOrder.setOrderId(orderId);
            exceptionOrder.setOrderGenerationIp(ip);
            exceptionOrder.setExplains(msg.trim());
            exceptionOrder.setOrderAccount(user);
            exceptionOrder.setExceptOrderAmount(amount);
            exceptionOrder.setExceptStatus(1);
            exceptionOrder.setExceptType(WIT);
            exceptionOrder.setOperation(OPERATION);
            exceptionOrderDao.insertSelective(exceptionOrder);
        });
    }

    @Override
    public void addDealEx(String user, String amount, String msg, String ip, String orderId) {
        ThreadUtil.execute(()->{
            ExceptionOrder exceptionOrder = new ExceptionOrder();
            exceptionOrder.setOrderExceptId(Number.getExc());
            exceptionOrder.setOrderId(Number.getExc());
            exceptionOrder.setOrderGenerationIp(ip);
            exceptionOrder.setExplains(msg.trim());
            exceptionOrder.setOrderAccount(user);
            exceptionOrder.setExceptOrderAmount(amount);
            exceptionOrder.setExceptType(DEAL);
            exceptionOrder.setExceptStatus(1);
            exceptionOrder.setOperation(OPERATION);
            int i = exceptionOrderDao.insertSelective(exceptionOrder);
        });
    }

    @Override
    public void addBankInfo(String orderId, String userId, String msg, Boolean status, String ip, BigDecimal dealAmount) {
        ThreadUtil.execute(()->{
            ExceptionOrder exceptionOrder = new ExceptionOrder();
            exceptionOrder.setOrderExceptId(Number.getExc());
            exceptionOrder.setOrderId(orderId);
            exceptionOrder.setCreateTime(new Date());
            exceptionOrder.setExceptStatus(2);
            exceptionOrder.setExceptType(6);//抢单
            exceptionOrder.setOperation(userId);
            exceptionOrder.setOrderGenerationIp(ip);
            exceptionOrder.setExceptOrderAmount(dealAmount.toString());
            exceptionOrder.setExplains(msg);
            exceptionOrder.setOrderAccount(userId);
            int i = exceptionOrderDao.insertSelective(exceptionOrder);
        });
    }

    @Override
    public void addFindBankInfo(String orderId, String userId, String msg, Boolean status, String ip, BigDecimal dealAmount) {
        ThreadUtil.execute(()->{
            ExceptionOrder exceptionOrder = new ExceptionOrder();
            exceptionOrder.setOrderExceptId(Number.getExc());
            exceptionOrder.setOrderId(orderId);
            exceptionOrder.setCreateTime(new Date());
            exceptionOrder.setExceptStatus(2);
            exceptionOrder.setExceptType(7);//抢单
            exceptionOrder.setOperation(userId);
            exceptionOrder.setOrderGenerationIp(ip);
            exceptionOrder.setExceptOrderAmount(dealAmount.toString());
            exceptionOrder.setExplains(msg);
            exceptionOrder.setOrderAccount(userId);
            int i = exceptionOrderDao.insertSelective(exceptionOrder);
        });
    }

    @Override
    public void unGrabOrder(String orderId, String userId, String 卡商放弃出款, Boolean aFalse, String clientIP, BigDecimal dealAmount) {
        ThreadUtil.execute(()->{
            ExceptionOrder exceptionOrder = new ExceptionOrder();
            exceptionOrder.setOrderExceptId(Number.getExc());
            exceptionOrder.setOrderId(orderId);
            exceptionOrder.setCreateTime(new Date());
            exceptionOrder.setExceptStatus(2);
            exceptionOrder.setExceptType(8);//放弃出款
            exceptionOrder.setOperation(userId);
            exceptionOrder.setOrderGenerationIp(clientIP);
            exceptionOrder.setExceptOrderAmount(dealAmount.toString());
            exceptionOrder.setExplains(卡商放弃出款);
            exceptionOrder.setOrderAccount(userId);
            int i = exceptionOrderDao.insertSelective(exceptionOrder);
        });
    }

    @Override
    public void enterBankOrder(String orderId, String userId, String s, Boolean aTrue, String clientIP, BigDecimal dealAmount) {
        ThreadUtil.execute(()->{
            ExceptionOrder exceptionOrder = new ExceptionOrder();
            exceptionOrder.setOrderExceptId(Number.getExc());
            exceptionOrder.setOrderId(orderId);
            exceptionOrder.setCreateTime(new Date());
            exceptionOrder.setExceptStatus(2);
            exceptionOrder.setExceptType(9);//卡商确认出款
            exceptionOrder.setOperation(userId);
            exceptionOrder.setOrderGenerationIp(clientIP);
            exceptionOrder.setExceptOrderAmount(dealAmount.toString());
            exceptionOrder.setExplains(s);
            exceptionOrder.setOrderAccount(userId);
            int i = exceptionOrderDao.insertSelective(exceptionOrder);
        });
    }


}
