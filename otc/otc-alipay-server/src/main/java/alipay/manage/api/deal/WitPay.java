package alipay.manage.api.deal;

import alipay.config.redis.RedisLockUtil;
import alipay.config.redis.RedisUtil;
import alipay.manage.api.AccountApiService;
import alipay.manage.api.DealAppApi;
import alipay.manage.api.VendorRequestApi;
import alipay.manage.api.config.FactoryForStrategy;
import alipay.manage.api.config.PayOrderService;
import alipay.manage.bean.*;
import alipay.manage.bean.util.WithdrawalBean;
import alipay.manage.mapper.ChannelFeeMapper;
import alipay.manage.service.ExceptionOrderService;
import alipay.manage.service.IAlipayBankConfigService;
import alipay.manage.service.UserInfoService;
import alipay.manage.service.WithdrawService;
import alipay.manage.util.BankTypeUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.api.alipay.Common;
import otc.bean.dealpay.Withdraw;
import otc.exception.order.OrderException;
import otc.result.Result;
import otc.util.MapUtil;
import otc.util.number.Number;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

@Component
public class WitPay extends PayOrderService {
    @Autowired
    VendorRequestApi vendorRequestApi;
    Logger log = LoggerFactory.getLogger(DealAppApi.class);
    @Autowired
    private FactoryForStrategy factoryForStrategy;
    @Autowired
    private AccountApiService accountApiServiceImpl;
    @Autowired
    private WithdrawService withdrawServiceImpl;
    @Resource
    private ChannelFeeMapper channelFeeDao;
    @Autowired
    private ExceptionOrderService exceptionOrderServiceImpl;
    @Autowired
    private UserInfoService userInfoServiceImpl;
    @Autowired
    private RedisLockUtil redisLockUtil;
    @Autowired
    private RedisUtil redis;
    @Autowired
    private IAlipayBankConfigService bankConfigService;

    static final String COMMENT = "等待推送中";

