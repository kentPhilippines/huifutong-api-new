package test.number;

import alipay.AlipayApplication;
import alipay.manage.service.IAlipayBankConfigService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AlipayApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BankCodeInit {
    @Autowired
    private IAlipayBankConfigService alipayBankConfigService;

    @Test
    public void initBank()
    {
        alipayBankConfigService.selectAlipayBankConfig("ICBC");
        log.info(alipayBankConfigService.selectAlipayBankConfig("ICBC")+"");
        log.info(alipayBankConfigService.selectAlipayBankConfig("ICBC")+"");
    }
}
