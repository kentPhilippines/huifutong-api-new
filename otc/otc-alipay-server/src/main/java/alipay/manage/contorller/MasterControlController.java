package alipay.manage.contorller;

import alipay.config.redis.RedisUtil;
import alipay.manage.bean.UserInfo;
import alipay.manage.bean.util.OnlineVO;
import alipay.manage.bean.util.RegisterSetting;
import alipay.manage.service.UserInfoService;
import alipay.manage.util.SessionUtil;
import alipay.manage.util.SettingFile;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import otc.api.ConfigServiceClientFeign;
import otc.bean.config.ConfigFile;
import otc.common.RedisConstant;
import otc.exception.user.UserException;
import otc.result.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Controller
@RequestMapping("/masterControl")
public class MasterControlController {
   private static Logger log= LoggerFactory.getLogger(MasterControlController.class);
    @Autowired ConfigServiceClientFeign configServiceClientFeignImpl;
	@Autowired SessionUtil sessionUtil;
	@Autowired RedisUtil redisUtil;
	@Autowired UserInfoService userInfoService;
	@Autowired SettingFile settingFile;
	/**
	 * <p>获取网站标题</p>
	 * @return
	 */
	@GetMapping("/getSystemSetting")
	@ResponseBody
	public Result getSystemSetting() {
		//	Result config = configServiceClientFeignImpl.getConfig(ConfigFile.ALIPAY, ConfigFile.Alipay.TIBLE_LINK);
		return Result.buildSuccessResults("赚钱宝");
	}

	/**
	 * <p>获取页面配置</p>
	 * @return
	 */
	@GetMapping("/getRegisterSetting")
	@ResponseBody
	public Result getRegisterSetting() {
		RegisterSetting bean = new RegisterSetting();
		bean.setInviteCodeEffectiveDuration(50000000);
		//Result config = configServiceClientFeignImpl.getConfig(ConfigFile.ALIPAY, ConfigFile.Alipay.REGISTER_ENABLED);
		bean.setRegisterEnabled( false);//是否开放注册功能 config.getResult().toString().equals("1")?true:false
		bean.setInviteRegisterEnabled(false);
		bean.setRegitserDefaultRebate(0.01);
		return Result.buildSuccessResult(bean);
	}

	/**
	 * 上级查询下级在线人数
	 *
	 * @param request
	 * @return
	 */
	@GetMapping("/querySubOnline")
	@ResponseBody
	public Result querySubOnline(HttpServletRequest request) {
		UserInfo user = sessionUtil.getUser(request);
		if (ObjectUtil.isNull(user)) {
			throw new UserException("未获取到登录用户",null);
		}
		//登陆状态的Key
		Set<String> loginKeys = redisUtil.keys(RedisConstant.User.LOGIN_PARENT + "*");
		System.out.println("loginKeys " + loginKeys);
		//获取所有key的值
		List<Object> loginKeysValue = redisUtil.multiGet(loginKeys);
		System.out.println("loginKeysValue " + loginKeysValue);
		//查询用户的下级用户
		List<String> subLevelMembers = userInfoService.findSubLevelMembers(user.getUserId());
		System.out.println("subLevelMembers " + subLevelMembers);
		String str = CollUtil.getFirst(subLevelMembers);
		System.out.println("str " + str);
		//两个集合的交集 在线人员集合
		List<String> loginMembers = subLevelMembers.stream().filter(item -> loginKeysValue.contains(item)).collect(toList());
		//接单状态队列
		Set<String> bizKeyMembers = redisUtil.keys(RedisConstant.User.BIZ_QUEUE + "*");
		//获取所有key的值
		List<Object> bizMembersValue = redisUtil.multiGet(bizKeyMembers);
		//取两个集合的交集 接单人员集合
		List<String> bizingMembers = subLevelMembers.stream().filter(item -> bizMembersValue.contains(item)).collect(toList());

		OnlineVO onlineVO = new OnlineVO();
		onlineVO.setLoginOnlineCount(loginMembers.size());
		onlineVO.setBizOnlineCount(bizingMembers.size());
		onlineVO.setOnlineList(loginMembers.size() == 0 ? "" : StringUtils.join(loginMembers.toArray(), "，"));
		onlineVO.setBizList(bizingMembers.size() == 0 ? "" : StringUtils.join(bizingMembers.toArray(), "，"));
		onlineVO.setIsAgent(user.getIsAgent());
		return Result.buildSuccessResult("数据获取成功", onlineVO);
	}
}