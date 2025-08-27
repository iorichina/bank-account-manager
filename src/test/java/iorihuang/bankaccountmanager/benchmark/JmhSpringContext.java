package iorihuang.bankaccountmanager.benchmark;

import iorihuang.bankaccountmanager.BankAccountManagerApplication;
import iorihuang.bankaccountmanager.dto.CreateAccountRequest;
import iorihuang.bankaccountmanager.helper.snowflakeid.RecyclableAtomicLong;
import iorihuang.bankaccountmanager.model.bankaccount.AccountType;
import iorihuang.bankaccountmanager.service.BankAccountService;
import org.openjdk.jmh.annotations.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import redis.embedded.RedisServer;

import java.util.concurrent.TimeUnit;

/**
 * JMH Spring上下文配置类，用于在所有基准测试之间共享Spring上下文
 */
@State(Scope.Benchmark)
@Fork(1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class JmhSpringContext {
    public static ConfigurableApplicationContext ctx;
    public static BankAccountService service;
    public static RedisServer redisServer;

    public static CreateAccountRequest createRequest;
    public static String testAccountNumber = "Account";
    public static String testAccountNumber1 = "A001";
    public static String testAccountNumber2 = "A002";
    public static String testAccountNumber3 = "A003";
    public static String testAccountNumber4 = "A004";
    public static String testBalance = "10000000.011504";

    public static volatile RecyclableAtomicLong createCount = new RecyclableAtomicLong(BankAccountServiceBenchmarkIface.THRESHOLD, 100);
    public static volatile RecyclableAtomicLong updateCount = new RecyclableAtomicLong(BankAccountServiceBenchmarkIface.THRESHOLD, 100);
    public static volatile RecyclableAtomicLong readCount = new RecyclableAtomicLong(BankAccountServiceBenchmarkIface.THRESHOLD, 100);
    public static volatile RecyclableAtomicLong deleteCount = new RecyclableAtomicLong(BankAccountServiceBenchmarkIface.THRESHOLD, 100);

    @Setup(Level.Trial)
    public void setup() {
        System.out.println();
        System.out.println("////////////////////////////////");
        System.out.println("JmhSpringContext setup - Starting Spring Boot application");
        System.out.println("////////////////////////////////");
        System.out.println();

        // 只有在上下文为null时才启动Spring Boot应用
        if (null == ctx) {
            ctx = SpringApplication.run(BankAccountManagerApplication.class);
            service = ctx.getBean(BankAccountService.class);
            
            try {
                redisServer = ctx.getBean(RedisServer.class);
            } catch (Exception e) {
                // 忽略Redis服务器获取异常
            }

            // 初始化测试数据
            createRequest = new CreateAccountRequest()
                    .setAccountNumber(testAccountNumber)
                    .setAccountType(AccountType.SAVINGS.getCode())
                    .setOwnerId("123456789")
                    .setOwnerName("张三")
                    .setInitialBalance(testBalance)
                    .setContactInfo("123456789");
            
            try {
                createRequest.setAccountNumber(testAccountNumber1);
                service.createAccount(createRequest);
                createRequest.setAccountNumber(testAccountNumber2);
                service.createAccount(createRequest);
                createRequest.setAccountNumber(testAccountNumber3);
                service.createAccount(createRequest);
                createRequest.setAccountNumber(testAccountNumber4);
                service.createAccount(createRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        System.out.println();
        System.out.println("////////////////////////////////");
        System.out.println("JmhSpringContext tearDown - Stopping Spring Boot application");
        System.out.println("////////////////////////////////");
        System.out.println();

        // 在所有测试完成后才关闭Spring上下文
        // 这里可以留空，或者在JVM关闭时处理
    }

    public static void closeContext() {
        if (redisServer != null) {
            redisServer.stop();
            System.out.println("redisServer.stop");
        }
        if (ctx != null) {
            ctx.close();
            System.out.println("ctx.close");
        }
    }
}