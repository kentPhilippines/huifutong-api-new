package alipay.manage.contorller;

import alipay.config.redis.RedisUtil;
import alipay.manage.bean.DealOrder;
import alipay.manage.bean.UserFund;
import alipay.manage.bean.UserInfo;
import alipay.manage.bean.UserRate;
import alipay.manage.bean.util.PageResult;
import alipay.manage.service.*;
import alipay.manage.util.QueueUtil;
import alipay.manage.util.SessionUtil;
import alipay.manage.util.bankcardUtil.ReWit;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import otc.bean.alipay.Medium;
import otc.bean.dealpay.Withdraw;
import otc.common.PayApiConstant;
import otc.common.RedisConstant;
import otc.result.Result;
import otc.util.RSAUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/qrcode")
public class QrcodeContorller {
    Logger log= LoggerFactory.getLogger(QrcodeContorller.class);
    @Value("${otc.payInfo.url}")
    public   String url;
    private static final String MARS = "SHENFU";
    private static final String MARK = ":";
    @Autowired
    private SessionUtil sessionUtil;
    @Autowired
    private MediumService mediumServiceImpl;
    @Autowired
    private WithdrawService withdrawServiceImpl;
    @Autowired
    private UserFundService userFundService;
    @Autowired
    private UserInfoService userInfoServer;
    @Autowired
    private ReWit reWit;
    @Autowired
    private ExceptionOrderService exceptionOrderServiceImpl;
    @Autowired UserInfoService accountServiceImpl;
    @Autowired UserRateService userRateService;
    @GetMapping("/findIsMyQrcodePage")
    @ResponseBody
    public Result findIsMyQrcodePage(HttpServletRequest request, String pageNum, String pageSize) {
        UserInfo user = sessionUtil.getUser(request);
        if (ObjectUtil.isNull(user)) {
            return Result.buildFailResult("用户未登录");
        }
        PageHelper.startPage(Integer.valueOf(pageNum), Integer.valueOf(pageSize));
        List<Medium> qmList = mediumServiceImpl.findIsMyMediumPage(user.getUserId());
        //List<QrCode> qrList = qrCodeServiceImpl.findIsMyQrcodePage(qrcode);
        PageInfo<Medium> pageInfo = new PageInfo<Medium>(qmList);
        PageResult<Medium> pageR = new PageResult<Medium>();
        pageR.setContent(pageInfo.getList());
        pageR.setPageNum(pageInfo.getPageNum());
        pageR.setTotal(pageInfo.getTotal());
        pageR.setTotalPage(pageInfo.getPages());
        return Result.buildSuccessResult(pageR);
    }
    /**
     * <p>远程队列入列</p>
     * @param request
     * @param id
     * @return
     */
    @GetMapping("/updataMediumStatusSu")
    @ResponseBody
    public Result updataMediumStatusSu(HttpServletRequest request, String id) {
        UserInfo user = sessionUtil.getUser(request);
        if (ObjectUtil.isNull(user)) {
            return Result.buildFailResult("用户未登录");
        }
        Medium med = new Medium();
        med.setId(Integer.valueOf(id));
        Result addNode = queueUtil.addNode(med);
        return addNode;
    }

    @Autowired
    private QueueUtil queueUtil;
    @Autowired
    private OrderService orderServiceImpl;
    @Autowired
    private WithdrawService withdrawService;
    @Autowired
    private RedisUtil redis;

    @GetMapping("/getBankCardList")
    @ResponseBody
    public Result getBankCardList(HttpServletRequest request, String id) {
        UserInfo user = sessionUtil.getUser(request);
        if (ObjectUtil.isNull(user)) {
            return Result.buildFailResult("用户未登录");
        }
        List<Medium> isMyMediumPage = mediumServiceImpl.findIsMyMediumPage(user.getUserId());
        return Result.buildSuccessResult(isMyMediumPage);
    }

    /**
     * <p>远程队列出列</p>
     *
     * @param request
     * @param id
     * @return
     */
    @GetMapping("/updataMediumStatusEr")
    @ResponseBody
    public Result updataMediumStatusEr(HttpServletRequest request, String id) {
        UserInfo user = sessionUtil.getUser(request);
        if (ObjectUtil.isNull(user)) {
            return Result.buildFailResult("用户未登录");
        }
        Medium med = new Medium();
        med.setId(Integer.valueOf(id));
        Result addNode = queueUtil.pop(med);
        return addNode;
    }

