package iorihuang.bankaccountmanager.benchmark;

import iorihuang.bankaccountmanager.dto.CreateAccountRequest;
import iorihuang.bankaccountmanager.dto.TransferRequest;
import iorihuang.bankaccountmanager.dto.UpdateAccountRequest;
import iorihuang.bankaccountmanager.service.BankAccountService;
import org.openjdk.jmh.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

/**
 * JMH 基准测试：accounts相关service方法
 */
@SpringBootTest
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class BankAccountServiceBenchmark {
    @Autowired
    private BankAccountService service;

    private CreateAccountRequest createRequest;
    private UpdateAccountRequest updateRequest;
    private TransferRequest transferRequest;
    private String testAccountNumber = "A001";

    @Setup
    public void setup() {
        createRequest = new CreateAccountRequest();
        createRequest.setOwnerName("张三");
        createRequest.setContactInfo("123456789");
        // ...可补充其它字段

        updateRequest = new UpdateAccountRequest();
        updateRequest.setOwnerName("李四");
        updateRequest.setContactInfo("987654321");
        // ...可补充其它字段

        transferRequest = new TransferRequest();
        transferRequest.setFromAccountNumber("A001");
        transferRequest.setToAccountNumber("A002");
        transferRequest.setAmount("100.00");
    }

    @Benchmark
    public void testCreateAccount() throws Exception {
        service.createAccount(createRequest);
    }

    @Benchmark
    public void testUpdateAccount() throws Exception {
        service.updateAccount(testAccountNumber, updateRequest);
    }

    @Benchmark
    public void testDeleteAccount() throws Exception {
        try {
            service.deleteAccount(testAccountNumber);
        } catch (Exception ignored) {
            // 允许删除不存在的账号
        }
    }

    @Benchmark
    public void testTransfer() throws Exception {
        try {
            service.transfer(transferRequest);
        } catch (Exception ignored) {
            // 允许转账失败
        }
    }

    @Benchmark
    public void testGetAccount() throws Exception {
        try {
            service.getAccount(testAccountNumber);
        } catch (Exception ignored) {
            // 允许查无此账号
        }
    }

    @Benchmark
    public void testListAccounts() throws Exception {
        service.listAccounts(null, 10);
    }
}

