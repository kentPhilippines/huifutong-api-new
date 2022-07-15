package alipay.manage.api.Impl;

import alipay.config.redis.RedisUtil;
import alipay.manage.bean.DealOrder;
import alipay.manage.bean.UserInfo;
import alipay.manage.bean.UserRate;
import alipay.manage.bean.util.AutoWitBean;
import alipay.manage.mapper.DealOrderMapper;
import alipay.manage.mapper.UserInfoMapper;
import alipay.manage.service.*;
import alipay.manage.util.bankcardUtil.ReWit;
import cn.hutool.core.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.bean.alipay.Medium;
import otc.bean.dealpay.Withdraw;
import otc.common.RedisConstant;
import otc.result.Result;
import otc.util.RSAUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Component
public class AutoWit {
    Logger log = LoggerFactory.getLogger(AutoWit.class);
    private final static  Integer ERROR = 0;
    private final static  Integer FALSE = 1;//正常抢单 失败
    @Resource
    private UserInfoMapper userInfoDao;
    private static final String MARS = "SHENFU";
    private static final String MARK = ":";
    @Resource
    private DealOrderMapper dealOrderMapper;
    @Autowired
    private MediumService mediumService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private WithdrawService withdrawService;
    @Autowired
    private MediumService mediumServiceImpl;
    @Autowired
    UserInfoService accountServiceImpl;
    @Autowired
    UserRateService userRateService;
    @Autowired
    private RedisUtil redisUtil;
    String bankInfo = "";
    @Autowired
    private ExceptionOrderService exceptionOrderServiceImpl;
  public Result autoWit(String bankNo, String userId , String amount,String ip){
      /**
       * 自动获取订单逻辑
       * 1，检查账户状态是否支持自动出款
       * 2，检查银行卡状态是否支持自动出款
       * 3，查看自身是否存在出款订单
       * 4，抢单最接近自己余额的出款金额
       * 5，返回出款结果
       */

      UserInfo userInfo = userInfoDao.findUserByUserId(userId);
      if(ObjectUtil.isNull(userInfo)){
          return Result.buildFailMessageCode("用户不存在",FALSE);
      }
      Integer autoWitstatusOfAccount = userInfo.getAutowitstatus();//1为开启
      if(0 == autoWitstatusOfAccount){
         return Result.buildFailMessageCode("账户自动出款关闭",FALSE);
      }
      Integer remitOrderState = userInfo.getRemitOrderState();
      if(1 != remitOrderState){
          return Result.buildFailMessageCode("账户代付状态已关闭",ERROR);
      }
      Medium bankm = new Medium();
      try {
          bankm = mediumService.findBank(bankNo);
          Integer autoWitstatusOfBank = bankm.getAutowitstatus();
          if(0 == autoWitstatusOfBank){
            return   Result.buildFailMessageCode("银行卡自动出款关闭",FALSE);
          }
      } catch (Throwable e ) {
          log.info("存在多张银行卡，当前卡号："+bankNo);
          return   Result.buildFailMessageCode("存在多张银行卡,银行卡自动出款关闭",FALSE);
      }
      Medium medium = mediumServiceImpl.findMediumId(bankm.getId()+"");

      if(ObjectUtil.isNull(medium)){
          log.info("当前银行卡不存在，当前卡号："+bankNo);
          return   Result.buildFailMessageCode("存在多张银行卡,银行卡自动出款关闭",ERROR);
      }
      String bankCheck = RSAUtils.md5(RedisConstant.Queue.HEARTBEAT + bankNo);// 验证银行 卡在线标记
      boolean hasKey = redisUtil.hasKey(bankCheck);
      if (hasKey) {
          log.info("当前银行卡不在线限制收款，当前卡号："+bankNo);
          return   Result.buildFailMessageCode("当前银行卡不在线限制收款",ERROR);
      }
      // 3，查看自身是否存在出款订单
      List<DealOrder> witOrder =  orderService.findWitOrder(userId);
      /**
       * 逻辑为：
       * 1，查看当前出款订单金额是否符合出款要求
       * 2，如果符合以最小的金额获取出款卡号
       * 3，返回出款卡号
       */
      AutoWitBean bank = getBank(witOrder,bankNo,amount,medium,ip);
      if(ObjectUtil.isNotNull(bank)){
          log.info("出款信息为："+bank.toString());
          return Result.buildSuccessResult("获取出款信息成功",bank);
      }
      //以上为查询自己的出款订单完毕 如果没有获取到 出款信息 进入下面的自动抢单环节
    /**
     * 逻辑为：
     * 1，获取到公共切款卡池的出款订单
     * 2，如果符合以最小的金额获取出款卡号
     * 3，返回出款卡号
     */
      /**
       * grabAnOrderListFind  查询出款订单， 如果出现异常 参数 true 默认查询小额出款 false 默认查询大额出款
       */
      List<DealOrder> dealOrders = orderService.grabAnOrderListFind("4", true,"");
      AutoWitBean banks = getBank(dealOrders,bankNo,amount,medium,ip);
      if(ObjectUtil.isNotNull(banks)){
          log.info("出款信息为："+banks.toString());
          return Result.buildSuccessResult("获取出款信息成功",banks);
      }
      return Result.buildFailMessageCode("抢单失败",FALSE);
    }

