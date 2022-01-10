package alipay.config.task;

import alipay.config.redis.RedisUtil;
import alipay.manage.api.channel.deal.jiabao.RSAUtil;
import alipay.manage.bean.DealOrder;
import alipay.manage.service.MediumService;
import alipay.manage.service.OrderService;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.bean.alipay.Medium;
import otc.util.RSAUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BankOpen {
    private static final Log log = LogFactory.get();
    public static List<String> BANK_LIST = new ArrayList();



    @Autowired private OrderService orderServiceImpl;

    public static final String HEARTBEAT = "HEARTBEAT_";
    @Autowired
    private RedisUtil redis;

    static {
      //  BANK_LIST.add("623059138002823992"); //fang777
      //  BANK_LIST.add("6230361215006774818");  //fang777
    }

    void open() {
        for (String bank : BANK_LIST) {
            String s = RSAUtils.md5(bank);
            ThreadUtil.execute(() -> {
                boolean set = redis.set(HEARTBEAT + bank, HEARTBEAT, 15);//设置心跳过期时间1分钟
                log.info("心跳检测值：" + HEARTBEAT + bank + "结果：" + set);

            });
        }
    }


    @Autowired
    private  MediumService mediumServiceImpl;

    /**
     * 银行卡余额自动更新定时任务
     */
    public void updateBnakAmount(DealOrder order) {
        log.info("进入结算银行卡余额的方法,当前订单号："+ order.getOrderId());
        /**
         *  更新核心字段
         *  1,业务余额   =  入款订单 - 出款订单 (成功的订单)
         *  2,当日总入款 = 当日的入款订单汇总
         *  3,当日总出款  =  当日出款订单汇总
         *  4,真实余额 = 监控的实时余额
         */
        //一个星期做好系统自动对账
        /**
         *
         * 前提， 现有 银行卡更新逻辑放弃
         * 业务余额更新逻辑
         * 1,根据时间查询 查询一段时间内的  交易订单
         * 2,查询当前银行卡的 余额数据
         * 3,根据订单类型和和金额 对银行卡的余额数据进行变更
         * 4,查询当前银行卡的真实流水记录表, 如果不存在 则显示为监控余额异常
         * 4,生成银行卡流水数据表
         * 5,对当前订单数据进行业务余额和银行卡实际余额标记
         * 6,修改订单为以结算
         * 7,银行卡余额变更完成
         */
        //查询所有未结算的订单 每次更新 10 条数据
       //  orderServiceImpl.

        //先更新银行卡余额
        //更新交易订单业务余额
        //生成银行卡简要流水

        //银行卡 当日入款
        // 累计入款
        //当日出款
        // 累计出款
        // 业务余额 = 当日入款 -当日出款 +
        //昨日余额 =  当日入款 - 当日出款 + 昨日余额   依次类推
        // 也就是   昨日余额不用更新 有每日定时清算直接算出   今日余额也直接算出
        try {
            String orderQr = order.getOrderQr();
            Medium bankInfo = null;
            String payType = order.getRetain1();
            if(!payType.contains("ALIPAY")){
                String[] split = orderQr.split(":");//
                String bankNo  = split[2];
                log.info("银行卡号："+bankNo);
                 bankInfo =  mediumServiceImpl.findMedByBankNo(bankNo);
            }else{
                bankInfo = mediumServiceImpl.findMediumById(orderQr);
            }
            String orderType = order.getOrderType();
            boolean flag = Boolean.FALSE;
            if( "1".equals(orderType) ) {//入款更新
                BigDecimal toDayDeal = bankInfo.getToDayDeal();
                BigDecimal sumDayDeal = bankInfo.getSumDayDeal();
                BigDecimal addToDayDeal = toDayDeal.add(order.getDealAmount());
                BigDecimal addSumDayDeal = sumDayDeal.add(order.getDealAmount());
                bankInfo.setToDayDeal(addToDayDeal);
                bankInfo.setSumDayDeal(sumDayDeal);
                flag =   mediumServiceImpl.upBuAmount(bankInfo.getVersion(),bankInfo.getId(),addToDayDeal,addSumDayDeal);
            } else {//出款更新
                BigDecimal toDayWit = bankInfo.getToDayWit();
                BigDecimal sumDayWit = bankInfo.getSumDayWit();
                BigDecimal addToDayWit = toDayWit.add(order.getDealAmount());
                BigDecimal addSumDayWit = sumDayWit.add(order.getDealAmount());
                bankInfo.setToDayWit(addToDayWit);
                bankInfo.setSumDayDeal(addSumDayWit);
                flag =   mediumServiceImpl.upBuAmountWit(bankInfo.getVersion(),bankInfo.getId(),addToDayWit,addSumDayWit);
            }
            //更新订单业务余额
            BigDecimal bu = bankInfo.getToDayDeal().subtract(bankInfo.getToDayWit()).add(bankInfo.getYseToday());
            orderServiceImpl.updateSystemBankAmount(bu,order.getOrderId());
        }catch (Exception s ){
            log.info("四方交易订单");
        }

        // 这里的银行卡流水 暂未完善
    }
}


class Heart {
    private String MD5;

    public String getMD5() {
        return MD5;
    }

    public void setMD5(String MD5) {
        this.MD5 = MD5;
    }
}