    /**
     * 代付接口
     *
     * @param request
     * @param amount  true   检查余额   false  不检查余额
     * @return
     */
    public Result wit(HttpServletRequest request, boolean amount) {
        String userId = request.getParameter("userId");
        if (ObjectUtil.isNull(userId)) {
            return Result.buildFailMessage("当前传参，参数格式错误，请使用[application/x-www-form-urlencoded]表单格式传参");
        }
        redisLockUtil.redisLock(RedisLockUtil.AMOUNT_USER_KEY + userId);
        String manage = request.getParameter("manage");
        boolean flag = false;
        if (StrUtil.isNotBlank(manage)) {
            log.info("后台提现");
            flag = true;
        }
        Result withdrawal = vendorRequestApi.withdrawal(request, flag);
        if (!withdrawal.isSuccess()) {
            return withdrawal;
        }
        Object result = withdrawal.getResult();
        WithdrawalBean wit = MapUtil.mapToBean((Map<String, Object>) result, WithdrawalBean.class);
        wit.setIp(VendorRequestApi.getIpAddress(request, wit.getAppid()));
        UserRate userRate = accountApiServiceImpl.findUserRateWitByUserId(wit.getAppid(), wit.getAmount());
        if (amount) {
            UserFund userFund = userInfoServiceImpl.fundUserFundAccounrBalace(userId);
            BigDecimal accountBalance = userFund.getAccountBalance();
            BigDecimal quota = userFund.getQuota();
            accountBalance = accountBalance.add(quota);
            if (accountBalance.compareTo(new BigDecimal(wit.getAmount()).add(userRate.getFee())) == -1) {
                exceptionOrderServiceImpl.addWitEx(userId, wit.getAmount(), "商户相应提示：当前账户金额不足；" + "处理方法：请检查商户金额是否足够，或者要求商户更换金额提交", HttpUtil.getClientIP(request), wit.getApporderid());
                return Result.buildFailMessage("当前账户金额不足");
            }
        }
        UserInfo userInfoByUserId = userInfoServiceImpl.findUserInfoByUserId(userRate.getChannelId());
        if (Common.Order.DAPY_OFF.equals(userInfoByUserId.getRemitOrderState())) {
            log.info("【渠道关闭】");
            exceptionOrderServiceImpl.addWitOrder(wit, "用户报错：渠道关闭，联系运营处理；处理方法：开启渠道代付状态", HttpUtil.getClientIP(request));
            return Result.buildFailMessage("渠道关闭，联系运营处理");
        }
        ChannelFee channelFee = channelFeeDao.findImpl(userRate.getChannelId(), userRate.getPayTypr());//缓存已加
        if (ObjectUtil.isNull(channelFee)) {
            log.info("【通道实体不存在，费率配置错误】");
            exceptionOrderServiceImpl.addWitOrder(wit, "用户报错：通道实体不存在，费率配置错误；处理方法：请检查商户提交的通道编码，反复确认", HttpUtil.getClientIP(request));
            return Result.buildFailMessage("通道实体不存在，费率配置错误");
        }
        //String bankcode = BankTypeUtil.getBank(wit.getBankcode());
        AlipayBankConfig alipayBankConfig = Optional.ofNullable(bankConfigService.selectAlipayBankConfig(wit.getBankcode())).orElseGet(()-> new AlipayBankConfig());
        String bankcode = alipayBankConfig.getAlias1() ;
        if (StrUtil.isBlank(bankcode)) {
            log.info("【当前银行不支持代付，当前商户：" + wit.getAppid() + "，当前订单号:" + wit.getApporderid() + "】");
            exceptionOrderServiceImpl.addWitOrder(wit, "用户报错：当前银行不支持合， 银行code值错误；处理方法：请商户检查提交的银行卡code是否正确，商户code值为：" + bankcode, HttpUtil.getClientIP(request));
            return Result.buildFailMessage("当前银行不支持合， 银行code值错误");
        }
        Object o = redis.get(wit.getApporderid() + userRate.getUserId());
        if (null != o) {
            if (o.toString().equals(wit.getApporderid() + userRate.getUserId())) {
                log.info("【当前商户订单号重复：" + wit.getApporderid() + "】");
                exceptionOrderServiceImpl.addWitOrder(wit, "用户报错：商户订单号重复；处理方法：提醒用户换一个订单号提交代付请求请求", HttpUtil.getClientIP(request));
                return Result.buildFailMessage("商户订单号重复");
            }
        }
        Result deal = null;
        try {
            Withdraw bean = createWit(wit, userRate, flag, channelFee);
            if (ObjectUtil.isNull(bean)) {
                return Result.buildFailMessage("代付订单生成失败");
            }
            deal = Result.buildSuccessMessage("代付处理中");
          /*  UserInfo userInfo = accountApiServiceImpl.findautoWit(wit.getAppid());
            //缓存数据
            if (1 == userInfo.getAutoWit()) {
                //自动推送
                deal = super.withdraw(bean);
                if (deal.isSuccess()) {
                    log.info("【渠道类为：" + channelFee.toString() + "】");
                    deal = factoryForStrategy.getStrategy(channelFee.getImpl()).withdraw(bean);  //   暂时注释，当前修改后方案可能存在不稳定
                } else {
                    withdrawServiceImpl.updateWitError(bean.getOrderId());
                    return Result.buildFailMessage("代付失败，当前排队爆满，请再次发起代付");
                }
            } else {
                //手动处理
                deal = super.withdraw(bean);
            }
*/
        } catch (Exception e) {
            log.error("[代码执行时候出现错误]", e);
            //		super.withdrawEr(bean, "系统异常，请联系技术人员处理", HttpUtil.getClientIP(request));
            exceptionOrderServiceImpl.addWitOrder(wit, "用户报错：当前通道编码不存在；处理方法：提交技术人员处理，报错信息：" + e.getMessage(), HttpUtil.getClientIP(request));
            log.info("【当前通道编码对于的实体类不存在】");
            //    withdrawServiceImpl.updateWitError(bean.getOrderId());
            redisLockUtil.unLock(RedisLockUtil.AMOUNT_USER_KEY + userId);
            return Result.buildFailMessage("当前通道编码不存在");
        } finally {
            //    bean = null;
            o = null;
          ///  bankcode = null;
            channelFee = null;
            wit = null;
            userRate = null;
            result = null;
            redisLockUtil.unLock(RedisLockUtil.AMOUNT_USER_KEY + userId);
        }
        return deal;
    }

