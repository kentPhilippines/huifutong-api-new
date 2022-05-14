package alipay.manage.api;

import alipay.config.redis.RedisUtil;
import alipay.manage.api.Impl.AutoWit;
import alipay.manage.api.config.FactoryForStrategy;
import alipay.manage.bean.Amount;
import alipay.manage.bean.ChannelFee;
import alipay.manage.bean.DealOrder;
import alipay.manage.bean.UserFund;
import alipay.manage.mapper.AmountMapper;
import alipay.manage.mapper.ChannelFeeMapper;
import alipay.manage.mapper.DealOrderMapper;
import alipay.manage.service.*;
import alipay.manage.util.*;
import alipay.manage.util.amount.AmountPublic;
import alipay.manage.util.amount.AmountRunUtil;
import alipay.manage.util.bankcardUtil.BankUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import otc.api.alipay.Common;
import otc.bean.alipay.FileList;
import otc.bean.alipay.Medium;
import otc.bean.dealpay.Withdraw;
import otc.common.PayApiConstant;
import otc.common.SystemConstants;
import otc.exception.BusinessException;
import otc.result.Result;
import otc.util.RSAUtils;
import otc.util.enums.DeductStatusEnum;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Api {
    private static final Log log = LogFactory.get();
    @Autowired
    MediumService mediumServiceImpl;
    @Autowired
    LogUtil logUtil;
    @Autowired
    AmountPublic amountPublic;
    @Autowired
    UserInfoService userInfoServiceImpl;
    @Autowired
    AmountRunUtil amountRunUtil;
    @Autowired
    OrderUtil orderUtil;
    @Autowired
    private BankUtil qrUtil;
    @Autowired
    NotifyUtil notifyUtil;
    @Autowired
    QueueUtil queueUtil;
    @Autowired
    FileListService fileListServiceImpl;
    @Autowired
    FactoryForStrategy factoryForStrategy;
    @Autowired
    WithdrawService withdrawServiceImpl;
    @Resource
    private AmountMapper amountDao;
    @Resource
    private DealOrderMapper dealOrderDao;
    @Resource
    private ChannelFeeMapper channelFeeDao;
    @Autowired
    private OrderService orderServiceImpl;
   @Autowired
    private AutoWit auto;


    /**
     * 自动出款抢单
     * @param request
     * @return
     */
    @PostMapping(PayApiConstant.Alipay.ORDER_API +  "/getBankInfo" )
    public Result getwit(@RequestBody Map<String, Object> paramMap, HttpServletRequest request) {
        log.info("【当前收到自动回调消息为：" + paramMap.toString() + "】");
        if (null == paramMap) {
            return Result.buildFail();
        }
        String bankNo = paramMap.get("bankNo").toString();//转账金额
        String userId = paramMap.get("userId").toString();//  income  转入      expenditure 转出
        String amount = paramMap.get("amount").toString();// 抓取到的银行卡号
        String ip = paramMap.get("ip").toString();// 抓取到的手机号
        Result result = auto.autoWit(bankNo, userId, amount, ip);
        log.info("自动抢单结果：result："+result.toString());
        return   result;
    }




    /**
     * <p>后台调用重新通知的方法</p>
     *  这里只对代付余额 进行修改
     * @param request
     * @return
     */
    @GetMapping(PayApiConstant.Alipay.ORDER_API + PayApiConstant.Alipay.BANK_AMOUNT_CHICK)
    public Result bank(HttpServletRequest request) {
        log.info("【接收到后台确认代付出款的方法调用，：" + request.getParameter("orderId") + " 】");
        String orderId = request.getParameter("orderId");
        String apply = request.getParameter("apply");
        String bankId = request.getParameter("bankId");
        String amount = request.getParameter("amount");
        String type = request.getParameter("type");
        if (StrUtil.isBlank(orderId)) {
            return Result.buildFailMessage("订单号为空");
        }
        if (StrUtil.isBlank(apply)||StrUtil.isBlank(bankId)||StrUtil.isBlank(amount)||StrUtil.isBlank(type)) {
            return Result.buildFailMessage("必传参数为空");
        }
        logUtil.addLog(request, "后台人员切代付订单出款：" + orderId +"，金额："+amount, apply);
     /*   boolean flag = mediumServiceImpl.updateMount(bankId,amount,type,"wait");
        if(flag){
             return Result.buildSuccessMessage(" 银行卡余额修改成功");
         }*/
        return Result.buildFailMessage("银行卡余额修改失败");


    }    /**
     * <p>后台调用重新通知的方法</p>
     *
     * @param request
     * @return
     */
    @GetMapping(PayApiConstant.Alipay.ORDER_API + PayApiConstant.Alipay.WIT_API_ENTER)
    public Result wit(HttpServletRequest request) {
        log.info("【接收到后台确认代付出款的方法调用，：" + request.getParameter("orderId") + " 】");
        String orderId = request.getParameter("orderId");
        String apply = request.getParameter("apply");
        if (StrUtil.isBlank(orderId)) {
            return Result.buildFailMessage("订单号为空");
        }
        if (StrUtil.isBlank(apply)) {
            return Result.buildFailMessage("操作人为空");
        }
        logUtil.addLog(request, "后台人员确认代付订单，当前代付订单号：" + orderId, apply);
        Withdraw witOrder = withdrawServiceImpl.findOrderId(orderId);
        String channnel = witOrder.getWitChannel();
        String witType = witOrder.getWitType();
        ChannelFee channelFee = channelFeeDao.findChannelFee(channnel, witType);
        Result withdraw = Result.buildFail();
        try {
            withdraw = factoryForStrategy.getStrategy(channelFee.getImpl()).withdraw(witOrder);
        } catch (Exception e) {
            return Result.buildFailMessage("代付渠道未接通或渠道配置错误，请联系技术人员处理");
        }
        return withdraw;
    }
    /**
     * <p>后台调用重新通知的方法</p>
     * @param request
     * @return
     */
    @GetMapping(PayApiConstant.Notfiy.NOTFIY_API + PayApiConstant.Notfiy.NOTFIY_AGENT)
    public Result notfiySystem(HttpServletRequest request) {
        log.info("【接收到后台重新通知的按钮，当前重新通知订单号为：" + request.getParameter("orderId") + " 】");
        String orderNo = request.getParameter("orderId");
        if (StrUtil.isBlank(orderNo)) {
            return Result.buildFailMessage("必传订单号为空");
        }
        notifyUtil.sendMsg(orderNo);
        return Result.buildSuccessMessage("重新通知成功");
    }

    @PostMapping(PayApiConstant.File.FILE_API + PayApiConstant.File.OFF_FILE)
    public void updateFileNotDeal(@RequestParam("fileId") String fileId) {
        log.info("【当前接收到远程调用方法，删除不合格二维码，当前二维码编号：" + fileId + "】");
        fileListServiceImpl.deleteFile(fileId);
    }
    ;

    @PostMapping(PayApiConstant.File.FILE_API + PayApiConstant.File.OPEN_FILE)
    public void updataFileIsDeal(@RequestParam("fileId") String fileId) {
        log.info("【当前接收到远程调用方法，将二维码标记为以剪裁，二维码编号：" + fileId + "】");
        fileListServiceImpl.updataFileIsCut(fileId);
    }
    ;

    /**
     * <p>获取没有剪裁的文件 </p>
     *
     * @return
     */
    @PostMapping(PayApiConstant.File.FILE_API + PayApiConstant.File.FIND_FILE_NOT_CUT)
    public List<FileList> findFileNotCut() {
        log.info("【当前远程调用，查询所有未剪裁二维码】");
        List<FileList> fileList = fileListServiceImpl.findFileNotCut();
        log.info("【返回集合长度：" + fileList.size() + "】");
        return fileList;
    }
    ;

    /**
     * <p>关闭收款媒介</p>
     *
     * @param mediumNumber
     * @return
     */
    @PostMapping(PayApiConstant.Alipay.MEDIUM_API + PayApiConstant.Alipay.OFF_MEDIUM_QR)
    Result offMediumQueue(@RequestParam("mediumNumber") String mediumNumber) {
        if (StrUtil.isBlank(mediumNumber)) {
            return Result.buildFailMessage("必传参数为空");
        }
        Medium medium = mediumServiceImpl.findMediumByMediumNumber(mediumNumber);
        Result pop = queueUtil.pop(medium);
        return pop;
    }
    ;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * <p>系统回调订单成功资金处理</p>
     *
     * @param request
     * @return
     */
    @PostMapping(PayApiConstant.Alipay.ORDER_API + PayApiConstant.Alipay.ORDER_ENTER_ORDER_SYSTEM)
    public Result enterOrderSystem(@RequestBody Map<String, Object> paramMap, HttpServletRequest request) {
        log.info("【当前收到自动回调消息为：" + paramMap.toString() + "】");

        //  外加参数  ； bankId    phoneId   userId
        //  {"balance":"10.15","bankName":"桂林银行","counterpartyAccountName":"财付通支付","myselfTailNumber":"1565","transactionAmount":"10","transactionDate":"06月25日16:32","transactionType":"income","typeDetail":"收入（代付）"}
        if (null == paramMap) {
            return Result.buildFail();
        }
        String transactionAmount = paramMap.get("transactionAmount").toString();//转账金额
        String transactionType = paramMap.get("transactionType").toString();//  income  转入      expenditure 转出
        String bankId = paramMap.get("bankId").toString();// 抓取到的银行卡号
        String phoneId = paramMap.get("phoneId").toString();// 抓取到的手机号
        String originText = paramMap.get("originText").toString();// 短信原始内容
        String balance = paramMap.get("balance").toString();// 短信原始内容
        String counterpartyAccountName = paramMap.get("counterpartyAccountName").toString();// 对方账户
        String deviceIp = "";//设备ip
        if (MapUtil.isEmpty(paramMap)) {
            return Result.buildFailMessage("未获取到参数");
        }
        String amount = transactionAmount.toString();
        String phone = phoneId.toString();
        String ip = HttpUtil.getClientIP(request);
        if (StrUtil.isNotEmpty(deviceIp)) {
            ip = deviceIp;
        }
        /**
         * ###################################
         * 1,通过回调参数拿到商户预订单号
         * 2,通过预订单参数修改码商交易订单并生成流水
         * 3,通过码商交易订单拿到商户订单并生成流水
         * 4,发送回调数据
         */
        /**
         * 如果金额是100.1 或者是  100.20  等
         * 就会被转换为      100    10       100     20
         */
        amount = getAmount(new BigDecimal(amount));
        log.info("=============【当前回调金额：" + amount + "】============");

        if (StrUtil.isNotEmpty(transactionType) && transactionType.contains("expenditure")) {
            //  根据出款的短信匹配卡商的出款订单  将订单标记为已 验证出款短信
            String witNotify = bankId + phoneId + amount; //验证当前 银行卡是否处于出款状态
            Object o = redisUtil.get("WIT:" + witNotify);//存在  则存在出款短信对应的订单号
            if (null != o) {
                String witOrderId = o.toString();
                DealOrder witOrder = dealOrderDao.findOrderByOrderId(witOrderId);
                if (null != witOrder) {
                    dealOrderDao.updatePayInfo(witOrderId, originText.toString());
                    if(StrUtil.isEmpty(balance)){
                        boolean a =   orderServiceImpl.updateBankAmount(bankId,witOrderId,null);
                    }else{
                        boolean a =   orderServiceImpl.updateBankAmount(bankId,witOrderId,balance);

                    }
                    redisUtil.deleteKey("WIT:" + witNotify);
                }
                return Result.buildSuccessResult("代付出款确认成功", witOrderId);
            }
            return Result.buildSuccessResult("代付出款确认成功");
        } else {


            Medium bank = mediumServiceImpl.findBank(bankId);
            Integer isClickPay = bank.getIsClickPay();
            try {
                if(isClickPay.equals(0)){
                    counterpartyAccountName = "";
                }
            }catch (Exception e ){
                log.info("回调异常");
            }
            String orderId = qrUtil.findOrderBy(amount, phone, bankId,counterpartyAccountName);
            if (StrUtil.isBlank(orderId)) {
                log.info("【商户交易订单失效，或订单匹配不正确】");
                return Result.buildFailMessage("商户交易订单失效，或订单匹配不正确");
            }
            DealOrder order = dealOrderDao.findOrderByOrderId(orderId);
            if (ObjectUtil.isNull(order)) {
                log.info("【通过商户订单号无法查询到码商交易订单号，当前交易订单号：" + orderId + "】");
                return Result.buildFailMessage("通过商户订单号无法查询到码商交易订单号，当前交易订单号：" + orderId + "");
            }
            ThreadUtil.execute(() -> {
                dealOrderDao.updatePayInfo(order.getOrderId(), originText.toString());
            });
            Result orderDealSu = orderUtil.orderDealSu(order.getOrderId(), ip);
            ThreadUtil.execute(() -> {
                if (orderDealSu.isSuccess()) {
                    if(StrUtil.isEmpty(balance)){
                        boolean a =   orderServiceImpl.updateBankAmount(bankId,orderId,null);
                    }else{
                        boolean a =   orderServiceImpl.updateBankAmount(bankId,orderId,balance);

                    }

                    notifyUtil.sendMsg(order.getOrderId());
                }
            });
            return Result.buildSuccessResult(order.getOrderId());
        }
    }

    @PostMapping(PayApiConstant.Alipay.MEDIUM_API + PayApiConstant.Alipay.FIND_MEDIUM_IS_DEAL)
    public List<Medium> findIsDealMedium(String mediumType, String code) {
        /**
         * ##############################################
         * 1，根据code值获取对应的所有支付媒介  code值即为自己的顶级代理账号
         * 2，如果code值为空 则获取所有的支付媒介
         */
        log.info("【远程调用获取初始化数据，传递参数为：" + mediumType + "，code  = " + code + "】");
        if (StrUtil.isBlank(code)) {
            //获取所有的支付媒介
            List<Medium> mediumList = mediumServiceImpl.findMediumByType(mediumType);
            return mediumList;
        } else {
            List<Medium> mediumList = mediumServiceImpl.findMediumByType(mediumType, code);
            return mediumList;
        }
    }

    /****
     * <p>人工加扣款接口</p>
     * @param param                    加扣款订单
     * @param request                    请求
     * #############
     * <li>如果是加款订单，且状态<strong>orderStatus</strong>字段为【成功】,修改订单状态为【成功】调用资金处理方法</li>
     * <li>如果是加款订单，且状态<strong>orderStatus</strong>字段为【失败】,修改订单状态为失败</li>
     * <li>如果是减款款订单，且状态<strong>orderStatus</strong>字段为【处理中】,订单状态不做修改，直接调用资金扣款方法</li>
     * <li>如果是减款款订单，且状态<strong>orderStatus</strong>字段为【成功】,订单状态修改为成功</li>
     * <li>如果是减款款订单，且状态<strong>orderStatus</strong>字段为【失败】,订单状态修改为失败，调用退还资金修改方法</li>
     */
    @Transactional
    @PostMapping(PayApiConstant.Alipay.ACCOUNT_API + PayApiConstant.Alipay.AMOUNT + "/{param:.+}")
    public Result addAmount(@PathVariable("param") String param, HttpServletRequest request) {
        log.info("【后台人员请求人工加减款的资金处理的方法参数为：" + param + "】");
        Map<String, Object> stringObjectMap = RSAUtils.retMapDecode(param, SystemConstants.INNER_PLATFORM_PRIVATE_KEY);
        if (CollUtil.isEmpty(stringObjectMap)) {
            log.info("【参数解密为空】");
            return Result.buildFailMessage("参数为空");
        }
        Object orderId = stringObjectMap.get("orderId");
        if (ObjectUtil.isNull(orderId)) {
            return Result.buildFailMessage("订单号为空");
        }
        Object orderStatus = stringObjectMap.get("orderStatus");
        if (ObjectUtil.isNull(orderStatus)) {
            return Result.buildFailMessage("订单状态为空");
        }
        Object approval = stringObjectMap.get("approval");
        if (ObjectUtil.isNull(approval)) {
            return Result.buildFailMessage("审核人为空");
        }
        Object comment = stringObjectMap.get("comment");
        if (ObjectUtil.isNull(comment)) {
            return Result.buildFailMessage("审核意见为空");
        }
        Amount amount = amountDao.findOrder(orderId.toString());
        if (ObjectUtil.isNull(amount)) {
            return Result.buildFailMessage("当前订单不存在");
        }
        log.info("【当前调用人工资金处理接口，当前订单号：" + orderId + "】");
        String clientIP = HttpUtil.getClientIP(request);
        if (StrUtil.isBlank(clientIP)) {
            return Result.buildFailMessage("当前使用代理服务器 或是操作ip识别出错，不允许操作");
        }
        String amountType = amount.getAmountType();
        String oldStatus = amount.getOrderStatus();//订单原始状态
        if (!DeductStatusEnum.DEDUCT_STATUS_PROCESS.matches(Integer.parseInt(oldStatus))) {//状态不相等，说明订单已经被处理
            return Result.buildFailMessage("订单已被处理，不允许重复操作");
        }

        switch (amountType) {

            case Common.Deal.AMOUNT_ORDER_DELETE_PROFIT:
                if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_SU)) {//减款订单成功，
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        logUtil.addLog(request, "当前发起订单修改操作，减款订单号：" + amount.getOrderId() + "，减款订单置为成功，减款用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                        return Result.buildSuccessMessage("操作成功");
                    }
                } else if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_ER)) {//减款失败，资金退回
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        UserFund userFund = new UserFund();  // userInfoServiceImpl.findUserFundByAccount(amount.getUserId());
                        userFund.setUserId(amount.getUserId());
                        Result addAmountAdd = amountPublic.addBankprofitAmount(userFund, amount.getAmount(), amount.getOrderId());
                        if (addAmountAdd.isSuccess()) {
                            Result deleteAmount = amountRunUtil.addBankprofitAmountB(amount, clientIP, "扣减卡商佣金订单驳回，还原扣减资金");
                            if (deleteAmount.isSuccess()) {
                                logUtil.addLog(request, "当前扣款订单置为失败，资金原路退回，扣款订单号：" + amount.getOrderId() + "，扣款用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                                return Result.buildSuccessMessage("操作成功");
                            }
                        }
                    }
                } else if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_HE)) {
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        UserFund userFund = new UserFund();//userInfoServiceImpl.findUserFundByAccount(amount.getUserId());
                        userFund.setUserId(amount.getUserId());
                        Result deleteAmount2 = amountPublic.deleteBankprofit(userFund, amount.getAmount(), amount.getOrderId());
                        if (deleteAmount2.isSuccess()) {
                            Result deleteAmount = amountRunUtil.deleteBankprofit(amount, clientIP);
                            if (deleteAmount.isSuccess()) {
                                logUtil.addLog(request, "当前发起扣款操作，扣款订单号：" + amount.getOrderId() + "，扣款成功，扣款用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                                return Result.buildSuccessMessage("操作成功");
                            }
                        }
                    }
                }
                return Result.buildFailMessage("人工处理加扣款失败");


            case Common.Deal.AMOUNT_ORDER_ADD_PROFIT:
                log.info("进入卡商手动加佣金接口，当前订单号：" + orderId.toString() + "");
                if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_SU)) {//佣金增加成功
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        UserFund userFund = new UserFund();    // userInfoServiceImpl.findUserFundByAccount(amount.getUserId());
                        userFund.setUserId(amount.getUserId());
                        Result result = amountPublic.addBankprofitAmount(userFund, amount.getAmount(), orderId.toString());
                        if (result.isSuccess()) {
                            Result result1 = amountRunUtil.addBankprofitAmount(amount, clientIP, "手动增加卡商佣金，当前操作人：" + amount.getApproval() + "，当前操作理由" + amount.getComment());
                            if (result1.isSuccess()) {
                                logUtil.addLog(request, "当前发起加钱操作，加款订单号：" + amount.getOrderId() + "，加款成功，加款用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                                return Result.buildSuccessMessage("操作成功");
                            }
                        }
                    }
                } else {//佣金增加失败
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        logUtil.addLog(request, "当前发起订单修改操作，加款订单号：" + amount.getOrderId() + "，加款订单置为失败，加款用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                        return Result.buildSuccessMessage("操作成功");
                    }
                }
            case Common.Deal.AMOUNT_ORDER_ADD:
                if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_SU)) {//加款订单成功，
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        UserFund userFund = new UserFund();    // userInfoServiceImpl.findUserFundByAccount(amount.getUserId());
                        userFund.setUserId(amount.getUserId());
                        Result addAmountAdd = amountPublic.addAmountAdd(userFund, amount.getAmount(), orderId.toString());
                        if (addAmountAdd.isSuccess()) {
                            Result addAmount = amountRunUtil.addAmount(amount, clientIP);
                            if (addAmount.isSuccess()) {
                                logUtil.addLog(request, "当前发起加钱操作，加款订单号：" + amount.getOrderId() + "，加款成功，加款用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                                return Result.buildSuccessMessage("操作成功");
                            }
                        }
                    }
                } else if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_ER)) {//加款失败
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        logUtil.addLog(request, "当前发起订单修改操作，加款订单号：" + amount.getOrderId() + "，加款订单置为失败，加款用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                        return Result.buildSuccessMessage("操作成功");
                    }
                }
                return Result.buildFailMessage("人工处理加扣款失败");
            case Common.Deal.AMOUNT_ORDER_DELETE:
                if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_SU)) {//减款订单成功，
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        logUtil.addLog(request, "当前发起订单修改操作，减款订单号：" + amount.getOrderId() + "，减款订单置为成功，减款用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                        return Result.buildSuccessMessage("操作成功");
                    }
                } else if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_ER)) {//减款失败，资金退回
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        UserFund userFund = new UserFund();  // userInfoServiceImpl.findUserFundByAccount(amount.getUserId());
                        userFund.setUserId(amount.getUserId());
                        Result addAmountAdd = amountPublic.addAmountAdd(userFund, amount.getAmount(), amount.getOrderId());
                        if (addAmountAdd.isSuccess()) {
                            Result deleteAmount = amountRunUtil.addAmount(amount, clientIP, "扣款失败，资金退回退回");
                            if (deleteAmount.isSuccess()) {
                                logUtil.addLog(request, "当前扣款订单置为失败，资金原路退回，扣款订单号：" + amount.getOrderId() + "，扣款用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                                return Result.buildSuccessMessage("操作成功");
                            }
                        }
                    }
                } else if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_HE)) {
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        UserFund userFund = new UserFund();//userInfoServiceImpl.findUserFundByAccount(amount.getUserId());
                        userFund.setUserId(amount.getUserId());
                        Result deleteAmount2 = amountPublic.deleteAmount(userFund, amount.getAmount(), amount.getOrderId());
                        if (deleteAmount2.isSuccess()) {
                            Result deleteAmount = amountRunUtil.deleteAmount(amount, clientIP);
                            if (deleteAmount.isSuccess()) {
                                logUtil.addLog(request, "当前发起扣款操作，扣款订单号：" + amount.getOrderId() + "，扣款成功，扣款用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                                return Result.buildSuccessMessage("操作成功");
                            }
                        }
                    }
                }
                return Result.buildFailMessage("人工处理加扣款失败");
            case Common.Deal.AMOUNT_ORDER_DELETE_FREEZE:   //资金冻结
                if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_SU)) {//资金冻结成功
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        logUtil.addLog(request, "当前发起订单修改操作，冻结订单号：" + amount.getOrderId() + "，冻结订单置为成功，冻结用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                        return Result.buildSuccessMessage("操作成功");
                    }
                } else if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_ER)) {//资金冻结失败
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        UserFund userFund = new UserFund(); //userInfoServiceImpl.findUserFundByAccount(amount.getUserId());
                        userFund.setUserId(amount.getUserId());
                        Result addAmountAdd = amountPublic.addFreeze(userFund, amount.getAmount(), amount.getOrderId());
                        if (addAmountAdd.isSuccess()) {
                            Result deleteAmount = amountRunUtil.addFreeze(amount, clientIP);
                            if (deleteAmount.isSuccess()) {
                                logUtil.addLog(request, "当前扣款订单置为失败，资金原路退回，扣款订单号：" + amount.getOrderId() + "，扣款用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                                return Result.buildSuccessMessage("操作成功");
                            }
                        }
                    }
                }
                return Result.buildFailMessage("人工处理冻结失败");
            case Common.Deal.AMOUNT_ORDER_ADD_FREEZE:
                if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_SU)) {//资金解冻成功
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        UserFund userFund = new UserFund();//userInfoServiceImpl.findUserFundByAccount(amount.getUserId());
                        userFund.setUserId(amount.getUserId());
                        Result addAmountAdd = amountPublic.addFreeze(userFund, amount.getAmount(), amount.getOrderId());
                        if (addAmountAdd.isSuccess()) {
                            Result addAmount = amountRunUtil.addFreeze(amount, clientIP);
                            if (addAmount.isSuccess()) {
                                logUtil.addLog(request, "当前发起解冻操作，解冻订单号：" + amount.getOrderId() + "，解冻成功，加款用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                                return Result.buildSuccessMessage("解冻成功");
                            }
                        }
                    }
                } else if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_ER)) {//资金解冻失败
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        logUtil.addLog(request, "当前发起订单修改操作，解冻订单号：" + amount.getOrderId() + "，解冻订单置为失败，解冻用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                        return Result.buildSuccessMessage("操作成功");
                    }
                }
                return Result.buildFailMessage("人工处理解冻失败");
            case Common.Deal.AMOUNT_ORDER_ADD_QUOTA:
                if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_SU)) {//授权订单成功
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        UserFund userFund = new UserFund(); // userInfoServiceImpl.findUserFundByAccount(amount.getUserId());
                        userFund.setUserId(amount.getUserId());
                        Result addAmountAdd = amountPublic.addQuotaAmount(userFund, amount.getAmount(), amount.getOrderId());
                        if (addAmountAdd.isSuccess()) {
                            Result addAmount = amountRunUtil.addQuota(amount, clientIP);
                            if (addAmount.isSuccess()) {
                                logUtil.addLog(request, "当前发起账户授权操作，授权订单号：" + amount.getOrderId() + "，授权成功，授权用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                                return Result.buildSuccessMessage("授权成功");
                            }
                        }
                    }
                } else if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_ER)) {//授权订单失败
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        logUtil.addLog(request, "当前发起订单修改操作，授权订单号：" + amount.getOrderId() + "，授权订单置为失败，授权用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                        return Result.buildSuccessMessage("操作成功");
                    }
                }
                return Result.buildFailMessage("人工处理授权失败");
            case Common.Deal.AMOUNT_ORDER_DELETE_QUOTA:   //减少授权订单
                if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_SU)) {//减少授权订单成功
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a == 1) {
                        logUtil.addLog(request, "当前发起账户减少授权操作，减少授权订单号：" + amount.getOrderId() + "，减少授权成功，授权用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                        return Result.buildSuccessMessage("操作成功");
                    }
                } else if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_ER)) {//授权订单失败
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0 && a < 2) {
                        UserFund userFund = new UserFund();// userInfoServiceImpl.findUserFundByAccount(amount.getUserId());
                        userFund.setUserId(amount.getUserId());
                        Result addAmountAdd = amountPublic.addQuotaAmount(userFund, amount.getAmount(), amount.getOrderId());
                        if (addAmountAdd.isSuccess()) {
                            Result addAmount = amountRunUtil.addQuota(amount, clientIP);
                            if (addAmount.isSuccess()) {
                                logUtil.addLog(request, "当前账户减少授权账户失败，给账户重新添加授权，授权订单号：" + amount.getOrderId() + "，减少授权失败增加授权成功，授权用户：" + amount.getUserId() + "，操作人：" + amount.getAccname() + "", amount.getAccname());
                                return Result.buildSuccessMessage("操作成功");
                            }
                        }
                    }
                }
            case Common.Deal.AMOUNT_ORDER_FRANSFER:   //减少授权订单
                if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_SU)) {
                    UserFund userFund = userInfoServiceImpl.fundUserFundAccounrBalace(amount.getUserId());
                    if (userFund == null) {
                        throw new BusinessException("此用户不存在");
                    }
                    BigDecimal balance = userFund.getAccountBalance();
                    BigDecimal deduct = amount.getAmount();
                    if (balance.compareTo(deduct) > -1) {//余额充足
                        int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                        //  userFund  减款人
                        String transferUserId = amount.getTransferUserId();
                        if (StrUtil.isEmpty(transferUserId)) {
                            return Result.buildFailMessage("转入账户为空，请确认");
                        }
                        UserFund userFund1 = userInfoServiceImpl.fundUserFundAccounrBalace(transferUserId);//加款人

                        Result result = amountPublic.deleteAmount(userFund, amount.getAmount(), orderId.toString());
                        if (result.isSuccess()) {
                            Result result1 = amountRunUtil.deleteAmountTRANS(amount, clientIP);
                            if (!result1.isSuccess()) {
                                throw new BusinessException("减款出错");
                            }
                        }
                        Result result1 = amountPublic.addAmountAdd(userFund1, amount.getAmount(), amount.getOrderId());
                        if (result1.isSuccess()) {
                            Result result2 = amountRunUtil.addAmountTRANS(amount, clientIP);
                            if (!result2.isSuccess()) {
                                throw new BusinessException("减款出错");
                            }
                        }
                        return Result.buildSuccessMessage("订单已处理");
                    } else {//余额不足
                        return Result.buildFailMessage("操作失败，账户余额不足");
                    }
                } else if (orderStatus.equals(Common.Deal.AMOUNT_ORDER_ER)) {
                    int a = amountDao.updataOrder(orderId.toString(), orderStatus.toString(), approval.toString(), comment.toString());
                    if (a > 0) {
                        return Result.buildSuccessMessage("订单已处理为失败");
                    }
                }
                return Result.buildFailMessage("人工处理失败");
            default:
                return Result.buildFailMessage("人工处理订单失败");
        }
    }



    /**
     * 后台置交易订单为成功
     * @param param
     * @param request
     * @return
     */
    @PostMapping(PayApiConstant.Alipay.ORDER_API + PayApiConstant.Alipay.ORDER_ENTER_ORDER_BACK + "/{param:.+}")
    public Result enterOrderBack(@PathVariable("param") String param, HttpServletRequest request) {
        log.info("【请求交易的终端用户交易请求参数为：" + param + "】");
        Map<String, Object> stringObjectMap = RSAUtils.retMapDecode(param, SystemConstants.INNER_PLATFORM_PRIVATE_KEY);
        if (CollUtil.isEmpty(stringObjectMap)) {
            log.info("【参数解密为空】");
            return Result.buildFailMessage("参数为空");
        }
        Object obj = stringObjectMap.get("orderId");
        if (ObjectUtil.isNull(obj)) {
            return Result.buildFailMessage("未识别当前订单号");
        }

        Object user = stringObjectMap.get("userName");
        if (ObjectUtil.isNull(user)) {
            return Result.buildFailMessage("未识别当前操作人");
        }
        String orderId = obj.toString();//订单号
        String userop = user.toString();//操作人
        log.info("【当前调用人工处理订单回滚接口，当前订单号：" + orderId  + "，当前操作人：" + userop + "】");
        DealOrder order = dealOrderDao.findOrderByOrderId(orderId);
        String clientIP = HttpUtil.getClientIP(request);
        if (StrUtil.isBlank(clientIP)) {
            return Result.buildFailMessage("当前使用代理服务器 或是操作ip识别出错，不允许操作");
        }
        if (ObjectUtil.isNull(order)) {
            return Result.buildFailMessage("当前订单不存在");
        }
        if (!order.getOrderStatus().equals(Common.Order.DealOrder.ORDER_STATUS_SU.toString())) {
            return Result.buildFailMessage("当前订单状态不允许操作");
        }
       if(!"4".equals(order.getOrderType())){
           log.info("【当前调用人工处理订单回滚接口，当前订单号：" + orderId +   "，当前操作人：" + userop + ",当前操作订单类型错误，当前订单类型为："+order.getOrderType()+"】");
           return Result.buildFailMessage("当前订单不允许操作");
       }

        Result orderDealEr =  orderUtil.backOrder(order,   clientIP);

        if(orderDealEr.isSuccess()){
            return orderDealEr;
        }
        return Result.buildFailMessage("操作失败");
    }













    /**
     * 后台置交易订单为成功
     * @param param
     * @param request
     * @return
     */
    @PostMapping(PayApiConstant.Alipay.ORDER_API + PayApiConstant.Alipay.ORDER_ENTER_ORDER + "/{param:.+}")
    public Result enterOrder(@PathVariable("param") String param, HttpServletRequest request) {
        log.info("【请求交易的终端用户交易请求参数为：" + param + "】");
        Map<String, Object> stringObjectMap = RSAUtils.retMapDecode(param, SystemConstants.INNER_PLATFORM_PRIVATE_KEY);
        if (CollUtil.isEmpty(stringObjectMap)) {
            log.info("【参数解密为空】");
            return Result.buildFailMessage("参数为空");
        }
        Object obj = stringObjectMap.get("orderId");
        if (ObjectUtil.isNull(obj)) {
            return Result.buildFailMessage("未识别当前订单号");
        }
        Object sta = stringObjectMap.get("orderStatus");
        if (ObjectUtil.isNull(sta)) {
            return Result.buildFailMessage("未识别当前订单状态");
        }
        Object user = stringObjectMap.get("userName");
        if (ObjectUtil.isNull(user)) {
            return Result.buildFailMessage("未识别当前操作人");
        }
        String orderId = obj.toString();//订单号
        String orderstatus = sta.toString();//将要改变订单状态
        String userop = user.toString();//操作人
        log.info("【当前调用人工处理订单接口，当前订单号：" + orderId + "，当前修改订单状态：" + orderstatus + "，当前操作人：" + userop + "】");
        DealOrder order = dealOrderDao.findOrderByOrderId(orderId);
        String clientIP = HttpUtil.getClientIP(request);
        if (StrUtil.isBlank(clientIP)) {
            return Result.buildFailMessage("当前使用代理服务器 或是操作ip识别出错，不允许操作");
        }
        if (ObjectUtil.isNull(order)) {
            return Result.buildFailMessage("当前订单不存在");
        }
        if (order.getOrderStatus().equals(Common.Order.DealOrder.ORDER_STATUS_ER.toString()) || order.getOrderStatus().equals(Common.Order.DealOrder.ORDER_STATUS_SU.toString())) {
            return Result.buildFailMessage("当前订单状态不允许操作");
        }
        if (orderstatus.equals(Common.Order.DealOrder.ORDER_STATUS_ER.toString())) {
            Result orderDealEr = orderUtil.orderDealEr(orderId, "后台人员置交易订单失败，操作人：" + userop + "", clientIP);
            if (orderDealEr.isSuccess()) {
                return Result.buildSuccessMessage("操作成功");
            } else {
                return orderDealEr;
            }
        } else if (orderstatus.equals(Common.Order.DealOrder.ORDER_STATUS_SU.toString())) {
            Result orderDealSu = orderUtil.orderDealSu(orderId, clientIP, userop);
            if (orderDealSu.isSuccess()) {


                return Result.buildSuccessMessage("操作成功");
            } else {
                return orderDealSu;
            }
        }
        return Result.buildFailMessage("操作失败");
    }

    /**
     * 后台减款申请生成订单，从用户账户里预扣款，生成流水
     *
     * @param param
     * @param request
     * @return
     */

    @Transactional
    @PostMapping(PayApiConstant.Alipay.ACCOUNT_API + PayApiConstant.Alipay.GENERATE_ORDER_DEDUCT + "/{param:.+}")
    public Result generateOrderDeduct(@PathVariable("param") String param, HttpServletRequest request) {
        log.info("【请求交易的终端用户交易请求参数为：" + param + "】");
        Map<String, Object> stringObjectMap = RSAUtils.retMapDecode(param, SystemConstants.INNER_PLATFORM_PRIVATE_KEY);
        Amount alipayAmount = new Amount();
        if (CollUtil.isEmpty(stringObjectMap)) {
            log.info("【参数解密为空】");
            return Result.buildFailMessage("参数为空");
        }
        Object userId = stringObjectMap.get("userId");
        if (ObjectUtil.isNull(userId)) {
            return Result.buildFailMessage("用户ID不能为空");
        }
        alipayAmount.setUserId(userId.toString());
        Object orderId = stringObjectMap.get("orderId");
        if (ObjectUtil.isNull(orderId)) {
            return Result.buildFailMessage("订单号不能为空");
        }
        alipayAmount.setOrderId(orderId.toString());
        Object orderStatus = stringObjectMap.get("orderStatus");
        if (ObjectUtil.isNull(orderStatus)) {
            return Result.buildFailMessage("订单状态为空");
        }
        alipayAmount.setOrderStatus(orderStatus.toString());
        Object amount = stringObjectMap.get("amount");
        if (ObjectUtil.isNull(amount)) {
            return Result.buildFailMessage("减款金额不能为空");
        }
        alipayAmount.setAmount(new BigDecimal(amount.toString()));
        Object dealDescribe = stringObjectMap.get("dealDescribe");
        if (ObjectUtil.isNull(dealDescribe)) {
            return Result.buildFailMessage("扣款描述不能为空");
        }
        alipayAmount.setDealDescribe(dealDescribe.toString());
        Object amountType = stringObjectMap.get("amountType");
        if (ObjectUtil.isNull(amountType)) {
            return Result.buildFailMessage("申请类型不能为空");
        }
        alipayAmount.setAmountType(amountType.toString());
        Object accname = stringObjectMap.get("accname");
        if (ObjectUtil.isNull(accname)) {
            return Result.buildFailMessage("申请人不能为空");
        }
        alipayAmount.setAccname(accname.toString());
        alipayAmount.setActualAmount(new BigDecimal(amount.toString()));
        String clientIP = HttpUtil.getClientIP(request);
        if (StrUtil.isBlank(clientIP)) {
            return Result.buildFailMessage("当前使用代理服务器 或是操作ip识别出错，不允许操作");
        }
        alipayAmount.setApproval(accname.toString());
        UserFund userFund = userInfoServiceImpl.fundUserFundAccounrBalace(userId.toString());
        if (userFund == null) {
            throw new BusinessException("此用户不存在");
        }
        if (amountType.toString().equals(Common.Deal.AMOUNT_ORDER_DELETE_FREEZE)) {
            BigDecimal balance = userFund.getAccountBalance();
            BigDecimal deduct = new BigDecimal(amount.toString());
            if (balance.compareTo(deduct) > -1) {//余额充足
                Result deleteAmount2 = amountPublic.deleteFreeze(userFund, deduct, orderId.toString());
                if (deleteAmount2.isSuccess()) {
                    Result deleteAmount = amountRunUtil.deleteFreeze(alipayAmount, clientIP);
                    if (deleteAmount.isSuccess()) {
                        int i = userInfoServiceImpl.insertAmountEntitys(alipayAmount);
                        if (i == 1) {
                            return Result.buildSuccessMessage("创建订单成功");
                        } else {
                            return Result.buildFailMessage("创建订单失败");
                        }
                    }
                }
            } else {//余额不足
                return Result.buildFailMessage("操作失败，账户余额不足");
            }
        } else if (amountType.toString().equals(Common.Deal.AMOUNT_ORDER_DELETE)) {
            BigDecimal balance = userFund.getAccountBalance();
            BigDecimal deduct = new BigDecimal(amount.toString());
            if ("2".equals(stringObjectMap.get("type"))) {
                Result result = amountPublic.deleteBankprofit(userFund, deduct, orderId.toString());
                if (result.isSuccess()) {
                    Result result1 = amountRunUtil.deleteBankprofit(alipayAmount, clientIP);
                    if (result1.isSuccess()) {
                        alipayAmount.setAmountType(Common.Deal.AMOUNT_ORDER_DELETE_PROFIT);
                        int i = userInfoServiceImpl.insertAmountEntitys(alipayAmount);
                        if (i == 1) {
                            return Result.buildSuccessMessage("创建订单成功");
                        } else {
                            return Result.buildFailMessage("创建订单失败");
                        }
                    }
                }
            }
            Result deleteAmount2 = amountPublic.deleteAmount(userFund, deduct, orderId.toString());
            if (deleteAmount2.isSuccess()) {
                Result deleteAmount = amountRunUtil.deleteAmount(alipayAmount, clientIP);
                if (deleteAmount.isSuccess()) {
                    int i = userInfoServiceImpl.insertAmountEntitys(alipayAmount);
                    if (i == 1) {
                        return Result.buildSuccessMessage("创建订单成功");
                    } else {
                        return Result.buildFailMessage("创建订单失败");
                    }
                    }
                }
        } else if (amountType.toString().equals(Common.Deal.AMOUNT_ORDER_DELETE_QUOTA)) {
            BigDecimal balance = userFund.getQuota();
            BigDecimal deduct = new BigDecimal(amount.toString());
            if (balance.compareTo(deduct) > -1) {//余额充足
                Result deleteAmount2 = amountPublic.deleteQuotaAmount(userFund, deduct, orderId.toString());
                if (deleteAmount2.isSuccess()) {
                    Result deleteAmount = amountRunUtil.deleteQuota(alipayAmount, clientIP);
                    if (deleteAmount.isSuccess()) {
                        int i = userInfoServiceImpl.insertAmountEntitys(alipayAmount);
                        if (i == 1) {
                            return Result.buildSuccessMessage("创建订单成功");
                        } else {
                            return Result.buildFailMessage("创建订单失败");
                        }
                    }
                }
            } else {//余额不足
                return Result.buildFailMessage("操作失败，账户余额不足");
            }
        }
        return Result.buildFailMessage("操作失败");
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
}
