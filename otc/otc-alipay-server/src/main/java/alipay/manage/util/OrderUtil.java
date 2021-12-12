package alipay.manage.util;

import alipay.manage.bean.*;
import alipay.manage.mapper.*;
import alipay.manage.service.*;
import alipay.manage.util.amount.AmountPublic;
import alipay.manage.util.amount.AmountRunUtil;
import alipay.manage.util.bankcardUtil.BankAccountUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import otc.api.alipay.Common;
import otc.bean.alipay.OrderDealStatus;
import otc.bean.dealpay.Recharge;
import otc.bean.dealpay.Withdraw;
import otc.exception.order.OrderException;
import otc.result.Result;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class OrderUtil {
    /**
     * <p>订单置为成功的方法</p>
     * ##############################
     * 1，查询当前订单是否是成功状态<p>若为成功则禁止修改</p>
     * 2，修改当前订单为成功
     * 3，根据订单类型处理不同订单类型的资金处理
     * 4，根据不同的类型处理不同资金的流水类型
     * 5，订单修改完毕
     */
    static Lock lock = new ReentrantLock();
    Logger log = LoggerFactory.getLogger(OrderUtil.class);
    @Resource
    private ChannelFeeMapper channelFeeDao;
    @Autowired
    private CheckUtils checkUtils;
    @Autowired
    private WithdrawService withdrawServiceImpl;
    @Autowired
    private OrderService orderServiceImpl;
    @Autowired
    private AmountPublic amountPublic;
    @Autowired
    private AmountRunUtil amountRunUtil;
    @Autowired
    private UserInfoService userInfoServiceImpl;
    @Resource
    private RechargeMapper rechargeDao;
    @Resource
    private WithdrawMapper withdrawDao;
    @Resource
    private DealOrderAppMapper dealOrderAppDao;
    @Resource
    private UserRateMapper userRateDao;
    @Autowired
    private RiskUtil riskUtil;
    @Autowired
    private CorrelationService correlationServiceImpl;
    @Autowired
    NotifyUtil notifyUtil;
    @Autowired
    private BankAccountUtil bankAccountUtil;
    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * <p>充值订单置为成功</p>
     *
     * @param orderId 订单号
     * @return
     */
    public Result rechargeOrderEr(String orderId) {
        if (StrUtil.isBlank(orderId)) {
            return Result.buildFailMessage("必传参数为空");
        }
        Recharge order = rechargeDao.findRechargeOrder(orderId);
        return rechargeOrderEr(order);
    }

    /**
     * <p>充值充值订单成功【自动回调】</p>
     *
     * @param orderIds 订单号
     * @return
     */
    public Result rechargeOrderSu(String orderIds) {
        if (StrUtil.isBlank(orderIds)) {
            return Result.buildFailMessage("必传参数为空");
        }
        return rechargeOrderSu(orderIds, false);
    }

    /**
     * <p>充值订单人工置为成功</p>
     *
     * @param orderIds
     * @return
     */
    public Result rechargeSu(String orderIds) {
        if (StrUtil.isBlank(orderIds)) {
            return Result.buildFailMessage("必传参数为空");
        }
        return rechargeOrderSu(orderIds, true);
    }

    /**
     * <p>代付订单置为失败【这里只能是人工操作】</p>
     *
     * @param orderId 这里只能是人工操作
     * @param ip      操作 ip
     * @return
     */
    public Result withrawOrderEr(String orderId, String approval, String comment, String ip) {
        Withdraw order = withdrawDao.findWitOrder(orderId);
        if (order == null) {
            return Result.buildFailMessage("平台订单号不存在");
        }
		/*if (!Common.Order.Wit.ORDER_STATUS_YU.equals(order.getOrderStatus())) {
			return Result.buildFailMessage("订单已被处理，不允许操作");
		 	2020-10-04 增加任意状态下都可将代付订单处理为失败，故注释该代码
		}*/
        order.setApproval(approval);
        order.setComment(comment);
        return withrawOrderEr(order, ip);
    }

    /**
     * <p>码商充值订单置为成功</p>
     *
     * @param orderId
     * @param flag
     * @return
     */
    public Result rechargeOrderSu(String orderId, boolean flag) {
        if (StrUtil.isBlank(orderId)) {
            return Result.buildFailMessage("必传参数为空");
        }
        Recharge order = rechargeDao.findRechargeOrder(orderId);
        return rechargeOrderSu(order, flag);
    }


    /**
     * ###############################################################
     * 【以下方法 不对外开放】
     * ###############################################################
     */

    /**
     * <p>【码商交易订单】系统成功调用方法</p>
     *
     * @param orderId 订单号
     * @param ip      ip
     * @return
     */
    public Result orderDealSu(String orderId, String ip) {
        Result orderDealSu = orderDealSu(orderId, ip, false, null);
        return orderDealSu;
    }

    /**
     * <p>【码商交易订单】人工成功调用方法</p>
     *
     * @param orderId 交易订单号
     * @param ip      ip
     * @param userId  操作人
     * @return
     */
    public Result orderDealSu(String orderId, String ip, String userId) {
        if (StrUtil.isBlank(userId)) {
            return Result.buildFailMessage("当前必传参数为空，请传递操作人ID");
        }
        return orderDealSu(orderId, ip, true, userId);
    }

    /**
     * <p>【码商交易订单】人工调用失败的方法</p>
     *
     * @param orderId
     * @param mag
     * @param ip
     * @return
     */
    public Result orderDealEr(String orderId, String mag, String ip) {
        log.info("调用订单失败的方法");
        if (StrUtil.isBlank(mag)) {
            return Result.buildFailMessage("请填写失败理由");
        }
        Result updateDealOrderEr = updateDealOrderEr(orderId, mag, ip);
        return updateDealOrderEr;
    }

    /**
     * <p>交易订单成功</p>
     *
     * @param orderId 交易订单
     * @param ip      交易ip
     * @param flag    true 自然流水     false  人工流水
     * @return
     */
    public Result orderDealSu(String orderId, String ip, boolean flag, String userId) {
        log.info("【调用码商交易订单为成功的方法】");
        Result updataDealOrderSu = updataDealOrderSu(orderId, flag ? "人工置交易订单为成功，操作人为：" + userId + "" : "系统置交易订单为成功", ip, flag);
        log.info("【返回结果：" + updataDealOrderSu.toString() + "】");

        return updataDealOrderSu;
    }

    @Transactional
    public Result updataDealOrderSu(String orderId, String msg, String ip, Boolean flag) {
        if (StrUtil.isBlank(orderId) || StrUtil.isBlank(ip)) {
            return Result.buildFailMessage("必传参数为空");
        }
        try {
            DealOrder order = orderServiceImpl.findOrderStatus(orderId);
            if (null == order) {
                return Result.buildFailMessage("当前订单不存在");
            }
            if (order.getOrderStatus().toString().equals(OrderDealStatus.成功.getIndex().toString())) {
                return Result.buildFailMessage("当前订单不支持修改");
            }
            if (order.getOrderStatus().toString().equals(OrderDealStatus.失败.getIndex().toString())) {
                return Result.buildFailMessage("当前订单不支持修改");
            }
            if (!orderServiceImpl.updateOrderStatus(order.getOrderId(), OrderDealStatus.成功.getIndex().toString(), msg)) {
                return Result.buildFailMessage("订单修改失败，请重新发起成功");
            } else {//修改成功，放入，等待定时任务跑结算
                ThreadUtil.execute(() -> {//更新风控数据 统计数据等
                    log.info("更新风控数据，当前订单号：" + order.getOrderId() + "");
                    DealOrder orderSu = orderServiceImpl.findOrderStatus(orderId);
                    String bankAccount = "";
                    if (orderSu.getOrderStatus().toString().equals(OrderDealStatus.成功.getIndex().toString())) {
                        riskUtil.orderSu(order);
                        if (order.getOrderType().equals(Common.Order.ORDER_TYPE_DEAL.toString()) || order.getOrderType().equals(Common.Order.ORDER_TYPE_BANKCARD_R.toString())) {
                             //如果是入款订单,当前银行卡的系统业务余额会增加
                                log.info("【更新银行卡余额  入款增加】");
                                String orderQr = order.getOrderQr();
                                log.info("[当前更新银行卡：" + orderQr + "]");
                              //  String[] split = orderQr.split(":");
                              //    bankAccount = split[2];
                              //  mediumServiceImpl.updateMount(bankAccount, order.getDealAmount().toString(), "add","succ");
                        }
                    }
                    log.info("若为代付订，则置商户代付订单为成功，当前订单号：" + orderSu.getOrderId() + "，当前订单类型：" + orderSu.getOrderType() + "");
                    if (Common.Order.ORDER_TYPE_BANKCARD_W.toString().equals(orderSu.getOrderType().toString())) {
                         //如果是入款订单,当前银行卡的系统业务余额会增加       这里对只对业务余额进行修改
                            log.info("【更新银行卡余额  出款减少】");
                            String orderQr = order.getOrderQr();
                            String[] split = orderQr.split(":");
                           //   bankAccount = split[2];
                           // mediumServiceImpl.updateMount(bankAccount, order.getDealAmount().toString(), "sub","succ");
                        settlementOrderApp(orderSu);//如果是代付订单，商户会瞬间成功
                    }
                    log.info("修改订单银行卡余额表示标识，当前订单号：" + orderSu.getOrderId() + "，当前订单类型：" + orderSu.getOrderType() + "");

                });
                return Result.buildSuccessResult("订单修改成功");
            }
        } catch (Exception e) {
            throw new OrderException("订单修改异常", null);
        } finally {
        }
    }


    public Result settlement(DealOrder order) {
        Result execute = settlementOrder(order);
        if (execute.isSuccess()) {
            return settlementOrderApp(order);
        }
        return Result.buildFail();
    }


    Result settlementOrder(DealOrder order) {
        if (StrUtil.isEmpty(order.getGenerationIp())) {
            InetAddress addr = null;
            try {
                addr = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
            }
            String ip = addr.getHostAddress();//获得本机IP
            order.setGenerationIp(ip);
        }
        Result dealAmount = null;

        boolean flag = false;
        if (order.getDealDescribe().contains("人工")) {
            flag = true;
        }
        if (Common.Order.ORDER_TYPE_DEAL.toString().equals(order.getOrderType().toString())) {
            //正常交易订单 结算
            dealAmount = dealAmount(order, order.getGenerationIp(), false, order.getDealDescribe()); //  渠道结算
        } else {
            dealAmount = bankAccountUtil.accountOrderDealChannel(order, order.getGenerationIp(), flag);
        }
        if (!dealAmount.isSuccess()) {
            throw new OrderException("结算失败", null);
        }
        return Result.buildSuccessResult();
    }

    @Autowired
    private MediumService mediumServiceImpl;

    Result settlementOrderApp(DealOrder order) {
        Result dealAmount1 = null;
        /**
         * 商户订单结算
         * 1， 商户代付结算
         * 2， 卡商充值结算
         * 3， 商户充值结算
         */
        if (Common.Order.ORDER_TYPE_DEAL.toString().equals(order.getOrderType().toString())) {
            //商户充值结算
            dealAmount1 = enterOrderApp(order.getAssociatedId(), order.getGenerationIp(), false);
            if (dealAmount1.isSuccess()) {
                log.info("【订单修改成功，向下游发送回调：" + order.getOrderId() + "】");
                notifyUtil.sendMsg(order.getOrderId());
                return Result.buildSuccessMessage("订单修改成功");
            }
        } else if (Common.Order.ORDER_TYPE_BANKCARD_W.toString().equals(order.getOrderType().toString())) {
            Withdraw orderWit = withdrawDao.findWitOrder(order.getAssociatedId());
            // 商户代付结算
            if (orderWit.getOrderStatus().equals("2")) {
                log.info("【当前订单已处理,订单号：" + orderWit.getOrderId() + "】");
                return Result.buildSuccessMessage("当前订单已处理");
            }
            orderWit.setComment("确认出款");
            Result result1 = withrawOrderSu1(orderWit);
            //商户代理商结算
            if (!result1.isSuccess()) {
                return result1;
            }
            Result result = agentDpayChannel(orderWit, order.getGenerationIp(), true);
            if (!result.isSuccess()) {
                return result1;
            }
            notifyUtil.wit(orderWit.getOrderId());
            return Result.buildSuccessMessage("订单修改成功");
        } else if (Common.Order.ORDER_TYPE_BANKCARD_R.toString().equals(order.getOrderType().toString())) {
            //卡商充值订单结算
            Result result = rechargeSu(order.getAssociatedId());
            if (!result.isSuccess()) {
                return result;
            }
        }
        return Result.buildSuccess();
    }


    @SuppressWarnings("unused")
    @Transactional
    public Result updateDealOrderEr(String orderId, String mag, String ip) {
        if (StrUtil.isBlank(orderId) || StrUtil.isBlank(ip)) {
            return Result.buildFailMessage("必传参数为空");
        }
        DealOrder order = orderServiceImpl.findOrderStatus(orderId);
        if (order.getOrderStatus().toString().equals(OrderDealStatus.成功.getIndex().toString())) {
            return Result.buildFailMessage("当前订单不支持修改");
        }
        if (order.getOrderStatus().toString().equals(OrderDealStatus.失败.getIndex().toString())) {
            return Result.buildFailMessage("当前订单不支持修改");
        }
        boolean updateOrderStatus = orderServiceImpl.updateOrderStatus(orderId, OrderDealStatus.失败.getIndex().toString(), mag);
        if(Common.Order.ORDER_TYPE_BANKCARD_W.toString().equals(order.getOrderType().toString())){
           ThreadUtil.execute(()->{
               /*DealOrder order1 = orderServiceImpl.findOrderByOrderId(order.getOrderId());
                         //如果是入款订单,当前银行卡的系统业务余额会增加    手动失败 驳回的时候只对  代付余额进行修改
                            log.info("【 手动失败 更新银行卡余额  出款减少】");
                            String orderQr = order.getOrderQr();
                            String[] split = orderQr.split(":");
                            String bankAccount   = split[2];
             //  boolean flag = mediumServiceImpl.updateMount(bankAccount,order1.getDealAmount().toString(), "add","wait");*/
           });
           };
        if (!updateOrderStatus) {
            return Result.buildFailMessage("订单修改失败，请重新发起成功");
        } else {
            return Result.buildSuccess();
        }
    }

    /**
     * <p>码商交易订单【渠道交易订单】置为成功的时候的资金和流水处理</p>
     *
     * @param order 交易订单
     * @param ip    交易成功回调ip
     * @param flag  true 自然流水     false  人工流水
     * @return
     */
    private Result dealAmount(DealOrder order, String ip, Boolean flag, String msg) {

        //TODO 这里结算模式可选设置为  是否为顶代模式，如果为订单模式 则  只扣减顶代的   账号金额     给当前码商增加利润
        //TODO 如果不为顶代模式 则直接按照当前现有模式  结算
        Future<UserInfo> userInfoFuture = ThreadUtil.execAsync(() -> {
                    UserInfo userByOrder = userInfoServiceImpl.findUserByOrder(order.getOrderQrUser());
                    return userByOrder;
                }
        );
        UserInfo user = null;
        try {
            user = userInfoFuture.get();
        } catch (Exception e) {
            log.error("系统异常:", e);
            user = userInfoServiceImpl.findUserByOrder(order.getOrderQrUser());
        }
        UserFund userFund = new UserFund();
        userFund.setUserId(order.getOrderQrUser());
        if ("3".equals(user.getUserType().toString()) || "2".equals(user.getUserType().toString())) {//渠道账户结算
            log.info("【进入渠道账户结算，当前渠道：" + user.getUserId() + "】");
            userFund.setUserId(user.getUserId());
            transactionTemplate.execute((Result) -> {
                Result deleteDeal = amountPublic.deleteDeal(userFund, order.getDealAmount(), order.getOrderId());//扣除交易点数账户变动
                if (!deleteDeal.isSuccess()) {
                    throw new OrderException("渠道扣减异常", null);
                }
                Result deleteRechangerNumber = amountRunUtil.deleteRechangerNumber(order, ip, flag);//扣除交易点数 流水生成
                if (!deleteRechangerNumber.isSuccess()) {
                    throw new OrderException("渠道流水异常", null);
                }
                return deleteRechangerNumber;
            });
            if (user.getUserType().toString().equals("2")) {
                transactionTemplate.execute((Result) -> {
                    Result addDeal = amountPublic.addDeal(userFund, new BigDecimal(order.getRetain2()), order.getDealAmount(), order.getOrderId());
                    if (!addDeal.isSuccess()) {
                        return addDeal;
                    }
                    Result addDealAmount = amountRunUtil.addDealAmount(order, ip, Boolean.FALSE);
                    if (!addDealAmount.isSuccess()) {
                        throw new OrderException("流水异常", null);
                    }

                    return addDealAmount;
                });
                ThreadUtil.execute(() -> {
                    bankAccountUtil.agentChannelBankCard(order, Boolean.FALSE, ip);
                });
            } else if (user.getUserType().toString().equals("3")) {
                ChannelFee channelFee = channelFeeDao.findChannelFee(order.getOrderQrUser(), order.getRetain1());
                String channelRFee = channelFee.getChannelRFee();
                BigDecimal bigDecimal = new BigDecimal(channelRFee);
                BigDecimal multiply = bigDecimal.multiply(order.getDealAmount());
                transactionTemplate.execute((Result) -> {
                    Result result = amountPublic.addAmounRecharge(userFund, multiply, order.getOrderId());
                    if (!result.isSuccess()) {
                        throw new OrderException("渠道扣减异常", null);

                    }
                    Result addDealAmount = amountRunUtil.addDealAmountChannel(order, ip, Boolean.FALSE, multiply);
                    if (!addDealAmount.isSuccess()) {
                        throw new OrderException("渠道流水异常", null);
                    }
                    return addDealAmount;
                });
            }
            log.info("【金额修改完毕，流水生成成功】");
            return Result.buildSuccessResult();
        }
        log.info("【错误代码，当前订单号：" + order.getOrderId() + "】");
        throw new OrderException("渠道订单成功时异常", null);
    }

    /**
     * <p>充值成功</p>
     *
     * @return
     */
    public Result rechargeOrderSu(Recharge rechaege, boolean flag) {
        /**
         * ###########################
         * 充值成功给该账户加钱
         */
        int a = rechargeDao.updateOrderStatus(rechaege.getOrderId(), Common.Order.Recharge.ORDER_STATUS_SU);
        if (a == 0 || a > 2) {
            return Result.buildFailMessage("订单状态修改失败");
        }
        UserFund userFund = new UserFund();
        userFund.setUserId(rechaege.getUserId());
        Result addAmounRecharge = amountPublic.addAmounRecharge(userFund, rechaege.getAmount(), rechaege.getOrderId());
        if (!addAmounRecharge.isSuccess()) {
            return addAmounRecharge;
        }
        Result addAmount = amountRunUtil.addAmount(rechaege, rechaege.getRetain1(), flag);
        if (!addAmount.isSuccess()) {
            return addAmount;
        }
        return Result.buildSuccessMessage("充值成功");
    }

    /**
     * <p>充值失败</p>
     *
     * @param rechaege
     * @return
     */
    public Result rechargeOrderEr(Recharge rechaege) {
        /**
         * ######################
         * 充值失败修改订单状态什么都不管
         */
        int a = rechargeDao.updateOrderStatus(rechaege.getOrderId(), Common.Order.Recharge.ORDER_STATUS_ER);
        if (a > 0 && a < 2) {
            return Result.buildSuccessMessage("充值失败，可能原因，暂无充值渠道");
        }
        return Result.buildFail();
    }

    /**
     * <p>代付订单置为成功</p>
     *
     * @param orderId
     * @return
     */
    @Transactional
    public Result withrawOrderSu(String orderId, String approval,
                                 String comment,
                                 String channelId, String witType) {
        Withdraw order = withdrawDao.findWitOrder(orderId);
        if (order == null) {
            return Result.buildFailMessage("平台订单号不存在");
        }
        if (!Common.Order.Wit.ORDER_STATUS_PUSH.equals(order.getOrderStatus())) {
            return Result.buildFailMessage("订单已被处理，不允许操作");
        }
        order.setApproval(approval);
        order.setComment(comment);
        order.setChennelId(channelId);
        order.setWitType(witType);
        return withrawOrderSu(order);
    }
    /**
     * <p>代付成功</p>
     * 手动渠道结算
     *
     * @return
     */
    @Transactional
    public Result withrawOrderSu(Withdraw wit) {
        /**
         * #########################
         * 代付成功修改订单状态
         */
        int a = withdrawDao.updataOrderStatus(wit.getOrderId(), wit.getApproval(), wit.getComment(), Common.Order.Wit.ORDER_STATUS_SU, wit.getChennelId());
        if (a == 0 || a > 2) {
            return Result.buildFailMessage("订单状态修改失败");
        }
        wit.setWitChannel(wit.getChennelId());
        //结算实际出款渠道
        UserFund channel = new UserFund();
        channel.setUserId(wit.getChennelId());
        channelWitSu(wit.getOrderId(), wit, wit.getRetain2(), channel);
        agentDpayChannel(wit, wit.getRetain2(), wit.getWitType(), false);//新加代付代理商结算
        notifyUtil.wit(wit.getOrderId());//通知
        UserFund userfund = new UserFund();
        userfund.setUserId(wit.getUserId());
        amountPublic.witStatis(userfund, wit.getAmount(), wit.getOrderId());
        return Result.buildSuccessMessage("代付成功");
    }

    /**
     * <p>三方系统回调成功</p>
     *
     * @param wit
     * @return
     */
    @Transactional
    public Result withrawOrderSu1(Withdraw wit) {
        int a = withdrawDao.updataOrderStatus(wit.getOrderId(), wit.getApproval(), wit.getComment(), Common.Order.Wit.ORDER_STATUS_SU, wit.getChennelId());
        if (a == 0 || a > 2) {

            return Result.buildFailMessage("订单状态修改失败");
        }
        UserFund userfund = new UserFund();
        userfund.setUserId(wit.getUserId());
        //  amountPublic.witStatis(userfund, wit.getAmount(), wit.getOrderId());
        return Result.buildSuccessMessage("代付成功");
    }


    /**
     * <p>新建代付订单时候账户扣款</p>
     *
     * @param orderId 代付订单号
     * @return
     */
    public Result withrawOrder(String orderId, String ip, Boolean flag) {
        if (StrUtil.isBlank(orderId)) {
            return Result.buildFailMessage("必传参数为空");
        }
        Withdraw wit = withdrawDao.findWitOrder(orderId);
        UserFund userFund = new UserFund();
        userFund.setUserId(wit.getUserId());
        Result witFrzen = transactionTemplate.execute((Result) -> {
            Result withdraw = amountPublic.deleteWithdraw(userFund, wit.getAmount(), wit.getOrderId());
            if (!withdraw.isSuccess()) {
                return withdraw;
            }
            Result deleteAmount = amountRunUtil.deleteAmount(wit, ip, flag, "卡商代付冻结");
            if (!deleteAmount.isSuccess()) {
                return deleteAmount;
            }
            return deleteAmount;
        });
        if (!witFrzen.isSuccess()) {
            log.info("【代付冻结失败，当前订单号：" + orderId + "】");
            return witFrzen;
        }
        Result witFeeFrzen = transactionTemplate.execute((Result) -> {
            Result withdraws = amountPublic.deleteBankprofit(userFund, wit.getFee(), wit.getOrderId());
            if (!withdraws.isSuccess()) {
                return withdraws;
            }
            Result deleteAmountFee = amountRunUtil.deleteAmountFee(wit, ip, flag, "卡商代付手续费冻结");
            if (!deleteAmountFee.isSuccess()) {
                return deleteAmountFee;
            }
            return deleteAmountFee;
        });
        if (!witFeeFrzen.isSuccess()) {
            log.info("【代付手续费冻结失败，当前订单号：" + orderId + "】");
            return witFrzen;
        }
        if ("2".equals(wit.getWithdrawType()) && "2".equals(wit.getRetain1())) {
            //当前订单为卡商代付订单，且卡商代付订单提款为 分润提款
            amountPublic.witBankCardAmount(wit);
            amountPublic.witBankCardAmountFee(wit);
            amountRunUtil.witBankCardAmount(wit);
        }
        return Result.buildSuccess();
    }

    /**
     * <p>商户订单结算</p>
     *
     * @param orderId
     * @return
     */
    public Result enterOrderApp(String orderId, String ip, Boolean flag) {
        log.info("【进入商户订单结算方法】");
        if (StrUtil.isBlank(orderId)) {
            return Result.buildFailMessage("必传参数为空");
        }
        Future<DealOrderApp> dealOrderAppFuture = ThreadUtil.execAsync(() -> {
            return dealOrderAppDao.findOrderByOrderId(orderId);
        });
        DealOrderApp orderApp = null;
        try {
            orderApp = dealOrderAppFuture.get();
        } catch (Exception e) {
            log.error("系统异常：", e);
            orderApp = dealOrderAppDao.findOrderByOrderId(orderId);
        }
        if (ObjectUtil.isNull(orderApp)) {
            return Result.buildFailMessage("当前订单号不存在");
        }
        if (!orderApp.getOrderStatus().toString().equals(Common.Order.DealOrderApp.ORDER_STATUS_DISPOSE)) {
            throw new OrderException("当前状态不允许操作", null);
        }
        boolean status = dealOrderAppDao.updateOrderSu(orderId, Common.Order.DealOrderApp.ORDER_STATUS_SU);
        if (!status) {
            return Result.buildFail();
        }
        Integer feeId = orderApp.getFeeId();
        String appId = orderApp.getOrderAccount();
        UserFund userFund = new UserFund();
        userFund.setUserId(appId);

        DealOrderApp finalOrderApp1 = orderApp;
        transactionTemplate.execute((Result) -> {
            Result addDealApp = amountPublic.addDealApp(userFund, finalOrderApp1.getOrderAmount(), finalOrderApp1.getOrderId());
            if (!addDealApp.isSuccess()) {
                throw new OrderException("商户结算失败", null);
            }
            Result addDealAmountApp = amountRunUtil.addDealAmountApp(finalOrderApp1, ip, flag);
            if (!addDealAmountApp.isSuccess()) {
                throw new OrderException("商户流水失败", null);
            }
            return addDealAmountApp;
        });
        Future<UserRate> userRateFuture = ThreadUtil.execAsync(() -> {
            return userRateDao.findFeeById(feeId);
        });
        UserRate findFeeById = null;
        try {
            findFeeById = userRateFuture.get();
        } catch (Exception e) {
            log.error("系统异常：查询费率异常：", e);
            findFeeById = userRateDao.findFeeById(feeId);
        }
        BigDecimal fee = findFeeById.getFee();
        BigDecimal multiply = orderApp.getOrderAmount().multiply(fee);
        log.info("【当前商户结算费率：" + fee + "，当前商户交易金额：" + orderApp.getOrderAmount() + "，当前商户收取交易手续费：" + multiply + "】");
        DealOrderApp finalOrderApp2 = orderApp;
        Result executeApp = transactionTemplate.execute((Result) -> {
            Result deleteDeal = amountPublic.deleteDeal(userFund, multiply, finalOrderApp2.getOrderId());
            if (!deleteDeal.isSuccess()) {
                throw new OrderException("手续费扣减异常", null);
            }
            Result feeApp = amountRunUtil.deleteDealAmountFeeApp(finalOrderApp2, ip, flag, multiply);
            return feeApp;
        });
        ThreadUtil.execute(() -> {
            log.info("【对当前商户订单的代理商进行结算】");
            agentDealPay(finalOrderApp2, flag, ip);
        });
        if (executeApp.isSuccess()) {
            return executeApp;
        }
        throw new OrderException("商户订单成功异常", null);
    }

    /**
     * <p>商户代理商结算</p>
     *
     * @param orderApp 商户订单
     * @return
     */
    boolean agentDealPay(DealOrderApp orderApp, boolean flag, String ip) {
        String appId = orderApp.getOrderAccount();
        UserInfo userInfo = userInfoServiceImpl.findUserAgent(appId);
        if (StrUtil.isBlank(userInfo.getAgent())) {
            log.info("【当前账户无代理商，不进行结算】");
            boolean flag1 = dealOrderAppDao.updateOrderIsAgent(orderApp.getOrderId(), "YES");
            return true;
        }
        Integer feeId = orderApp.getFeeId();
        UserRate findFeeById = userRateDao.findFeeById(feeId);


        Result findUserRateList = findUserRateList(userInfo.getAgent(), findFeeById.getPayTypr(), findFeeById.getChannelId(), findFeeById, orderApp, flag, ip);
        if (findUserRateList.isSuccess()) {
            log.info("【当前订单代理商结算成功】");
            boolean flag1 = dealOrderAppDao.updateOrderIsAgent(orderApp.getOrderId(), "YES");
            return true;
        }

        return false;
    }

    private Result findUserRateList(String agent, String product, String channelId, UserRate rate, DealOrderApp orderApp, boolean flag, String ip) {
        UserInfo userInfo = userInfoServiceImpl.findUserAgent(agent);
        UserRate userRate = userRateDao.findProductFeeBy(agent, product, channelId);
        log.info("【当前代理商为：" + userRate.getUserId() + "】");
        log.info("【当前代理商结算费率：" + userRate.getFee() + "】");
        log.info("【当前当前我方：" + rate.getUserId() + "】");
        log.info("【当前我方结算费率：" + rate.getFee() + "】");
        BigDecimal fee = userRate.getFee();
        BigDecimal fee2 = rate.getFee();
        BigDecimal subtract = fee2.subtract(fee);
        log.info("【当前结算费率差为：" + subtract + "】");
        BigDecimal amount = orderApp.getOrderAmount();
        BigDecimal multiply = amount.multiply(subtract);
        log.info("【当前结算订单金额为：" + amount + "，当前结算代理分润为：" + multiply + "】");
        log.info("【开始结算】");

        UserFund fund = new UserFund();
        fund.setUserId(agent);
        Result addAmounProfit = amountPublic.addAmounProfit(fund, multiply, orderApp.getOrderId());
        if (addAmounProfit.isSuccess()) {
            Result addAppProfit = amountRunUtil.addAppProfit(orderApp.getOrderId(), fund.getUserId(), multiply, ip, flag);
            if (addAppProfit.isSuccess()) {
                log.info("【流水成功】");
                if (StrUtil.isNotBlank(userInfo.getAgent())) {
                    return findUserRateList(userInfo.getAgent(), product, channelId, userRate, orderApp, flag, ip);
                }
            }
        }
        return Result.buildSuccessMessage("结算成功");
    }

    public Result withrawOrderErBySystem(String orderId, String ip, String msg) {
        Withdraw order = withdrawDao.findWitOrder(orderId);
        if (order == null) {
            return Result.buildFailMessage("平台订单号不存在");
        }
        if (!Common.Order.Wit.ORDER_STATUS_PUSH.equals(order.getOrderStatus())) {
            return Result.buildFailMessage("订单已被处理，不允许操作");
        }
        return withrawOrderErBySystem(order, ip, msg);
    }

    /*
     *
     * @param wit
     * @param ip
     * @return
     */
    public Result withrawOrderErBySystem(Withdraw wit, String ip, String msg) {
        /*
         * ###########################
         * 代付失败给该用户退钱
         */
        int a = withdrawDao.updataOrderStatusEr(wit.getOrderId(),
                Common.Order.Wit.ORDER_STATUS_ER, msg);
        if (a == 0 || a > 2) {
            return Result.buildFailMessage("订单状态修改失败");
        }
        UserFund userFund = new UserFund();
        userFund.setUserId(wit.getUserId());
        Result addTransaction =   transactionTemplate.execute((Result) -> {
                    Result addAmountAdd = amountPublic.addAmountAdd(userFund, wit.getAmount(), wit.getOrderId());
                    if (!addAmountAdd.isSuccess()) {
                        return addAmountAdd;
                    }
                    Result addAmountW = amountRunUtil.addAmountW(wit, ip);
                    if (!addAmountW.isSuccess()) {
                        return addAmountW;
                    }
                    return addAmountW;
                });
        if(!addTransaction.isSuccess()){
            return addTransaction;
        }
        Result addFeeTransaction =   transactionTemplate.execute((Result) -> {
                    Result addFee = amountPublic.addAmountAdd(userFund, wit.getFee(), wit.getOrderId());
                    if (!addFee.isSuccess()) {
                        return addFee;
                    }
                    Result addAmountWFee = amountRunUtil.addAmountWFee(wit, ip);
                    if (!addAmountWFee.isSuccess()) {
                        return addAmountWFee;
                    }
                    return addAmountWFee;
                });
            if(!addFeeTransaction.isSuccess()){
                return addFeeTransaction;
            }
            notifyUtil.wit(wit.getOrderId());
        return Result.buildSuccessMessage("代付金额解冻成功");
    }

    /**
     * <p>代付失败</p>
     *
     * @return
     */
    public Result withrawOrderEr(Withdraw wit, String ip) {
        /**
         * ###########################
         * 代付失败给该用户退钱
         */
        Withdraw witd = wit;
        String userId = wit.getUserId();

        if (!"2".equals(wit.getWithdrawType())) {
            if (wit.getOrderStatus().equals(Common.Order.Wit.ORDER_STATUS_SU)) {
                //1，订单成功时候的时候除了退换商户资金还会对渠道账户进行扣款操作
                //2，对实际出款订单和配置出款订单加加款进行区分
                if (StrUtil.isNotBlank(witd.getChennelId())) {//配置出款
                    //按照配置的出款费率给渠道退款
                    channelWitEr(witd, witd.getChennelId());
                } else {//手动推送出款
                    channelWitEr(witd, witd.getWitChannel());
                }
            }
        }
        int a = withdrawDao.updataOrderStatus1(wit.getOrderId(), wit.getApproval(), wit.getComment(), Common.Order.Wit.ORDER_STATUS_ER);
        if (a == 0 || a > 2) {
            return Result.buildFailMessage("订单状态修改失败");
        }
        witd.setUserId(userId);
        UserFund userFund = new UserFund();
        userFund.setUserId(wit.getUserId());
        Result addTransaction = transactionTemplate.execute((Result) -> {
            Result addAmountAdd = amountPublic.addAmountAdd(userFund, wit.getAmount(), wit.getOrderId());
            if (!addAmountAdd.isSuccess()) {
                        return addAmountAdd;
                    }
                    Result addAmountW = amountRunUtil.addAmountW(wit, ip);
                    if (!addAmountW.isSuccess()) {
                        return addAmountW;
                    }
                    return addAmountW;
                });
        if(!addTransaction.isSuccess()){
            return  addTransaction;
        }
        Result addFeeTransaction =   transactionTemplate.execute((Result) -> {
                    Result result = amountPublic.addAmountAdd(userFund, wit.getFee(), wit.getOrderId());
                    if (!result.isSuccess()) {
                        return result;
                    }
            Result result1 = amountRunUtil.addAmountWFee(wit, ip);
            if (!result1.isSuccess()) {
                return result1;
            }
            return result1;
        });
        if (!addFeeTransaction.isSuccess()) {
            return addFeeTransaction;
        }

        if ("2".equals(wit.getWithdrawType()) && "2".equals(wit.getRetain1())) {//卡商佣金退回
            Result result = amountPublic.addBankprofit(wit);
            if (!result.isSuccess()) {
                return result;
            }
            Result result1 = amountRunUtil.addBankprofit(wit);
            if (!result1.isSuccess()) {
                return result1;
            }
        }


        notifyUtil.wit(wit.getOrderId());
        return Result.buildSuccessMessage("代付金额解冻成功");
    }


    public Result settlementWit(Withdraw wit) {
        UserFund userFund = new UserFund();
        userFund.setUserId(wit.getWitChannel());
        Result result1 = channelWitSu(wit.getOrderId(), wit, wit.getRetain2(), userFund);
        if (result1.isSuccess()) {
            Result result = agentDpayChannel(wit, wit.getRetain2(), true);
            if (result.isSuccess()) {
                UserFund userFundApp = new UserFund();
                userFundApp.setUserId(wit.getUserId());
                amountPublic.witStatis(userFundApp, wit.getAmount(), wit.getOrderId());
            }
            return result;
        }
        return result1;
    }
    /**
     * <p>代付成功渠道结算</p>
     *
     * @param orderId 代付订单
     * @param wit     代付订单实体类
     * @param ip      代付订单ip
     * @param channel 结算代付渠道账户
     * @return
     */
    public Result channelWitSu(String orderId, Withdraw wit, String ip, UserFund channel) {
        Result addTransaction =   transactionTemplate.execute((Result) -> {
                    log.info("【当前代付订单成功，代付订单号为：" + orderId + "，对代付渠道进行加款操作】");
                    Result addAmountAdd = amountPublic.addAmountAdd(channel, wit.getAmount(), wit.getOrderId());
                    if (addAmountAdd.isSuccess()) {
                        log.info("【当前代付渠道账户加款成功，代付订单号为：" + orderId + "，生成渠道加款流水】");
                    } else {
                        log.info("【当前代付渠道账户加款【失败】，代付订单号为：" + orderId + "，加款渠道为：" + wit.getOrderId() + "】");
                    }
                    if(!addAmountAdd.isSuccess()){
                        return addAmountAdd;
                    }
                    Result addChannelWit = amountRunUtil.addChannelWit(wit, ip);
                    if (addChannelWit.isSuccess()) {
                        log.info("【当前代付渠道账户加款流水成功，代付订单号为：" + orderId + "，生成渠道加款流水】");
                    } else {
                        log.info("【当前代付渠道账户加款流水【失败】，代付订单号为：" + orderId + "，加款渠道为：" + wit.getOrderId() + "】");
                    }
                    return addChannelWit;

                });
        if(!addTransaction.isSuccess()){
            return addTransaction;
        }
        ChannelFee findChannelFee = channelFeeDao.findChannelFee(wit.getWitChannel(), wit.getWitType());
        Result addChanenlTransaction =   transactionTemplate.execute((Result) -> {
            String channelDFee = findChannelFee.getChannelDFee();
            log.info("【当前渠道代付手续费为：" + channelDFee + " 】");
            Result add = amountPublic.addAmountAdd(channel, new BigDecimal(channelDFee), wit.getOrderId());
            log.info("【渠道账户记录代付手续费为：" + channelDFee + " 】");
            if (add.isSuccess()) {
                log.info("【当前代付渠道账户取款手续费加款成功，代付订单号为：" + orderId + "，生成渠道加款手续费流水】");
            } else {
                log.info("【当前代付渠道账户取款手续费加款【失败】，代付订单号为：" + orderId + "，加款渠道为：" + wit.getOrderId() + "】");
            }
            if(!add.isSuccess()){
                return  add;
            }
            Result addChannelWitFee = amountRunUtil.addChannelWitFee(wit, ip, new BigDecimal(channelDFee));
            if (addChannelWitFee.isSuccess()) {
                log.info("【当前代付渠道账户取款手续费加款流水成功，代付订单号为：" + orderId + "，生成渠道加款流水】");
            } else {
                log.info("【当前代付渠道账户取款手续费加款流水【失败】，代付订单号为：" + orderId + "，加款渠道为：" + wit.getOrderId() + "】");
            }
            if(!addChannelWitFee.isSuccess()){
                return  addChannelWitFee;
            }
            return  addChannelWitFee;
        });
        if (addChanenlTransaction.isSuccess() && addTransaction.isSuccess()) {
            return Result.buildSuccess();
        } else {
            return Result.buildFail();
        }
    }
    /**
     * 自动结算
     *
     * @param wit
     * @param ip
     * @param flag1
     * @return
     */
    public Result agentDpayChannel(Withdraw wit, String ip, boolean flag1) {
        return agentDpayChannel(wit, true, null, ip, flag1);
    }
    /**
     * 手动结算
     *
     * @param wit
     * @param ip
     * @param product
     * @param flag1
     * @return
     */
    public Result agentDpayChannel(Withdraw wit, String ip, String product, boolean flag1) {
        return agentDpayChannel(wit, false, product, ip, flag1);
    }
    /**
     * 【当前为代付代理商结算     配置渠道结算的时候  出款渠道费率为配置出款渠道   实际出款渠道时候，当前出款渠道为实际出款】
     * 代理商代付利润结算
     *
     * @param wit         代付订单
     * @param flag        是否手动推送出款  true 配置出款渠道结算          false    实际出款渠道
     * @param trueProduct 实际出款渠道
     * @param ip          代父ip
     * @param flag1       是否手动
     * @return
     */
    private Result agentDpayChannel(Withdraw wit, Boolean flag, String trueProduct, String ip, boolean flag1) {
        String channelId = "";
        String product = "";
        if (flag) {
            channelId = wit.getWitChannel();
            product = wit.getWitType();
        } else {
            channelId = wit.getChennelId();//实际出款渠道
            product = trueProduct;//实际出款产品
        }
        UserRate userRate = userRateDao.findProductFeeByAll(wit.getUserId(), product, channelId);//查询费率情况
        final String finalChannelId = channelId;
        final String finalProduct = product;
        UserInfo userInfo = userInfoServiceImpl.findUserAgent(wit.getUserId());
        if (StrUtil.isNotBlank(userInfo.getAgent())) {
            ThreadUtil.execute(() -> {
                witAgent(wit, userInfo.getAgent(), finalProduct, finalChannelId, userRate, ip, flag1);
            });
        }
        return Result.buildSuccessMessage("代付代理商结算");
    }
    private Result witAgent(Withdraw wit, String username, String product, String channelId, UserRate rate, String ip, boolean flag) {
        UserRate userRate = userRateDao.findProductFeeByAll(username, product, channelId);//查询当前代理费率情况
        UserInfo userInfo = userInfoServiceImpl.findUserAgent(username);
        log.info("【当前代理商为：" + userRate.getUserId() + "】");
        log.info("【当前代理商结算费率：" + userRate.getFee() + "】");
        log.info("【当前当前我方：" + rate.getUserId() + "】");
        log.info("【当前我方结算费率：" + rate.getFee() + "】");
        BigDecimal fee = userRate.getFee();//代理商的费率
        BigDecimal fee2 = rate.getFee();//商户的费率
        BigDecimal subtract = fee2.subtract(fee);//
        log.info("【当前结算费率差为：" + subtract + "】");
        log.info("【当前结算费率差为代理商分润：" + subtract + "】");//这个钱要加给代理商
        log.info("【开始结算】");
        UserFund fund = new UserFund();
        fund.setUserId(username);
        Result addAmounProfit = amountPublic.addAmounProfit(fund, subtract, wit.getOrderId());
        if (addAmounProfit.isSuccess()) {
            amountRunUtil.addWitFee(fund, subtract, wit, ip, flag, rate.getUserId());
        }
        if (StrUtil.isNotBlank(userInfo.getAgent())) {
            witAgent(wit, userInfo.getAgent(), product, channelId, userRate, ip, flag);
        }
        return Result.buildSuccessMessage("结算成功");
    }


    Result channelWitEr(Withdraw wit, String userId) {
        UserFund userFund = new UserFund();
        userFund.setUserId(userId);
        Result add =   transactionTemplate.execute((Result) -> {
           Result result1 = amountPublic.addAmountAdd(userFund, wit.getActualAmount(), wit.getOrderId());
           if (!result1.isSuccess()) {
               log.info("【代付订单置为失败渠道账户加减款异常，请详细查看原因，当前代付订单号：" + wit.getOrderId() + "】");
               return result1;
           }
           wit.setUserId(userId);
           Result addAmountW = amountRunUtil.addAmountW(wit, wit.getRetain2());
           if (!addAmountW.isSuccess()) {
               log.info("【代付订单置为失败渠道账户加减款流水生成异常，请详细查看原因，当前代付订单号：" + wit.getOrderId() + "】");
               return addAmountW;
           }
           return addAmountW;
       });
        if(!add.isSuccess()){
            return  add;
        }
        ChannelFee findChannelFee = channelFeeDao.findChannelFee(userId, wit.getWitType());
        Result execute = transactionTemplate.execute((Result) -> {
            Result result = amountPublic.addAmountAdd(userFund, new BigDecimal(findChannelFee.getChannelDFee()), wit.getOrderId());
            if (!result.isSuccess()) {
                log.info("【代付订单置为失败渠道账户手续费加减款生成异常，请详细查看原因，当前代付订单号：" + wit.getOrderId() + "】");
                return result;
            }
            Result result2 = amountRunUtil.addAmountChannelWitEr(wit, wit.getRetain2(), new BigDecimal(findChannelFee.getChannelDFee()));
            if (!result2.isSuccess()) {
                log.info("【代付订单置为失败渠道账户手续费加减款流水生成异常，请详细查看原因，当前代付订单号：" + wit.getOrderId() + "】");
                return result2;
            }
            return result2;
        });
        if(!execute.isSuccess()){
            return  execute;
        }
        return Result.buildSuccessMessage("渠道退款成功");
    }
    @Autowired
    private RunOrderService runOrderServiceImpl;
    /**
     *
     * 当前卡商出款交易订单回滚
     * @param order
     * @param username
     * @param clientIP
     * @return
     */
     private static final String AMOUNT_TYPE_R = "0";//对于当前账户来说是   收入
     public Result backOrder(DealOrder order,  String clientIP) {
        log.info("【进入卡商代付订单回滚方法，当前卡商代付订单号："+order.getOrderId()+"】");
        /**
         * 1，先查看是否有结算流水
         * 1，如果有结算，按照结算流水退回，订单失败即可
         * 1，如果没有结算，直接将订单失败
         */
        List<RunOrder> assOrder = runOrderServiceImpl.findAssOrder(order.getOrderId());
        for(RunOrder run : assOrder){
            if(run.getRunOrderType()==40){//流水类型为  卡商资金退回
                return Result.buildSuccessMessage("渠道退款成功");
            }
        }
        for(RunOrder run    : assOrder){
            UserFund userFund = new UserFund();
            userFund.setUserId(run.getOrderAccount());
            String amountType = run.getAmountType();
            if(run.getRunOrderType().equals(34)){
                if(AMOUNT_TYPE_R.equals(amountType)){//当前流水为收入流水 ， 现在我们要处理该笔流水为 支出
                    Result result1 = amountPublic.deleteAmount(userFund, run.getAmount(), run.getOrderId());
                    if (!result1.isSuccess()) {
                        log.info("【当前单笔资金退回出错，请详细查看原因，当前代付订单号：" + order.getOrderId() + "】");
                        return result1;
                    }
                    Result result2 = amountRunUtil.deleteBackBank(userFund.getUserId(),order.getOrderId(),run.getAmount(),clientIP);
                    if (!result2.isSuccess()) {
                        log.info("【当前单笔资金退回出错，请详细查看原因，当前代付订单号：" + order.getOrderId() + "】");
                        return result2;
                    }
                }else{//当前流水为 收入流水 现在我们处理该笔流水为  收入
                    Result result1 = amountPublic.addAmountAdd(userFund, run.getAmount(), run.getOrderId());
                    if (!result1.isSuccess()) {
                        log.info("【当前单笔资金退回出错，请详细查看原因，当前订单号：" + order.getOrderId() + "】");
                        return result1;
                    }
                    Result result2 = amountRunUtil.addBackBank(userFund.getUserId(),order.getOrderId(),run.getAmount(),clientIP);
                    if (!result2.isSuccess()) {
                        log.info("【当前单笔资金退回出错，请详细查看原因，当前代付订单号：" + order.getOrderId() + "】");
                        return result2;
                    }
                }
            }else{
                if(AMOUNT_TYPE_R.equals(amountType)){//当前流水为收入流水 ， 现在我们要处理该笔流水为 支出
                    Result result1 = amountPublic.deleteBankprofit(userFund, run.getAmount(), run.getOrderId());
                    if (!result1.isSuccess()) {
                        log.info("【当前单笔资金退回出错，请详细查看原因，当前代付订单号：" + order.getOrderId() + "】");
                        return result1;
                    }
                    Result result2 = amountRunUtil.deleteBackBank(userFund.getUserId(),order.getOrderId(),run.getAmount(),clientIP);
                    if (!result2.isSuccess()) {
                        log.info("【当前单笔资金退回出错，请详细查看原因，当前代付订单号：" + order.getOrderId() + "】");
                        return result2;
                    }
                }else{//当前流水为 收入流水 现在我们处理该笔流水为  收入
                    Result result1 = amountPublic.addAmountAdd(userFund, run.getAmount(), run.getOrderId());
                    if (!result1.isSuccess()) {
                        log.info("【当前单笔资金退回出错，请详细查看原因，当前订单号：" + order.getOrderId() + "】");
                        return result1;
                    }
                    Result result2 = amountRunUtil.addBackBank(userFund.getUserId(),order.getOrderId(),run.getAmount(),clientIP);
                    if (!result2.isSuccess()) {
                        log.info("【当前单笔资金退回出错，请详细查看原因，当前代付订单号：" + order.getOrderId() + "】");
                        return result2;
                    }
                }
            }
        }
        ThreadUtil.execute(()->{//这里失败之后 直接讲业务余额扣减
            orderServiceImpl.updateOrderStatus(order.getOrderId(), Common.Order.DealOrderApp.ORDER_STATUS_ER,"代付回滚");
            String orderQr = order.getOrderQr();
            String[] split = orderQr.split(":");
            String bankno =   split[2];
            mediumServiceImpl.updateMountWit(bankno,order.getDealAmount().toString());
        });
        return Result.buildSuccessMessage("渠道退款成功");
    }
}
