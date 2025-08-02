package iorihuang.bankaccountmanager.benchmark;

import iorihuang.bankaccountmanager.BankAccountManagerApplication;
import iorihuang.bankaccountmanager.dto.CreateAccountRequest;
import iorihuang.bankaccountmanager.dto.TransferRequest;
import iorihuang.bankaccountmanager.dto.UpdateAccountRequest;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.helper.snowflakeid.RecyclableAtomicLong;
import iorihuang.bankaccountmanager.model.bankaccount.AccountType;
import iorihuang.bankaccountmanager.service.BankAccountService;
import org.junit.jupiter.api.Order;
import org.openjdk.jmh.annotations.*;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import redis.embedded.RedisServer;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JMH 基准测试：accounts相关service方法
 */
//@SpringBootTest
@Fork(1)
//@State(Scope.Benchmark)
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class BankAccountServiceTransferBenchmark implements BankAccountServiceBenchmarkIface {
    static ConfigurableApplicationContext ctx;
    static BankAccountService service;
    static RedisServer redisServer;

    static CreateAccountRequest createRequest;
    static UpdateAccountRequest updateRequest;
    static TransferRequest transferRequest;
    static String testAccountNumber = "Account";
    static String testAccountNumber1 = "A001";
    static String testAccountNumber2 = "A002";
    static String testAccountNumber3 = "A003";
    static String testAccountNumber4 = "A004";
    static String testBalance = "10000000.011504";

    static volatile RecyclableAtomicLong createCount = new RecyclableAtomicLong(THRESHOLD, 100);
    static volatile RecyclableAtomicLong updateCount = new RecyclableAtomicLong(THRESHOLD, 100);
    static volatile RecyclableAtomicLong readCount = new RecyclableAtomicLong(THRESHOLD, 100);
    static volatile RecyclableAtomicLong deleteCount = new RecyclableAtomicLong(THRESHOLD, 100);
    static volatile AtomicLong lastId;

    @Setup(Level.Trial)
    public void setup() {
//    static {
        System.out.println();
        System.out.println("////////////////////////////////");
        System.out.println("setup");
        System.out.println("////////////////////////////////");
        System.out.println();
        if (null != ctx) {
            return;
        }
        ctx = SpringApplication.run(BankAccountManagerApplication.class);
        try {
            service = ctx.getBean(BankAccountService.class);
        } catch (BeansException e) {
            //
        }
        try {
            redisServer = ctx.getBean(RedisServer.class);
        } catch (BeansException e) {
            //
        }
        System.out.println();
        System.out.println("----------------------------------");
        System.out.println(Arrays.toString(ctx.getBeanDefinitionNames()));
        System.out.println("---------------------------------");
        System.out.println();

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
        } catch (AccountError e) {
            throw new RuntimeException(e);
        } catch (AccountException e) {
            throw new RuntimeException(e);
        }

        updateRequest = new UpdateAccountRequest();
        updateRequest.setOwnerName("李四");
        updateRequest.setContactInfo("987654321");

        transferRequest = new TransferRequest();
        transferRequest.setFromAccountNumber(testAccountNumber1);
        transferRequest.setToAccountNumber(testAccountNumber2);
        transferRequest.setAmount("1.01");

    }

    @Benchmark
    @Order(3)
    public void testTransfer() {
        try {
            service.transfer(transferRequest);
        } catch (AccountException e) {
            //
        } catch (AccountError e) {
//            throw new RuntimeException(e);
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        System.out.println();
        System.out.println("////////////////////////////////");
        System.out.println("tearDown");
        System.out.println("////////////////////////////////");
        System.out.println();
        if (null != redisServer) {
            redisServer.stop();
            System.out.println("redisServer.stop");
            System.out.println("////////////////////////////////");
        }
        if (ctx != null) {
            ctx.close();
            System.out.println("ctx.close");
            System.out.println("////////////////////////////////");
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //
        }
    }

}
