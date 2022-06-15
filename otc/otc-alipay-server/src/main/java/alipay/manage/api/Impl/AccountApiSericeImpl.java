package alipay.manage.api.Impl;

import alipay.manage.api.AccountApiService;
import alipay.manage.bean.UserFund;
import alipay.manage.bean.UserInfo;
import alipay.manage.bean.UserInfoExample;
import alipay.manage.bean.UserInfoExample.Criteria;
import alipay.manage.bean.UserRate;
import alipay.manage.mapper.UserFundMapper;
import alipay.manage.mapper.UserInfoMapper;
import alipay.manage.mapper.UserRateMapper;
import alipay.manage.service.UserInfoService;
import alipay.manage.util.UserUtil;
import alipay.manage.util.amount.AmountPublic;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import otc.result.Result;
import otc.util.encode.HashKit;
import otc.util.enums.BizStatusEnum;
import otc.util.enums.UserStatusEnum;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Component
public class AccountApiSericeImpl implements AccountApiService {
    Logger log = LoggerFactory.getLogger(AccountApiSericeImpl.class);
    @Resource
    private UserRateMapper userRateDao;
    @Resource
    private UserInfoMapper userInfoDao;
    @Resource
    private UserFundMapper userFundDao;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    AmountPublic amountPublic;
    @Autowired
    UserUtil userUtil;
    @Override
    public Result addAccount(UserInfo user) {
        if (ObjectUtil.isNull(user)) {
            return Result.buildFailMessage("实体类为空，请检查传递方法是否正确");
        }
        if (StrUtil.isBlank(user.getUserId())
                || StrUtil.isBlank(user.getUserName())
                || ObjectUtil.isNull(user.getUserType())
                || StrUtil.isBlank(user.getIsAgent())
        ) {
            return Result.buildFailMessage("必传参数为空");
        }
        UserInfo user1 = userInfoDao.findUserId(user.getUserId(), user.getUserName());
        if (ObjectUtil.isNotNull(user1)) {
            return Result.buildFailMessage("当前账户、当前用户名已经被占用，请重新选择");
        }
        String salt = HashKit.randomSalt();
        Result password = HashKit.encodePassword(user.getUserId(), user.getPassword(), salt);
        Result payPasword = HashKit.encodePassword(user.getUserId(), user.getPayPasword(), salt);
        if (!password.isSuccess()) {
            return Result.buildFailMessage("生成密钥失败");
        }
        if (!payPasword.isSuccess()) {
            return Result.buildFailMessage("生成支付密钥失败");
        }
        user.setPassword(password.getResult().toString());
        user.setPayPasword(payPasword.getResult().toString());
        user.setSalt(salt);
        user.setCreateTime(new Date());
        int insertSelective = userInfoDao.insertSelective(user);
        boolean addUserFund = addUserFund(user);
        if (insertSelective > 0 && insertSelective < 2 && addUserFund) {
            userUtil.openAccountCorrlation(user.getUserId());
            return Result.buildSuccess();
        }
        return Result.buildFailMessage("新增用户失败，联系技术人员处理");
    }

    boolean addUserFund(UserInfo info) {
        UserFund fund = new UserFund();
        fund.setAgent(StrUtil.isBlank(info.getAgent()) ? "" : info.getAgent());
        fund.setIsAgent(StrUtil.isBlank(info.getIsAgent()) ? "" : info.getIsAgent());
        fund.setUserType(ObjectUtil.isNull(info.getUserType()) ? "" : info.getUserType().toString());
        fund.setUserId(info.getUserId());
        fund.setUserName(info.getUserName());
        int insertSelective = userFundDao.insertSelective(fund);
        return insertSelective > 0 && insertSelective < 2;
    }

