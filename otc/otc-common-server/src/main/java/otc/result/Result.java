package otc.result;

import cn.hutool.json.JSONUtil;

/**
 * <p>全局返回值类</p>
 * @author K
 */
public class Result {
	private boolean success; 
	private String message; 
	private Object result; 
	private Integer code;

	public Result(boolean b, Integer o, Object result) {
		this.success=b;
		this.result=result;
		this.code=o;

	}

	/**
	 * <p>成功无返回值</p>
	 * @return
	 */
	public static Result buildSuccess() {
		return new Result(true, null, null, null);
	}
	/**
	 * <p>成功、有通知消息，无code无结果</p>
	 * @param message
	 * @return
	 */
	public static Result buildSuccessMessage(String message) {
		return new Result(true, message, null, null);
	}
	/**
	 * <p>成功、有结果集，无通知，无code</p>
	 * @param result
	 * @return
	 */
	public static Result buildSuccessResult(Object result) {
		return new Result(true, null, result,null);
	}

	/**
	 * <p>成功、无结果集，无通知，无code</p>
	 * @param result
	 * @return
	 */
	public static Result buildSuccessResult( ) {
		return new Result(true, null, null,null);
	}
	/**
	 * <p>成功、有结果集，无通知，无code</p>
	 * @param result
	 * @return
	 */
	public static Result buildSuccessResult(String msg ,Object result) {
		return new Result(true, msg, result, 1);
	}
	public static Result buildSuccessResultCode(String msg ,Object result,Integer i) {
		return new Result(true, msg, result,i);
	}
	/**
	 * <p>返回结果为成功,无返回提示,结果集</p>
	 * <li>boolean success : true // 请求操作成功</li>
	 * <li>String  message : null // 提示消息null</li>
	 * <li>Object  result  : result // 返回结果集result</li>
	 * @param result 返回结果集result
	 * @return
	 */
	public static Result buildSuccessResults(Object result) {
		return new Result(true, null, result);
	}
	/**
	 * <p>失败有结果集</p>
	 * @param result
	 * @return
	 */
	public static Result buildFailResult(Object result) {
		return new Result(false, null, result,null);
	}
	/**
	 * <p>失败</p>
	 * @param result
	 * @return
	 */
	public static Result buildFail( ) {
		return new Result(false, null, null,null);
	}
	/**
	 * <p>失败有通知消息</p>
	 * @param result
	 * @return
	 */
	public static Result buildFailMessage(String message) {
		return new Result(false, message, null,null);
	}
	
		public Result(boolean success, String message, Object result, Integer code) {
			super();
			this.success = success;
			this.message = message;
			this.result = result;
			this.code = code;
		}
	public Result() {
		super();
	}

	public static Result buildSuccessMessageCode(String msg, Object o, int i) {
			return new Result(true, msg, o,i);
	}
	public static Result buildFailMessageCode(String msg, int i) {
			return new Result(false, msg, null,i);
	}

	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "Result [success=" + success + ", message=" + message + ", result=" + result + ", code=" + code + "]";
	}
	public  String toJson(){
		return JSONUtil.parse(this).toString();
	}
}
