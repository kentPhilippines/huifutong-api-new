package alipay.manage.util.amount;

import alipay.manage.bean.*;
import alipay.manage.service.RunOrderService;
import alipay.manage.service.UserInfoService;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.bean.dealpay.Recharge;
import otc.bean.dealpay.Withdraw;
import otc.exception.user.UserException;
import otc.result.Result;

import java.math.BigDecimal;

/**
 * <p>资金流水处理类</p>
 *
 * @author hx08
 */
@Component
public class AmountRunUtil {
    /**
     * ######################################
     * <p>流水处理接口</p>
     * 1，人工加款接口				已完成
     * 2，人工扣款接口				已完成
     * 3，交易减少充值点数接口		已完成
     * 4，交易增加交易分润接口		已完成
     * 5，充值						已完成
     * 6，提现						已完成
     */
    Logger log = LoggerFactory.getLogger(AmountRunUtil.class);
    private static final String SYSTEM_APP = "SYSTEM_APP";//系统账户
    @Autowired private UserInfoService userInfoServiceImpl;
    @Autowired
    private RunOrderService runOrderServiceImpl;
    @Autowired
    private AmountPublic amountPublic;
    private static final String RUN = "RUN";
    private static final String ADD_AMOUNT = "ADD_AMOUNT";//人工加钱
    private static final Integer ADD_AMOUNT_NUMBER = 17;
    private static final String DETETE_AMOUNT = "DETETE_AMOUNT";//人工扣钱
    private static final Integer DETETE_AMOUNT_NUMBER = 18;//人工扣钱
    private static final String DEAL_AMOUNT_DETETE = "DEAL_AMOUNT_DETETE";//交易时交易点数扣除
    private static final Integer DEAL_AMOUNT_DETETE_NUMBER = 12;//交易时交易点数扣除
    private static final String PROFIT_AMOUNT_DEAL = "PROFIT_AMOUNT_DEAL";//自己账号交易时产生的分润
    private static final String PROFIT_AMOUNT_AGENT = "PROFIT_AMOUNT_AGENT";//自己作为代理商的分润
    private static final Integer PROFIT_AMOUNT_AGENT_NUMBER = 13;//自己作为代理商的分润
    private static final Integer PROFIT_AMOUNT_DEAL_NUMBER = 14;//自己账号交易时产生的分润
    private static final String RECHANGE_AMOUNT = "RECHANGE_AMOUNT";//充值 及 增加交易点数
    private static final Integer RECHANGE_AMOUNT_NUMBER = 11;//充值 及  增加交易点数
    private static final String WITHDRAY_AMOUNT = "WITHDRAY_AMOUNT";//代付冻结
    private static final String WITHDRAY_AMOUNT_FEE = "WITHDRAY_AMOUNT_FEE";//代付手续费冻结
    private static final Integer WITHDRAY_AMOUNT_NUMBER = 10;//代付冻结
    private static final Integer WITHDRAY_AMOUNT_FEE_NUMBER = 9;//代付手续费冻结
    private static final String WITHDRAY_AMOUNT_OPEN_FEE = "WITHDRAY_AMOUNT_OPEN_FEE";//代付失败解冻
    private static final Integer WITHDRAY_AMOUNT_OPEN_FEE_NUMBER = 22;//代付失败手续费解冻
    private static final String WITHDRAY_AMOUNT_OPEN = "WITHDRAY_AMOUNT_OPEN";//代付失败解冻
    private static final Integer WITHDRAY_AMOUNT_OPEN_NUMBER = 8;//代付失败解冻

    private static final String ADD_DEAL_AMOUNT_APP = "ADD_DEAL_AMOUNT_APP";//下游商户交易加款
    private static final String DELETE_DEAL_FEE_AMOUNT_APP = "ADD_DEAL_FEE_AMOUNT_APP";//下游商户交易手续费扣款
    private static final Integer ADD_DEAL_AMOUNT_APP_NUMBER = 20;// 下游商户交易加款编号
    private static final Integer DELETE_DEAL_FEE_AMOUNT_APP_NUMBER = 21;// 下游商户交易手续费扣款编号
    private static final String CHANNEL_ADD_R = "CHANNEL_ADD_R";// 渠道手续费标识
    private static final Integer CHANNEL_ADD_R_NUMBER = 24;// 渠道手续费编号
    private static final Integer CHANNEL_ADD_WIT_NUMBER = 23;// 渠道代付加款成功标识
    private static final Integer CHANNEL_ADD_WIT_FEE_NUMBER = 25;// 渠道代付加款成功标识手续费
    private static final Integer AGNEET_WIT_FRREE_NUMBER = 26;// 商户代理商代付利润结算
    private static final String AGNEET_WIT_FRREE = "AGNEET_WIT_FRREE";// 商户代理商代付利润结算
    private static final String CHANNEL_ADD_WIT = "CHANNEL_ADD_WIT";// 渠道代付成功标识
    private static final String CHANNEL_ADD_WIT_FEE = "CHANNEL_ADD_WIT_FEE";// 渠道代付成功标识手续费


