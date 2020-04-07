package deal.manage.contorller;


import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import deal.manage.bean.BankList;
import deal.manage.bean.UserInfo;
import deal.manage.service.BankListService;
import deal.manage.service.UserInfoService;
import deal.manage.util.LogUtil;
import deal.manage.util.SessionUtil;
import otc.api.dealpay.Common;
import otc.result.Result;

@Controller
@RequestMapping("/recharge")
public class RechargeController {
	Logger log = LoggerFactory.getLogger(RechargeController.class);
	@Autowired UserInfoService accountServiceImpl;
	@Autowired SessionUtil sessionUtil;
	@Autowired LogUtil logUtil;
	@Autowired BankListService bankCardSvc;
	
}