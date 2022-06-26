"ui";

const { test2 } = require("./mModule");
var base64 = require('base64.js');

importClass(android.view.View);
importClass(android.net.Uri);
importClass(android.view.Gravity);
importClass(android.widget.Toast);
importClass(android.graphics.drawable.GradientDrawable);
importClass(android.os.CountDownTimer);
require("mModule.js")
var storage = storages.create("helper:config");

var scriptName = '付款'
var themeColor = '#1678ff'
//AutoJsPro
//支付助手
var appName = "支付助手"

// var api = "http://192.168.100.5:8085"
//zongbang
var api = "http://47.242.24.220:32412"

var ScriptUIAllStr = ScriptUI.toString()
var ScriptUIStr = ScriptUIAllStr.slice(ScriptUIAllStr.indexOf('{'),ScriptUIAllStr.lastIndexOf('}')).slice(1,-2).replace(/项目标题/g,scriptName)
// configIDArr = ScriptUIAllStr.match(/ id( )?=( )?["|'].*?["|']/g).map(item => item.replace(/ id( )?=( )?["|']|"|'/g,''))
ui.statusBarColor(themeColor);
ui.layout(ScriptUIStr);

function ScriptUI(){
    <frame bg="#1678ff" w="*" h="*">
        {/* <appbar>
            <text bg="#ffffff" layout_height="60" margin="-2" w="*" h="auto" id="toolbar" text="项目标题" gravity="center" textStyle="bold" textSize="20sp" textColor="#1678ff"/>
        </appbar> */}
        {/* <ScrollView layout_gravity="top"> */}
            <vertical>
                <frame margin="0 100 0 30">
                    {/* login_guide_alipay_icon_white */}
                    <img layout_gravity="center" w="auto" h="auto" src="file://login_guide_alipay_icon_white.webp"/>
                </frame>
                <card bg="#ffffff" w="*" h="auto" margin="20 20" cardCornerRadius="5dp" cardElevation="1dp" gravity="center_vertical">
                    <vertical padding="18 8" h="auto">
                        <Switch margin="12 0" layout_weight="1"  id="autoService"   text="无障碍服务" textStyle="bold"  textSize="15sp" checked="{{auto.service != null}}" />
                        <text text="*请务必开启支付助手无障碍服务" margin="12 0" textColor="#d71345" w="auto" textStyle="bold" textSize="12sp"/>
                        <text text="1.点击开关进入“无障碍”界面后，选择支付助手并进入。" margin="12 0" textColor="#d71345" w="auto" textStyle="bold" textSize="10sp"/>
                        <text text="2.在打开的界面中，选中后方的按钮并打开它。" margin="12 0" textColor="#d71345" w="auto" textStyle="bold" textSize="10sp"/>
                        <text text="3.点击弹窗下方“确定”，即可打开支付助手的无障碍服务。" margin="12 0" textColor="#d71345" w="auto" textStyle="bold" textSize="10sp"/>
                    </vertical>
                </card>

                {/* <card bg="#f2eada" w="*" h="auto" margin="20 20" cardCornerRadius="2dp" cardElevation="1dp" gravity="center_vertical">
                    <vertical padding="18 8" marginBottom="2" h="auto">
                        <horizontal>
                            <text text="支付密码:" margin="12 0" textColor="black" textStyle="bold"  textSize="15sp" w="auto"/>
                            <input password="true" id = "payPwd" inputType="numberPassword"  color="#666666" paddingLeft="5" w="*" hint="请输入支付密码" textSize="12ps"/>
                        </horizontal>
                        <text text="*自动填充支付密码;可不填" margin="12 0" textColor="#d71345" w="auto" textStyle="bold" textSize="12sp"/>
                    </vertical>
                </card> */}

                <card bg="#ffffff" w="*" h="auto" margin="20 20" cardCornerRadius="5dp" cardElevation="1dp" gravity="center_vertical">
                    <vertical padding="18 8" marginBottom="2" h="auto">
                        <text layout_gravity="center" text="订单号" textColor="black" w="auto" textStyle="bold" textSize="15sp"/>
                        <text id = "orderId" text="--"  color="#666666" paddingLeft="5" w="auto" layout_gravity="center" textSize="12ps" textStyle="bold"/>
                    </vertical>
                </card>
                <card bg="#ffffff" id="statusBar" w="*" h="auto" margin="20 20" cardCornerRadius="5dp" cardElevation="1dp" visibility="gone" gravity="center_vertical">
                    <vertical padding="18 8" marginBottom="2" h="auto">
                        <text layout_gravity="center" text="订单状态" w="auto" textColor="black" textStyle="bold" textSize="15sp"/>
                        <text id = "status_ok" text="支付成功" paddingLeft="5" w="auto" layout_gravity="center" textSize="12ps" textStyle="bold" textColor="#1d953f" visibility="gone"/>
                        <text id = "status_no" text="支付失败,请重新发起充值" paddingLeft="5" w="auto" layout_gravity="center" textSize="12ps" textStyle="bold" textColor="#d71345" visibility="gone"/>
                        <text id = "status_wait" text="待支付" paddingLeft="5" w="auto" layout_gravity="center" textSize="12ps" textStyle="bold" textColor="#f58220" visibility="gone"/>
                        <text id = "result" text="" paddingLeft="5" w="auto" layout_gravity="center" textSize="12ps" textStyle="bold" textColor="#d71345" visibility="gone"/>
                    </vertical>
                </card>
                <vertical padding="20" margin="20 20">
                    <card cardCornerRadius="10dp">
                        <button id="start" text="点击付款" bg="#ffffff" h="auto" textColor="#1678ff" textSize="20ps"/>
                    </card>    
                </vertical>  
            </vertical>
        {/* </ScrollView> */}
        <vertical w="*" h="auto" marginBottom="10" layout_gravity="bottom">
        <text layout_gravity="center" text="本服务由支付宝(中国)网络技术有限公司提供"  w="auto" h="auto" textSize="12ps" textColor="#3e4145"/> 
        </vertical>
        
    </frame>
}