    @Override
    public Result login(UserInfo user) {
        UserInfoExample info = new UserInfoExample();
        Criteria criteria = info.createCriteria();
        if (StrUtil.isNotBlank(user.getUserId())) {
            criteria.andUserIdEqualTo(user.getUserId());
        }
        criteria.andUserTypeEqualTo(2);
        List<UserInfo> userList = userInfoDao.selectByExample(info);
        if (userList.size() > 1) {
            return Result.buildFailMessage("当前用户错误，联系技术人员处理");
        }
        if (CollUtil.isEmpty(userList)) {
            return Result.buildFailMessage("当前用户不存在，请检查账号");
        }
        UserInfo first = CollUtil.getFirst(userList);
        Result password = HashKit.encodePassword(user.getUserId(), user.getPassword(), first.getSalt());
        if (!password.isSuccess()) {
            return Result.buildFailMessage("当前用户错误，联系技术人员处理");
        } //password.getResult().toString()
        if (first.getPassword().equals(password.getResult().toString())) {
            return Result.buildSuccess();
        }
        return Result.buildFailMessage("密码错误，请检查!");
    }

    /**
     * <p>修改密码【登录密码】</p>
     */
    @Override
    public Result updateLoginPassword(UserInfo user) {
    	UserInfo userInfo = userInfoDao.findUserByUserId(user.getUserId());
    	Result password = HashKit.encodePassword(user.getUserId(), user.getPassword(), userInfo.getSalt());
    	if(password.isSuccess()) {
    		boolean equals = userInfo.getPassword().equals(password.getResult().toString());
    		if(equals) {
                Result result = HashKit.encodePassword(user.getUserId(), user.getNewPassword(), userInfo.getSalt());
                int a = userInfoDao.updataPassword(user.getUserId(), result.getResult().toString());
                if (a > 0 && a < 2) {
                    return Result.buildSuccessMessage("密码修改成功");
                }
            }
    	}
        return Result.buildFailMessage("密码修改失败，请检查密码是否正确");
    }

    /**
     * <p>修改支付密码</p>
     */
    @Override
    public Result updatePayPassword(UserInfo user) {
    	UserInfo userInfo = userInfoDao.findUserByUserId(user.getUserId());
    	Result password = HashKit.encodePassword(user.getUserId(), user.getPayPasword(), userInfo.getSalt());
    	if(password.isSuccess()) {
    		boolean equals = userInfo.getPayPasword().equals(password.getResult().toString());
    		if(equals) {
                Result result = HashKit.encodePassword(user.getUserId(), user.getNewPayPassword(), userInfo.getSalt());
                int a = userInfoDao.updataPayPassword(user.getUserId(), result.getResult().toString());
                if (a > 0 && a < 2) {
                    return Result.buildSuccessMessage("密码修改成功");
                }
            }
    	}
        return Result.buildFailMessage("密码修改失败，请检查密码是否正确");
    }

    @Override
    public UserInfo findUserInfo(String userId) {
        return userInfoDao.findUserByUserId(userId);
    }

    @Override
    @CacheEvict(value="user", allEntries=true)
    public boolean updateIsAgent(Integer id,String userId) {
    	UserInfo record1 = new UserInfo();
		record1.setSubmitTime(null);
		record1.setCreateTime(new Date());
		record1.setStatus(null);
		record1.setId(id);
		record1.setUserId(userId);
		record1.setIsAgent("1");
		int updateByPrimaryKey = userInfoDao.updateByPrimaryKeySelective(record1);
		return updateByPrimaryKey > 0 && updateByPrimaryKey < 2;
    }

    
    @Override
    public Result editAccount(UserInfo user) {
        UserInfoExample example = new UserInfoExample();
        Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(user.getUserId());
        int updateByExampleSelective = userInfoDao.updateByExampleSelective(user, example);
        if (updateByExampleSelective > 0 && updateByExampleSelective < 2) {
            return Result.buildSuccessMessage("用户修改成功");
        }
        return Result.buildFail();
    }

