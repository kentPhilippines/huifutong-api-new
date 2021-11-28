package alipay.manage.mapper;

import alipay.manage.bean.Amount;
import alipay.manage.bean.UserFund;
import alipay.manage.bean.UserFundExample;
import alipay.manage.bean.UserInfo;
import org.apache.ibatis.annotations.*;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserFundMapper {
    static final String USER = "USERINFO:FUND";
    int countByExample(UserFundExample example);
    int deleteByExample(UserFundExample example);
    int deleteByPrimaryKey(Integer id);
    int insert(UserFund record);
    int insertSelective(UserFund record);
    List<UserFund> selectByExample(UserFundExample example);
    UserFund selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") UserFund record, @Param("example") UserFundExample example);

    int updateByExample(@Param("record") UserFund record, @Param("example") UserFundExample example);

    int updateByPrimaryKeySelective(UserFund record);

    int updateByPrimaryKey(UserFund record);


    @Select("select * from alipay_user_fund auf " +
            " left join alipay_user_info aui on auf.userId = aui.userId " +
            " where aui.userType = 2 and  ( ( auf.accountBalance + auf.quota  - auf.sumProfit - auf.freezeBalance - ((100 - aui.credit   ) / 100 *  (auf.accountBalance + auf.quota  - auf.sumProfit - auf.freezeBalance)) ) > #{amount}  ) and" +
            " ( auf.accountBalance + auf.quota  - auf.sumProfit  <=  auf.deposit ) " +
            " and aui.switchs = 1 and aui.receiveOrderState = 1 ")
    List<UserFund> findUserByAmount(@Param("amount") BigDecimal amount);

    @Select(" select  id, userId, userName, cashBalance, rechargeNumber, freezeBalance, accountBalance, " +
            "    sumDealAmount, sumRechargeAmount, sumProfit, sumAgentProfit, sumOrderCount, todayDealAmount, " +
            "    todayProfit, todayOrderCount, todayAgentProfit, userType, agent, isAgent, createTime, " +
            "    version ,quota  , sumOtherWitAmount,todayWitAmount,sumWitAmount,todayOtherWitAmount , deposit   from alipay_user_fund  NOLOCK where userId=#{userId}")
    UserFund findUserFundByUserId(@Param("userId") String userId);

    @Update("update alipay_user_fund set rechargeNumber = rechargeNumber + #{deduct}, freezeBalance = freezeBalance - #{deduct}, " +
            "accountBalance = accountBalance - #{deduct}, version = version + 1 where id = #{id} and version = #{version} ")
    int updateBalanceById(@Param("id") Integer id, @Param("deduct") BigDecimal deduct, @Param("version") Integer version);

    @Insert("insert into alipay_amount (orderId, userId, amountType, accname, orderStatus, amount, actualAmount, dealDescribe) " +
            "values (#{orderId}, #{userId},#{amountType},#{accname},#{orderStatus},#{amount},#{amount},#{dealDescribe} )")
    int insetAmountEntity(Amount amount);

    @Select("select * from  alipay_user_fund WHERE   userId IN ( " +
            "			select childrenName from alipay_correlation WHERE parentName IN ( " +
            "			select userId from alipay_user_info WHERE userType  = 2 AND isAgent = '1' AND agent IS NULL AND accountBalance > #{amount}  ))")
    List<UserFund> findUserByAmountAgent(@Param("amount") BigDecimal amount);


    @Select("select  userId, userName, cashBalance, rechargeNumber, " +
            "freezeBalance, accountBalance ,quota  from alipay_user_fund where userId=#{userId}")
    UserFund fundUserFundAccounrBalace(@Param("userId") String userId);

    @Select("select  userId, userName, accountBalance from alipay_user_fund where userId=#{userId}")
    UserFund findBalace(String userId);


    @Cacheable(cacheNames = {USER}, unless = "#result == null")
    @Select("select userId ,currency from alipay_user_fund where userId = #{userId} ")
    UserFund findCurrency(@Param("userId") String userId);

    List<UserFund> findUserByWeight(List<Object> asList2);


    @Select("select * from alipay_user_fund where  accountBalance > 0  " +
            " and    userType = 2 and userId in" +
            " ( select userId from alipay_user_info where " +
            "remitOrderState = 1 and `switchs` = 1 ) order by todayDealAmount desc ")
    List<UserFund> findBankUserId(BigDecimal amount);


    @Select("select * from alipay_user_fund where userType = 2  ")
    List<UserFund> findFundAllQr();
}