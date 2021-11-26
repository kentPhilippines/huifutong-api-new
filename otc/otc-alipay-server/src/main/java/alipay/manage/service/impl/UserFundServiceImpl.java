package alipay.manage.service.impl;

import alipay.manage.bean.DealOrder;
import alipay.manage.bean.UserFund;
import alipay.manage.bean.UserInfo;
import alipay.manage.mapper.DealOrderMapper;
import alipay.manage.mapper.MediumMapper;
import alipay.manage.mapper.UserFundMapper;
import alipay.manage.service.UserFundService;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.stereotype.Component;
import otc.bean.alipay.Medium;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserFundServiceImpl implements UserFundService {
	public static final Log log = LogFactory.get();

	@Resource
	private UserFundMapper userFundDao;
	@Resource
	private MediumMapper mediumDao;

	@Resource
	DealOrderMapper orderMapper;
	@Override
	public UserFund showTodayReceiveOrderSituation(String userId) {
		return userFundDao.findUserFundByUserId(userId);
	}

	@Override
	public UserFund findUserInfoByUserId(String userId) {
		return userFundDao.findUserFundByUserId(userId);
	}

	@Override
	public List<UserFund> findBankUserId(BigDecimal amount) {

		List<UserFund>  fundList  = new ArrayList<>();
		List<UserFund> bankUser = userFundDao.findBankUserId(amount);
		List<DealOrder> orderList = orderMapper.findWaitWitUser();
		for (DealOrder order : orderList){
			log.info(order.getOrderQrUser());
			log.info(order.getDealAmount().toString());
		}
		try {
			ConcurrentHashMap<String, DealOrder> qrCollect = orderList.stream().collect(Collectors.toConcurrentMap(DealOrder::getOrderQrUser, Function.identity(), (o1, o2) -> o1, ConcurrentHashMap::new));

			for(UserFund fund : bankUser){
				log.info(fund.getDeposit().toString());
				log.info(qrCollect.toString());
				DealOrder dealOrder = qrCollect.get(fund.getUserId());
				if(null == dealOrder ){
					continue;
				}
				BigDecimal dealAmount = dealOrder.getDealAmount();//正在处理金额
				BigDecimal deposit = fund.getDeposit();
				BigDecimal divide = deposit.subtract(dealAmount);
				if(amount.doubleValue()<divide.doubleValue()){
					fundList.add(fund);
				}
			}
		}catch (Throwable e){
			log.error("选择出款卡商异常",e.getMessage(),e);
			fundList = bankUser;
		}

	//	List<Medium> bankByAmountWit = mediumDao.findBankByAmountWit(amount);






















		return fundList;
	}

	@Override
	public List<UserFund> findFundAllQr() {



		return userFundDao.findFundAllQr();
	}

}
