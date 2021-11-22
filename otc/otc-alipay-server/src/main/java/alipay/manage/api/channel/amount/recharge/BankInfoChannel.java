package alipay.manage.api.channel.amount.recharge;

import alipay.manage.api.config.PayOrderService;
import alipay.manage.bean.DealOrderApp;
import alipay.manage.bean.util.ResultDeal;
import alipay.manage.util.bankcardUtil.CreateOrder;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otc.common.PayApiConstant;
import otc.result.Result;


/**
 * 自营转卡渠道
 */
@Component("BankInfoChannelToMe")
public class BankInfoChannel extends PayOrderService {
    @Autowired
    private CreateOrder createOrder;

    @Override
    public Result deal(DealOrderApp dealOrderApp, String channel) throws Exception {
        log.info("【进入自营渠道，当前渠道银行卡转卡，当前交易预订单为：" + dealOrderApp.getOrderId() + "】");
        if(StrUtil.isEmpty(dealOrderApp.getDealDescribe())){
            return Result.buildFailMessage("付款人为空");
        }else if("充值交易".equals(dealOrderApp.getDealDescribe())){
            orderAppEr(dealOrderApp,"付款人为空");
            return Result.buildFailMessage("付款人为空");
        }
        Result result = createOrder.dealAddOrder(dealOrderApp);
        log.info("【自营渠道返回数据为：" + result.toString() + " 】");
        if (!result.isSuccess()) {
            orderAppEr(dealOrderApp, result.getMessage());
            return result;
        }
        return Result.buildSuccessResult("支付处理中", ResultDeal.sendUrlAndPayInfo(result.getResult(), result.getMessage()));
    }
}
