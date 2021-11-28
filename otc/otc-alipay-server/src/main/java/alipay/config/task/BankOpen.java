package alipay.config.task;

import alipay.config.redis.RedisUtil;
import alipay.manage.api.channel.deal.jiabao.RSAUtil;
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
import otc.util.RSAUtils;

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


    /**
     * 银行卡余额自动更新定时任务
     */
    public void updateBnakAmount() {
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