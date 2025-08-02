package iorihuang.bankaccountmanager.benchmark;

import iorihuang.bankaccountmanager.BankAccountManagerApplication;
import iorihuang.bankaccountmanager.dto.BankAccountListDTO;
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
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JMH 基准测试：accounts相关service方法
 */
//@SpringBootTest
@Fork(1)//避免多次启动springboot容器
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class BankAccountServiceBenchmark {
    ConfigurableApplicationContext ctx;
    private BankAccountService service;

    private CreateAccountRequest createRequest;
    private UpdateAccountRequest updateRequest;
    private TransferRequest transferRequest;
    private String testAccountNumber = "Account";
    private String testAccountNumber1 = "A001";
    private String testAccountNumber2 = "A002";
    private String testAccountNumber3 = "A003";
    private String testAccountNumber4 = "A004";
    private String testBalance = "10000000.011504";

    volatile RecyclableAtomicLong createCount = new RecyclableAtomicLong(10000, 100);
    volatile RecyclableAtomicLong updateCount = new RecyclableAtomicLong(10000, 100);
    volatile RecyclableAtomicLong readCount = new RecyclableAtomicLong(10000, 100);
    volatile RecyclableAtomicLong deleteCount = new RecyclableAtomicLong(10000, 100);
    volatile AtomicLong lastId;

    @Setup
    public void setup() {
        this.ctx = SpringApplication.run(BankAccountManagerApplication.class);
        this.service = this.ctx.getBean(BankAccountService.class);
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
    @Order(1)
    public void testCreateAccount() throws Exception {
        createRequest.setAccountNumber(testAccountNumber + createCount.getAndIncrementWithRecycle());
        service.createAccount(createRequest);
    }

    @Benchmark
    @Order(2)
    public void testUpdateAccount() throws Exception {
        service.updateAccount(testAccountNumber + updateCount.getAndIncrementWithRecycle(), updateRequest);
    }

    @Benchmark
    @Order(3)
    public void testTransfer() throws Exception {
        service.transfer(transferRequest);
    }

    @Benchmark
    @Order(4)
    public void testGetAccount() throws Exception {
        service.getAccount(testAccountNumber + readCount.getAndIncrementWithRecycle());
    }

    @Benchmark
    @Order(5)
    public void testListAccounts() throws Exception {
        BankAccountListDTO dto = service.listAccounts(lastId.get(), 10);
        if (null != dto && null != dto.getLastId()) {
            long lastId = Long.parseLong(dto.getLastId());
            //简单对比，多线程情况会有问题，但刚好覆盖一些重复请求的场景
            for (long i = this.lastId.get(), j = 0; lastId < i && j < 10; i = this.lastId.get(), j++) {
                this.lastId.compareAndSet(i, lastId);
            }
        }
    }

    @Benchmark
    @Order(6)
    public void testDeleteAccount() throws Exception {
        service.deleteAccount(testAccountNumber + deleteCount.getAndIncrementWithRecycle());
    }

    @TearDown
    public void tearDown() {
        if (ctx != null) {
            ctx.close();
        }
    }

}
