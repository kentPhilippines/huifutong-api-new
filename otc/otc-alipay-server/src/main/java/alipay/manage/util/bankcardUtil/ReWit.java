package alipay.manage.util.bankcardUtil;

import alipay.config.redis.RedisUtil;
import alipay.manage.bean.DealOrder;
import alipay.manage.bean.UserRate;
import alipay.manage.service.MediumService;
import alipay.manage.service.OrderService;
import alipay.manage.service.UserRateService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.bean.alipay.Medium;
import otc.result.Result;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Component
public class ReWit {
    Logger log = LoggerFactory.getLogger(ReWit.class);
    @Autowired
    private UserRateService userRateServiceImpl;
    @Autowired
    private OrderService orderServiceImpl;
    @Resource
    RedisUtil redisUtil;
    @Autowired private MediumService mediumServiceImpl;
  public Result rewit(String userId){
      log.info("开始重新配单，当前重新配单账号："+userId);
      if(StrUtil.isEmpty(userId)){
          return  Result.buildFailMessage("必传参数为空");
      }
      List<DealOrder> witOrderByUserId = orderServiceImpl.findWitOrderByUserId(userId);
      if(CollUtil.isNotEmpty(witOrderByUserId)){
          log.info("当前账号存在代付订单，不予分配订单："+userId);
          return  Result.buildSuccess();
      }
      List<DealOrder> dealOrders = orderServiceImpl.findWitOrderByUserId("zhongbang-bank");//所有的代付订单
      if(CollUtil.isEmpty(dealOrders)){
          log.info("当前不存在代付订单，");
          return Result.buildFail();
      }
      List<Medium> allMedium = mediumServiceImpl.findAllMedium(userId);
      if(CollUtil.isEmpty(allMedium)){
          log.info("当前不存在出款卡");
          return Result.buildFail();
      }
      for (Medium medium : allMedium){
          BigDecimal witAmount =new BigDecimal(medium.getWitAmount()) ;
          for (DealOrder order  : dealOrders){
              BigDecimal dealAmount = order.getDealAmount();
              if(witAmount.compareTo(dealAmount) > 0){//这里走切单逻辑
                  String   bankInfo = medium.getAccount() + ":" +   medium.getMediumHolder() + ":" + medium.getMediumNumber();
                  order.setOrderQrUser(medium.getQrcodeId());
                  order.setOrderQr(bankInfo);
                  UserRate rateFeeType = userRateServiceImpl.findUserRateWitByUserIdApp(order.getOrderAccount());
                  UserRate channnelFee = userRateServiceImpl.findUserRateWitByUserIdApp(medium.getQrcodeId());
                  BigDecimal fee = channnelFee.getFee();                  //渠道成本费率  卡出款费率
                  log.info("【当前渠道成本费率为：" + fee + "】");
                  fee = fee.multiply(dealAmount);
                  /*长久缓存*/
                  BigDecimal fee1 = rateFeeType.getFee();  //商户出款 手续费
                  log.info("【当前出款账户交易扣除手续费为：" + fee1 + "】");
                  log.info("【当前出款账户交易实际手续费为：" + dealAmount + "】");
                  log.info("【当前渠道收取手续费：" + fee + "】");
                  log.info("【当前收取商户手续费：" + fee1 + "】");
                  BigDecimal subtract = fee1.subtract(fee);
                  log.info("【当前订单系统盈利：" + subtract + "】");
                  order.setFeeId(channnelFee.getId());
                  order.setRetain2(fee.toString());  //出款款渠道成本， 即为卡商结算费率
                  order.setRetain3(subtract.toString());  //当前系统利润       =      收款费率金额  - 渠道成本
                  String orderMark = "ORDER:" + order.getOrderQrUser() + ":AUTO";
                  redisUtil.set(orderMark, orderMark, 10);//金额锁定时间标记     , 如果在20分钟内回调就会删除锁定金额
                  boolean flag =   orderServiceImpl.updateWitQr(order);
                if(flag){
                    return Result.buildSuccess();
                }
              }
          }
      }
      return Result.buildSuccess();
  }


   public Result grabOrder(String userId,DealOrder order){
       BigDecimal dealAmount = order.getDealAmount();
       order.setOrderQrUser(userId);
       order.setOrderQr("");
       UserRate rateFeeType = userRateServiceImpl.findUserRateWitByUserIdApp(order.getOrderAccount());
       UserRate channnelFee = userRateServiceImpl.findUserRateWitByUserIdApp(userId);
       BigDecimal fee = channnelFee.getFee();                  //渠道成本费率  卡出款费率
       log.info("【当前渠道成本费率为：" + fee + "】");
       fee = fee.multiply(order.getDealAmount());
       /*长久缓存*/
       BigDecimal fee1 = rateFeeType.getFee();  //商户出款 手续费
       log.info("【当前出款账户交易扣除手续费为：" + fee1 + "】");
       log.info("【当前出款账户交易实际手续费为：" + dealAmount + "】");
       log.info("【当前渠道收取手续费：" + fee + "】");
       log.info("【当前收取商户手续费：" + fee1 + "】");
       BigDecimal subtract = fee1.subtract(fee);
       log.info("【当前订单系统盈利：" + subtract + "】");
       order.setFeeId(channnelFee.getId());
       order.setRetain2(fee.toString());  //出款款渠道成本， 即为卡商结算费率
       order.setRetain3(subtract.toString());  //当前系统利润       =      收款费率金额  - 渠道成本
       String orderMark = "ORDER:" + order.getOrderQrUser() + ":AUTO";
       redisUtil.set(orderMark, orderMark, 10);//金额锁定时间标记     , 如果在20分钟内回调就会删除锁定金额
       order.setGrabOrder(1);//抢单
       boolean flag =   orderServiceImpl.updateGrabOrder(order);
       if(flag){
           return Result.buildSuccess();
       }
       return Result.buildSuccess();
    }



}