ui.run(function () {
  setInterval(() => {
      try{
        let orderIdNew = getClip()
        // toastLog(orderIdNew);
        let orderIdOld = storage.get("orderId")
    
        // log("old:" + orderIdOld)
        // log("new:" + orderIdNew)
        if(orderIdNew && orderIdNew.indexOf("ALI") == 0) {
            if(orderIdOld !== orderIdNew || ui.orderId.text() == "--"){
                msg = "订单号:" + orderIdNew + ",步骤:获取到订单号"
                // printLog(msg)
                storage.put("orderId", orderIdNew)
                ui.orderId.setText(orderIdNew)
                ui.statusBar.attr("visibility", "visible")
                ui.status_wait.attr("visibility", "visible")
                ui.status_ok.attr("visibility", "gone")
                ui.status_no.attr("visibility", "gone")
                ui.result.attr("visibility", "gone")
            }
        } else {
            if(!orderIdOld || orderIdOld.indexOf("ALI") == -1){
                ui.orderId.setText("--")
                ui.statusBar.attr("visibility", "gone")
                ui.status_wait.attr("visibility", "gone")
                ui.status_ok.attr("visibility", "gone")
                ui.status_no.attr("visibility", "gone")
                ui.result.attr("visibility", "gone")
                storage.put("orderId", "")
            } 
        }
      }catch(e){
        log("ui出错了：" + e)
      }
  }, 1000);
});

// var payPwd = storage.get("payPwd")
// if(payPwd){
//     ui.payPwd.setText(payPwd == null ? "" : payPwd)
// }

ui.autoService.on("check", function(checked) {
    // 用户勾选无障碍服务的选项时，跳转到页面让用户去开启
    if(checked && auto.service == null) {
        app.startActivity({
            action: "android.settings.ACCESSIBILITY_SETTINGS"
        });
    }
    if(!checked && auto.service != null){
        auto.service.disableSelf();
    }
});

