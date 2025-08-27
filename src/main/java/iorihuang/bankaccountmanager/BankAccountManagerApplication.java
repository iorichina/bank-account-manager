package iorihuang.bankaccountmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

//using micrometer for tracing
@SpringBootApplication
@EnableCaching
@EnableAspectJAutoProxy
public class BankAccountManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankAccountManagerApplication.class, args);
    }
}
