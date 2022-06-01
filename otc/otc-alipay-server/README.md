# otcjinx
卡商可用余额= userFund.getAccountBalance() - userFund.getSumProfit() 账户余额-总分佣
updataReceiveOrderStateNO --此接口已作废没在用

----------------代付下单逻辑---------
RedisLockUtil.AMOUNT_USER_KEY + userId //redis上锁
flag==true manage！=null //是后台提现 
--协议验证md5
--出款查询费率逻辑
select * from alipay_user_rate where feeType = 2 and `switchs` = 1 and userId = #{userId}
retain1 优先级，先根据优先级排序
retain2 出款限制，出款金额必须大于才会匹配上

查询用户信息
select from alipay_user_info where userId = #{userId}
remitOrderState // 1 接单 2 暂停接单
switchs  // 1 接单 2 暂停接单

getWitip //匹配出款ip  非后台取款会判断
getEnterWitOpen //这个数据默认是1  ，没有找到其他地方有set值 应该没有用到 ，代付反查？ 非后台取款会判断


userRate.getRetain2() //这个判断有点多余和上面的查询rate重复了
higMoney //最高出款限额判断
isNumber(witAmount) //代付金额不能存在小数

amount //是否检查余额

UserInfo userInfoByUserId  //查询渠道，判断状态
ChannelFee channelFee  //根据通道和PayTypr 查询出通道费率
wit.getBankcode()  //判断银行卡编码是否在代码种存在
wit.getApporderid() + userRate.getUserId() //根据订单+商户名 判断是否重复订单
createWit  //创建订单 并根据订单+商户名加redis锁 1小时

RedisLockUtil.AMOUNT_USER_KEY + userId  //redis 解锁
-----------代付下单逻辑结束


-------------代付结算逻辑
TASK:ORDER:PUSH: + "lock"  //代码块加锁 防止重复执行
select * from alipay_withdraw where  orderStatus = 1 and  withdrawType = 1   limit 15 //轮询取出15条待结算数据
TASK:ORDER:PUSH:+order.getOrderId() //检查redis 是否已经处理过 防并发
TASK:ORDER:PUSH:+order.getOrderId() //加锁200秒
select  *  from alipay_run_order  where associatedId = #{associatedId} //查流水表 是否已结算
--开始结算
WitPayImpl.witAutoPush
fundUserFundAccounrBalace //判断金额是否足够
super.withdraw //扣款

zhongbang-bank --大额出款通道
zhongbang-bank-s --小额出款通道

addOrder //创建订单
"ORDER:" + order.getOrderQrUser() + ":AUTO" //redis创建缓存，10MINS  接收短信供回调用

TASK:ORDER:PUSH: + "lock"  //代码块解锁
--------------代付结算逻辑结束



****
总体流程：
下单->异步结算(扣钱)创建订单
异步场景： 卡商后台查询订单->卡商选择卡/qrcode/setBankCard->收银台->支付-->接收短信
异步场景： 小助手抓取短信->回调api 流程结束
****