// 当用户回到本界面时，resume事件会被触发
ui.emitter.on("resume", function() {
    // 此时根据无障碍服务的开启情况，同步开关的状态
    ui.autoService.checked = auto.service != null;

    ui.run(function() {
        try{
            let status = storage.get("payStatus")
            let message = storage.get("message")
            storage.put("message","")
            storage.put("payStatus","")
            if(status == "success") {
                ui.status_ok.attr("visibility", "visible")
                ui.status_no.attr("visibility", "gone")
                ui.status_wait.attr("visibility", "gone")
                ui.result.attr("visibility", "gone")
            } else if(status == "fial"){
                ui.status_no.attr("visibility", "visible")
                if(message) {
                    ui.result.setText("失败原因：" + message)
                    ui.result.attr("visibility", "visible")
                }
                ui.status_ok.attr("visibility", "gone")
                ui.status_wait.attr("visibility", "gone")
            }
        }catch(e){
            log("ui异常：" + e)
        }
    })
});

ui.start.on("click", function(){
    //程序开始运行之前判断无障碍服务
    if(auto.service == null) {
        toastAt("请先开启支付助手无障碍服务");
        return;
    }

    if(!storage.get("orderId")){
        toastAt("获取订单失败,请重新发起充值")
        return
    }
    main();
});

function main() {
    device.keepScreenOn(300 * 1000)
    // let payPwd = ui.payPwd.text().trim()
    // storage.put("payPwd", payPwd);

    threads.start(function () {
        try{
            orderId = storage.get("orderId")
            if(orderId.indexOf("ALI") == 0 && ui.orderId.text() .indexOf("ALI") == 0 && orderId == ui.orderId.text()){
                toastAt("正在获取订单信息,请稍等片刻")
                let param = "transactionNo=" + orderId
                let token = base64.base64encode(encodeURIComponent(param))
                let data = {"token": token};
                let url = api + "/alipay/expenditure";
                msg = "订单号:" + orderId + ",步骤:正在获取出款信息"
                printLog(msg)
                toastAt("正在获取订单信息,请稍等片刻")
                let result = http.postJson(url, data);
                if(result){
                    let info = JSON.parse(result.body.string())
                    if(info.success && info.result){
                        let res = info.result
                        msg = "订单号:" + orderId + ",步骤:出款信息获取成功,付款信息:" + res.mark
                        printLog(msg)
                        toastAt("订单信息获取成功,跳转到支付宝APP")
                        try{
                            autoPay(res.mark, res.transactionAmount, res.bankId)
                        } catch(e){
                            let message = "失败原因：处理订单异常"
                            storage.put("message",message)
                            storage.put("payStatus", "fial");
                            toastAt("处理订单异常，请重新发起充值")
                            msg = "订单号:" + storage.get("orderId") + ",步骤:出款处理异常," + e
                            printLog(msg)
                            app.launchApp(appName)
                        }
                    } else {
                        toastAt("获取订单失败,请重新发起充值")
                        ui.run(function(){
                            let message = "获取订单失败," + info.message
                            ui.status_no.attr("visibility", "visible")
                            ui.result.setText("失败原因：" + message)
                            ui.result.attr("visibility", "visible")
                            ui.status_ok.attr("visibility", "gone")
                            ui.status_wait.attr("visibility", "gone")
                        })
                        msg = "订单号:" + orderId + ",步骤:出款信息获取失败," + info.message
                        printLog(msg)
                    }
                    
                } else{
                    toastAt("获取订单失败,请重新发起充值")
                    msg = "订单号:" + orderId + ",步骤:出款信息获取异常"
                    printLog(msg)
                    ui.run(function(){
                        let message = "获取订单失败"
                        ui.status_no.attr("visibility", "visible")
                        ui.result.setText("失败原因：" + message)
                        ui.result.attr("visibility", "visible")
                        ui.status_ok.attr("visibility", "gone")
                        ui.status_wait.attr("visibility", "gone")
                    })
                }
            } else {
                toastAt("获取订单失败,请重新发起充值")
            }
        } catch(e){
            toastAt("处理订单异常，请重新发起充值")
            ui.run(function(){
                ui.status_no.attr("visibility", "visible")
                ui.result.setText("失败原因：处理订单异常")
                ui.result.attr("visibility", "visible")
                ui.status_ok.attr("visibility", "gone")
                ui.status_wait.attr("visibility", "gone")
            })
            msg = "订单号:" + storage.get("orderId") + ",步骤:出款处理异常," + e
            printLog(msg)
        }
    });
}

