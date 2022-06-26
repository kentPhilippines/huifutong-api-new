package alipay.manage.api.channel.amount.recharge;

import alipay.manage.api.config.PayOrderService;
import alipay.manage.bean.DealOrder;
import alipay.manage.bean.DealOrderApp;
import alipay.manage.bean.util.ResultDeal;
import alipay.manage.service.OrderService;
import alipay.manage.util.bankcardUtil.CreateOrder;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import otc.common.PayApiConstant;
import otc.result.Result;

/**
 * 自营渠道支付宝转卡
 */
@Component("AlipayToBank")
public class AlipayToBank extends PayOrderService {
    @Value("${otc.payInfo.url}")
    public   String url;
    @Autowired
    private CreateOrder createOrder;
    @Autowired
    private OrderService orderServiceImpl;
    @Override
    public Result deal(DealOrderApp dealOrderApp, String channel) throws Exception {
        log.info("【进入自营渠道，支付宝转卡，当前交易预订单为：" + dealOrderApp.getOrderId() + "】");
        dealOrderApp.setDealDescribe("付款人：扫码");
        Result result = createOrder.dealAddOrder(dealOrderApp);
        log.info("【自营渠道返回数据为：" + result.toString() + " 】");
        if (!result.isSuccess()) {
            orderAppEr(dealOrderApp, dealOrderApp.getDealDescribe());
            return result;
        }
        DealOrder assOrder = orderServiceImpl.findAssOrder(dealOrderApp.getOrderId());
        String orderId = assOrder.getOrderId();
        return Result.buildSuccessResult("支付处理中", ResultDeal.sendUrl(url+ "/pay/alipayToCard?order_id="+orderId ));
    }




}
