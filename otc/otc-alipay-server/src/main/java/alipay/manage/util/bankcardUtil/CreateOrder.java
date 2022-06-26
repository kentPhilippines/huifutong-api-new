package alipay.manage.util.bankcardUtil;


import alipay.config.redis.RedisUtil;
import alipay.manage.bean.*;
import alipay.manage.mapper.ChannelFeeMapper;
import alipay.manage.mapper.MediumMapper;
import alipay.manage.service.*;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.api.alipay.Common;
import otc.bean.alipay.Medium;
import otc.bean.dealpay.Recharge;
import otc.bean.dealpay.Withdraw;
import otc.common.PayApiConstant;
import otc.result.Result;
import otc.util.number.GenerateOrderNo;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 卡商系统订单生产类
 */

@Component
public class CreateOrder {
    public static final Log log = LogFactory.get();
    /**
     * 一下几种情况生产卡商订单
     * 一，卡商入款订单
     * 1，商户充值 卡商入款
     * 2，卡商充值 卡商入款
     * 二，卡商出款订单
     * 1，商户代付卡商入款
     * 2，卡商代付，卡商入款
     */
    private static final String MARK = ":";
    ;
    private static final String MARS = "SHENFU";
    private static final String WIT_BANK_COUNT = "WIT:BANK:COUNT";//代付出款缓存数据统计
    @Autowired
    RedisUtil redis;
    @Autowired
    private UserInfoService userInfoServiceImpl;
    @Autowired
    private UserRateService userRateServiceImpl;
    @Resource
    private ChannelFeeMapper channelFeeDao;
    @Autowired
    private OrderService orderServiceImpl;
    @Autowired
    private BankUtil queue;
    @Autowired
    private UserFundService userFundService;
    @Autowired
    private CorrelationService correlationServiceImpl;
    /**
     * 卡商充值 卡商入款订单生成
     *
     * @param order
     * @return
     */
    Result addOrderRe(Recharge order) {
        return null;
    }

    /**
     * 提现订单提现
     *
     * @param wit    提现订单
     * @param userId 是否指定出款人，当前参数为出款人
     * @param flag1  true 为小额出款  false 为其他出款
     * @return
     */
    public Result witAddOrder(Withdraw wit, String userId,boolean flag1 ) {
        String channnelId = "";
        String channelFeeId = null;
        Boolean flag = false;
        String bankInfo = "";
        Integer userFeeId = null;
        String bc = GenerateOrderNo.Generate("BW");
        UserInfo accountInfo = userInfoServiceImpl.findUserInfoByUserId(wit.getUserId());//这里有为商户配置的 供应队列属性
        UserRate rateFee = userRateServiceImpl.findUserRateWitByUserIdApp(accountInfo.getUserId());
        userFeeId = rateFee.getId();
        String witprople = "";
        //出款选卡算法
        Medium bankinfo =  null; // getBankInfo(userId, accountInfo.getQueueList(), bc,wit.getAmount());//出款人   采取手动配单
        if (null == bankinfo) {
            if(flag1){
                witprople =   "zhongbang-bank-s";
            }else{
                witprople =   "zhongbang-bank";
            }

            bankInfo =  "";
        }else{
            witprople = bankinfo.getQrcodeId();
            bankInfo = bankinfo.getAccount() + ":" +   bankinfo.getMediumHolder() + ":" + bankinfo.getMediumNumber();
        }
        if(StrUtil.isEmpty(witprople)){
            return Result.buildFailMessage("暂无出款渠道");
        }
        channnelId = witprople;
        UserRate channnelFee = userRateServiceImpl.findUserRateWitByUserIdApp(channnelId);
        if (null == channnelFee) {
            return Result.buildFailMessage("暂无出款渠道");
        }
        channelFeeId = String.valueOf(channnelFee.getId());
        Result result = addOrder(bc, wit.getOrderId(), wit.getAppOrderId(), wit.getUserId(),
                wit.getAmount().toString(), channnelId, channelFeeId,
                flag, bankInfo, userFeeId, Boolean.FALSE, wit.getRetain2(), wit.getNotify(), null,null);
        ThreadUtil.execute(() -> {
            corr(bc, null);
        });

        return result;
    }


