package alipay.manage.api.channel.amount.recharge.alipay;

import alipay.manage.api.config.PayOrderService;
import alipay.manage.bean.DealOrderApp;
import org.springframework.stereotype.Component;
import otc.result.Result;


@Component("AlipayQr")
public class AlipayQr extends PayOrderService {
    @Override
    public Result deal(DealOrderApp dealOrderApp, String channel) throws Exception {
        Result result = dealAlipayScan(dealOrderApp);
        return result;
    }

}
