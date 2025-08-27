package iorihuang.bankaccountmanager.benchmark;

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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JMH 基准测试：accounts相关service createAccount方法
 */
@Fork(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class BankAccountServiceCreateAccountBenchmark implements BankAccountServiceBenchmarkIface {
    BankAccountService service;

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
        // 使用共享的Spring上下文
        service = JmhSpringContext.service;
        createRequest = JmhSpringContext.createRequest;
        
        // 初始化其他请求对象
        updateRequest = new UpdateAccountRequest();
        updateRequest.setOwnerName("李四");
        updateRequest.setContactInfo("987654321");

        transferRequest = new TransferRequest();
        transferRequest.setFromAccountNumber(testAccountNumber1);
        transferRequest.setToAccountNumber(testAccountNumber2);
        transferRequest.setAmount("1.01");
    }

    @Benchmark
    @Order(1)
    public void testCreateAccount() {
        createRequest.setAccountNumber(testAccountNumber + createCount.getAndIncrementWithRecycle());
        try {
            service.createAccount(createRequest);
        } catch (AccountError e) {
            //
        } catch (AccountException e) {
            //
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        // 不再需要在这里关闭Spring上下文，由JmhSpringContext统一管理
        System.out.println("BankAccountServiceCreateAccountBenchmark tearDown completed");
    }

}