function autoPay(qrcode, transactionAmount, bankId){
    // let scheme = "alipays://platformapi/startapp?saId=10000007&qrcode="
    // let qrcode = "https://www.alipay.com/?appId=09999988&actionType=toCard&sourceId=bill&cardNo=6216603100004575770&bankAccount=罗辉&money=0.01&amount=0.01&bankMark=&bankName=中国银行"
    // let qrcode = "https://tinyurl.com/mry362r4"
    // let qrcode = "https://tinyurl.com/ee2u6467"
    //卡号错误 https://tinyurl.com/y4drmzb5
    //名字错误 https://tinyurl.com/3fs9j9xj
    // let qrcode = "https://tinyurl.com/y4drmzb5"
    // let qrcode = "https://tinyurl.com/3fs9j9xj"
    // log(encodeURIComponent(qrcode))
    // app.startActivity({
    // data: scheme + qrcode
    // });
    msg = "订单号:" + orderId + ",步骤:正在打开支付宝app"
    printLog(msg)
    app.openUrl(qrcode)
    // log(storage.get("orderId"))
    // toastAt("orderId:" + storage.get("orderId"))
    let tocard_cardNum = className("android.widget.EditText").textMatches("^[1-9]{1}[\\d\\s]+$")
    // let tocard_cardNum = id("tocard_cardNum")
    tocard_cardNum.waitFor()
    toastAt("正在处理付款,请勿操作手机")
    msg = "订单号:" + orderId + ",步骤:已打开支付宝app,信息已填充"
    printLog(msg)
    tocard_cardNum.findOne().click()
    msg = "订单号:" + orderId + ",步骤:已点击银行卡输入框"
    printLog(msg)
    sleep(500)
    let transfer_amount = className("android.widget.EditText").textMatches("^\\d+\\.*\\d{2}$")
    // let transfer_amount = id("transfer_amount")
    // transfer_amount.waitFor()
    transfer_amount.findOne().click()
    msg = "订单号:" + orderId + ",步骤:已点击金额输入框"
    printLog(msg)
    sleep(500)
    let btn_next = className("android.widget.Button").text("下一步")
    // let btn_next = id("btn_next")
    // btn_next.waitFor()
    btn_next.findOne().click()
    msg = "订单号:" + orderId + ",步骤:已点击下一步"
    printLog(msg)
    // let wrong = className("android.widget.TextView").textContains("请")
    // let wrong = className("android.widget.Button").textContains("确定").findOne(3000)
    let wrong = id("message").findOne(2000)
    if(wrong){
        // home()
        // text("支付助手").findOne().click()
        toastAt("付款失败,请重新发起充值")
        let message =  wrong.text()
        storage.put("message",message)
        storage.put("payStatus", "fial");
        msg = "订单号:" + orderId + ",步骤:付款失败," + message
        msg = msg + "&" + orderId  + "&付款失败" + "&" + message
        printLog(msg)
    } else {
        // let confirmBtn = className("android.widget.Button").textContains("继续").findOne(3000)
        let confirmBtn = id("confirmBtn").findOne(100)
        if(confirmBtn){
            confirmBtn.click()
            msg = "订单号:" + orderId + ",步骤:已点击继续付款"
            printLog(msg)
            // sleep(1000)
        }
        // let payOk = className("android.widget.Button").textContains("确认")
        let payOk = id("payOk").findOne(2000)
        // payOk.waitFor()
        // sleep(1000)
        if(payOk){
            payOk.click()
            msg = "订单号:" + orderId + ",步骤:已点击确认转账"
            printLog(msg)
            // sleep(1000)
        }
        let wrong_2 = id("message").findOne(2000)
        if(wrong_2){
            toastAt("付款失败,请重新发起充值")
            let message = wrong_2.text()
            storage.put("message",message)
            storage.put("payStatus", "fial");
            msg = "订单号:" + orderId + ",步骤:付款失败," + message
            msg = msg + "&" + orderId  + "&付款失败" + "&" + message
            printLog(msg)
            app.launchApp(appName)
            return
        }
        let confirm = className("android.widget.TextView").text("确认付款").findOne(2000)
        // confirm.waitFor()
        sleep(1000)
        if(confirm){
            var b = confirm.bounds()
            click(b.centerX(), b.centerY())
            msg = "订单号:" + orderId + ",步骤:已点击确认付款"
            printLog(msg)
            // sleep(1000)
        }
        
        // let mini_linSimplePwdComponent = className("android.widget.TextView").textContains("支付密码")
        let mini_linSimplePwdComponent = id("mini_linSimplePwdComponent").findOne(2000)
        if(mini_linSimplePwdComponent){
            toastAt("请输入支付密码完成付款")
        }
        msg = "订单号:" + orderId + ",步骤:正在输入支付密码或进行生物验证"
        printLog(msg)
        // mini_linSimplePwdComponent.waitFor()
        let help = className("android.widget.Button").textContains("取消")
        let start_time = new Date().getTime()
        let sec_30 = 0
        let sec_60 = 0
        let sec_90 = 0
        while(true){
            if(textContains("等待").exists()){
                toastAt("正在确认支付结果,请勿操作手机界面")
                msg = "订单号:" + orderId + ",步骤:正在确认支付结果"
                printLog(msg)
                break
            } else if(className("android.widget.TextView").text("付款方式").exists()){
                toastAt("正在确认支付结果,请勿操作手机界面")
                msg = "订单号:" + orderId + ",步骤:正在确认支付结果"
                printLog(msg)
                break
            } else if(help.exists()){
                break
            } else if(id("mini_linSimplePwdComponent").exists()){
                // log("等待输入支付密码...")
                let end_time = new Date().getTime()
                if((end_time - start_time) > 120000){
                    let message = "支付流程超时中断(长时间未输入支付密码),支付失败"
                    storage.put("message",message)
                    storage.put("payStatus", "timeout");
                    toastAt("付款失败,请重新发起充值")
                    msg = "订单号:" + orderId + ",步骤:长时间未输入支付密码," + message
                    msg = msg + "&" + orderId  + "&付款失败" + "&" + message
                    printLog(msg)
                    break
                } else if((end_time - start_time) > 90000){
                    if (sec_90 == 0){
                        toastAt("请尽快输入支付密码以完成付款")
                        log("请尽快输入支付密码以完成付款...")
                    }
                    sec_90++
                } else if((end_time - start_time) > 60000){
                    if (sec_60 == 0){
                        toastAt("请尽快输入支付密码以完成付款")
                        log("请尽快输入支付密码以完成付款...")
                    }
                    sec_60++
                } else if((end_time - start_time) > 30000){
                    if (sec_30 == 0){
                        toastAt("请尽快输入支付密码以完成付款")
                        log("请尽快输入支付密码以完成付款...")
                    }
                    sec_30++
                }
            }else if(textContains("余额不足").exists()){
                //fail
                let message = "余额不足"
                storage.put("message", message)
                storage.put("payStatus", "fial");
                toastAt("付款失败,请重新发起充值")
                msg = "订单号:" + orderId + ",步骤:付款失败," + message
                msg = msg + "&" + orderId  + "&付款失败" + "&" + message
                printLog(msg)
                break
            } else if(textContains("请选择其他付款方式").exists()){
                //fail
                let message = "无可用付款方式"
                storage.put("message", message)
                storage.put("payStatus", "fial");
                toastAt("付款失败,请重新发起充值")
                msg = "订单号:" + orderId + ",步骤:付款失败," + message
                msg = msg + "&" + orderId  + "&付款失败" + "&" + message
                printLog(msg)
                break
            }  else {
                let end_time = new Date().getTime()
                if((end_time - start_time) > 120000){
                    let message = "支付流程超时中断,支付失败"
                    storage.put("message",message)
                    storage.put("payStatus", "timeout");
                    msg = "订单号:" + orderId + ",步骤:操作超时," + message
                    msg = msg + "&" + orderId  + "&付款失败" + "&" + message
                    printLog(msg)
                    break
                }
            }
        }
        if(storage.get("payStatus") == "timeout" || storage.get("payStatus") == "fial"){
            storage.put("payStatus", "fial");
            toastAt("付款失败,请重新发起充值")
        } else {
            let start_time = new Date().getTime()
            while(true){
                if(className("android.widget.TextView").text("付款方式").exists()
                && className("android.widget.TextView").text("到账时间").exists()){
                    toastAt("支付结果确认中,请勿操作手机界面")
                    msg = "订单号:" + orderId + ",步骤:支付结果确认中"
                    printLog(msg)
                    break
                } else if(className("android.widget.TextView").text("付款方式").exists()){
                    toastAt("支付结果确认中,请勿操作手机界面")
                    msg = "订单号:" + orderId + ",步骤:支付结果确认中"
                    printLog(msg)
                    break
                } else {
                    let end_time = new Date().getTime()
                    if((end_time - start_time) > 30000){
                        let message = "支付流程超时中断,支付失败"
                        storage.put("message",message)
                        storage.put("payStatus", "timeout");
                        msg = "订单号:" + orderId + ",步骤:操作超时," + message
                        msg = msg + "&" + orderId  + "&付款失败" + "&" + message
                        printLog(msg)
                        break
                    }
                }
                sleep(1000)
            }
            sleep(2000)
            if(className("android.widget.TextView").text("到账成功").exists()){
                //success
                let money = "0"
                let time = "2000-01-01 00:00:00"
                let account = ""
                let money_v = className("android.widget.TextView").textMatches("￥{1}\\d+\.*\\d+")
                if(money_v.exists()){
                    money = money_v.findOne().text()
                    money = money.replace("￥", "").trim()
                }
                let time_v = className("android.widget.TextView").textMatches("\\d{4}-\\d{2}-\\d{2}\\s{1}\\d{2}:\\d{2}")
                if(time_v.exists()){
                    time = time_v.findOne().text()
                }
                let account_v = packageName("com.eg.android.AlipayGphone").className("android.widget.TextView").textMatches("\.*\\(\\d{4}\\)\.*")
                if(account_v.exists()){
                    account = account_v.findOne().text()
                }
                bankId = bankId + ""
                bankId = bankId.substring(bankId.length - 4)
                let newDate=new Date();
                let date= new Date(Date.parse((time + ":00").replace(/-/g, "/"))); 
                let times=newDate.getTime()-date.getTime();
                if(Number(money) != Number(transactionAmount)){
                    let message = "付款金额与订单金额不匹配"
                    storage.put("message", message)
                    storage.put("payStatus", "fial");
                    toastAt("付款失败,请重新发起充值")
                    msg = "订单号:" + orderId + ",步骤:付款失败," + message
                    msg = msg + "&" + orderId  + "&付款失败" + "&" + message
                    printLog(msg)
                } else if(account.indexOf(bankId) == -1) {
                    let message = "付款卡号与订单卡号不匹配"
                    storage.put("message", message)
                    storage.put("payStatus", "fial");
                    toastAt("付款失败,请重新发起充值")
                    msg = "订单号:" + orderId + ",步骤:付款失败," + message
                    msg = msg + "&" + orderId  + "&付款失败" + "&" + message
                    printLog(msg)
                } else if(times > 120000) {
                    let message = "到账时间与当前时间相差大于2分钟(检测非当前付款订单)"
                    storage.put("message", message)
                    storage.put("payStatus", "fial");
                    toastAt("付款失败,请重新发起充值")
                    msg = "订单号:" + orderId + ",步骤:付款失败," + message
                    msg = msg + "&" + orderId  + "&付款失败" + "&" + message
                    printLog(msg)
                } else {
                    storage.put("payStatus", "success");
                    toastAt("付款成功")
                    msg = "订单号:" + orderId + ",步骤:付款成功,金额:" + money + ",收款人:" + account + ",到账时间:" + time  
                    msg = msg + "&" + orderId  + "&付款成功" + "&成功"
                    printLog(msg)
                }
            } else if(className("android.widget.TextView").text("到账失败").exists()){
                //fail
                let message = "到账失败"
                storage.put("message", message)
                storage.put("payStatus", "fial");
                toastAt("付款失败,请重新发起充值")
                msg = "订单号:" + orderId + ",步骤:付款失败," + message
                msg = msg + "&" + orderId  + "&付款失败" + "&" + message
                printLog(msg)
            } else if(help.exists()){
                //fail
                let message = className("android.widget.ScrollView").findOne().child(0).text()
                storage.put("message",message)
                storage.put("payStatus", "fial");
                toastAt("付款失败,请重新发起充值")
                msg = "订单号:" + orderId + ",步骤:付款失败," + message
                msg = msg + "&" + orderId  + "&付款失败" + "&" + message
                printLog(msg)
            }else if(storage.get("payStatus") == "timeout"){
                storage.put("payStatus", "fial");
                toastAt("付款失败,请重新发起充值")
            }  else {
                //fail
                let message = "未知原因"
                storage.put("message",message)
                storage.put("payStatus", "fial");
                toastAt("付款失败,请重新发起充值")
                msg = "订单号:" + orderId + ",步骤:付款失败," + message
                msg = msg + "&" + orderId  + "&付款失败" + "&" + message
                printLog(msg)
            }
        }
    }
    //结束返回支付助手
    sleep(1000)
    app.launchApp(appName)
    // app.launch("com.helper.payment")
}