    /**
     * <p>修改用户的登录密码</p>
     */
    @Override
    public Result editAccountPassword(UserInfo user) {
        if (StrUtil.isBlank(user.getUserId()) || StrUtil.isBlank(user.getPassword()) || StrUtil.isBlank(user.getNewPassword())) {
            return Result.buildSuccessMessage("必传参数未传，请核实修改");
        }
        UserInfo userInfo = userInfoDao.findUserByUserId(user.getUserId());
        Result password = HashKit.encodePassword(userInfo.getUserId(), user.getPassword(), userInfo.getSalt());
        if (!password.isSuccess() && userInfo.getPassword().equals(password.getResult().toString())) {
            return Result.buildFailMessage("密码错误，请重新出修改密码，或联系客服人处理");
        }
        Result encodePassword = HashKit.encodePassword(userInfo.getUserId(), user.getNewPassword(), userInfo.getSalt());
        if (!encodePassword.isSuccess()) {
            return Result.buildFailMessage("生成密钥失败，联系客服人员处理");
        }
        return null;
    }

    /**
     * 查询商户的下单费率
     *
     * @param userId
     * @param passCode
     * @return
     */
    @Override
    public UserRate findUserRateByUserId(String userId, String passCode, String amount) {
        List<UserRate> userRate = userInfoDao.selectUserRateByUserId(userId, passCode);
        CollUtil.sortByProperty(userRate, "retain1");
        for (UserRate rate : userRate) {
            BigDecimal systemAmount = new BigDecimal(rate.getRetain2());//金额限制
            BigDecimal bigDecimalAmount= new BigDecimal(rate.getRetain3());//金额限制
            BigDecimal bigDecimal = new BigDecimal(amount);
            if (bigDecimal.compareTo(systemAmount) >= 0 &&  bigDecimal.compareTo(bigDecimalAmount) <= 0   ) {
                return rate;
            } else {
                continue;
            }
        }
        return null;
    }

    @Override
    public UserRate findUserRateWitByUserId(String userId, String amount) {
        List<UserRate> userRate = userRateDao.findUserRateWitByUserId(userId);
        CollUtil.sortByProperty(userRate, "retain1");
        for (UserRate rate : userRate) {
            BigDecimal systemAmount = new BigDecimal(rate.getRetain2());//金额限制
            BigDecimal bigDecimal = new BigDecimal(amount);
            if (bigDecimal.compareTo(systemAmount) >= 0) {
                return rate;
            } else {
                continue;
            }
        }
        return null;
    }

    @Override
    public UserFund findUserFundByUserId(String userId) {
        return userInfoDao.selectUsrFundByUserId(userId);
    }

    /**
     * 根据商户的userId修改状态
     *
     * @param userId     商户账号
     * @param paramKey   字段名
     * @param paramValue 状态值
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result auditMerchantStatusByUserId(String userId, String paramKey, String paramValue) {
        int i = 0;
        UserInfo userInfo = null;
        UserStatusEnum userStatusEnum = null;
        switch (paramKey) {
            case "switchs"://商户状态
                //更新userInfo表里的状态
                i = userInfoDao.updateMerchantStatusByUserId(userId, paramKey, paramValue);
                //停止接单，代付状态
                userInfoDao.stopAllStatusByUserId(userId, BizStatusEnum.BIZ_STATUS_CLOSE.getCode());
                //关闭费率通道
                userInfoDao.closeMerchantRateChannel(userId, UserStatusEnum.CLOSE.getCode());
                break;
            case "remitOrderState"://代付状态
            case "enterWitOpen"://代付反查询开关
            case "receiveOrderState"://接单状态
                userInfo = userInfoDao.findUserByUserId(userId);
                userStatusEnum = UserStatusEnum.resolve(UserStatusEnum.CLOSE.getCode());
                if (userStatusEnum.matches(userInfo.getSwitchs())) {
                    return Result.buildFailMessage("此商户已被停用，无法操作");
                }
                i = userInfoDao.updateMerchantStatusByUserId(userId, paramKey, paramValue);
                break;
        }
        if (i == 1) {//成功
            return Result.buildSuccess();
        } else {
            return Result.buildFailMessage("更新商户状态失败");
        }
    }

    @Override
    public UserInfo findPrivateKey(String userId) {
        return userInfoDao.findPrivateKey(userId);
    }

    @Override
    public UserInfo findClick(String userId) {
        return userInfoDao.findClick(userId);
    }

    @Override
    public UserInfo findautoWit(String userId) {
        return userInfoDao.findautoWit(userId);
    }

}
