package alipay.manage.api.channel.notfiy;

import alipay.manage.api.channel.util.yifu.YiFu02Util;
import alipay.manage.api.config.NotfiyChannel;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import otc.common.PayApiConstant;
import otc.result.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RequestMapping(PayApiConstant.Notfiy.NOTFIY_API_WAI)
@RestController
public class YiFu02H5Notify extends NotfiyChannel {
    @RequestMapping("/YiFuH502-notfiy")
    public String notify(HttpServletRequest request,@RequestBody Map<String,Object> data) {
        log.info("【收到YiFu02回调】");
        String clientIP = HttpUtil.getClientIP(request);
        Map map = new HashMap();
        map.put("14.192.67.110", "14.192.67.110");
        map.put("14.192.67.114", "14.192.67.114");
        Object object = map.get(clientIP);
        if (ObjectUtil.isNull(object)) {
            log.info("【当前回调ip为：" + clientIP + "，固定IP登记为：" + map.toString() + "】");
            log.info("【当前回调ip不匹配】");
            return "ip is error";
        }
        map = null;
        /**
         * 0: 待打款
         1: 打款成功
         2: 打款失败
         *
         商户订单号         order_id        是       String(16)      商户系统内部的订单号
         商户用户ID         user_id         是       String(32)      商户系统内部用户ID
         订单金额           amount          是       Int             订单金额(分)
         支付金额           real_amount     是   String(32)      实际支付金额(分)，请以此金额为准
         平台交易号         transact_id     是   String(32)      易付平台唯一交易标识
         支付时间           finished_time   是       String(32)      订单支付时间
         订单状态           status          是       Int
         签名                sign            是           String(32)      MD5签名结果
         */
        //{
        // amount=100000,
        // order_id=C1596119675803817527,
        // pay_time=2020-07-30 22:35:25,
        // real_amount=100000, status=1,
        // transact_id=aca047bcdebb68598f5f719b9e86f8e4,
        // user_id=2u7rMduh,
        // sign=7693189d0ed57388f8ab71464a1e8f29}
        log.info("【易付02回调参数："+data.toString()+"】");
        String order_id = data.get("order_id").toString();
        String user_id = data.get("user_id").toString();
        String amount = data.get("amount").toString();
        String real_amount = data.get("real_amount").toString();
        String transact_id = data.get("transact_id").toString();
        String pay_time = data.get("pay_time").toString();
        String status = data.get("status").toString();
        String sign = data.get("sign").toString();
        Map<String,Object> yifuMap = new HashMap<>();
        yifuMap.put("order_id",order_id);
        yifuMap.put("user_id",user_id);
        yifuMap.put("amount",amount);
        yifuMap.put("real_amount",real_amount);
        yifuMap.put("transact_id", transact_id);
        yifuMap.put("pay_time",pay_time);
        yifuMap.put("status",status);
        String channelKey = super.getChannelKey(order_id);
        String param = YiFu02Util.createParam(yifuMap);
        log.info("【易付2号加签前的参数："+param+"】");
        String s = YiFu02Util.md5(param + "key=" + channelKey);
        if(s.equals(sign)) {
            log.info("【当前支付成功回调签名参数："+sign+"，当前我方验证签名结果："+s+"】");
            log.info("【签名成功】");
        } else {
            log.info("【当前支付成功回调签名参数："+sign+"，当前我方验证签名结果："+s+"】");
            log.info("【签名失败】");
            return  "sign is error";
        }
        if("1".equals(status)){
            Result dealpayNotfiy = dealpayNotfiy(order_id, clientIP, "yifu02回调订单成功");
            if(dealpayNotfiy.isSuccess()) {
                log.info("【订单回调修改成功，订单号为 ："+order_id+" 】");
                return "success";
            }
        }
        return "error";
    }
}
