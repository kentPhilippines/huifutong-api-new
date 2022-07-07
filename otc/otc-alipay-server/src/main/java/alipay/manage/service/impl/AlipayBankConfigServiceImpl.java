package alipay.manage.service.impl;


import alipay.manage.bean.AlipayBankConfig;
import alipay.manage.mapper.AlipayBankConfigMapper;
import alipay.manage.service.IAlipayBankConfigService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import otc.util.date.DateUtils;

import java.util.List;

/**
 * bankconfigService业务层处理
 * 
 * @author ruoyi
 * @date 2022-07-07
 */
@Service
public class AlipayBankConfigServiceImpl implements IAlipayBankConfigService
{
    static final String BANK_CONFIG = "CONFIG:INFO:BANKCONFIG";
    @Autowired
    private AlipayBankConfigMapper alipayBankConfigMapper;

    /**
     * 查询bankconfig
     * 
     * @param id bankconfigID
     * @return bankconfig
     */
    @Override
    public AlipayBankConfig selectAlipayBankConfigById(Integer id)
    {
        return alipayBankConfigMapper.selectAlipayBankConfigById(id);
    }

    /**
     * 查询bankconfig列表
     * 
     * @param alipayBankConfig bankconfig
     * @return bankconfig
     */
    @Override
    public List<AlipayBankConfig> selectAlipayBankConfigList(AlipayBankConfig alipayBankConfig)
    {
        return alipayBankConfigMapper.selectAlipayBankConfigList(alipayBankConfig);
    }

    //1天过期
    @Cacheable(cacheNames = {BANK_CONFIG+"#1"}, unless = "#result == null")
    @Override
    public AlipayBankConfig selectAlipayBankConfig(String bankCode)
    {
        AlipayBankConfig alipayBankConfig =new AlipayBankConfig();
        alipayBankConfig.setCodeValue(bankCode);
        List<AlipayBankConfig> list = alipayBankConfigMapper.selectAlipayBankConfigList(alipayBankConfig);
        if(CollectionUtil.isNotEmpty(list))
        {
            return list.get(0);
        }
        return null;
    }

    /**
     * 新增bankconfig
     * 
     * @param alipayBankConfig bankconfig
     * @return 结果
     */
    @Override
    public int insertAlipayBankConfig(AlipayBankConfig alipayBankConfig)
    {
        alipayBankConfig.setCreateTime(DateUtils.getNowDate());
        return alipayBankConfigMapper.insertAlipayBankConfig(alipayBankConfig);
    }

    /**
     * 修改bankconfig
     * 
     * @param alipayBankConfig bankconfig
     * @return 结果
     */
    @Override
    public int updateAlipayBankConfig(AlipayBankConfig alipayBankConfig)
    {
        return alipayBankConfigMapper.updateAlipayBankConfig(alipayBankConfig);
    }

    /**
     * 删除bankconfig对象
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteAlipayBankConfigByIds(String ids)
    {
        return alipayBankConfigMapper.deleteAlipayBankConfigByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除bankconfig信息
     * 
     * @param id bankconfigID
     * @return 结果
     */
    @Override
    public int deleteAlipayBankConfigById(Integer id)
    {
        return alipayBankConfigMapper.deleteAlipayBankConfigById(id);
    }
}
