package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.config.SnowFlakeIdAutoConfig;
import iorihuang.bankaccountmanager.dto.BankAccountDTO;
import iorihuang.bankaccountmanager.dto.CreateAccountRequest;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.exception.AccountParamException;
import iorihuang.bankaccountmanager.exception.exception.DuplicateAccountException;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdHelper;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdProperties;
import iorihuang.bankaccountmanager.model.BankAccount;
import iorihuang.bankaccountmanager.model.bankaccount.AccountType;
import iorihuang.bankaccountmanager.repository.BankAccountRepository;
import iorihuang.bankaccountmanager.repository.BankAccountTrans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BankAccountCreateTest {
    @Mock
    private BankAccountRepository repository;
    @Mock
    private BankAccountTrans trans;
    //    @Mock
//    private SnowFlakeIdHelper idHelper;
//    @Mock
//    private SnowFlakeIdHelper verHelper;
    @Spy
    private SnowFlakeIdHelper idHelper = new SnowFlakeIdAutoConfig().idHelper(new SnowFlakeIdProperties());
    @Spy
    private SnowFlakeIdHelper verHelper = new SnowFlakeIdAutoConfig().verHelper(new SnowFlakeIdProperties());
    @InjectMocks
    private BankAccountServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_success() throws AccountError, AccountException {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setAccountNumber("A001");
        req.setOwnerId("4500003333000x");
        req.setOwnerName("张三");
        req.setContactInfo("13800000000");
        req.setAccountType(1);
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.empty());
        when(idHelper.genId()).thenReturn(1L);
        when(verHelper.genId()).thenReturn(100L);
        BankAccount account = BankAccount.builder()
                .id(1L)
                .accountNumber("A001")
                .ownerId("4500003333000x")
                .ownerName("张三")
                .contactInfo("13800000000")
                .balance(new BigDecimal("0.00"))
                .accountType(1)
                .state(1)
                .version(100L)
                .build();
        when(trans.createAccount(any(), any(), any())).thenReturn(account);
        BankAccountDTO dto = service.createAccount(req);
        assertEquals("A001", dto.getAccountNumber());
        assertEquals("张三", dto.getOwnerName());
        assertEquals("0.000000", dto.getBalance());
    }

    @Test
    void createAccount_success_initialBalance() throws AccountError, AccountException {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setAccountNumber("A001");
        req.setAccountType(AccountType.SAVINGS.getCode());
        req.setOwnerId("4500003333000x");
        req.setOwnerName("张三");
        req.setContactInfo("13800000000");
        req.setInitialBalance(new BigDecimal("105.32085").toString());
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.empty());
        when(trans.createAccount(any(), any(), any())).thenAnswer(i -> i.getArgument(0));
        BankAccountDTO dto = service.createAccount(req);
        assertEquals("A001", dto.getAccountNumber());
        assertEquals("张三", dto.getOwnerName());
        assertEquals("105.320850", dto.getBalance());
    }

    @Test
    void createAccount_duplicate() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setAccountNumber("A001");
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(new BankAccount()));
        assertThrows(DuplicateAccountException.class, () -> service.createAccount(req));
    }

    @Test
    void createAccount_duplicate_by_trans() throws AccountError, AccountException {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setAccountNumber("A001");
        req.setAccountType(AccountType.SAVINGS.getCode());
        req.setOwnerId("4500003333000x");
        req.setOwnerName("张三");
        req.setContactInfo("13800000000");
        req.setInitialBalance(new BigDecimal("105.32085").toString());
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.empty());
        when(trans.createAccount(any(), any(), any())).thenThrow(new DuplicateAccountException("Duplicate account"));
        // Simulate the transaction layer throwing a DuplicateAccountException
        assertThrows(DuplicateAccountException.class, () -> service.createAccount(req));
    }

    @Test
    void createAccount_invalidParams() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setAccountNumber("");
        req.setOwnerId("");
        req.setOwnerName("");
        req.setContactInfo("");
        req.setInitialBalance(null);
        when(repository.findByAccountNumber(any())).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> service.createAccount(req));
    }

    @Test
    void createAccount_invalid_balance() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setAccountNumber("111");
        req.setAccountType(1);
        req.setOwnerId("111");
        req.setOwnerName("111");
        req.setContactInfo("111");
        req.setInitialBalance("32343.2034934934");
        when(repository.findByAccountNumber(any())).thenReturn(Optional.empty());
        assertThrows(AccountParamException.class, () -> service.createAccount(req));
    }

    @Test
    void createAccount_invalid_accountType() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setAccountNumber("111");
        req.setAccountType(5000);
        req.setOwnerId("111");
        req.setOwnerName("111");
        req.setContactInfo("111");
        req.setInitialBalance("32343.2034934934");
        when(repository.findByAccountNumber(any())).thenReturn(Optional.empty());
        assertThrows(AccountParamException.class, () -> service.createAccount(req));
    }
}

