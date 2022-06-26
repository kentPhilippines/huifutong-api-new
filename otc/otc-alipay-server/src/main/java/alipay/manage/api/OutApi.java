package alipay.manage.api;

import alipay.config.redis.RedisUtil;
import alipay.manage.api.feign.QueueServiceClien;
import alipay.manage.bean.UserFund;
import alipay.manage.bean.UserInfo;
import alipay.manage.service.BankListService;
import alipay.manage.service.MediumService;
import alipay.manage.service.UserFundService;
import alipay.manage.service.UserInfoService;
import alipay.manage.util.QueueUtil;
import alipay.manage.util.bankcardUtil.BankUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import otc.bean.alipay.Medium;
import otc.common.RedisConstant;
import otc.result.Result;
import otc.util.RSAUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequestMapping("/out")
@RestController
public class

OutApi {
    private static final Log log = LogFactory.get();
    private static final String UTF_8 = "utf-8";
    private static final String ENCODE_TYPE = "md5";
    @Autowired UserInfoService userInfoServiceImpl;
    @Autowired
    private UserFundService uerFundServiceImpl;
    private static final String REDISKEY_QUEUE = RedisConstant.Queue.QUEUE_REDIS;//卡商入列标识

    public static String md5(String a) {
        String c = "";
        MessageDigest md5;
        String result = "";
        try {
            md5 = MessageDigest.getInstance(ENCODE_TYPE);
            md5.update(a.getBytes(UTF_8));
            byte[] temp;
            temp = md5.digest(c.getBytes(UTF_8));
            for (int i = 0; i < temp.length; i++) {
                result += Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
        }
        return result;
    }


    @Autowired
    BankUtil bankUtil;
    @Autowired
    private QueueServiceClien queueServiceClienFeignImpl;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private MediumService mediumServiceImpl;

    @Value("${otc.payInfo.url}")
    public   String url;
    @RequestMapping("/geturl")
    public Result geturl() {
        return Result.buildSuccessResult( url);
    }



    /**
     * 通过顶代卡商查询信息
     *
     * @param cardInfo
     * @return
     */
    @RequestMapping("/findQueue")
    public Result findQueue(String cardInfo) {
        try {
            List<BankInfo> list = new ArrayList<>();
            Map<String, Medium> medMap = mediumServiceImpl.findBankOpen();
            List<UserFund> fundAllQr  =  uerFundServiceImpl.findFundAllQr();
            ConcurrentHashMap<String, UserFund> usercollect = fundAllQr.stream().collect(Collectors.toConcurrentMap(UserFund::getUserId, Function.identity(), (o1, o2) -> o1, ConcurrentHashMap::new));
            if (StrUtil.isEmpty(cardInfo)) {
                List<UserInfo> agentQr = userInfoServiceImpl.findAgentQr();

                for (UserInfo info : agentQr) {
                    String queueKey = REDISKEY_QUEUE + info.getUserId();
                    log.info("【查询队列数据key：" + queueKey + "】");
                    LinkedHashSet<ZSetOperations.TypedTuple<Object>> zRangeWithScores = redisUtil.zRangeWithScores(queueKey, 0, -1);//linkedhashset 保证set集合查询最快
                    List<ZSetOperations.TypedTuple<Object>> collect = zRangeWithScores.stream().collect(Collectors.toList());
                    log.info("队列数据为："+ collect.size());
                    for (ZSetOperations.TypedTuple type : collect) {
                        log.info( type.toString());
                        Object value = type.getValue();
                        Double score = type.getScore();
                        log.info("队列数据为 value："+ value);
                        log.info("队列数据为 score："+ score);
                        if( null == value){
                            continue;
                        }
                        BankInfo bank = new BankInfo();
                        bank.setBankId(value.toString());
                        Medium medium = medMap.get(value.toString());
                        if(null == medium){
                            continue;
                        }
                        bank.setScore(score);
                        bank.setGourp(queueKey);
                        bank.setAmount(medium.getMountNow());
                        bank.setUserId(medium.getQrcodeId());
                        bank.setBankAccount(medium.getMediumHolder());
                        bank.setBankName(medium.getAccount());
                        UserFund userFund = usercollect.get(medium.getQrcodeId());
                        if(null != userFund){
                            BigDecimal subtract = userFund.getAccountBalance().subtract(userFund.getSumProfit());
                            bank.setFund(subtract+"");
                        }
                        try {
                            bank.setStartFund( medium.getStartAmount().toString());
                            bank.setDeposit(userFund.getDeposit().toString());
                            if(null != bankUtil.getUserAmount(medium.getQrcodeId())){
                                bank.setFreezeBalance(bankUtil.getUserAmount(medium.getQrcodeId()).toString());
                            }
                        }catch (Exception e ){

                        }
                        log.info("【银行卡："+value.toString()+"】");
                        list.add(bank);
                    }
                }
            } else {
                String queueKey = REDISKEY_QUEUE + cardInfo;
                LinkedHashSet<ZSetOperations.TypedTuple<Object>> zRangeWithScores = redisUtil.zRangeWithScores(queueKey, 0, -1);//linkedhashset 保证set集合查询最快
                List<ZSetOperations.TypedTuple<Object>> collect = zRangeWithScores.stream().collect(Collectors.toList());
                for (ZSetOperations.TypedTuple type : collect) {
                    log.info( type.toString());
                    Object value = type.getValue();
                    Double score = type.getScore();
                    log.info("队列数据为 value："+ value);
                    log.info("队列数据为 score："+ score);
                    if( null == value){
                        continue;
                    }
                    BankInfo bank = new BankInfo();
                    bank.setBankId(value.toString());
                    Medium medium = medMap.get(value.toString());
                    if(null == medium){
                        continue;
                    }
                    bank.setUserId(medium.getQrcodeId());
                    bank.setScore(score);
                    bank.setGourp(queueKey);
                    bank.setAmount(medium.getMountNow());
                    bank.setBankAccount(medium.getMediumHolder());
                    bank.setBankName(medium.getAccount());
                    UserFund userFund = usercollect.get(medium.getQrcodeId());
                    if(null != userFund){
                        BigDecimal subtract = userFund.getAccountBalance().subtract(userFund.getSumProfit());
                        bank.setFund(subtract+"");
                    }
                    try {
                        bank.setStartFund(bankUtil.limitAmountOpenAmount(medium.getQrcodeId()));
                        bank.setDeposit(userFund.getDeposit().toString());
                        if(null != bankUtil.getUserAmount(medium.getQrcodeId())){
                            bank.setFreezeBalance(bankUtil.getUserAmount(medium.getQrcodeId()).toString());
                        }
                    }catch (Exception e ){

                    }
                    log.info("【银行卡："+value.toString()+"】");
                    list.add(bank);
                }
            }
            return Result.buildSuccessResult("请求成功", list);
        } catch (Exception e) {
             log.error("查询在线卡池异常",e);
        }
        return Result.buildFail();

    }

    @RequestMapping("/pushCard")
    public Result pushCard(String cardInfo, String userId) {

        if (StrUtil.isEmpty(cardInfo)) {
            return Result.buildFailMessage("数据为空");
        }
        boolean a = redisUtil.zRemove(userId, cardInfo) > 0;
        if (a) {
            Boolean zAdd = redisUtil.zAdd(userId, cardInfo, 0);
        }
        return Result.buildSuccessResult("推送成功");
    }


    @GetMapping("/updatePassword")
    public Result updatePassword(String userId) {
        List<UserInfo> userList = userInfoServiceImpl.finauserAll(userId);
        int a = 0;
        Map map = new HashMap<>();
        for (UserInfo user : userList) {
            List<String> strings = RSAUtils.genKeyPair();
            String publickey = strings.get(0);
            String privactkey = strings.get(1);
            String key = md5(IdUtil.objectId().toUpperCase() + IdUtil.objectId().toUpperCase()).toUpperCase();
            log.info("【商户" + user.getUserId() + ",执行更新密钥方法】");
            boolean flag = userInfoServiceImpl.updateDealKey(user.getUserId(), publickey, privactkey, key);
            if (flag) {
                log.info("【商户" + user.getUserId() + ",执行更新密钥方法,成功】");
            } else {
                a++;
                log.info("【商户" + user.getUserId() + ",执行更新密钥方法,失败】");
                map.put(user.getUserId(), user.getUserId());
            }
        }
        if (a == 0) {
            return Result.buildFailMessage("更新密钥成功");
        }
        return Result.buildFailMessage("更换密钥失败,失败个数,失败商户详情：" + map.toString());
    }
}

class BankInfo {
    private String bankId;
    private String gourp;
    private String userId;
    private String startFund;//起收额度
    private String fund;//小组额度
    private String deposit;//小组押金
    private String status;//小组额度      1 正常     12 仅收单    13 仅出单 0 暂停
    private String freezeBalance;// 锁定额度

    private Double score;

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getFreezeBalance() {
        return freezeBalance;
    }

    public void setFreezeBalance(String freezeBalance) {
        this.freezeBalance = freezeBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFund() {
        return fund;
    }

    public void setFund(String fund) {
        this.fund = fund;
    }

    public String getStartFund() {
        return startFund;
    }

    public void setStartFund(String startFund) {
        this.startFund = startFund;
    }

    private String bankName;            //银行名
    private String bankAccount;         //银行账号
    private String amount;         //参考余额

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGourp() {
        return gourp;
    }

    public void setGourp(String gourp) {
        this.gourp = gourp;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }
}
