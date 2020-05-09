package alipay.manage.mapper;

import alipay.manage.bean.UserRate;
import alipay.manage.bean.UserRateExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
@Mapper
public interface UserRateMapper {
    int countByExample(UserRateExample example);

    int deleteByExample(UserRateExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(UserRate record);

    int insertSelective(UserRate record);

    List<UserRate> selectByExample(UserRateExample example);

    UserRate selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") UserRate record, @Param("example") UserRateExample example);

    int updateByExample(@Param("record") UserRate record, @Param("example") UserRateExample example);

    int updateByPrimaryKeySelective(UserRate record);

    int updateByPrimaryKey(UserRate record);

    /**
     * <p>查询当前用户唯一可用的代付费率</p>
     * @param userId
     * @param prytype 
     * @return
     */
    @Select("select * from alipay_user_rate where feeType = 2 and `switchs` = 1 and userId = #{userId} ")
	UserRate findUserRateWitByUserId(@Param("userId") String userId );

    @Select("select * from alipay_user_rate where feeType = 1 and userId = #{userId} and `switchs` = 1 and payTypr = #{productAlipayScan} and status  = 1")
	UserRate findUserRate(@Param("userId")String userId,@Param("productAlipayScan") String productAlipayScan);

    /**
     * 通过用户查询产品的费率
     * @param userId
     * @param productCode
     * @return
     */
    @Select("select * from alipay_user_rate where userId = #{userId} and payTypr = #{payTypr}")
    UserRate findProductFeeBy(@Param("userId")String userId,@Param("payTypr") String productCode);
    
    
    @Select("select * from alipay_user_rate where id = #{id}")
	UserRate findFeeById(@Param("id")Integer id);
    
    @Select("select * from alipay_user_rate where userId = #{userId}")
	UserRate findUserRateInfoByUserId(@Param("userId") String userId);
	/**
	 * 查询码商入款费率
	 * @param account
	 * @return
	 */
	@Select("select * from alipay_user_rate where feeType = 1 and userId =  #{userId}")
	UserRate findUserRateR(@Param("userId") String userId);


	@Update("update alipay_user_rate set fee = #{fee},payTypr=#{payTypr} where feeType = 1 and userId = #{userId} ")
	int updateRateR(@Param("userId")String userId, @Param("fee")String fee,@Param("payTypr")String payTypr);
}