    @Autowired
    private RedisUtil redisUtil;
    @GetMapping("/setBankCard")
    @ResponseBody
    public Result setBankCard(HttpServletRequest request, String bankCard, String orderId) {
        UserInfo user = sessionUtil.getUser(request);
        if (ObjectUtil.isNull(user)) {
            return Result.buildFailResult("用户未登录");
        }
        log.info("银行卡号："+bankCard);
        DealOrder order = orderServiceImpl.findOrderByOrderId(orderId);
        try {
            String mediumNumber = "";
            String mediumHolder = "";
            String account = "";
            String mediumPhone = "";
            Withdraw wit = withdrawServiceImpl.findOrderId(order.getAssociatedId());
            if (StrUtil.isEmpty(order.getOrderQr())) {// 第一次进入绑定 银行卡， 这个地方需要 验证 银行卡是否在线
                if(StrUtil.isEmpty(bankCard)){
                    bankCard =order.getOrderQr().split(MARK)[2];
                }
                Result result = enterWit(order, bankCard, orderId,request);
                if(!result.isSuccess()){
                    return  result;
                }
            } else {
                String[] split = order.getOrderQr().split(MARK);
                mediumNumber = split[2];//卡号
                mediumHolder = split[1];//开户人
                account = split[0];//开户行
                Medium mediumByMediumNumber = mediumServiceImpl.findMediumByMediumNumber(mediumNumber);
                Result result = enterWit(order, mediumByMediumNumber.getId().toString(), orderId,request);
                if(!result.isSuccess()){
                    return  result;
                }
            }
            Map cardmap = new HashMap();
            cardmap.put("bank_name", wit.getBankName());
            cardmap.put("card_no", wit.getBankNo());
            cardmap.put("card_user", wit.getAccname());
            cardmap.put("money_order", order.getDealAmount());
            cardmap.put("no_order", orderId);
            cardmap.put("oid_partner", orderId);
            cardmap.put("address", wit.getBankName());
            redis.hmset(MARS + orderId, cardmap, 600);
        }catch (Throwable t ){
            log.error("选卡异常",t);
            log.info("出款信息异常，当前订单号："+orderId);
            exceptionOrderServiceImpl.addFindBankInfo(order.getOrderId(),user.getUserId()," 当前报错：选卡异常",Boolean.FALSE, HttpUtil.getClientIP(request),order.getDealAmount());
        }
        try {
            ThreadUtil.execute(()->{
                log.info("出款卡锁定，当前订单号："+orderId);
                orderServiceImpl.enterOrderLock( orderId);
            });
        }catch (Throwable e ){
            log.info("出款卡锁定异常");
        }
        exceptionOrderServiceImpl.addFindBankInfo(order.getOrderId(),user.getUserId(),"当前用户成功选卡出款，请关注",Boolean.TRUE, HttpUtil.getClientIP(request),order.getDealAmount());
        return Result.buildSuccessResult(url + "/pay?orderId=" + orderId + "&type=203");
    }
    String getAmount(BigDecimal dealAmount) {
        String amount = "";
        String[] split = dealAmount.toString().split("\\.");
        if (split.length == 1) {
            String s = dealAmount.toString();
            s += ".0";
            split = s.split("\\.");
        }
        String startAmount = split[0];
        String endAmount = split[1];
        int length = endAmount.length();
        if (length == 1) {//当交易金额为整小数的时候        补充0
            endAmount += "0";
        } else if (endAmount.length() > 2) {
            endAmount = "00";
        }
        amount = startAmount + "." + endAmount;//得到正确的金额
        return amount;
    }

    boolean islittle (String userId){
        UserInfo userInfoByUserId = accountServiceImpl.findUserInfoByUserId(userId);
        if(ObjectUtil.isNotNull(userInfoByUserId)){
            String agent = userInfoByUserId.getAgent();
            UserRate userRateW = userRateService.findUserRateW(agent);
            return userRateW.getPayTypr().equals("BANK_WIT_S");
        }
        return false;

    }



