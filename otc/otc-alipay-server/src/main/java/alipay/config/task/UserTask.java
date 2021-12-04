package alipay.config.task;

import alipay.manage.mapper.MediumMapper;
import alipay.manage.mapper.UserInfoMapper;
import alipay.manage.service.MediumService;
import alipay.manage.service.impl.MediumServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserTask {
	@Resource
	private UserInfoMapper userInfoDao;


	@Resource
	MediumMapper mediumDao;

	/**
	 * <p>定时清理账户统计数据</p>
	 */
	public void userTask() {
		userInfoDao.updateUserTime();
		mediumDao.updateUserTime();
	}
	public void userAddTask() {
		userInfoDao.bak();
		mediumDao.bak();

	}
	
	/**
	 * <p>备份账户表</p>
	 */
	public void copyUserTask() {
		
	}
	
	

}
