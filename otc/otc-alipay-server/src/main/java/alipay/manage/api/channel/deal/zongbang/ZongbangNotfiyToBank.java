package alipay.manage.api.channel.deal.zongbang;

import alipay.manage.api.channel.util.shenfu.PayUtil;
import alipay.manage.api.config.NotfiyChannel;
import alipay.manage.service.UserInfoService;
import alipay.manage.util.CheckUtils;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import otc.common.PayApiConstant;
import otc.result.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequestMapping(PayApiConstant.Notfiy.NOTFIY_API_WAI)
@RestController
public class ZongbangNotfiyToBank extends NotfiyChannel {
    private static final Log log = LogFactory.get();
    @Autowired
    private UserInfoService userInfoServiceImpl;

    @RequestMapping("/zongbang-bank-notify")
    public String notify(HttpServletRequest req, HttpServletResponse res) throws IOException {
        log.info("【收到汇通付手动支付成功回调】");
        String clientIP = HttpUtil.getClientIP(req);
        log.info("【当前回调ip为：" + clientIP + "】");
        Map map = PayUtil.ipMap;
        Object object = map.get(clientIP);
        if (ObjectUtil.isNull(object)) {
            log.info("【当前回调ip为：" + clientIP + "，固定IP登记为：" + map.toString() + "】");
            log.info("【当前回调ip不匹配】");
            return "ip errer";
        }
        String apporderid = req.getParameter("apporderid");
        String tradesno = req.getParameter("tradesno");
        String status = req.getParameter("status");
        String amount = req.getParameter("amount");
        String appid = req.getParameter("appid");
        String statusdesc = req.getParameter("statusdesc");
        String sign = req.getParameter("sign");
        Map<String, Object> mapParar = new HashMap<String, Object>();
        mapParar.put("apporderid", apporderid);
        mapParar.put("tradesno", tradesno);
        mapParar.put("status", status);
        mapParar.put("amount", amount);
        mapParar.put("appid", appid);
        mapParar.put("statusdesc", statusdesc);
        String channelKey = super.getChannelKey(apporderid);
        String md5 = CheckUtils.getSign(mapParar, channelKey);
        if (md5.equals(sign)) {
            log.info("【当前支付成功回调签名参数：" + sign + "，当前我方验证签名结果：" + md5 + "】");
            log.info("【签名成功】");
        } else {
            log.info("【当前支付成功回调签名参数：" + sign + "，当前我方验证签名结果：" + md5 + "】");
            log.info("【签名失败】");
            return "sign is error";
        }
        Result dealpayNotfiy = dealpayNotfiy(apporderid, clientIP);
        if (dealpayNotfiy.isSuccess()) {
            return  "success";
        }
        return "error";
    }
}