    Result  enterWit( DealOrder order ,String bankCard,String orderId,HttpServletRequest request){
        String mediumNumber = "";
        String mediumHolder = "";
        String account = "";
        String mediumPhone = "";
        Medium mediumId = mediumServiceImpl.findMediumId(bankCard);
        mediumNumber = mediumId.getMediumNumber();//卡号
        mediumHolder = mediumId.getMediumHolder();//开户人
        account = mediumId.getAccount();//开户行
        mediumPhone = mediumId.getMediumPhone();
        String bankInfo = "";
        String isWit = mediumNumber  + getAmount(order.getDealAmount());
        boolean b1 = redisUtil.hasKey(isWit);
        if (b1) {
            exceptionOrderServiceImpl.addFindBankInfo(order.getOrderId(),order.getOrderQrUser()," 当前报错：当前银行卡限制出款，请用户稍等几分钟后重新拉单",Boolean.FALSE, HttpUtil.getClientIP(request),order.getDealAmount());
            return Result.buildFailMessage("当前银行卡限制出款，请等待");
        }
        String bankCheck = RSAUtils.md5(RedisConstant.Queue.HEARTBEAT + mediumNumber);// 验证银行 卡在线标记
        boolean hasKey = redisUtil.hasKey(bankCheck);
        if (hasKey) {
            return Result.buildFailMessage("当前银行卡未绑定监控，无法出款");
        }
        String amount1 = getAmount(order.getDealAmount());
        String witNotify1 = mediumNumber  + amount1 ; //验证当前 银行卡是否处于出款状态
        Object o = redisUtil.get("WIT:" + witNotify1);
        if (null != o) {
            exceptionOrderServiceImpl.addFindBankInfo(order.getOrderId(),order.getOrderQrUser()," 当前报错：当前银行卡 正在出款， 请更换银行卡出款",Boolean.FALSE, HttpUtil.getClientIP(request),order.getDealAmount());
            return Result.buildFailMessage("当前银行卡 正在出款， 请更换银行卡出款");
        }
        bankInfo = account + MARK + mediumHolder + MARK + mediumNumber ;

        boolean b = orderServiceImpl.updateBankInfoByOrderId(bankInfo, orderId);
        if (b) {
            String amount = getAmount(order.getDealAmount());
            String witNotify = mediumNumber  + amount; //代付回调成功 标记
            log.info("当前订单号为："+witNotify+"");
            redisUtil.set("WIT:" + witNotify, order.getOrderId(), 500);
        }
        return Result.buildSuccess();
    }