    String getAmount(BigDecimal dealAmount) {
        String amount = "";
        String[] split = dealAmount.toString().split("\\.");
        if (split.length == 1) {
            String s = dealAmount.toString();
            s += ".0";
            split = s.split("\\.");
        }
        String startAmount = split[0];
        String endAmount = split[1];
        int length = endAmount.length();
        if (length == 1) {//当交易金额为整小数的时候        补充0
            endAmount += "0";
        } else if (endAmount.length() > 2) {
            endAmount = "00";
        }
        amount = startAmount + "." + endAmount;//得到正确的金额
        return amount;
    }

    @Autowired
    private OrderService orderServiceImpl;
    @Autowired
    private ReWit reWit;
    AutoWitBean  getBank(List<DealOrder> witOrder,String bankNo,String amount ,  Medium medium,String ip){
        for (DealOrder wit : witOrder){
            BigDecimal dealAmount = wit.getDealAmount().setScale(2,BigDecimal.ROUND_HALF_UP);
            BigDecimal witAmount = new BigDecimal(amount);
            if(witAmount.compareTo(dealAmount) > -1 ){//符合出款要求
                String associatedId = wit.getAssociatedId();//出款订单
                Withdraw order = withdrawService.findOrderId(associatedId);
                if(ObjectUtil.isNull(order)){
                    continue;
                }
                String bankNo1 = order.getBankNo();//卡号
                String accname = order.getAccname();//入款人
                String bankName = order.getBankName();//银行
                AutoWitBean bean = new AutoWitBean();
                bean.setAmount(dealAmount.toString());
                bean.setBankNo(bankNo1);
                bean.setBankName(bankName);
                bean.setBankAccount(accname);
                bean.setOrderId(wit.getOrderId());
                String isWit = bankNo + medium.getMediumPhone() + getAmount(dealAmount);
                boolean b1 = redisUtil.hasKey(isWit);
                if(b1){
                    log.info("当前银行卡正在入款，入款金额："+dealAmount+"，当前银行卡："+bankNo);
                    return  null;
                }
                Object o = redisUtil.get("WIT:" + isWit);
                if (null != o) {
                    log.info("当前银行卡正在出款，出款金额："+dealAmount+"，当前银行卡："+bankNo);
                    return  null;
                }
                bankInfo = bankName + MARK + accname + MARK + bankNo1 + MARK + "电话" + MARK + medium.getMediumPhone();
                boolean b = orderServiceImpl.updateBankInfoByOrderIdAUTO(bankInfo, wit.getOrderId());
                if (b) {
                    String witNotify = bankNo + medium.getMediumPhone() + getAmount(wit.getDealAmount()); //代付回调成功 标记
                    log.info("当前订单号为："+witNotify+"");
                    redisUtil.set("WIT:" + witNotify, order.getOrderId(), 500);
                }
                boolean aiAuto = true;
                Result result = reWit.grabOrder(wit.getOrderQrUser(), wit,islittle(wit.getOrderQrUser()),aiAuto);
                if(result.isSuccess()){
                    exceptionOrderServiceImpl.addBankInfo(wit.getOrderId(),wit.getOrderQrUser(),"抢单成功" ,Boolean.TRUE, ip,wit.getDealAmount());
                    log.info("获取出款卡号订单及金额为："+bean.toString());
                    return  bean;
                }
            } else {
                continue;
            }
        }
      return new AutoWitBean();
    }

    boolean islittle (String userId){
        UserInfo userInfoByUserId = accountServiceImpl.findUserInfoByUserId(userId);
        if(ObjectUtil.isNotNull(userInfoByUserId)){
            String agent = userInfoByUserId.getAgent();
            UserRate userRateW = userRateService.findUserRateW(agent);
            return userRateW.getPayTypr().equals("BANK_WIT_S");
        }
        return false;

    }
}
