package alipay.config.task;

import alipay.config.listener.ServerConfig;
import alipay.manage.util.file.FileUtils;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@Configuration
@EnableScheduling
public class TaskSelf {
    private static final Log log = LogFactory.get();
    @Autowired
    private UserTask userTaskImpl;
    @Autowired
    private OrderTask orderTask;
    @Autowired
    private BankOpen banks;
    @Autowired
    private ServerConfig serverConfig;

    @Scheduled(cron = "0/10 * * * * ?")
    public void orderTask() {
        if (serverConfig.getServerPort() != 9010) {
            log.info("当前任务端口号不正确");
            return;
        }
        log.info("【开始进行每10秒订单清算】");
        orderTask.orderTask();
        log.info("【开始进行10秒代付订单推送】");
        orderTask.orderWitTask();

    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void openBank() {
        if (serverConfig.getServerPort() != 9010) {
            log.info("当前任务端口号不正确");
            return;
        }
        log.info("【开始放开银行卡限制】");
        banks.open();
        ThreadUtil.execute(()->{
           // FileUtils.load(FileUtils.getFile("/img"));
        });
    }
    @Scheduled(cron = "0/5 * * * * ?")
    public void updateBnakAmount() {
        if (serverConfig.getServerPort() != 9010) {
            log.info("当前任务端口号不正确");
            return;
        }
        log.info("【开始放开银行卡限制】");
       // banks.updateBnakAmount();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void userTask() {
        if (serverConfig.getServerPort() != 9010) {
            log.info("当前任务端口号不正确");
            return;
        }
        log.info("【开始进行每日账户清算】");
        userTaskImpl.userAddTask();
        userTaskImpl.userTask();

    }
   // @Scheduled(cron = "0/10 0 0-6 * * ? *")
    public void nightBankFee() {
        if (serverConfig.getServerPort() != 9010) {
            log.info("当前任务端口号不正确");
            return;
        }
        log.info("【晚间卡商浮动点位结算】");
      //  banks.nightBankFee();

    }
}
