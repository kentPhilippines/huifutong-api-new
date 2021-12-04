package alipay.manage.mapper;

import alipay.manage.bean.WithdrawExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import otc.bean.dealpay.Withdraw;

import java.util.List;
@Mapper
public interface WithdrawMapper {
    int countByExample(WithdrawExample example);

    int deleteByExample(WithdrawExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Withdraw record);

    int insertSelective(Withdraw record);

    List<Withdraw> selectByExample(WithdrawExample example);

    Withdraw selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Withdraw record, @Param("example") WithdrawExample example);

    int updateByExample(@Param("record") Withdraw record, @Param("example") WithdrawExample example);

    int updateByPrimaryKeySelective(Withdraw record);

    int updateByPrimaryKey(Withdraw record);

    @Update("update alipay_withdraw set " +
            "orderStatus = #{orderStatus}, " +
            "approval = #{approval}," +
            "comment = #{comment}, " +
            "submitTime = sysdate() ," +
            "ethFee = 0 ," +
            "channelId = #{channelId} " +
            "where orderId = #{orderId}")
    int updataOrderStatus(@Param("orderId") String orderId, @Param("approval") String approval,
                          @Param("comment") String comment, @Param("orderStatus") String orderStatus,
                          @Param("channelId") String channelId);

    @Select("select * from alipay_withdraw where orderId = #{orderId}")
    Withdraw findWitOrder(@Param("orderId") String orderId);


    /**
     * 代付失败  附加修改订单为已推送
     *
     * @param orderId
     * @param orderStatus
     * @param comment
     * @return
     */
    @Update("update alipay_withdraw set " +
            "orderStatus = #{orderStatus}, " +
            "comment = #{comment}, " +
            "submitTime = sysdate() " +
            "where orderId = #{orderId}")
    int updataOrderStatusEr(@Param("orderId") String orderId,
                            @Param("orderStatus") String orderStatus,
                            @Param("comment") String comment);

    @Select("select * from alipay_withdraw where appOrderId = #{orderId} and userId = #{appId}")
    Withdraw findOrderByApp(@Param("appId") String appId, @Param("orderId") String orderId);

    @Update("update alipay_withdraw set " +
            "orderStatus = #{orderStatus}, " +
            "approval = #{approval}," +
            "comment = #{comment}, " +
            "submitTime = sysdate() " +
            "where orderId = #{orderId}")
    int updataOrderStatus1(@Param("orderId") String orderId, @Param("approval") String approval,
                           @Param("comment") String comment, @Param("orderStatus") String orderStatusEr);


    @Update("update alipay_withdraw set " +
            "comment = #{comment} " +
            "where orderId = #{orderId}")
    void updateComment(@Param("orderId") String orderId, @Param("comment") String comment);


    @Select("select orderId from alipay_withdraw where ethFee = 0 and currency = 'USDT' and orderStatus = 2 order by createTime asc limit 1 ")
    Withdraw findEthFee();

    @Update("update alipay_withdraw set ethFee = 1 , txhash = #{hash} where orderId = #{orderId} ")
    void updateEthFee(@Param("orderId") String orderId, @Param("hash") String hash);


    @Select(
            "select  sum(amount) as amount " +
                    " from alipay_withdraw where userId = #{appId} and TO_DAYS(createTime) =  TO_DAYS(now()) and orderStatus = 2   group by orderStatus ; ")
    Withdraw findOrderByAppSum(@Param("appId") String appId);

    @Update("update alipay_withdraw set orderStatus = 4 where orderId = #{orderId}")
    boolean updatePush(@Param("orderId") String orderId);


    /**
     * 未结算且  cny   且  成功
     *
     * @return
     */
    @Select("select * from alipay_withdraw where ethFee = 0 and currency = 'CNY' and orderStatus = 2  and submitTime >= CURRENT_TIMESTAMP - INTERVAL 10 MINUTE    limit 15")
    List<Withdraw> findSuccessAndNotAmount();

    /**
     * 修改订单未已结算
     *
     * @param orderId
     */
    @Update("update alipay_withdraw set ethFee = 1 where orderId = #{orderId}")
    void updateSuccessAndAmount(String orderId);

    @Select("select * from alipay_withdraw where  orderStatus = 1 and  withdrawType = 1   limit 15")
    List<Withdraw> findNotPush();

    @Update("update alipay_withdraw set " +
            "comment = #{msg}, " +
            "submitTime = sysdate() " +
            "where orderId = #{orderId}")
    void updateMsg(@Param("orderId") String orderId, @Param("msg") String msg);

    @Update("update alipay_withdraw set    comment =  '无法处理当前订单,请再次推送' ,pushOrder = 3   where orderId  = #{orderId} ")
    int updatePushAgent(String orderId);
}