    private Withdraw createWit(WithdrawalBean wit, UserRate userRate, Boolean fla, ChannelFee channelFee) {
        log.info("【当前转换参数 代付实体类为：" + wit.toString() + "】");
        String type = "";
        String bankName = "";
        if (fla) {
            type = Common.Order.Wit.WIT_TYPE_API;
        } else {
            type = Common.Order.Wit.WIT_TYPE_MANAGE;
        }
        Withdraw witb = new Withdraw();
        witb.setUserId(wit.getAppid());
        witb.setAmount(new BigDecimal(wit.getAmount()));





        //TODO 新增 代付取款抽点收费逻辑
        BigDecimal psf = new BigDecimal(Optional.ofNullable(userRate.getRetain4()).orElse("0")) ;//抽点收费比例
        BigDecimal fee = userRate.getFee();//单笔收费
        fee =   psf.multiply(new BigDecimal(wit.getAmount())).add(fee);
        witb.setFee(fee);
        witb.setActualAmount(new BigDecimal(wit.getAmount()));
    //    witb.setFee(userRate.getFee());
    //    witb.setActualAmount(new BigDecimal(wit.getAmount()));



        witb.setMobile(wit.getMobile());
        witb.setBankNo(wit.getAcctno());
        witb.setAccname(wit.getAcctname());
        bankName = wit.getBankName();

        if (StrUtil.isBlank(bankName)) {
            //bankName = BankTypeUtil.getBankName(wit.getBankcode());
            AlipayBankConfig alipayBankConfig = Optional.ofNullable(bankConfigService.selectAlipayBankConfig(wit.getBankcode())).orElseGet(()-> new AlipayBankConfig());
            bankName = alipayBankConfig.getBankName() ;
        }



        witb.setBankName(bankName);
        witb.setWithdrawType(Common.Order.Wit.WIT_ACC);
        witb.setOrderId(Number.getWitOrder());
        witb.setOrderStatus(Common.Order.DealOrderApp.ORDER_STATUS_DISPOSE.toString());
        witb.setNotify(wit.getNotifyurl());
        witb.setRetain2(wit.getIp());//代付ip
        witb.setAppOrderId(wit.getApporderid());
        witb.setRetain1(type);
        witb.setWitType(userRate.getPayTypr());//代付类型
        witb.setApply(wit.getApply());
        witb.setBankcode(wit.getBankcode());
        witb.setWitChannel(channelFee.getChannelId());
        witb.setPushOrder(0);
        UserFund userFund = userInfoServiceImpl.findCurrency(wit.getAppid());//缓存以加
        //    witb.setStatus(2);//未推送处理   未推送三方处理状态
        witb.setCurrency(userFund.getCurrency());
        //  witb.setComment(Common.Order.DealOrderApp.COMMENT_WIT.toString());
        boolean flag = false;
        try {
            flag = withdrawServiceImpl.addOrder(witb);
        } catch (Exception e) {
            log.info("【当前商户订单号重复：" + wit.getApporderid() + "】");
            exceptionOrderServiceImpl.addWitOrder(wit, "用户报错：商户订单号重复；处理方法：提醒用户换一个订单号提交代付请求请求", wit.getIp());
            //	throw new OrderException("订单号重复", null);
        }
        if (flag) {
            redis.set(witb.getAppOrderId() + witb.getUserId(), witb.getAppOrderId() + witb.getUserId(), 60 * 60);
            return witb;
        }
        return null;
    }

    /**
     * 此方法目的是防止结算并发
     */


