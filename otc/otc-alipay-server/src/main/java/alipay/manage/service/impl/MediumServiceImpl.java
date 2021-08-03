package alipay.manage.service.impl;

import alipay.config.redis.RedisUtil;
import alipay.manage.bean.MediumExample;
import alipay.manage.bean.MediumExample.Criteria;
import alipay.manage.bean.UserInfo;
import alipay.manage.mapper.MediumMapper;
import alipay.manage.mapper.UserInfoMapper;
import alipay.manage.service.CorrelationService;
import alipay.manage.service.MediumService;
import alipay.manage.util.bankcardUtil.BankTypeUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otc.api.alipay.Common;
import otc.bean.alipay.Medium;
import otc.common.RedisConstant;
import otc.util.number.Number;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MediumServiceImpl implements MediumService {

    Logger log = LoggerFactory.getLogger(MediumServiceImpl.class);
    @Resource
    MediumMapper mediumDao;
    @Resource
    UserInfoMapper userInfoMapper;
    @Autowired
    CorrelationService correlationService;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public boolean addMedium(Medium medium) {
        MediumExample example = new MediumExample();
        MediumExample.Criteria criteria = example.createCriteria();
        if (StrUtil.isNotBlank(medium.getMediumNumber())) {
            criteria.andMediumNumberEqualTo(medium.getMediumNumber());//银行卡号
            /**
             * 这里验证唯一需要加上  银行卡类型
             */
        }
        criteria.andIsDealEqualTo(Common.Medium.QR_IS_DEAL_ON);
        if (CollUtil.isNotEmpty(mediumDao.selectByExample(example))) {//验证银行卡号唯一
            return false;
        }
        String medium2 = Number.getMedum();
        medium.setMediumId(medium2);
        medium.setIsDeal(Common.Medium.QR_IS_DEAL_ON);
        medium.setStatus(Integer.valueOf(Common.STATUS_IS_NOT_OK));
        String agentId = correlationService.findAgent(medium.getQrcodeId());
        medium.setAttr(agentId);//顶代标识
        /**
         * 还需要   添加当前 银行卡当日最高接单金额， 以及当前银行卡当前回调标识，，， 各个银行都是不一样的   回调标识  在这里单独写方法以做区分
         */
        medium.setNotfiyMask(BankTypeUtil.replaceBank(medium.getAccount(), medium.getMediumNumber()));
        int insertSelective = mediumDao.insertSelective(medium);
        Medium medium1 = mediumDao.findMediumBy(medium2);
        UserInfo user = userInfoMapper.findUserByUserId(medium1.getQrcodeId());
        boolean deleteAccountMedium = correlationService.addAccountMedium(user.getUserId(), user.getUserId(), medium1.getId());
        if (deleteAccountMedium) {
            log.info("【账户，账户代理关系，账户媒介关系更新完毕】");
        } else {
            log.info("【账户，账户代理关系，账户媒介关系更新【【失败】】】");
        }
        return insertSelective > 0 && insertSelective < 2;
    }

    @Override
    public List<Medium> findMedium(Medium medium) {
        System.out.println("medium====> " + medium.getStatus());
        MediumExample example = new MediumExample();
        MediumExample.Criteria criteria = example.createCriteria();
        if (StrUtil.isNotBlank(medium.getMediumNumber())) {
            criteria.andMediumNumberEqualTo(medium.getMediumNumber());
        }
        if (StrUtil.isNotBlank(medium.getMediumId())) {
            criteria.andMediumIdEqualTo(medium.getMediumId());
        }
        if (StrUtil.isNotBlank(medium.getMediumHolder())) {
            criteria.andMediumHolderEqualTo(medium.getMediumHolder());
        }
        if (StrUtil.isNotBlank(medium.getMediumPhone())) {
            criteria.andMediumPhoneEqualTo(medium.getMediumPhone());
        }
        if (StrUtil.isNotBlank(medium.getQrcodeId())) {
            criteria.andQrcodeIdEqualTo(medium.getQrcodeId());
        }
        if (StrUtil.isNotBlank(String.valueOf(medium.getStatus()))) {
            criteria.andStatusEqualTo(medium.getStatus());
        }
        criteria.andIsDealEqualTo(Common.Medium.QR_IS_DEAL_ON);
        List<Medium> selectByExample = mediumDao.selectByExample(example);
        return selectByExample;
    }
     /**
      *  <p>分页查询收款媒介:当查询条件为空查询收款媒介所属商户</p>
      */
	@Override
	public List<Medium> findAllMedium(String qrcodeId) {
		MediumExample example =  new MediumExample();
		Criteria criteria = example.createCriteria();
        criteria.andQrcodeIdEqualTo(qrcodeId);
        criteria.andIsDealEqualTo(Common.Medium.QR_IS_DEAL_ON);
        List<Medium> selectByExample = mediumDao.selectByExample(example);
        return selectByExample;
	}
    
    @Override
    public Medium findMediumById(String mediumId) {
        MediumExample example =  new MediumExample();
        MediumExample.Criteria criteria = example.createCriteria();
        criteria.andMediumIdEqualTo(mediumId);
        criteria.andIsDealEqualTo(Common.Medium.QR_IS_DEAL_ON);
        List<Medium> selectByExample = mediumDao.selectByExample(example);
        return CollUtil.getFirst(selectByExample);
    }

    @Override
    public Boolean updataMediumById(Medium medium) {
        MediumExample example=new MediumExample();
        MediumExample.Criteria criteria = example.createCriteria();
        criteria.andMediumIdEqualTo(medium.getMediumId());
        medium.setCreateTime(null);
        int update = mediumDao.updateByExampleSelective(medium, example);
        return update > 0 && update < 2;
    }

    @Override
    public List<Medium> findIsDealMedium(String mediumAlipay) {
        MediumExample example =  new MediumExample();
        MediumExample.Criteria criteria = example.createCriteria();
        criteria.andQrcodeIdEqualTo(mediumAlipay);
        criteria.andIsDealEqualTo(Common.Medium.QR_IS_DEAL_ON);
        criteria.andStatusEqualTo(Integer.valueOf(Common.STATUS_IS_OK));
        List<Medium> selectByExample = mediumDao.selectByExample(example);
        return selectByExample;
    }

    /**
     * <p>删除媒介，根据媒介系统编号</p>
     */
    @Override
    public Boolean deleteMedium(String mediumId) {
        MediumExample example =  new MediumExample();
        MediumExample.Criteria criteria = example.createCriteria();
        criteria.andMediumIdEqualTo(mediumId);
        Medium medium = new Medium();
        medium.setIsDeal("1"); //二维码不可用
        medium.setStatus(Common.STATUS_IS_NOT_OK);
        int updateByExample = mediumDao.updateByExampleSelective(medium, example);
        return updateByExample >  0 && updateByExample < 2;
    }

    @Override
    public List<Medium> findIsMyMediumPage(String accountId) {
        List<Medium> selectByExample = mediumDao.findIsMyMediumPage(accountId);
        for (Medium medium : selectByExample) {
            if (redisUtil.hasKey(medium.getMediumNumber() + RedisConstant.User.QUEUEQRNODE)) {
                medium.setIsQueue("1");
            } else {
                medium.setIsQueue("2");
            }
        }
        return selectByExample;
    }

    @Override
    public boolean updataMediumStatusEr(String id) {
    	Medium record=new Medium();
    	record.setId(Integer.parseInt(id));
    	record.setStatus(Common.STATUS_IS_NOT_OK);
    	int flag=mediumDao.updateByPrimaryKeySelective(record);
        return flag>0 && flag<2;
    }

    @Override
    public Medium findMediumId(String id) {
    	Medium medium=mediumDao.selectByPrimaryKey(Integer.valueOf(id));
        return medium;
    }

    @Override
    public boolean updataMediumStatusSu(String id) {
    	Medium record=new Medium();
    	record.setId(Integer.parseInt(id));
    	record.setStatus(Common.STATUS_IS_OK);
    	int flag=mediumDao.updateByPrimaryKeySelective(record);
        return flag>0 && flag<2;
    }

    @Override
    public boolean updataQr(Medium qr) {
        return false;
    }

    @Override
    public boolean selectOpenAlipayAccount(String accountId) {
        return false;
    }

    @Override
    public Medium findMediumByMediumNumber(String mediumNumber) {
        MediumExample example =  new MediumExample();
        MediumExample.Criteria criteria = example.createCriteria();
        criteria.andMediumNumberEqualTo(mediumNumber);
        List<Medium> selectByExample = mediumDao.selectByExample(example);
        return CollUtil.isEmpty(selectByExample)?null:CollUtil.getFirst(selectByExample);
    }

    @Override
    public List<Medium> findMediumByType(String mediumType) {
        return mediumDao.findMediumByType1(mediumType);
    }

    @Override
    public List<Medium> findMediumByType(String mediumType, String code) {
        return mediumDao.findMediumByType(mediumType, code);
    }

    @Override
    public List<Medium> findBankByAmount(BigDecimal amount, List<String> code) {
      return mediumDao.findBankByAmount(amount);
    }

    @Override
    public void updateMountNow(String bankAccount, BigDecimal dealAmount, String add) {
        if (add.equals("sub")) {//减款
            mediumDao.subMountNow(bankAccount, dealAmount);
        } else if (add.equals("add")) {//加款
            mediumDao.addMountNow(bankAccount, dealAmount);
        }
    }

    @Override
    public Medium findMediumByBankAndId(String cardInfo, String userId) {
        return mediumDao.findMediumByBankAndId(cardInfo, userId);
    }


    @Override
    public Medium findBank(String bankInfo) {
        return mediumDao.findBank(bankInfo);
    }

    @Override
    public Map<String, Medium> findBankOpen() {
        List<Medium> mediumList = mediumDao.findBankOpen();
        ConcurrentHashMap<String, Medium> qrCollect = mediumList.stream().collect(Collectors.toConcurrentMap(Medium::getMediumNumber, Function.identity(), (o1, o2) -> o1, ConcurrentHashMap::new));
        return qrCollect;
    }

}
