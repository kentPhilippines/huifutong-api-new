package alipay.manage.service.impl;

import alipay.manage.api.channel.amount.recharge.usdt.USDTOrder;
import alipay.manage.bean.*;
import alipay.manage.mapper.*;
import alipay.manage.service.CorrelationService;
import alipay.manage.service.OrderService;
import alipay.manage.util.SettingFile;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otc.bean.dealpay.Recharge;
import otc.bean.dealpay.Withdraw;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OrderServiceImpl implements OrderService {
    Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    @Resource
    private DealOrderMapper dealOrderMapper;
    @Resource
    private DealOrderAppMapper dealOrderAppDao;
    @Resource
    private RunOrderMapper runOrderMapper;
    @Resource
    private RechargeMapper rechargeDao;
    @Resource
    private WithdrawMapper withdrawMapper;
    @Resource
    private UserInfoMapper userInfoDao;
    @Autowired
    private SettingFile settingFile;
    @Autowired
    private CorrelationService correlationServiceImpl;

    @Autowired
    private UserRateServiceImpl userRateServiceImpl;

    @Override
    public List<DealOrder> findOrderByUser(String userId, String createTime, String orderStatus,String orderType) {
        List<DealOrder> selectByExample = dealOrderMapper.selectByExampleByMyId(userId, createTime, orderStatus,orderType);
        return selectByExample;
    }

    @Override
    public DealOrder findOrderByAssociatedId(String orderId) {
        DealOrder order = dealOrderMapper.findOrderByAssociatedId(orderId);
        return order;
    }

    @Override
    public List<RunOrder> findOrderRunByPage(RunOrder order) {
        //遍历子元素查询订单流水
        RunOrderExample example = new RunOrderExample();
        RunOrderExample.Criteria criteria = example.createCriteria();
        if (StrUtil.isNotBlank(order.getOrderAccount())) {
            criteria.andOrderAccountEqualTo(order.getOrderAccount());
        }
        if (null != order.getRunOrderType()) {
            criteria.andRunOrderTypeEqualTo(order.getRunOrderType());
        }
        if (StrUtil.isNotBlank(order.getTime())) {
            Date date = getDate(order.getTime());
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            System.out.println("开始时间：" + calendar.getTime());
            Date time = calendar.getTime();
            calendar.set(Calendar.HOUR, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            System.out.println("结束时间：" + calendar.getTime());
            criteria.andCreateTimeBetween(time, calendar.getTime());
        }
        example.setOrderByClause("createTime desc");
        List<RunOrder> listRunOrder = runOrderMapper.selectByExample(example);
        return listRunOrder;
    }

    Date getDate(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateTime = null;
        try {
            dateTime = simpleDateFormat.parse(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateTime;
    }


    @Override
    public List<RunOrder> findAllOrderRunByPage(RunOrder order) {
        // 遍历子元素查询所有的下级订单流水
        RunOrderExample example = new RunOrderExample();
        RunOrderExample.Criteria criteria = example.createCriteria();
        if (StrUtil.isNotBlank(order.getOrderAccount())) {
            criteria.andOrderAccountEqualTo(order.getOrderAccount());
        }
        if (CollUtil.isNotEmpty(order.getOrderAccountList())) {
            criteria.andOrderAccountIn(order.getOrderAccountList());
        }
        List<RunOrder> listRunOrder = runOrderMapper.selectByExample(example);
        log.info("======****======》", listRunOrder);
        return listRunOrder;
    }


    @Override
    public List<DealOrder> findOrderByPage(DealOrder order) {
        DealOrderExample example = new DealOrderExample();
        DealOrderExample.Criteria criteria = example.createCriteria();
        if (StrUtil.isNotBlank(order.getOrderQrUser())) {
            criteria.andOrderQrUserEqualTo(order.getOrderQrUser());
        }
        if (CollUtil.isNotEmpty(order.getOrderQrUserList())) {
            criteria.andOrderQrUserListEqualTo(order.getOrderQrUserList());
        }
        if (StrUtil.isNotBlank(order.getTime())) {
            Date date = getDate(order.getTime());
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            System.out.println("开始时间：" + calendar.getTime());
            Date time = calendar.getTime();
            calendar.set(Calendar.HOUR, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            System.out.println("结束时间：" + calendar.getTime());
            criteria.andCreateTimeBetween(time, calendar.getTime());
        }
        if (StrUtil.isNotBlank(order.getAssociatedId())) {
            criteria.andAssociatedIdEqualTo(order.getAssociatedId());
        }
        if (StrUtil.isNotBlank(order.getOrderAccount())) {
            criteria.andOrderAccountEqualTo(order.getOrderAccount());
        }
        if (StrUtil.isNotBlank(order.getOrderStatus())) {
            criteria.andOrderStatusEqualTo(order.getOrderStatus());
        }
        if (StrUtil.isNotBlank(order.getOrderId())) {
            criteria.andOrderIdEqualTo(order.getOrderId());
        }
        example.setOrderByClause("createTime desc");
        return dealOrderMapper.selectByExample(example);
    }

    @Override
    public List<Recharge> findRechargeOrder(Recharge bean) {
        RechargeExample example = new RechargeExample();
        RechargeExample.Criteria criteria = example.createCriteria();
        if (StrUtil.isNotBlank(bean.getUserId())) {
            criteria.andChargeBankcardEqualTo(bean.getUserId());
        }
        if (StrUtil.isNotBlank(bean.getTime())) {
            Date date = getDate(bean.getTime());
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            System.out.println("开始时间：" + calendar.getTime());
            Date time = calendar.getTime();
            calendar.set(Calendar.HOUR, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            System.out.println("结束时间：" + calendar.getTime());
            criteria.andCreateTimeBetween(time, calendar.getTime());
        }
        example.setOrderByClause("createTime desc");
        List<Recharge> selectByExample = rechargeDao.selectByExample(example);
        return selectByExample;
    }

    @Override
    public List<Withdraw> findWithdrawOrder(Withdraw bean) {
        WithdrawExample example = new WithdrawExample();
        WithdrawExample.Criteria criteria = example.createCriteria();
        if (StrUtil.isNotBlank(bean.getUserId())) {
            criteria.andUserIdEqualTo(bean.getUserId());
        }
        if (StrUtil.isNotBlank(bean.getTime())) {
            Date date = getDate(bean.getTime());
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            System.out.println("开始时间：" + calendar.getTime());
            Date time = calendar.getTime();
            calendar.set(Calendar.HOUR, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            System.out.println("结束时间：" + calendar.getTime());
            criteria.andCreateTimeBetween(time, calendar.getTime());
        }
        example.setOrderByClause("createTime desc");
        List<Withdraw> selectByExample = withdrawMapper.selectByExample(example);
        return selectByExample;
    }

    @Override
    public DealOrder findOrderByOrderId(String orderId) {
        return dealOrderMapper.findOrderByOrderId(orderId);
    }

    @Override
    public boolean updateOrderStatus(String orderId, String status, String mag) {
        int a = dealOrderMapper.updateOrderStatus(orderId, status, mag);
        return a > 0 && a < 2;
    }

    /**
     * <p>根据用户id查询自己的交易订单号记录</p>
     *
     * @param order
     * @return
     */
    @Override
    public List<DealOrder> findMyOrder(DealOrder order) {
        DealOrderExample example = new DealOrderExample();
        DealOrderExample.Criteria criteria = example.createCriteria();
        if (StrUtil.isNotBlank(order.getOrderQrUser())) {
            criteria.andOrderQrUserEqualTo(order.getOrderQrUser());
        }
        if (StrUtil.isNotBlank(order.getRetain1())) {
            criteria.andOrderRetain1EqualTo(order.getRetain1());
        }
        if (StrUtil.isNotBlank(order.getTime())) {
            Date date = getDate(order.getTime());
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            System.out.println("开始时间：" + calendar.getTime());
            Date time = calendar.getTime();
            calendar.set(Calendar.HOUR, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            System.out.println("结束时间：" + calendar.getTime());
            criteria.andCreateTimeBetween(time, calendar.getTime());
        }
        return dealOrderMapper.selectByExample(example);
    }

    @Override
    public boolean addOrder(DealOrder orderApp) {
        int insertSelective = dealOrderMapper.insertSelective(orderApp);
        return insertSelective > 0 && insertSelective < 2;
    }

    @Override
    public boolean updataOrderStatusByOrderId(String orderId, String status) {
        log.info("=======【根据订单编号修改交易订单为成功,捕获订单编号:" + orderId + "】======");
        DealOrder record = new DealOrder();
        DealOrderExample example = new DealOrderExample();
        DealOrderExample.Criteria criteriaDealOrder = example.createCriteria();
        criteriaDealOrder.andOrderIdEqualTo(orderId);
        record.setOrderStatus(status);
        record.setCreateTime(null);
        int updateByExampleSelective = dealOrderMapper.updateByExampleSelective(record, example);
        boolean flag = updateByExampleSelective > 0 && updateByExampleSelective < 2;
        return flag;
    }

    @Override
    public boolean updataOrderisNotifyByOrderId(String orderId, String isNotify) {
        log.info("=======【根据订单编号修改交易订单是否发送通知为YES,捕获订单编号:" + orderId + "】=======");
        DealOrder record = new DealOrder();
        DealOrderExample example = new DealOrderExample();
        DealOrderExample.Criteria criteriaDealOrder = example.createCriteria();
        criteriaDealOrder.andOrderIdEqualTo(orderId);
        record.setIsNotify(isNotify);
        record.setCreateTime(null);
        int updateByExampleSelective = dealOrderMapper.updateByExampleSelective(record, example);
        boolean flag = updateByExampleSelective > 0 && updateByExampleSelective < 2;
        log.info("=======【修改订单通知状态完毕:修改结果为:" + flag + "】=======");
        return flag;
    }

    @Override
    public boolean addRechargeOrder(Recharge order) {
        int insertSelective = rechargeDao.insertSelective(order);
        return insertSelective > 0 && insertSelective < 2;
    }

    @Override
    public DealOrder findAssOrder(String orderId) {
        return dealOrderMapper.findOrderByAssociatedId(orderId);
    }

    @Override
    public void updataXianyYu(String orderId, String id) {
        dealOrderMapper.updataXianyYu(orderId, id);
    }

    @Override
    public boolean updateBankInfoByOrderId(String bank, String orderId) {
        int a = dealOrderMapper.updateBankInfoByOrderId(bank, orderId);
        return a == 1;
    }

    @Override
    public DealOrder findOrderNotify(String orderId) {
        return dealOrderMapper.findOrderNotify(orderId);
    }

    @Override
    public DealOrder findOrderStatus(String orderId) {
        return dealOrderMapper.findOrderStatus(orderId);
    }

    @Override
    public int addUsdtOrder(USDTOrder order) {
        return dealOrderMapper.addUsdtOrder(order.getBlockNumber(), order.getTimeStamp(), order.getHash()
                , order.getBlockHash(), order.getFrom(), order.getContractAddress(), order.getTo(), order.getValue(), order.getTokenName(), order.getTokenSymbol());
    }

    @Override
    public void updateUsdtTxHash(String orderId, String hash) {
        int i = dealOrderMapper.updateUsdtTxHash(orderId, hash);
        DealOrder orderByOrderId = dealOrderMapper.findOrderByOrderId(orderId);
        int k = dealOrderAppDao.updateUsdtTxHash(orderByOrderId.getAssociatedId(), hash);
    }

    @Override
    public List<DealOrder> findExternalOrderId(String externalOrderId) {
        return null;
    }

    @Override
    public Recharge findrecharge(String rechargeId) {
        return null;
    }

    @Override
    public boolean updatePayImg(String orderId, String qrcodeId) {
        return dealOrderMapper.updatePayImg(orderId, qrcodeId);
    }
    @Resource
    MediumMapper mediumDao;
    @Override
    public boolean updateBankAmount(String bankAccount, String orderId, String amount) {
        int i =  dealOrderMapper.updateBankAmount(orderId,amount);
        return i>0;
    }
    @Override
    public boolean enterOrderLock(String orderId) {
        dealOrderMapper.enterOrderLock(orderId);
        return true;
    }

    @Override
    public List<DealOrder> findWitOrderByUserId(String qrcodeId) {
        return dealOrderMapper.findWitOrderByUserId(qrcodeId);
    }

    @Override
    public boolean updateWitQr(DealOrder order) {
        return dealOrderMapper.updateWitQr(order);
    }


    /**
     * 获取卡商和商户的对应关系
     * multimap c- [1,2,3]   b- [2,3]
     * TreeMultimap
     * get(1) == Set[c]
     * get(2) ==Set[b,c]
     * @return
     */
    public TreeMultimap<String, String> getCardDealerMapToMerchant(List<UserRate> merchantRates)
    {

        Map<String,List<String>> cardDerlerMapToMerchants = Maps.newHashMap();
        merchantRates = merchantRates.stream().filter(userRate -> StringUtils.isNotEmpty(userRate.getQueueList())).collect(Collectors.toList());
        ArrayListMultimap<String, String> multimap = ArrayListMultimap.create();
        merchantRates.forEach(merchantRate->{
            multimap.putAll(merchantRate.getUserId(), Sets.newHashSet(merchantRate.getQueueList().split(",")) );
        });
        TreeMultimap<String, String> inverse = Multimaps.invertFrom(multimap, TreeMultimap.create());
        return inverse;
    }


    @Override
    public List<DealOrder> grabAnOrderListFind(String orderType, boolean islittle, String userId) {
        List<DealOrder> dealOrders1 = new ArrayList<>();
        String publicAccount = "";
        String publicAccounts = "";
        try {
            //卡商只接指定商户的单子

           publicAccounts = "zhongbang-bank-s";
           publicAccount = "zhongbang-bank";
           List<DealOrder> dealOrders = dealOrderMapper.grabAnOrderListFind(orderType, publicAccount);
           List<DealOrder> dealOrderss = dealOrderMapper.grabAnOrderListFind(orderType, publicAccounts);

            dealOrders.addAll(dealOrderss);//合并大额 小额单

            log.info("dealOrdersall----{}",dealOrders.stream().map(DealOrder::getId).collect(Collectors.toList()));
            String agent = findAgent(userId);
            log.info("{}----{}",userId,agent);
            //从admin写入的缓存里获取到商户-顶代卡商的映射关系，
            List<UserRate> merchantRates = userRateServiceImpl.getMerchantWitRateFromAdminCache();
            //然后反向映射出顶代卡商-商户的关系
            //这里实际是string-HashSet<Stirng> 的map
            TreeMultimap<String, String> cardDerlerToMerchantsMap = getCardDealerMapToMerchant(merchantRates);

            //如果当前卡商顶代有对应的商户,那么相关商户的单子都可以抢
            if(cardDerlerToMerchantsMap.containsKey(agent))
            {
                Set<String> merchants = cardDerlerToMerchantsMap.get(agent);
                log.info("merchants----{}", JSONUtil.toJsonStr(merchants));
                dealOrders1 = dealOrders.stream().filter(dealOrder -> merchants.contains(dealOrder.getOrderAccount())).collect(Collectors.toList());
                log.info("dealOrders1----{}",dealOrders1.stream().map(DealOrder::getId).collect(Collectors.toList()));
            }
            //否则不显示任何单子给卡商

            /*if(agent.equals("lang916")){
                dealOrders1 = dealOrders.stream().filter(dealOrder -> "al918".equals(dealOrder.getOrderAccount())).collect(Collectors.toList());

                log.info("dealOrders1----{}",dealOrders1.stream().map(DealOrder::getId).collect(Collectors.toList()));
            }else{
                dealOrders1 = dealOrders.stream().filter(dealOrder -> !"al918".equals(dealOrder.getOrderAccount())).collect(Collectors.toList());
                log.info("mark5----{}", dealOrders1.stream().map(DealOrder::getId).collect(Collectors.toList()));
            }*/
       }catch (Throwable e ){
            e.printStackTrace();
            log.error("查询抢单异常",e);
            if(islittle){
                publicAccount = "zhongbang-bank-s";
            }else {
                publicAccount = "zhongbang-bank";
            }
            dealOrders1 = dealOrderMapper.grabAnOrderListFind(orderType, publicAccount);
        }
        return dealOrders1;
    }

   String findAgent(String userId){
       UserInfo userAgent = userInfoDao.findUserAgent(userId);

       if(StrUtil.isEmpty(userAgent.getAgent())){
           return userId;
       }else{
           return findAgent(userAgent.getAgent());
       }



   }


    @Override
    public DealOrder findOrderByUserqr(String orderId, String userId) {
        return dealOrderMapper.findOrderByUserqr(orderId,userId);
    }

    @Override
    public boolean updateGrabOrder(DealOrder order, boolean islittle, boolean aiAuto) {
        String publicAccount = "";
        if(islittle){
            publicAccount = "zhongbang-bank-s";
        }else {
            publicAccount = "zhongbang-bank";
        }
        publicAccount = order.getOrderQrUser();

        return dealOrderMapper.updateGrabOrder(order,publicAccount)>1;
    }

    @Override
    public void unGrabOrder(String orderId) {
          dealOrderMapper.unGrabOrder(orderId);

    }

    @Override
    public void updateSystemBankAmount(BigDecimal bu, String orderId) {

        dealOrderMapper.updateSystemBankAmount(bu,orderId);


    }

    @Override
    public List<DealOrder> findWitOrder(String userId) {
        return dealOrderMapper.findWitOrder(userId);
    }

    @Override
    public boolean updateBankInfoByOrderIdAUTO(String bankInfo, String orderId) {
        int a = dealOrderMapper.updateBankInfoByOrderIdAUTO(bankInfo, orderId);
        return a == 1;
    }

    @Override
    public List<DealOrder> findNightBankFee() {

        List<DealOrder> nightBankFee = dealOrderMapper.findNightBankFee();
        return nightBankFee;
    }
}