    /***
     * 推送消息给卡商提示出款
     * @param bankInfoUser
     */
    private void push(String bankInfoUser) {


    }

    /**
     * 新增交易订单
     *
     * @param dealApp
     * @return
     */
    private String name = "付款人：";
    public Result dealAddOrder(DealOrderApp dealApp) {
        String channnelId = "";
        String channelFeeId = null;
        Boolean flag = true;
        Integer userFeeId = dealApp.getFeeId();
        UserInfo accountInfo = userInfoServiceImpl.findUserInfoByUserId(dealApp.getOrderAccount());//这里有为商户配置的 供应队列属性
        String[] queueCode = {};
        String bankInfo = "";
        if (StrUtil.isNotBlank(accountInfo.getAgent())) {
            UserInfo agent = findAgent(accountInfo.getAgent());
            queueCode = agent.getQueueList().split(",");//队列供应标识数组
        } else {
            String queueList = accountInfo.getQueueList();
            queueCode = queueList.split(",");//队列供应标识数组
        }
        String bc = GenerateOrderNo.Generate("BA");

        String payInfo = dealApp.getDealDescribe();
        if(StrUtil.isNotEmpty(payInfo)) {
            String[] split = payInfo.split(name);
            String payName = split[1];
            payInfo = payName;
        }
        Medium qr = queue.findQr(bc, dealApp.getOrderAmount(), Arrays.asList(queueCode), false, "card", payInfo);//当前接口限制 收款回调，接单限制，接单评率等数据
        if (null == qr) {
            return Result.buildFailMessage("暂无对应银行卡");
        }
        channnelId = qr.getQrcodeId();
        UserRate userRateR = userRateServiceImpl.findUserRateR(channnelId);
        channelFeeId = userRateR.getId().toString();
        bankInfo = qr.getAccount() + MARK + qr.getMediumHolder() + MARK + qr.getMediumNumber() + MARK + "电话" + MARK + qr.getMediumPhone();
        Result result = addOrder(bc, dealApp.getOrderId(),
                dealApp.getAppOrderId(), dealApp.getOrderAccount(), dealApp.getOrderAmount().toString(), channnelId,
                channelFeeId, flag, bankInfo, userFeeId,
                Boolean.FALSE, dealApp.getOrderIp(), dealApp.getNotify(), dealApp.getBack(),payInfo);
        if (!result.isSuccess()) {
            return result;
        }
        Map cardmap = new HashMap();
        cardmap.put("bank_name", qr.getAccount());
        cardmap.put("card_no", qr.getMediumNumber());
        cardmap.put("card_user", qr.getMediumHolder());
        cardmap.put("money_order", dealApp.getOrderAmount());
        cardmap.put("no_order", bc);
        cardmap.put("oid_partner", dealApp.getOrderId());
        cardmap.put("address", qr.getPayInfo());
        redis.hmset(MARS + bc, cardmap, 600);
        result.setMessage(qr.getMediumHolder() + ":" + qr.getAccount() + ":" + qr.getMediumNumber() + ":" + qr.getPayInfo() );
        result.setResult(PayApiConstant.Notfiy.OTHER_URL + "/pay?orderId=" + bc + "&type=203");
        ThreadUtil.execute(() -> {
            corr(bc, qr.getMediumNumber());
        });
        return result;
    }

    @Resource
    RedisUtil redisUtil;

