package alipay.manage.api.channel.deal.zongbang;

import alipay.manage.api.channel.util.shenfu.PayUtil;
import alipay.manage.api.config.NotfiyChannel;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import otc.common.PayApiConstant;
import otc.result.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 惠付通手动 代付回调处理类
 */
@RequestMapping(PayApiConstant.Notfiy.NOTFIY_API_WAI)
@RestController
public class ZongbangDpayNotifyToBank extends NotfiyChannel {
    private static final Log log = LogFactory.get();

    @RequestMapping("/zongbang-wit-noyfit")
    public String notify(HttpServletRequest req, HttpServletResponse res ) {
        String clientIP = HttpUtil.getClientIP(req);
        log.info("【当前回调ip为：" + clientIP + "】");
        Map mapIp = PayUtil.ipMap;
        Object object = mapIp.get(clientIP);
        if (ObjectUtil.isNull(object)) {
            log.info("【当前回调ip为：" + clientIP + "，固定IP登记为：" + mapIp.toString() + "】");
            log.info("【当前回调ip不匹配】");
            return "ip errer";
        }
        String apporderid = req.getParameter("apporderid");
        String tradesno = req.getParameter("tradesno");
        String status = req.getParameter("status");
        String amount = req.getParameter("amount");
        String appid = req.getParameter("appid");
        String sign = req.getParameter("sign");
        Map<String, Object> map = new HashMap<>();
        map.put("apporderid", apporderid);
        map.put("tradesno", tradesno);
        map.put("status", status);
        map.put("amount", amount);
        map.put("appid", appid);
        String createParam = PayUtil.createParam(map);
        log.info("【汇通付代付签名前参数：" + createParam + "】");
        String dpAyChannelKey = getDPAyChannelKey(apporderid);
        String md5 = PayUtil.md5(createParam + dpAyChannelKey);
        if (sign.equals(md5)) {
            if ("2".equals(status)) {
                Result result = witNotfy(apporderid, clientIP);
                if (result.isSuccess()) {
                    return "success";
                }else {
                    witNotSuccess(apporderid);
                }
            }
        } else {
            return "error";
        }
        return "end errer";
    }
}
