package otc.apk.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import otc.apk.util.HeartUtil;
import otc.common.PayApiConstant;

@Component
@Configuration
@EnableScheduling
public class Task {
    private static final Log log = LogFactory.get();
    @Autowired
    HeartUtil heartUtil;


    @Scheduled(cron = "0/10 * * * * ?")
    public void task() {
        log.info("【执行更新队列操作】");
       // heartUtil.clickHeart();
    }


}