    void corr(String orderId, String BankNo) {
        ThreadUtil.execute(() -> {
            DealOrder order = orderServiceImpl.findOrderByOrderId(orderId);
            CorrelationData corr = new CorrelationData();
            corr.setAmount(order.getDealAmount());
            corr.setOrderId(order.getOrderId());
            if (StrUtil.isNotEmpty(BankNo)) {
                corr.setQrId(BankNo);
            }
            corr.setOrderStatus(Integer.valueOf(order.getOrderStatus()));
            corr.setUserId(order.getOrderQrUser());
            corr.setAppId(order.getOrderAccount());
            boolean addCorrelationDate = correlationServiceImpl.addCorrelationDate(corr);
            if (addCorrelationDate) {
                log.info("【订单号：" + order.getOrderId() + "，添加数据统计成功】");
            } else {
                log.info("【订单号：" + order.getOrderId() + "，添加数据统计失败】");
            }
        });
    }

    public Result rechargeAddOrder(Recharge recharge) {
        String channnelId = "";
        String channelFeeId = null;
        Boolean flag = true;
        Integer userFeeId = null;
        UserInfo accountInfo = userInfoServiceImpl.findUserInfoByUserId(recharge.getUserId());//这里有为商户配置的 供应队列属性
        String[] queueCode = {};
        if (StrUtil.isNotBlank(accountInfo.getQueueList())) {
            queueCode = accountInfo.getQueueList().split(",");//队列供应标识数组
        }
        String bc = GenerateOrderNo.Generate("RE");
        String payInfo = recharge.getChargeReason();
        Medium qr = queue.findQr(bc, recharge.getAmount(), Arrays.asList(queueCode), false, "card", payInfo);//当前接口限制 收款回调，接单限制，接单评率等数据
        if (null == qr) {
            return Result.buildFailMessage("暂无收款渠道");
        }
        channnelId = qr.getQrcodeId();
        UserRate userRateR = userRateServiceImpl.findUserRateR(channnelId);
        channelFeeId = userRateR.getId().toString();
        Result result = addOrder(bc, recharge.getOrderId(), recharge.getUserId(),
                recharge.getOrderId(), recharge.getAmount().toString(),
                channnelId, channelFeeId, flag, null, userFeeId,
                Boolean.TRUE, null, null, null,null);
        if (!result.isSuccess()) {
            return result;
        }

        Map cardmap = new HashMap();
        cardmap.put("bank_name", qr.getAccount());
        cardmap.put("card_no", qr.getMediumNumber());
        cardmap.put("card_user", qr.getMediumHolder());
        cardmap.put("money_order", recharge.getAmount());
        cardmap.put("no_order", bc);
        cardmap.put("oid_partner", recharge.getOrderId());
        redis.hmset(MARS + bc, cardmap, 600000);
        result.setResult(PayApiConstant.Notfiy.OTHER_URL + "/pay?orderId=" + bc + "&type=203");
        return result;

    }

