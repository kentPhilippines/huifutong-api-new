package alipay.manage.mapper;

import alipay.manage.bean.DealOrder;
import alipay.manage.bean.DealOrderExample;
import org.apache.ibatis.annotations.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Mapper
public interface DealOrderMapper {
    static final String ORDER_INFO_CHANNEL = "ORDER:INFO:CHANNEL";

    int countByExample(DealOrderExample example);

    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    int deleteByExample(DealOrderExample example);

    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    int deleteByPrimaryKey(Integer id);
    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    int insert(DealOrder record);

    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    int insertSelective(DealOrder record);

    List<DealOrder> selectByExampleWithBLOBs(DealOrderExample example);

    List<DealOrder> selectByExample(DealOrderExample example);

    DealOrder selectByPrimaryKey(Integer id);

    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    int updateByExampleSelective(@Param("record") DealOrder record, @Param("example") DealOrderExample example);

    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    int updateByExampleWithBLOBs(@Param("record") DealOrder record, @Param("example") DealOrderExample example);

    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    int updateByExample(@Param("record") DealOrder record, @Param("example") DealOrderExample example);

    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    int updateByPrimaryKeySelective(DealOrder record);

    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    int updateByPrimaryKeyWithBLOBs(DealOrder record);

    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    int updateByPrimaryKey(DealOrder record);

    /**
     * <p>根据用户id 查询交易订单</p>
     *
     * @param id
     * @param userId
     * @param createTime
     * @return
     */
    List<DealOrder> selectByExampleByMyId( @Param("userId") String userId, @Param("createTime") String createTime, @Param("orderStatus") String orderStatus,@Param("orderType")String orderType);

    /**
     * <p>根据用户id查询自己的交易订单号记录</p>
     * @param order
     * @return
     */
    List<DealOrder> findMyOrder(DealOrder order);
    @Cacheable(cacheNames = {ORDER_INFO_CHANNEL}, unless = "#result == null")
    @Select("select * from alipay_deal_order where orderId = #{orderId}")
	DealOrder findOrderByOrderId(@Param("orderId")String orderId);

    /**
     * 修改订单状态
     * @param orderId				订单号
     * @param status				订单状态
     * @param mag					修改备注
     * @return
     */
    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    @Update("update alipay_deal_order set orderStatus  = #{status} , dealDescribe   = #{mag} ,submitTime = NOW()  , retain4  = 1   where  orderId = #{orderId}")
	int updateOrderStatus(@Param("orderId")String orderId, @Param("status")String status, @Param("mag")String mag);

    @Cacheable(cacheNames = {ORDER_INFO_CHANNEL}, unless = "#result == null")
    @Select("select * from alipay_deal_order where associatedId = #{associatedId}")
	DealOrder findOrderByAssociatedId(@Param("associatedId")String associatedId);

    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    @Update("update alipay_deal_order set retain2  = #{id}  where  orderId = #{orderId}")
    void updataXianyYu(@Param("orderId") String orderId, @Param("id") String id);

    @Select("select retain2 , orderId from alipay_deal_order where createTime > DATE_ADD(NOW(),INTERVAL-3 HOUR)  and orderStatus = 1 and orderQrUser = 'XianYuZhifubao'")
    List<DealOrder> findXianYuOrder();

    @Select("SELECT retain2 , orderId FROM alipay_deal_order WHERE createTime > DATE_ADD(NOW(),INTERVAL -20 MINUTE)  AND orderStatus = 1 AND orderQrUser = 'XYALIPAYSCAN' LIMIT 50")
    List<DealOrder> findXianYuOrder1();

    @Select("SELECT retain2 , orderId FROM alipay_deal_order WHERE createTime > DATE_ADD(NOW(),INTERVAL -20 MINUTE)  AND orderStatus = 1 AND orderQrUser = 'ChuanShanJia' LIMIT 100")
    List<DealOrder> findXianYuOrder2();
    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    @Update("update alipay_deal_order set orderQr = #{bank}  , lockWit  =  1 ,  lockWitTime = now()  where orderId = #{orderId}")
    int updateBankInfoByOrderId(String bank, String orderId);

    @Cacheable(cacheNames = {ORDER_INFO_CHANNEL}, unless = "#result == null")
    @Select("select id, orderId, associatedId, orderStatus, dealAmount ,orderAccount, orderQrUser,externalOrderId,  notify  , isNotify  FROM alipay_deal_order  where  orderId = #{orderId}")
    DealOrder findOrderNotify(@Param("orderId") String orderId);

    @Cacheable(cacheNames = {ORDER_INFO_CHANNEL}, unless = "#result == null")
    @Select("select id, orderId, associatedId, orderType , orderQr ,  orderStatus, dealAmount ,orderAccount, orderQrUser,externalOrderId FROM alipay_deal_order  where  orderId = #{orderId}")
    DealOrder findOrderStatus(String orderId);

