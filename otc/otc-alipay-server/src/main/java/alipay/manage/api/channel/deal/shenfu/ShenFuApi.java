package alipay.manage.api.channel.deal.shenfu;

import alipay.config.redis.RedisUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import otc.common.PayApiConstant;

import java.util.Map;

@RequestMapping(PayApiConstant.Alipay.ORDER_API + "/ShenFuBankCardInfo")
@RestController
public class ShenFuApi {
    public static final Log log = LogFactory.get();
    private static final String MARS = "SHENFU";
    @Autowired
    RedisUtil redis;

    @GetMapping()
    public ShenFuBankInfo getBankCardInfo(String orderId) {
        ShenFuBankInfo info = new ShenFuBankInfo();
        log.info("【收到用户请求申付银行卡参数，当前请求订单号为：" + orderId + "】");
        try {
            Map<Object, Object> hmget = redis.hmget(MARS + orderId);
            info.setBank_name(hmget.get("bank_name").toString());
            info.setCard_no(hmget.get("card_no").toString());
            info.setCard_user(hmget.get("card_user").toString());
            info.setMoney_order(hmget.get("money_order").toString());
            info.setAddress(hmget.get("address").toString());
            info.setNo_order(hmget.get("no_order").toString());
            info.setOid_partner(hmget.get("oid_partner").toString());
        } catch (Exception e) {
            log.info("【请求缓存银行卡数据失败，当前请求订单号：" + orderId + "】");
        }
        log.info("【当前银行卡信息为：" + info.toString() + "】");
        return info;
    }
}

/**
 * // "bank_name":"建设银行",
 * // "card_no":"6217003110033402130",
 * // "card_user":"农洪共",
 * // "money_order":"1000.00",
 * // "no_order":"C20210212151359978932599",
 * // "oid_partner":"202102101152580034",
 */
class ShenFuBankInfo {
    private String bank_name;
    private String card_no;
    private String card_user;
    private String money_order;
    private String no_order;
    private String oid_partner;
    private String address;

    public String getNo_order() {
        return no_order;
    }

    public void setNo_order(String no_order) {
        this.no_order = no_order;
    }

    public String getOid_partner() {
        return oid_partner;
    }

    public void setOid_partner(String oid_partner) {
        this.oid_partner = oid_partner;
    }

    public String getMoney_order() {
        return money_order;
    }

    public void setMoney_order(String money_order) {
        this.money_order = money_order;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCard_user() {

        return card_user;
    }

    public void setCard_user(String card_user) {
        this.card_user = card_user;
    }

    public String getCard_no() {
        return card_no;
    }

    public void setCard_no(String card_no) {
        this.card_no = card_no;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    @Override
    public String toString() {
        return "ShenFuBankInfo{" +
                "bank_name='" + bank_name + '\'' +
                ", card_no='" + card_no + '\'' +
                ", card_user='" + card_user + '\'' +
                ", money_order='" + money_order + '\'' +
                ", no_order='" + no_order + '\'' +
                ", oid_partner='" + oid_partner + '\'' +
                '}';
    }
}