package iorihuang.bankaccountmanager;

import com.github.fppt.jedismock.RedisServer;
import iorihuang.bankaccountmanager.config.EmbeddedRedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.io.IOException;

//using micrometer for tracing
@SpringBootApplication
@EnableCaching
@EnableAspectJAutoProxy
@Slf4j
public class BankAccountManagerApplication {
        public static void main(String[] args) {
            //redis mock
            int mockPort = 19737;
            String port = System.getProperty("spring.redis.port", String.valueOf(mockPort));
            RedisServer redisMock;
            try {
                redisMock = new EmbeddedRedisConfig().redisServer(Integer.parseInt(port));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.setProperty("spring.redis.port", port);

            SpringApplication.run(BankAccountManagerApplication.class, args);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    redisMock.stop();
                    log.info("redisMock stop");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
    }
}