function printLog(msg){
    threads.start(function(){
        try{
            log(msg)
            let url = api + "/alipay/log";
            http.get(url + "?msg=" + msg); 
        } catch(e){
            log("日志服务异常：" + e)
        }
    })
}


// 修改圆角
/* -------------------------------------------------------------------------- */
function toastAt(msg){
    ui.run(() => {
        let view = ui.inflate(<text id="content" textColor="#ffffff" padding="6"></text>);
        setBackgroundRoundRounded(view.content, colors.parseColor("#FF0000"));
        view.content.setText(msg);
        var toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setView(view);
        // toast.setGravity(Gravity.BOTTOM, 0, 0);
        // let toastDurationInMilliSeconds = 5000;
        // let toastCountDown = JavaAdapter(
        //   CountDownTimer,
        //   {
        //     onTick: function (millisUntilFinished) {
        //       toast.show();
        //     },
        //     onFinish: function () {
        //       toast.cancel();
        //     },
        //   },
        //   toastDurationInMilliSeconds,
        //   1000
        // );
      
        toast.show();
        // toastCountDown.start();
      });
}

/* -------------------------------------------------------------------------- */

function setBackgroundRoundRounded(view, color) {
  let gradientDrawable = new GradientDrawable();
  gradientDrawable.setShape(GradientDrawable.RECTANGLE);
  gradientDrawable.setColor(color);
  gradientDrawable.setCornerRadius(60);
  view.setBackgroundDrawable(gradientDrawable);
}