    /**
     * 新增主交易订单
     *
     * @param asOrder        关联订单， 如果是卡商入款订单   关联订单为   商户预订单好， 如果是卡商出款订单  关联订单为  代付 订单号
     * @param userId         商户账户号
     * @param amount         交易金额
     * @param channeId       渠道账号，这里就是卡商账户号
     * @param channelIdFeeId 渠道结算费率id，这里就是卡商的出入款费率id
     * @param flag           true  商户订单卡商入款订单    false  商户代付卡商出款订单
     * @param bankInfo       出款时商户的详细信息，   格式为   开户人:开户行:银行卡号
     * @param userFeeId      商户结算费率id
     * @param isBankRechage  是否为卡商充值，卡商出款订单
     * @return
     */
    Result addOrder(String orderId, String asOrder, String exTerId, String userId, String amount, String channeId, String channelIdFeeId, Boolean flag, String bankInfo,
                    Integer userFeeId, Boolean isBankRechage, String ip, String notify, String back,String payer) {
        try {
            log.info("【开始创建本地订单，当前创建订单的关联订单为：" + asOrder + "】");
            log.info("【当前交易的渠道账号为：" + channeId + "】");
            DealOrder order = new DealOrder();
            UserInfo userinfo = userInfoServiceImpl.findDealUrl(channeId);//查询渠道账户
            UserRate channelRate = userRateServiceImpl.findRateFeeType(Integer.valueOf(channelIdFeeId));//长久缓存 卡商费率
            order.setAssociatedId(asOrder);
            String orderQrCh = orderId;//
            order.setOrderId(orderQrCh);
            order.setOrderQrUser(userinfo.getUserId());
            order.setOrderStatus(Common.Order.DealOrder.ORDER_STATUS_DISPOSE.toString());
            order.setRetain1(channelRate.getPayTypr());
            order.setExternalOrderId(exTerId);
            order.setGenerationIp(ip);
            order.setNotify(notify);
            order.setBack(back);
            order.setPayer(payer);
            order.setOrderAccount(userId);
            if (flag) {
                /**
                 *  1，卡商充值          费率计算方式为      交易手续费极为  当前收益     ， 对我运营人员    手续费实际为 我方渠道成本
                 *  2，商户充值          费率计算方式为       交易手续费为 商户手续费   渠道手续费为 卡商利润， 我放利润为   商户收费 -  渠道收费
                 */
                BigDecimal dealAmount = new BigDecimal(amount);
                order.setDealAmount(dealAmount);
                BigDecimal fee = channelRate.getFee(); //渠道成本费率
                log.info("【当前渠道成本费率为：" + fee + "】");
                BigDecimal dealFee = dealAmount.multiply(fee);
                log.info("【当前渠道成本花费金额为：" + dealFee + "】");
                UserRate rateFeeType = null;
                BigDecimal fee1 = BigDecimal.ZERO;
                if (!isBankRechage) {
                    rateFeeType = userRateServiceImpl.findRateFeeType(userFeeId);
                    fee1 = rateFeeType.getFee();
                }
                /*长久缓存*/
                order.setOrderQr(bankInfo);
                BigDecimal actualAmount = null;
                log.info("【当前入款账户交易费率为：" + fee + "】");

                BigDecimal multiply = fee1.multiply(dealAmount);  //上缴系统金额
                log.info("【当前入款账户交易扣除手续费为：" + multiply + "】");
                actualAmount = dealAmount.subtract(multiply);
                log.info("【当前入款账户交易实际手续费为：" + actualAmount + "】");
                order.setActualAmount(actualAmount);      //实际到账金额   =     渠道卡商收款金额  + 渠道卡商收取费用    渠道卡商费率 =  交易金额  *  渠道费率
                order.setDealFee(multiply);
                order.setFeeId(channelRate.getId());
                order.setDealDescribe("正常入款交易订单");
                if (asOrder.contains(Common.Deals.YUCHUANG_FLOW)) {//商户充值   结算需要
                    order.setOrderType(Common.Order.ORDER_TYPE_DEAL.toString());
                } else {
                    order.setOrderType(Common.Order.ORDER_TYPE_BANKCARD_R.toString());//    卡商充值
                }
                log.info("【当前渠道收取手续费：" + dealFee + "】");
                log.info("【当前收取商户手续费：" + multiply + "】");
                BigDecimal subtract = multiply.subtract(dealFee);
                log.info("【当前订单系统盈利：" + subtract + "】");
                order.setRetain2(dealFee.toString());  //出入款渠道成本， 即为卡商结算费率
                order.setRetain3(subtract.toString());  //当前系统利润       =      收款费率金额  - 渠道成本
            } else {
                //出款订单享受单比补贴
                BigDecimal dealAmount = new BigDecimal(amount);         //代付金额
                order.setDealAmount(dealAmount);
                BigDecimal fee = channelRate.getFee();                  //渠道成本费率  卡出款费率
                log.info("【当前渠道成本费率为：" + fee + "】");
                fee = fee.multiply(dealAmount);
                UserRate rateFeeType = userRateServiceImpl.findRateFeeType(userFeeId);
                /*长久缓存*/
                BigDecimal fee1 = rateFeeType.getFee();  //商户出款 手续费
                log.info("【当前出款账户交易扣除手续费为：" + fee1 + "】");
                log.info("【当前出款账户交易实际手续费为：" + dealAmount + "】");
                order.setActualAmount(dealAmount);      //实际到账金额   =     渠道卡商收款金额  + 渠道卡商收取费用    渠道卡商费率 =  交易金额  *  渠道费率
                order.setDealFee(fee1);
                order.setOrderAccount(userId);                  //商户
                order.setDealDescribe("正常出款交易订单");
                log.info("【当前渠道收取手续费：" + fee + "】");
                log.info("【当前收取商户手续费：" + fee1 + "】");
                BigDecimal subtract = fee1.subtract(fee);
                log.info("【当前订单系统盈利：" + subtract + "】");
                order.setOrderQr(bankInfo);
                order.setFeeId(channelRate.getId());
                order.setRetain2(fee.toString());  //出款款渠道成本， 即为卡商结算费率
                order.setRetain3(subtract.toString());  //当前系统利润       =      收款费率金额  - 渠道成本
                order.setDealDescribe("正常出款交易订单");
                order.setOrderType(Common.Order.ORDER_TYPE_BANKCARD_W.toString());
            }

            String orderMark = "ORDER:" + order.getOrderQrUser() + ":AUTO";
            redisUtil.set(orderMark, orderMark, 10);//金额锁定时间标记     , 如果在20分钟内回调就会删除锁定金额


            boolean b = orderServiceImpl.addOrder(order);
            if (b) {
                return Result.buildSuccess();
            }
        } catch (Exception e) {
            log.error("【当前订单异常，当前订单号：" + asOrder + "】", e);
            log.info("【异常信息：" + e.getMessage() + "】");
            return Result.buildFailMessage("订单异常");
        }
        return Result.buildFailMessage("失败未知");
    }
    @Resource
    private MediumMapper mediumDao;
    @Autowired
    private MediumService mediumService;
    Medium getBankInfo(String userId, String weight, String orderId, BigDecimal amount) {





        /**
         * #################出款选卡逻辑################
         * 如果指定出款人出款则直接选中出款人直接出款，如果未指定出款人，则按照以下逻辑选择出款
         *
         * 1，当日交易额度最多        【确保有钱】
         * 2，实际金额最多           【减少冻结概率】
         */
        List<UserFund> userFundList = null;
        //    if (StrUtil.isEmpty(weight)) {
        List<Medium> bankByAmountWit = mediumDao.findBankByAmountWit(amount);
       // userFundList = userFundService.findBankUserId(amount);
        for(Medium med : bankByAmountWit) {
            String qrcodeId = med.getQrcodeId();
            if (StrUtil.isEmpty(qrcodeId)) {
                return null;
            }
            List<DealOrder> order = orderServiceImpl.findWitOrderByUserId(med.getQrcodeId());
            if(CollUtil.isNotEmpty(order) ){
                continue;
            }
            queue.saveWit(qrcodeId, orderId);
            returnMed(med,amount);
            return med;
        }
        for (Medium med : bankByAmountWit) {//如果选不到 全部删除
            queue.openWit(med.getQrcodeId());
        }
        for(Medium med : bankByAmountWit) {
            String qrcodeId = med.getQrcodeId();
            if (StrUtil.isEmpty(qrcodeId)) {
                return null;
            }
            List<DealOrder> order = orderServiceImpl.findWitOrderByUserId(med.getQrcodeId());
            if(CollUtil.isNotEmpty(order) ){
                continue;
            }
            queue.saveWit(qrcodeId, orderId);
            returnMed(med,amount);
            return med;
        }
        return null;
    }

    private void returnMed(Medium med, BigDecimal amount) {
        mediumService.updateMount(med.getMediumNumber(),amount.toString(),"sub","wait");
    }

    UserInfo findAgent(String userId) {
        UserInfo userAgent = userInfoServiceImpl.findUserAgent(userId);
        if (StrUtil.isNotEmpty(userAgent.getAgent())) {
            return findAgent(userAgent.getAgent());
        }
        return userAgent;

    }
}