    @GetMapping("/grabOrder")
    @ResponseBody
    public Result grabOrder(HttpServletRequest request , String orderId) {
        UserInfo user = sessionUtil.getUser(request);
        if (ObjectUtil.isNull(user)) {
            return Result.buildFailResult("用户未登录");
        }
        ThreadUtil.execute(()->{
            String publicAccount = "";
            if(islittle(user.getUserId())){
                publicAccount = "zhongbang-bank-s";
            }else {
                publicAccount = "zhongbang-bank";
            }
            DealOrder orderWit = orderServiceImpl.findOrderByUserqr(orderId,publicAccount);
            UserInfo user1 = userInfoServer.findUserInfoByUserId(user.getUserId());
            Integer receiveOrderState = user1.getRemitOrderState();
            if(2 == receiveOrderState){
                exceptionOrderServiceImpl.addBankInfo(orderWit.getOrderId(),user.getUserId()," 当前报错：当前用户代付状态未开启",Boolean.FALSE, HttpUtil.getClientIP(request),orderWit.getDealAmount());
                return;
            }

            /**
             * 抢单要求
             * 1，当前订单抢到后总出款单 不超过4单
             * 2, 当前订单金额不超过总押金额度-处理中的出款订单
             */

            if(new BigDecimal(12000).compareTo(orderWit.getDealAmount()) < 0 ){
                log.info("当前抢单订单号："+orderWit.getOrderId()+" 当前抢单用户："+user.getUserId()+" 当前抢单订单金额："+orderWit.getDealAmount()+" 当前报错："+"订单金额不合符抢单要求");
                exceptionOrderServiceImpl.addBankInfo(orderWit.getOrderId(),user.getUserId()," 当前报错：订单金额不合符抢单要求",Boolean.FALSE, HttpUtil.getClientIP(request),orderWit.getDealAmount());
                return ;
            }
            if(null == orderWit ){
                log.info("当前抢单订单号："+orderWit.getOrderId()+" 当前抢单用户："+user.getUserId()+" 当前抢单订单金额："+orderWit.getDealAmount()+" 当前报错："+"当前订单已被抢");
                exceptionOrderServiceImpl.addBankInfo(orderWit.getOrderId(),user.getUserId()," 当前报错：当前订单已经被抢",Boolean.FALSE, HttpUtil.getClientIP(request),orderWit.getDealAmount());
                return ;
            }
            log.info("当前抢单订单号："+orderWit.getOrderId()+" 当前抢单用户："+user.getUserId()+" 当前抢单订单金额："+orderWit.getDealAmount()+""+"");
            List<DealOrder> witOrderByUserId = orderServiceImpl.findWitOrderByUserId(user.getUserId());
            if( witOrderByUserId.size()>=2){
                log.info("当前抢单订单号："+orderWit.getOrderId()+" 当前抢单用户："+user.getUserId()+" 当前抢单订单金额："+orderWit.getDealAmount()+" 当前报错："+"当前账户抢单过多，请先出款");
                exceptionOrderServiceImpl.addBankInfo(orderWit.getOrderId(),user.getUserId()," 当前报错：当前账户抢单过多，需要先处理其他订单",Boolean.FALSE, HttpUtil.getClientIP(request),orderWit.getDealAmount());
                return ;
            }
            BigDecimal amount = BigDecimal.ZERO;
            if(CollUtil.isNotEmpty(witOrderByUserId)){
                for (DealOrder order : witOrderByUserId){
                    BigDecimal dealAmount = order.getDealAmount();
                     amount = amount.add(dealAmount);
                }
            }
            amount =  amount.add(orderWit.getDealAmount());
            UserFund userFund = userFundService.findUserInfoByUserId(user.getUserId());
            BigDecimal deposit = userFund.getDeposit();
            if(deposit.compareTo(amount) < 0 ){
                log.info("当前抢单订单号："+orderWit.getOrderId()+" 当前抢单用户："+user.getUserId()+" 当前抢单订单金额："+orderWit.getDealAmount()+" 当前报错："+"抢单失败，可用额度不足");
                exceptionOrderServiceImpl.addBankInfo(orderWit.getOrderId(),user.getUserId()," 当前报错：抢单失败，可用额度不足" ,Boolean.TRUE, HttpUtil.getClientIP(request),orderWit.getDealAmount());
                return ;
            }
            Result result = reWit.grabOrder(user.getUserId(), orderWit,islittle(user.getUserId()), false);
            if(result.isSuccess()){
                exceptionOrderServiceImpl.addBankInfo(orderWit.getOrderId(),user.getUserId(),"抢单成功" ,Boolean.TRUE, HttpUtil.getClientIP(request),orderWit.getDealAmount());
            }else {
                exceptionOrderServiceImpl.addBankInfo(orderWit.getOrderId(),user.getUserId(),result.getMessage() ,Boolean.FALSE, HttpUtil.getClientIP(request),orderWit.getDealAmount());
            }


        });
        return Result.buildSuccessMessage("抢单已处理，请到出款页面查看结果");
    }
    @GetMapping("/unGrabOrder")
    @ResponseBody
    public Result unGrabOrder(HttpServletRequest request , String orderId) {
        UserInfo user = sessionUtil.getUser(request);
        if (ObjectUtil.isNull(user)) {
            return Result.buildFailResult("用户未登录");
        }
        ThreadUtil.execute(()->{
            log.info("放弃出款当前订单号："+orderId);
            /**
             * 抢单要求
             * 1，当前订单抢到后总出款单 不超过4单
             * 2, 当前订单金额不超过总押金额度-处理中的出款订单
             */
            DealOrder orderWit = orderServiceImpl.findOrderByUserqr(orderId,user.getUserId());
            orderServiceImpl.unGrabOrder(orderWit.getOrderId());//放弃出款按钮
            exceptionOrderServiceImpl.unGrabOrder(orderWit.getOrderId(),user.getUserId(),"卡商放弃出款" ,Boolean.FALSE, HttpUtil.getClientIP(request),orderWit.getDealAmount());
        });
        return Result.buildSuccessMessage("已经放弃出款,等待客服切款");
    }




}
