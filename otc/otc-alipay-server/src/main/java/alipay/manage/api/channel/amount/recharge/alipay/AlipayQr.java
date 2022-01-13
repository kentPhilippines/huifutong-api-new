package alipay.manage.api.channel.amount.recharge.alipay;

import alipay.manage.api.config.PayOrderService;
import alipay.manage.bean.DealOrderApp;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.stereotype.Component;
import otc.result.Result;


@Component("AlipayQr")
public class AlipayQr extends PayOrderService {
    public static final Log log = LogFactory.get();
    @Override
    public Result deal(DealOrderApp dealOrderApp, String channel) throws Exception {
        log.info("当前进入支付宝扫码处理类");
        log.info("【当前处理订单："+dealOrderApp.toString()+"】");
        try {
            boolean b = addOrder(dealOrderApp, dealOrderApp.getOrderIp());
            if(b){
                Result result = dealAlipayScan(dealOrderApp);
                return result;
            }else{
                orderEr(dealOrderApp, "错误消息：暂无对应资源"  );
                return  Result.buildFailMessage("暂无对应资源");
            }
        }catch (Throwable t){
            orderEr(dealOrderApp, "错误消息：暂无对应资源" );
            return  Result.buildFailMessage("暂无对应资源");
        }
    }

}
