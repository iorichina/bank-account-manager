package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.config.SnowFlakeIdAutoConfig;
import iorihuang.bankaccountmanager.dto.*;
import iorihuang.bankaccountmanager.exception.exception.AccountNotFoundException;
import iorihuang.bankaccountmanager.exception.exception.DuplicateAccountException;
import iorihuang.bankaccountmanager.exception.exception.InsufficientBalanceException;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdHelper;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdProperties;
import iorihuang.bankaccountmanager.model.BankAccount;
import iorihuang.bankaccountmanager.model.bankaccount.AccountState;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class BankAccountServiceImplTest {
    @Mock
    private BankAccountRepository repository;
    @Mock
    private BankAccountTrans trans;
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
    void createAccount_success() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setAccountNumber("A001");
        req.setAccountType(AccountType.SAVINGS.getCode());
        req.setOwnerId("4500003333000x");
        req.setOwnerName("张三");
        req.setContactInfo("13800000000");
        req.setInitialBalance(BigDecimal.valueOf(100).toString());
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.empty());
        when(trans.createAccount(any())).thenAnswer(i -> i.getArgument(0));
        BankAccountDTO dto = service.createAccount(req);
        assertEquals("A001", dto.getAccountNumber());
        assertEquals("张三", dto.getOwnerName());
        assertEquals(BigDecimal.valueOf(100.000000).setScale(6).toString(), dto.getBalance());
    }

    @Test
    void createAccount_duplicate() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setAccountNumber("A001");
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(new BankAccount()));
        assertThrows(DuplicateAccountException.class, () -> service.createAccount(req));
    }

    @Test
    void deleteAccount_notFound() {
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> service.deleteAccount("A001"));
    }

    @Test
    void updateAccount_success() {
        BankAccount acc = BankAccount.builder()
                .id(idHelper.genId())
                .accountNumber("A001")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("张三")
                .contactInfo("13800000000")
                .balance(BigDecimal.valueOf(100))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .version(verHelper.genId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleteAt(LocalDateTime.now())
                .build();
        BankAccount acc2 = BankAccount.builder()
                .id(idHelper.genId())
                .accountNumber("A001")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("李四")
                .contactInfo("13900000000")
                .balance(BigDecimal.valueOf(100))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .version(verHelper.genId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleteAt(LocalDateTime.now())
                .build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc), Optional.of(acc2));
        when(trans.updateAccount(any(), any(), any(), anyLong())).thenReturn(LocalDateTime.now());
        UpdateAccountRequest req = new UpdateAccountRequest();
        req.setOwnerName("李四");
        req.setContactInfo("13900000000");
        BankAccountDTO dto = service.updateAccount("A001", req);
        assertEquals("李四", dto.getOwnerName());
        assertEquals("13900000000", dto.getContactInfo());
    }

    @Test
    void transfer_success() {
        BankAccount from = BankAccount.builder()
                .id(idHelper.genId())
                .accountNumber("A001")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("张三")
                .contactInfo("13800000000")
                .balance(BigDecimal.valueOf(100))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .version(verHelper.genId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleteAt(LocalDateTime.now())
                .build();
        BankAccount to = BankAccount.builder()
                .id(idHelper.genId())
                .accountNumber("A002")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("张四")
                .contactInfo("13900000000")
                .balance(BigDecimal.valueOf(101))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .version(verHelper.genId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleteAt(LocalDateTime.now())
                .build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(from), Optional.of(to));
        when(repository.findByAccountNumber("A002")).thenReturn(Optional.of(to), Optional.of(from));
        BankTransferDTO dto = service.transfer(new TransferRequest().setFromAccountNumber("A001").setToAccountNumber("A002").setAmount(BigDecimal.valueOf(30).toString()));
        assertEquals(BigDecimal.valueOf(101.000000).setScale(6).toString(), dto.getFrom().getBalance());
        assertEquals(BigDecimal.valueOf(100.000000).setScale(6).toString(), dto.getTo().getBalance());
    }

    @Test
    void transfer_insufficient() {
        BankAccount from = BankAccount.builder()
                .id(idHelper.genId())
                .accountNumber("A001")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("张三")
                .contactInfo("13800000000")
                .balance(BigDecimal.valueOf(100))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .version(verHelper.genId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleteAt(LocalDateTime.now())
                .build();
        BankAccount to = BankAccount.builder()
                .id(idHelper.genId())
                .accountNumber("A002")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("张四")
                .contactInfo("13900000000")
                .balance(BigDecimal.valueOf(101))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .version(verHelper.genId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleteAt(LocalDateTime.now())
                .build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(from));
        when(repository.findByAccountNumber("A002")).thenReturn(Optional.of(to));
        assertThrows(InsufficientBalanceException.class, () -> service.transfer(new TransferRequest().setFromAccountNumber("A001").setToAccountNumber("A002").setAmount(BigDecimal.valueOf(300).toString())));
    }

    @Test
    void listAccounts_success() {
        BankAccount from = BankAccount.builder()
                .id(idHelper.genId())
                .accountNumber("A001")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("张三")
                .contactInfo("13800000000")
                .balance(BigDecimal.valueOf(100))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .version(verHelper.genId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleteAt(LocalDateTime.now())
                .build();
        BankAccount to = BankAccount.builder()
                .id(idHelper.genId())
                .accountNumber("A002")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("张四")
                .contactInfo("13900000000")
                .balance(BigDecimal.valueOf(101))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .version(verHelper.genId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleteAt(LocalDateTime.now())
                .build();
        when(repository.findByState(anyInt(), anyLong(), eq(2))).thenReturn(List.of(from, to));
        BankAccountListDTO page = service.listAccounts(null, 1);
        assertEquals(1, page.getHasMore());
        assertEquals("A001", page.getElements().get(0).getAccountNumber());
    }
}

