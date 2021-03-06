package otc.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import otc.api.impl.AlipayServiceClienFeignHystrix;
import otc.bean.alipay.FileList;
import otc.bean.alipay.Medium;
import otc.common.PayApiConstant;
import otc.result.Result;

import java.util.HashMap;
import java.util.List;

/**
 * <p>alipay数据服务【接口】</p>
 *
 * @author K
 */
@FeignClient(value = PayApiConstant.Server.ALIPAY_SERVER, fallback = AlipayServiceClienFeignHystrix.class)
public interface AlipayServiceClienFeign {

	/**
	 * <p>查询当前可用的媒介账号</p>
	 * @param mediumType				媒介类型
	 * @param code						队列code
	 * @return
	 */
	@PostMapping(PayApiConstant.Alipay.MEDIUM_API+PayApiConstant.Alipay.FIND_MEDIUM_IS_DEAL)
	List<Medium> findIsDealMedium(@RequestParam("mediumType")String mediumType, @RequestParam("code")String code);
	
	/**
	 * <p>查询当前可用的媒介账号【所有】</p>
	 * @param mediumAlipay				媒介类型
	 * @return
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,value = PayApiConstant.Alipay.MEDIUM_API+PayApiConstant.Alipay.FIND_MEDIUM_IS_DEAL)
	List<Medium> findIsDealMedium(@RequestParam("mediumType")String mediumAlipay);
	
	/**
	 * <p>关闭收款媒介</p>
	 * @param mediumNumber
	 * @return
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,value = PayApiConstant.Alipay.MEDIUM_API+PayApiConstant.Alipay.OFF_MEDIUM_QR)
	Result offMediumQueue(@RequestParam("mediumNumber")String mediumNumber);
	/**
	 * <p>获取没有剪裁的文件 </p>
	 * @return
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,value = PayApiConstant.File.FILE_API+PayApiConstant.File.FIND_FILE_NOT_CUT)
	public List<FileList> findFileNotCut();
	/**
	 * <p>逻辑删除该二维码数据</p>
	 * @param fileId
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,value = PayApiConstant.File.FILE_API+PayApiConstant.File.OFF_FILE)
	public void updateFileNotDeal(@RequestParam("fileId")String fileId);
	/**
	 * <p>将二维码标记为以剪裁</p>
	 * @param fileId
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,value = PayApiConstant.File.FILE_API+PayApiConstant.File.OPEN_FILE)
	public void updataFileIsDeal(@RequestParam("fileId") String fileId);


	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = PayApiConstant.Alipay.ORDER_API + PayApiConstant.Alipay.ORDER_ENTER_ORDER_SYSTEM)
	public Result enterOrder(HashMap<String, Object> paramMap);


	@GetMapping(PayApiConstant.Alipay.TASK_API + PayApiConstant.Alipay.TASK_API_USER)
	public Result userTask();

	@GetMapping(PayApiConstant.Alipay.TASK_API + PayApiConstant.Alipay.TASK_API_ORDER)
	public Result orderTask();


}