    private static final String DELETE_FREEZE = "DELETE_FREEZE";// 商户金额冻结
    private static final String ADD_FREEZE = "ADD_FREEZE";// 商户金额解冻
    private static final Integer DELETE_FREEZE_NUMBER = 27;// 商户金额冻结
    private static final Integer ADD_FREEZE_NUMBER = 28;// 商户金额解冻


    private static final String DELETE_QUOTA = "DELETE_QUOTA";// 商户金额冻结
    private static final String ADD_QUOTA = "ADD_QUOTA";// 商户金额解冻
    private static final Integer DELETE_QUOTA_NUMBER = 29;// 商户金额冻结
    private static final Integer ADD_QUOTA_NUMBER = 30;// 商户金额解冻


    private static final String AMOUNT_TYPE_R = "0";//对于当前账户来说是   收入
    private static final String AMOUNT_TYPE_W = "1";//对于当前账户来说是   支出

    private static final String RUNTYPE_ARTIFICIAL = "2";//人工流水
    private static final String RUNTYPE_NATURAL = "1";//自然流水


    private static final String USDT_GAS_FEE = "USDT_GAS_FEE";// USDT结算矿工手续费
    private static final Integer USDT_GAS_FEE_NUMBER = 31;// USDT结算矿工手续费


    private static final String DETETE_AMOUNT_TRANSTER = "DETETE_AMOUNT_TRANSTER";//转账减款
    private static final Integer DETETE_AMOUNT_TRANSTER_NUMBER = 32;//转账减款
    private static final String ADD_AMOUNT_TRANSTER = "ADD_AMOUNT_TRANSTER";//转账加款
    private static final Integer ADD_AMOUNT_TRANSTER_NUMBER = 33;//转账加款


    private static final Integer RECHANGE_AMOUNT_NUMBER_WIT_NUMBER = 34;
    private static final String RECHANGE_AMOUNT_NUMBER_WIT = "RECHANGE_AMOUNT_NUMBER_WIT";
    private static final String RECHANGE_AMOUNT_NUMBER_WIT_FEE = "RECHANGE_AMOUNT_NUMBER_WIT_FEE";
    private static final Integer RECHANGE_AMOUNT_NUMBER_WIT_FEE_NUMBER = 35;


    private static final String PROFIT_AMOUNT_AGENT_BANK_R = "PROFIT_AMOUNT_AGENT_BANK_R";
    private static final Integer PROFIT_AMOUNT_AGENT_BANK_R_NUMBER = 36;


    private static final String PROFIT_AMOUNT_AGENT_BANK_W = "PROFIT_AMOUNT_AGENT_BANK_W";
    private static final Integer PROFIT_AMOUNT_AGENT_BANK_W_NUMBER = 37;


    private static final Integer PROFIT_AMOUNT_BANK_W_NUMBER = 38;//卡商佣金扣减
    private static final Integer PROFIT_AMOUNT_BANK_R_NUMBER = 39;//卡商佣金扣除
    private static final Integer PROFIT_AMOUNT_BANK_B_NUMBER = 41;//卡商佣金退回
    public static final Integer BANK_CARD_BACK_NUMBER = 40;//卡商流水退回
    private static final String PROFIT_AMOUNT_BANK_W = "PROFIT_AMOUNT_BANK_W";
    private static final String BANK_CARD_BACK = "BANK_CARD_BACK";
    private static final String PROFIT_AMOUNT_BANK_R = "PROFIT_AMOUNT_BANK_R";
    private static final String PROFIT_AMOUNT_BANK_B = "PROFIT_AMOUNT_BANK_B";