    public Result witAutoPush(Withdraw order) {
        Result deal = Result.buildFail();
        try {
            UserInfo userInfo = accountApiServiceImpl.findautoWit(order.getUserId());
            Future<Result> result = ThreadUtil.execAsync(() -> {
                //缓存数据
                BigDecimal witAmount = order.getAmount();
                UserFund userFund = userInfoServiceImpl.fundUserFundAccounrBalace(order.getUserId());
                BigDecimal accountBalance = userFund.getAccountBalance();
                BigDecimal quota = userFund.getQuota();
                accountBalance = accountBalance.add(quota);
                if (accountBalance.compareTo(witAmount.add(order.getFee())) == -1) {
                    exceptionOrderServiceImpl.addWitEx(order.getUserId(), order.getAmount().toString(), "商户相应提示：当前账户金额不足；" + "处理方法：请检查商户金额是否足够，或者要求商户更换金额提交", order.getRetain2(), order.getAppOrderId());
                    withdrawErByAmount(order.getOrderId(), "当前账户金额不足");
                    return Result.buildFailMessage("当前账户金额不足");
                }
                return Result.buildSuccessResult();
            });

            try {
                Result result1 = result.get();
                if (!result1.isSuccess()) {
                    return result1;
                }
            } catch (Exception ex) {
                push("金额不足或其他异常情况，请及时处理，当前订单号：" + order.getOrderId() + "，当前程序堆栈数据：" + printStackTrace(ex.getStackTrace()));
                return Result.buildFailMessage("金额不足或其他异常情况，请及时处理");
            }
            if (1 == userInfo.getAutoWit()) {
                deal = super.withdraw(order);
                if (deal.isSuccess()) {
                    ThreadUtil.execute(() -> {
                        ChannelFee channelFee = channelFeeDao.findImpl(order.getWitChannel(), order.getWitType());//缓存已加
                        Result withdraw = Result.buildFail();
                        try {
                            withdraw = factoryForStrategy.getStrategy(channelFee.getImpl()).withdraw(order);
                        } catch (Exception e) {
                            push("当前订单推送异常，请及时检查异常情况，当前订单号：" + order.getOrderId() + "，当前程序堆栈数据：" + printStackTrace(e.getStackTrace()));
                            //  return Result.buildFailMessage("推送异常");
                            log.error("代付异常", e);
                        }
                    });
                    ThreadUtil.execute(() -> {
                        //修改订单为已推送 不管当前订单是否推送成功
                        boolean b = withdrawServiceImpl.updatePush(order.getOrderId());
                        if (b) {
                            log.info("【当前订单已推送，状态已修改，当前订单号：" + order.getOrderId() + "】");
                        } else {
                            log.info("【当前订单已推送，状态未修改，当前订单号：" + order.getOrderId() + "】");
                        }
                    });
                } else {
                    throw new OrderException("代付订单结算失败", null);
                }
            } else {
                deal = super.withdraw(order);
            }
        } catch (Exception e) {
            push("当前推送发生异常，修改订单为已推送状态， 请及时检查异常情况，当前订单号：" + order.getOrderId() + "，当前程序堆栈数据：" + printStackTrace(e.getStackTrace()));
            boolean b = withdrawServiceImpl.updatePush(order.getOrderId());
            if (b) {
                log.info("【当前订单已推送，状态已修改，当前订单号：" + order.getOrderId() + "】");
            } else {
                log.info("【当前订单已推送，状态未修改，当前订单号：" + order.getOrderId() + "】");
            }
        }
        return deal;
    }

    void push(String msg) {
        ThreadUtil.execute(() -> {
            String url = "http://10.170.0.4:8888/api/send?text=";
            String test = msg + "，触发时间：" + DatePattern.NORM_DATETIME_FORMAT.format(new Date());
            test = HttpUtil.encode(test, "UTF-8");
            String id = "&id=";
            String ids = "-414940159";
            id += ids;
            String s = url + test + id;
            HttpUtil.get(s, 1000);
        });
    }

    String printStackTrace(StackTraceElement[] s) {
        String er = "";
        StackTraceElement[] stackTrace = s;
        for (StackTraceElement stack : stackTrace) {
            er = "-------" + stack.getMethodName() + " : " + stack;
        }
        return er;
    }


}
