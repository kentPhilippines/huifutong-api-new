package otc.common;

import org.springframework.beans.factory.annotation.Value;

public class PayApiConstant {
	public class Server{
		public static final String PAY_SERVER = "otc-pay-server";//四方支付服务
		public static final String CONFIG_TASK_SERVER = "otc-task-config-server";//定时任务和配置类服务
		public static final String ALIPAY_SERVER = "otc-alipay-server";//支付宝服务
		public static final String FILE_SERVER = "otc-file-server";
		public static final String QUEUE_APK_SERVER = "otc-queue-apk-server";
		public static final String DEALPAY_SERVER = "otc-dealpay-server";
	}
	public class Test{
		public static final String FIND_TEST = "/findTest";
		public static final String TEST = "/test";

	}
	public class Config{
		public static final String CONFIG_API = "/config-api";//后台专用接口
		public static final String CONFIG_API_GET_CONFIG_MANAGE = "/getConfigAdmin";//后台获取所有接口配置的接口
		public static final String CONFIG_API_GET_CONFIG_SYSTEM = "/getConfigSystem";//获取统一配置文件接口
	}


	public class Dealpay{
		public static final String ACCOUNT_API = "/account-api";//账户类型接口
		public static final String ADD_ACCOUNT = "/add-account";//用户开户
		public static final String EDIT_ACCOUNT = "/edit-account";//修改用户资料
		public static final String EDIT_ACCOUNT_PASSWORD = "/edit-account-password";//修改登录密码
		public static final String EDIT_ACCOUNT_PAY_PASSWORD = "/edit-account-pay-password";//修改资金密码
		public static final String AMOUNT = "/amount";//账户加钱接口 【充值点数】
		public static final String DELETE_AMOUNT ="/delete_amount";//账户减钱接口【账户金额扣除】
		public static final String FREEZE_AMOUNT = "/freeze_amount";//账户冻结接口【账户资金冻结】
		public static final String GENERATE_ORDER_DEDUCT = "/deduct_order"; //生成扣款订单【扣款订单】
		public static final String AUDIT_MERCHANT_STATUS = "/audit-merchant-status"; //修改商户的状态

		public static final String RECHARGE_ORDER = "/recharge_order";//充值订单状态修改接口
		public static final String WIT_ORDER = "/wit_order";//代付订单状态修改接口


		public static final String DEAL_API = "/deal-api";// 卡商交易接口
		public static final String RECHARGE_URL = "/recharge-url";//卡商充值交易【卡商入款交易】
		public static final String WITH_PAY = "/with-pay";//卡商出款交易【卡商出款交易】
	}


	public class Alipay{
		public static final String ACCOUNT_API = "/account-api";//账户类型接口
		public static final String ADD_ACCOUNT = "/add-account";//用户开户
		public static final String EDIT_ACCOUNT = "/edit-account";//修改用户资料
		public static final String EDIT_ACCOUNT_PASSWORD = "/edit-account-password";//修改登录密码
		public static final String EDIT_ACCOUNT_PAY_PASSWORD = "/edit-account-pay-password";//修改资金密码
		public static final String AMOUNT = "/amount";//账户加钱接口 【充值点数】
		public static final String DELETE_AMOUNT = "/delete_amount";//账户减钱接口【账户金额扣除】
		public static final String FREEZE_AMOUNT = "/freeze_amount";//账户冻结接口【账户资金冻结】
		public static final String GENERATE_ORDER_DEDUCT = "/deduct_order"; //生成扣款订单【扣款订单】


		public static final String ORDER_API = "/order-api";//订单处理接口
		public static final String WIT_API_ENTER = "/wit-api-enter";//代付确认处理
		public static final String BANK_AMOUNT_CHICK = "/bank-amount-enter";//卡商银行卡余额确认接口
		public static final String ORDER_ENTER_ORDER = "/enter-orderQr";//订单状态修改及资金流水账户变动
		public static final String ORDER_ENTER_ORDER_BACK = "/enter-orderQr-back";//卡商代付订单回滚
        public static final String ORDER_ENTER_ORDER_SYSTEM = "/enter-orderQr-system";//自动回调确认订单接口


        public static final String MEDIUM_API = "/medium";                                    //媒介处理公共接口
        public static final String FIND_MEDIUM_IS_DEAL = "/find-medium-is-deal";            //查询当前可以交易的媒介
        public static final String AUDIT_MERCHANT_STATUS = "/audit-merchant-status"; //修改商户的状态
        public static final String OFF_MEDIUM_QR = "/off-medium-qr"; //将不再队列中的二维码踢出队列

        public static final String TASK_API = "/task-api";    //定时任务接口
        public static final String TASK_API_USER = "/user-api";    //账户的定时任务
        public static final String TASK_API_ORDER = "/order-api";    //订单结算的定时任务


    }

	public class File{
		public static final String FILE_API = "/file-api";//文件服务api
		public static final String ADD_FILE = "/add-file";//新增文件
		public static final String FIND_FILE = "/find-file";//查询文件
		public static final String FIND_FILE_NOT_CUT = "/find-file-not-cut";//查询未剪裁的图片
		public static final String OFF_FILE = "/off-file-file";//关闭一个二维码
		public static final String OPEN_FILE = "/open-file-file";//当前二维码裁剪完成
		public static final String FILE_TASK = "/file-task";//图片裁剪定时任务
	}

	public class Queue{
		public static final String QUEUE_API = "/queue-api";
		public static final String FIND_QR = "/find-queue-qr";
        public static final String UPDATA_QR = "/updata-queue-qr";
        public static final String ADD_QR = "/add-queue-qr";
        public static final String DELETE_QR = "/delete-queue-qr";
        public static final String TASK_MEDIUM = "/task-medium";
		public static final String UPDATA_BANK = "/updata-queue-bank";
    }
	@Value("${otc:payInfo.url}")
	public   String url;


    public class Notfiy {
        public static final String NOTFIY_API = "/notfiy-api";
        public static final String NOTFIY_API_WAI = "/notfiy-api-pay";
        public static final String NOTFIY_AGENT = "/notfiy-agent";
	//	public final String OTHER_URL = url ; //"http://8.210.97.36:43421";
    }
}