    /**
     * <p>码商代付流水生成</p>
     *
     * @param withdraw     代付订单表
     * @param generationIp ip
     * @param flag         true 自然流水     false  人工流水
     * @return
     */
    public Result deleteAmount(Withdraw withdraw, String generationIp, Boolean flag, String dealDescribe) {
        Result delete = delete(WITHDRAY_AMOUNT, withdraw.getUserId(), withdraw.getOrderId(), withdraw.getActualAmount(), generationIp, dealDescribe, flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL);
        if (delete.isSuccess()) {
            return delete;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    public Result deleteAmount(Withdraw withdraw, String generationIp, Boolean flag) {
        return deleteAmount(withdraw, generationIp, flag, "账户代付冻结");
    }

    /**
     * 商户代付代理费用结算
     *
     * @param subtract 金额
     * @param wit      代付订单
     * @param userId   我的下级代理账号
     * @return
     */
    public Result addWitFee(UserFund userFund1, BigDecimal subtract, Withdraw wit, String generationIp, boolean flag, String userId) {
        return add(AGNEET_WIT_FRREE, userFund1.getUserId(), wit.getOrderId(), subtract, generationIp, "代付代理商分润", flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL, userId);
    }

    /**
     * <p>代付失败解冻</p>
     *
     * @param withdraw     代付订单
     * @param generationIp 操作ip
     * @return
     */
    public Result addAmountW(Withdraw withdraw, String generationIp) {
        return add(WITHDRAY_AMOUNT_OPEN, withdraw.getUserId(), withdraw.getOrderId(), withdraw.getAmount(), generationIp, "代付失败解冻", RUNTYPE_ARTIFICIAL);
    }

    /**
     * 代付失败渠道退回
     * @param withdraw
     * @param generationIp
     * @param bigDecimal
     * @return
     */
    public Result addAmountChannelWitEr(Withdraw withdraw, String generationIp, BigDecimal bigDecimal) {
        return delete(WITHDRAY_AMOUNT_OPEN, withdraw.getUserId(), withdraw.getOrderId(), bigDecimal, generationIp, "代付订单失败", RUNTYPE_ARTIFICIAL);
    }

    /**
     * <p>代付失败手续费解冻</p>
     *
     * @param withdraw
     * @param generationIp
     * @return
     */
    public Result addAmountWFee(Withdraw withdraw, String generationIp) {
        return add(WITHDRAY_AMOUNT_OPEN_FEE, withdraw.getUserId(), withdraw.getOrderId(), withdraw.getFee(), generationIp, "代付失败手续费解冻", RUNTYPE_ARTIFICIAL);
    }

    /**
     * <p>代付手续费冻结</p>
     *
     * @param withdraw
     * @param generationIp
     * @param flag
     * @return
     */
    public Result deleteAmountFee(Withdraw withdraw, String generationIp, Boolean flag, String dealDescribe) {
        Result delete = delete(WITHDRAY_AMOUNT_FEE, withdraw.getUserId(), withdraw.getOrderId(), withdraw.getFee(), generationIp, dealDescribe, flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL);
        if (delete.isSuccess()) {
            return delete;
        }
        throw new UserException("账户流水异常", null);
    }

    public Result deleteAmountFee(Withdraw withdraw, String generationIp, Boolean flag) {
        return deleteAmountFee(withdraw, generationIp, flag, "账户代付手续费冻结");
    }

    /**
     * <p>充值</p>
     *
     * @param recharge     充值订单
     * @param generationIp 充值ip
     * @param flag         true 自然流水     false  人工流水
     * @return
     */
    public Result addAmount(Recharge recharge, String generationIp, Boolean flag) {
        Result add = add(RECHANGE_AMOUNT, recharge.getUserId(), recharge.getOrderId(), recharge.getActualAmount(), generationIp, "码商充值", flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL);
        if (add.isSuccess()) {
            return add;
        }
        throw new UserException("账户流水异常", null);
    }
    /**
     * <p>商户代理分润计算</p>
     *
     * @param orderId
     * @param userId
     * @param amount
     * @param generationIp
     * @param flag
     * @return
     */
    public Result addAppProfit(String orderId, String userId, BigDecimal amount, String generationIp, Boolean flag) {
        Result add = add(PROFIT_AMOUNT_AGENT, userId, orderId, amount, generationIp, "商户代理商，代理分润结算", flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL);
        return add;
    }



    /**
     * <p>增加商户交易流水</p>
     *
     * @param order        商户交易订单
     * @param generationIp 商户ip
     * @param flag         是否为人工操作
     * @return
     */
    public Result addDealAmountApp(alipay.manage.bean.DealOrderApp order, String generationIp, Boolean flag) {
        Result add = add(ADD_DEAL_AMOUNT_APP, order.getOrderAccount(), order.getOrderId(), order.getOrderAmount(), generationIp, "下游商户交易加款", flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL);
        if (add.isSuccess()) {
            return add;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    public Result deleteDealAmountFeeApp(alipay.manage.bean.DealOrderApp order, String generationIp, Boolean flag, BigDecimal fee) {
        Result delete = delete(DELETE_DEAL_FEE_AMOUNT_APP, order.getOrderAccount(), order.getOrderId(), fee, generationIp, "下游商户交易手续费扣除", flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL);
        if (delete.isSuccess()) {
            return delete;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    /**
     * <p>码商会员正常交易分润流水</p>
     *
     * @param order        交易订单
     * @param generationIp 交易ip
     * @param flag         true 自然流水     false  人工流水
     * @return
     */
    public Result addDealAmount(DealOrder order, String generationIp, Boolean flag) {
        UserRate rate = userInfoServiceImpl.findUserRateById(order.getFeeId());
        log.info("【三方卡商结算手续费流水】当前加入流水账号：" + order.getOrderQrUser() + "，当前流水金额：" + order.getDealAmount() + "，当前流水费率：" + rate.getFee() + "，");
        BigDecimal dealAmount = order.getDealAmount();
        BigDecimal fee = rate.getFee();
        BigDecimal amount = dealAmount.multiply(fee);
        Result add = add(PROFIT_AMOUNT_DEAL, order.getOrderQrUser(), order.getOrderId(), amount, generationIp, "卡商正常接单分润", flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL);
        if (add.isSuccess()) {
            return add;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    public Result addDealAmountChannel(DealOrder order, String generationIp, Boolean flag, BigDecimal amount) {
        log.info("【四方渠道结算手续费流水】当前加入流水账号：" + order.getOrderQrUser() + "，当前流水金额：" + order.getDealAmount() + "，当前流水费率：" + amount + "，");
        BigDecimal dealAmount = order.getDealAmount();
        Result add = add(PROFIT_AMOUNT_DEAL, order.getOrderQrUser(), order.getOrderId(), amount, generationIp, "卡商正常接单分润", flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL);
        if (add.isSuccess()) {
            return add;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    /**
     * <p>人工加钱</p>
     *
     * @param amount       加减款订单
     * @param generationIp 操作ip
     * @return
     */
    public Result addAmount(Amount amount, String generationIp) {
        Result add = add(ADD_AMOUNT, amount.getUserId(), amount.getOrderId(), amount.getActualAmount(),
                generationIp, amount.getDealDescribe(), RUNTYPE_ARTIFICIAL);
        if (add.isSuccess()) {
            return add;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    public Result addAmount(Amount amount, String clientIP, String descr) {
        Result add = add(ADD_AMOUNT, amount.getUserId(), amount.getOrderId(), amount.getActualAmount(),
                clientIP, descr, RUNTYPE_ARTIFICIAL);
        if (add.isSuccess()) {
            return add;
        }
        return Result.buildFailMessage("流水生成失败");
    }


    /**
     * <p>交易扣除交易点数【订单置为成功时调用该方法扣除点数】</p>
     *
     * @param order        交易订单
     * @param generationIp 操作ip
     * @param flag         true 自然流水     false  人工流水
     * @return
     */
    public Result deleteRechangerNumber(DealOrder order, String generationIp, Boolean flag) {
        Result delete = delete(DEAL_AMOUNT_DETETE, order.getOrderQrUser(), order.getOrderId(), order.getDealAmount(), generationIp,
                "交易流水,扣除用户交易点数", flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL);
        if (delete.isSuccess()) {
            return delete;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    /**
     * <p>人工扣款</p>
     *
     * @param amount       加减款订单
     * @param generationIp 操作ip
     * @return
     */
    public Result deleteAmount(Amount amount, String generationIp) {
        Result delete = delete(DETETE_AMOUNT, amount.getUserId(), amount.getOrderId(), amount.getActualAmount(),
                generationIp, amount.getDealDescribe(), RUNTYPE_ARTIFICIAL);
        if (delete.isSuccess()) {
            return delete;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    /**
     * <p>当前账户加款流水</p>
     *
     * @param associatedId
     * @param amount
     * @return
     */
    @SuppressWarnings("unused")
    private Result add(String orderType, String userId, String associatedId, BigDecimal amount, String generationIp, String dealDescribe, String runType) {
        String orderAccount, amountType, acountR, accountW;
        Integer runOrderType = null;
        BigDecimal amountNow;
        orderAccount = userId;
        amountType = AMOUNT_TYPE_R;
        acountR = orderAccount;
        accountW = SYSTEM_APP;
        runOrderType = getRunOrderType(orderType);
        Result amountRun = amountRun(associatedId, orderAccount, runOrderType, amount, generationIp, acountR, accountW, runType, amountType, dealDescribe);
        if (amountRun.isSuccess()) {
            return amountRun;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    /**
     * <p>代理上分润专用</p>
     *
     * @param orderType
     * @param associatedId
     * @param amount
     * @param generationIp
     * @param dealDescribe
     * @param runType
     * @param user
     * @return
     */
    private Result add(String orderType, String userId, String associatedId, BigDecimal amount, String generationIp, String dealDescribe, String runType, String user) {
        String orderAccount, amountType, acountR, accountW;
        Integer runOrderType = null;
        BigDecimal amountNow;
        orderAccount = userId;
        amountType = AMOUNT_TYPE_R;
        acountR = orderAccount;
        accountW = SYSTEM_APP;
        runOrderType = getRunOrderType(orderType);
        Result amountRun = amountRun(associatedId, orderAccount, runOrderType, amount, generationIp, acountR, accountW, runType, amountType, dealDescribe, user);
        if (amountRun.isSuccess()) {
            return amountRun;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    /**
     * <p>当前账户扣款流水</p>
     *
     * @param associatedId
     * @param amount
     * @return
     */
    public Result delete(String orderType, String userId, String associatedId, BigDecimal amount, String generationIp, String dealDescribe, String runType) {
        String orderAccount, amountType, acountR, accountW;
        Integer runOrderType = null;
        BigDecimal amountNow;
        orderAccount = userId;
        amountType = AMOUNT_TYPE_W;
        acountR = SYSTEM_APP;
        accountW = orderAccount;
        runOrderType = getRunOrderType(orderType);
        amount = new BigDecimal("-1").multiply(amount);//如果是扣款则为 负数显示
        Result amountRun = amountRun(associatedId, orderAccount, runOrderType, amount, generationIp, acountR, accountW, runType, amountType, dealDescribe);
        if (amountRun.isSuccess()) {
            return amountRun;
        }
        return Result.buildFail();
    }

    /**
     * <p>创建流水总类</p>
     *
     * @param associatedId 关联订单号
     * @param orderAccount 关联账户号
     * @param runOrderType 订单类型类型					订单类型(1充值交易,2系统加款,3交易手续费,4系统扣款,5代付,6代付手续费,7冻结,8解冻,9代付手手续费冻结,10代付冻结,11增加交易点数,12点数扣除,13代理商分润，14码商分润，15人工加款，16人工减款，17人工加点数，18人工减点数，19 卡商交易加钱)
     * @param amount       流水金额
     * @param generationIp 关联ip
     * @param acountR      入款账户
     * @param accountW     出款账户
     * @param runType      人工操作					1.自然状态 2.人工操作
     * @param amountType   流水类型					1支出0收入
     * @param dealDescribe 流水说明
     * @return
     */
    @SuppressWarnings("unused")
    private Result amountRun(String associatedId, String orderAccount,
                             Integer runOrderType, BigDecimal amount, String generationIp,
                             String acountR, String accountW, String runType, String amountType
            , String dealDescribe) {
        if (StrUtil.isBlank(associatedId) || StrUtil.isBlank(orderAccount)
                || StrUtil.isBlank(generationIp)
                || StrUtil.isBlank(runType)
                || StrUtil.isBlank(amountType)
                || StrUtil.isBlank(dealDescribe)
        ) {
            return Result.buildFailMessage("必传参数为空");
        }
        RunOrder run = new RunOrder();
        run.setAssociatedId(associatedId);
        run.setAccountW(accountW);
        run.setAcountR(acountR);
        run.setGenerationIp(generationIp);
        run.setOrderAccount(orderAccount);
        run.setAmountType(amountType);
        run.setDealDescribe(dealDescribe);
        run.setRunOrderType(runOrderType);
        run.setRunType(runType);
        run.setAmount(amount);
        boolean addOrder = runOrderServiceImpl.addOrder(run);
        if (addOrder) {
            return Result.buildSuccess();
        }
        return Result.buildFail();
    }

    /**
     * <p>代理分润专用</p>
     *
     * @param associatedId
     * @param orderAccount
     * @param runOrderType
     * @param amount
     * @param generationIp
     * @param acountR
     * @param accountW
     * @param runType
     * @param amountType
     * @param dealDescribe
     * @param user
     * @return
     */
    private Result amountRun(String associatedId, String orderAccount,
                             Integer runOrderType, BigDecimal amount, String generationIp,
                             String acountR, String accountW, String runType, String amountType
            , String dealDescribe, String user) {
        if (StrUtil.isBlank(associatedId) || StrUtil.isBlank(orderAccount)
                || StrUtil.isBlank(generationIp)
                || StrUtil.isBlank(runType)
                || StrUtil.isBlank(amountType)
                || StrUtil.isBlank(dealDescribe)
        ) {
            return Result.buildFailMessage("必传参数为空");
        }
        RunOrder run = new RunOrder();
        run.setAssociatedId(associatedId);
        run.setAccountW(accountW);
        run.setAcountR(acountR);
        run.setGenerationIp(generationIp);
        run.setOrderAccount(orderAccount);
        run.setAmountType(amountType);
        run.setDealDescribe(dealDescribe);
        run.setRunOrderType(runOrderType);
        run.setRunType(runType);
        run.setAmount(amount);
        run.setRetain4(user);
        boolean addOrder = runOrderServiceImpl.addOrder(run);
        if (addOrder) {
            return Result.buildSuccess();
        }
        return Result.buildFail();
    }

    /**
     * <p>获取流水订单类型</p>
     *
     * @param orderType 流水订单标识
     * @return
     */
    private Integer getRunOrderType(String orderType) {
        Integer runOrderType = null;
        switch (orderType) {
            case ADD_AMOUNT:
                runOrderType = ADD_AMOUNT_NUMBER;
                break;
            case DETETE_AMOUNT:
                runOrderType = DETETE_AMOUNT_NUMBER;
                break;
            case DEAL_AMOUNT_DETETE:
                runOrderType = DEAL_AMOUNT_DETETE_NUMBER;
                break;
            case PROFIT_AMOUNT_DEAL:
                runOrderType = PROFIT_AMOUNT_DEAL_NUMBER;
                break;
            case RECHANGE_AMOUNT:
                runOrderType = RECHANGE_AMOUNT_NUMBER;
                break;
            case WITHDRAY_AMOUNT:
                runOrderType = WITHDRAY_AMOUNT_NUMBER;
                break;
            case WITHDRAY_AMOUNT_FEE:
                runOrderType = WITHDRAY_AMOUNT_FEE_NUMBER;
                break;
            case PROFIT_AMOUNT_AGENT:
                runOrderType = PROFIT_AMOUNT_AGENT_NUMBER;
                break;
            case ADD_DEAL_AMOUNT_APP:
                runOrderType = ADD_DEAL_AMOUNT_APP_NUMBER;
                break;
            case DELETE_DEAL_FEE_AMOUNT_APP:
                runOrderType = DELETE_DEAL_FEE_AMOUNT_APP_NUMBER;
                break;
            case WITHDRAY_AMOUNT_OPEN:
                runOrderType = WITHDRAY_AMOUNT_OPEN_NUMBER;
                break;
            case WITHDRAY_AMOUNT_OPEN_FEE:
                runOrderType = WITHDRAY_AMOUNT_OPEN_FEE_NUMBER;
                break;
            case CHANNEL_ADD_WIT:
                runOrderType = CHANNEL_ADD_WIT_NUMBER;
                break;
            case AGNEET_WIT_FRREE:
                runOrderType = AGNEET_WIT_FRREE_NUMBER;
                break;
            case CHANNEL_ADD_R:
                runOrderType = CHANNEL_ADD_R_NUMBER;
                break;
            case CHANNEL_ADD_WIT_FEE:
                runOrderType = CHANNEL_ADD_WIT_FEE_NUMBER;
                break;
            case DELETE_FREEZE:
                runOrderType = DELETE_FREEZE_NUMBER;
                break;
            case ADD_FREEZE:
                runOrderType = ADD_FREEZE_NUMBER;
                break;
            case ADD_QUOTA:
                runOrderType = ADD_QUOTA_NUMBER;
                break;
            case DELETE_QUOTA:
                runOrderType = DELETE_QUOTA_NUMBER;
                break;
            case USDT_GAS_FEE:
                runOrderType = USDT_GAS_FEE_NUMBER;
                break;
            case DETETE_AMOUNT_TRANSTER:
                runOrderType = DETETE_AMOUNT_TRANSTER_NUMBER;
                break;
            case ADD_AMOUNT_TRANSTER:
                runOrderType = ADD_AMOUNT_TRANSTER_NUMBER;
                break;
            case RECHANGE_AMOUNT_NUMBER_WIT:
                runOrderType = RECHANGE_AMOUNT_NUMBER_WIT_NUMBER;
                break;
            case RECHANGE_AMOUNT_NUMBER_WIT_FEE:
                runOrderType = RECHANGE_AMOUNT_NUMBER_WIT_FEE_NUMBER;
                break;
            case PROFIT_AMOUNT_AGENT_BANK_R:
                runOrderType = PROFIT_AMOUNT_AGENT_BANK_R_NUMBER;
                break;
            case PROFIT_AMOUNT_AGENT_BANK_W:
                runOrderType = PROFIT_AMOUNT_AGENT_BANK_W_NUMBER;
                break;
            case PROFIT_AMOUNT_BANK_R:
                runOrderType = PROFIT_AMOUNT_BANK_R_NUMBER;
                break;
            case PROFIT_AMOUNT_BANK_B:
                runOrderType = PROFIT_AMOUNT_BANK_B_NUMBER;
                break;
            case PROFIT_AMOUNT_BANK_W:
                runOrderType = PROFIT_AMOUNT_BANK_W_NUMBER;
                break;
            case BANK_CARD_BACK:
                runOrderType = BANK_CARD_BACK_NUMBER;
                break;
            default:
                break;
        }
        return runOrderType;
    }

    /**
     * <p>渠道代付手续费</p>
     *
     * @param withdraw
     * @param generationIp
     * @return
     */
    public Result addChannelWitFee(Withdraw withdraw, String generationIp, BigDecimal fee) {
        return add(CHANNEL_ADD_WIT_FEE, withdraw.getWitChannel(), withdraw.getOrderId(), fee, generationIp, "渠道代付手续费流水", RUNTYPE_NATURAL);
    }

    /**
     * <p>渠道代付账户修改，流水生成</p>
     *
     * @param withdraw
     * @param generationIp
     * @return
     */
    public Result addChannelWit(Withdraw withdraw, String generationIp) {
        return add(CHANNEL_ADD_WIT, withdraw.getWitChannel(), withdraw.getOrderId(), withdraw.getAmount(), generationIp, "渠道代付成功加款", RUNTYPE_NATURAL);
    }


    public Result addFreeze(Amount amount, String generationIp) {
        return add(ADD_FREEZE, amount.getUserId(), amount.getOrderId(), amount.getAmount(), generationIp, "小金库转入余额账户", RUNTYPE_ARTIFICIAL);
    }

    public Result deleteFreeze(Amount amount, String generationIp) {
        return add(DELETE_FREEZE, amount.getUserId(), amount.getOrderId(), amount.getAmount(), generationIp, "余额账户转入小金库", RUNTYPE_ARTIFICIAL);
    }

    public Result addQuota(Amount amountPrivate, String generationIp) {
        return add(ADD_QUOTA, amountPrivate.getUserId(), amountPrivate.getOrderId(), amountPrivate.getAmount(), generationIp, "增加授权额度", RUNTYPE_ARTIFICIAL);
    }

    public Result deleteQuota(Amount amount, String generationIp) {
        return add(DELETE_QUOTA, amount.getUserId(), amount.getOrderId(), amount.getAmount(), generationIp, "减少授权额度", RUNTYPE_ARTIFICIAL);
    }

    public Result deleteUsdtFeeGas(Withdraw withdraw, String generationIp, String describe, BigDecimal amount) {
        return add(USDT_GAS_FEE, withdraw.getUserId(), withdraw.getOrderId(), amount, generationIp, describe, RUNTYPE_NATURAL);
    }


    public Result deleteAmountTRANS(Amount amount, String clientIP) {

        Result delete = delete(DETETE_AMOUNT_TRANSTER, amount.getUserId(), amount.getOrderId(), amount.getActualAmount(),
                clientIP, amount.getDealDescribe(), RUNTYPE_ARTIFICIAL);
        if (delete.isSuccess()) {
            return delete;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    public Result addAmountTRANS(Amount amount, String clientIP) {
        Result add = add(ADD_AMOUNT_TRANSTER, amount.getTransferUserId(), amount.getOrderId(), amount.getActualAmount(),
                clientIP, amount.getDealDescribe(), RUNTYPE_ARTIFICIAL);
        if (add.isSuccess()) {
            return add;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    public Result addOrderBankCardWit(DealOrder order, String clientIP, Boolean flag) {
        Result add = add(RECHANGE_AMOUNT_NUMBER_WIT, order.getOrderQrUser(), order.getOrderId(), order.getActualAmount(),
                clientIP, order.getDealDescribe(), flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL);
        if (add.isSuccess()) {
            return add;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    public Result addOrderBankCardWitFee(DealOrder order, String ip, BigDecimal amount, Boolean flag) {
        Result add = add(RECHANGE_AMOUNT_NUMBER_WIT_FEE, order.getOrderQrUser(), order.getOrderId(), amount,
                ip, order.getDealDescribe(), flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL);
        if (add.isSuccess()) {
            return add;
        }
        return Result.buildFailMessage("流水生成失败");
    }

    public Result addAppProfitBank(String orderId, String userId, BigDecimal amount, String ip, boolean flag, Integer feeType) {
        if (feeType == 1) {//卡商入款 代理分润
            Result add = add(PROFIT_AMOUNT_AGENT_BANK_R, userId, orderId, amount, ip, "入款，代理分润结算", flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL);
            return add;
        }
        Result add = add(PROFIT_AMOUNT_AGENT_BANK_W, userId, orderId, amount, ip, "出款，代理分润结算", flag ? RUNTYPE_ARTIFICIAL : RUNTYPE_NATURAL);
        return add;
    }


    /**
     * 卡商押金扣减流水生成
     *
     * @param orderWit
     */
    public void witBankCardAmount(Withdraw orderWit) {
        Result delete = delete(PROFIT_AMOUNT_BANK_W, orderWit.getUserId(), orderWit.getOrderId(), orderWit.getActualAmount(),
                orderWit.getRetain2(), "当前卡商佣金扣减，当前扣减佣金为：" + orderWit.getAmount(), RUNTYPE_ARTIFICIAL);
    }

    public Result addBankprofit(Withdraw wit) {

        Result add = add(PROFIT_AMOUNT_BANK_R, wit.getUserId(), wit.getOrderId(), wit.getAmount(), wit.getRetain2(), "当前卡商佣金退回，当前退回佣金为：" + wit.getAmount(), RUNTYPE_ARTIFICIAL);
        return add;
    }

    public Result addBankprofitAmount(Amount alipayAmount, String clientIP, String descr) {
        Result add = add(PROFIT_AMOUNT_BANK_R, alipayAmount.getUserId(), alipayAmount.getOrderId(), alipayAmount.getAmount(), clientIP, "当前卡商佣金退回，当前退回佣金为：" + alipayAmount.getAmount(), RUNTYPE_ARTIFICIAL);
        return add;
    }
 public Result addBankprofitAmountB(Amount alipayAmount, String clientIP, String descr) {
        Result add = add(PROFIT_AMOUNT_BANK_B, alipayAmount.getUserId(), alipayAmount.getOrderId(), alipayAmount.getAmount(), clientIP, descr + alipayAmount.getAmount(), RUNTYPE_ARTIFICIAL);
        return add;
    }

    public Result addBankprofitAmount(Amount alipayAmount, String clientIP) {
        return addBankprofitAmount(alipayAmount, clientIP, "当前卡商佣金退回，当前退回佣金为：" + alipayAmount.getAmount());
    }


    public Result deleteBankprofit(Amount alipayAmount, String clientIP, String descr) {
        Result add = delete(PROFIT_AMOUNT_BANK_R, alipayAmount.getUserId(), alipayAmount.getOrderId(), alipayAmount.getAmount(), clientIP, descr, RUNTYPE_ARTIFICIAL);
        return add;
    }

    public Result deleteBankprofit(Amount alipayAmount, String clientIP) {
        return deleteBankprofit(alipayAmount, clientIP, "手动扣除卡商佣金：" + alipayAmount.getAmount() + "，当前处理人：" + alipayAmount.getApproval());
    }


    public Result addBackBank(String username,String orderId,BigDecimal amount,String ip) {
        Result add = add(BANK_CARD_BACK, username, orderId, amount, ip, "卡商代付回滚，资金退回：" + amount, RUNTYPE_ARTIFICIAL);
        return add;
    }
    public Result deleteBackBank(String username,String orderId,BigDecimal amount,String ip) {
        Result add = delete(BANK_CARD_BACK, username, orderId, amount, ip, "卡商代付回滚，资金退回：" + amount, RUNTYPE_ARTIFICIAL);
        return add;
    }



}
