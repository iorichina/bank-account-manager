package iorihuang.bankaccountmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//using micrometer for tracing
@SpringBootApplication
public class BankAccountManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankAccountManagerApplication.class, args);
    }
}