    /*  @Insert("insert into  alipay_usdt_order (blockNumber, timeStamp, hash, blockHash, fromAccount, contractAddress, toAccount, value,tokenName,tokenSymbol) " +
              "values (#{order.blockNumber}, #{order.timeStamp},#{order.hash},#{order.blockHash},#{order.from}" +
              ",#{order.ontractAddress},#{order.to}#{order.value},#{order.tokenName},#{order.tokenSymbol})")
      int addUsdtOrder(@Param("order")USDTOrder order);*/
    @Insert("insert into  alipay_usdt_order (blockNumber, timeStamp, hash, blockHash, fromAccount, contractAddress, toAccount, value,tokenName,tokenSymbol) " +
            "values (#{blockNumber}, #{timeStamp},#{hash},#{blockHash},#{from}" +
            ",#{ontractAddress},#{to},#{value},#{tokenName},#{tokenSymbol})")
    int addUsdtOrder(@Param("blockNumber") String blockNumber, @Param("timeStamp") String timeStamp,
                     @Param("hash") String hash, @Param("blockHash") String blockHash, @Param("from") String from,
                     @Param("contractAddress") String contractAddress, @Param("to") String to,
                     @Param("value") String value, @Param("tokenName") String tokenName, @Param("tokenSymbol") String tokenSymbol);


    @Update("update alipay_deal_order set txhash = #{hash} where orderId = #{orderId}")
    int updateUsdtTxHash(@Param("orderId") String orderId, @Param("hash") String hash);


    /*
        查询未结算账户的订单  成功   且   retain4  = 1    且   10秒内 结算最多15 笔
     */
    @Select("" + " " +
            " ( select * from alipay_deal_order where orderStatus = 2  and  retain4 = 1   and (orderType = 1 or orderType = 3 )      and    submitTime  between    CURRENT_TIMESTAMP - INTERVAL 50 MINUTE   " +
            "     and  now() order by id limit 25) " +
            " union all " +
            " ( select * from alipay_deal_order  where orderStatus = 2  and  retain4 = 1   and  orderType = 4  and enterPay = 1  and    submitTime  between    CURRENT_TIMESTAMP - INTERVAL 500 MINUTE  " +
            "        and  CURRENT_TIMESTAMP - INTERVAL 5 MINUTE )  ")
    List<DealOrder> findSuccessAndNotAmount();


    /**
     * 修改订单为以结算
     *
     * @param orderId
     */
    @Update(" update alipay_deal_order set retain4 = 0  where orderId = #{orderId}")
    void updateSuccessAndAmount(@Param("orderId") String orderId);


    /**
     * 修改10分钟之前的订单为，未收到回调， 订单类型为    卡商收款,(卡商收款为   商户充值    卡商充值 )
     *
     * @return
     */
    @Update("update alipay_deal_order set orderStatus = 3 where ( orderType = 1 or orderType = 3  ) and orderStatus = 1  and  createTime <= CURRENT_TIMESTAMP - INTERVAL 5 MINUTE ")
    int updateUnNotify();

    @CacheEvict(value = ORDER_INFO_CHANNEL, allEntries = true)
    @Update("update alipay_deal_order set payImg = #{qrcodeId} where orderId = #{orderId} ")
    boolean updatePayImg(@Param("orderId") String orderId, @Param("qrcodeId") String qrcodeId);

    /**
     * 填充当前订单的回调原始消息
     *
     * @param orderId
     * @param payInfo
     */
    @Update("update alipay_deal_order set payInfo = #{payInfo} where orderId = #{orderId} ")
    void updatePayInfo(@Param("orderId") String orderId, @Param("payInfo") String payInfo);

    @Select(" SELECT sum(dealAmount) as  dealAmount , orderQrUser as orderQrUser  FROM  alipay_deal_order  " +
            "WHERE  orderStatus  = 1   GROUP  by orderQrUser")
    List<DealOrder> findWaitWitUser();
    @Update("update alipay_deal_order set bankAmountNow = #{mountSystem} where orderId = #{orderId} ")
    int updateBankAmount( @Param("orderId") String orderId, @Param("mountSystem") String mountSystem);
    @Update("update alipay_deal_order set   lockWit  =  1 ,  lockWitTime = now()  where orderId = #{orderId}")
    void enterOrderLock( @Param("orderId") String orderId);
    @Select("select  *  from alipay_deal_order  where   orderType = 4 and orderStatus = 1 and   orderQrUser = #{orderQrUser}  order by id")
    List<DealOrder> findWitOrderByUserId(@Param("orderQrUser") String orderQrUser);




    @Update("update alipay_deal_order set orderQrUser = #{order.orderQrUser} , orderQr = #{order.orderQr} , lockWit  = 0  ,  enterPayTime  = null ," +
            " retain1 = #{order.retain1} , retain2 = #{order.retain1}  , feeId = #{order.feeId} where orderId = #{order.orderId} ")
    boolean updateWitQr(@Param("order")  DealOrder order);

}
