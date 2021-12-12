package alipay;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
/**
 * <p>宝转宝支付接口服务</p>
 * @author K
 */
@SpringBootApplication
@EnableEurekaClient        //服务注册
@EnableFeignClients        //远程调用
@ServletComponentScan(basePackages = "alipay.*")
@ComponentScan(basePackages = "alipay.*") //注入扫描
@EnableTransactionManagement //事务
@EnableRedisHttpSession //redis   session 共享
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AlipayApplication {
	public static void main(String[] args) {
		SpringApplication.run(AlipayApplication.class, args);
		//new SpringApplicationBuilder(AlipayApplication.class).properties("server.port=" + port).run(args);
	}
}
