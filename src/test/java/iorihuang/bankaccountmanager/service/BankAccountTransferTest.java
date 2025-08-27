package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.config.SnowFlakeIdAutoConfig;
import iorihuang.bankaccountmanager.dto.BankTransferDTO;
import iorihuang.bankaccountmanager.dto.TransferRequest;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.exception.AccountNotFoundException;
import iorihuang.bankaccountmanager.exception.exception.AccountParamException;
import iorihuang.bankaccountmanager.exception.exception.AccountTransferException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class BankAccountTransferTest {
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
    void transfer_success() throws AccountError, AccountException {
        BankAccount from = BankAccount.builder().state(AccountState.ACTIVE.getCode()).id(1L).accountNumber("A001").balance(new BigDecimal("100.00")).build();
        BankAccount to = BankAccount.builder().state(AccountState.ACTIVE.getCode()).id(2L).accountNumber("A002").balance(new BigDecimal("50.00")).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(from));
        when(repository.findByAccountNumber("A002")).thenReturn(Optional.of(to));
        when(trans.transfer(any(), any(), any(), anyLong(), any(), any(), any())).thenReturn(LocalDateTime.now());
        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("A001");
        req.setToAccountNumber("A002");
        req.setAmount(new BigDecimal("30.00").toString());
        assertDoesNotThrow(() -> service.transfer(req));
    }

    @Test
    void transfer_success_balance() throws AccountError, AccountException {
        BankAccount from = BankAccount.builder()
                .id(idHelper.genId())
                .accountNumber("A001")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("张三")
                .contactInfo("13800000000")
                .balance(new BigDecimal("100.560807"))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .ver(verHelper.genId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
        BankAccount to = BankAccount.builder()
                .id(idHelper.genId())
                .accountNumber("A002")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("张四")
                .contactInfo("13900000000")
                .balance(new BigDecimal("0.439193"))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .ver(verHelper.genId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(from), Optional.of(to));
        when(repository.findByAccountNumber("A002")).thenReturn(Optional.of(to), Optional.of(from));
        BankTransferDTO dto = service.transfer(new TransferRequest().setFromAccountNumber("A001").setToAccountNumber("A002").setAmount("0.560807"));
        assertEquals("100.000000", dto.getFrom().getBalance());
        assertEquals("1.000000", dto.getTo().getBalance());
    }

    @Test
    void transfer_same_account() throws AccountError, AccountException {
        BankAccount from = BankAccount.builder().state(AccountState.ACTIVE.getCode()).id(1L).accountNumber("A001").balance(new BigDecimal("100.00")).build();
        BankAccount to = BankAccount.builder().state(AccountState.ACTIVE.getCode()).id(2L).accountNumber("A002").balance(new BigDecimal("50.00")).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(from));
        when(repository.findByAccountNumber("A002")).thenReturn(Optional.of(to));
        when(trans.transfer(any(), any(), any(), anyLong(), any(), any(), any())).thenReturn(LocalDateTime.now());
        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("A001");
        req.setToAccountNumber("A001");
        req.setAmount(new BigDecimal("30.00").toString());
        assertThrows(AccountParamException.class, () -> service.transfer(req));
    }

    @Test
    void transfer_null_amount() throws AccountError, AccountException {
        BankAccount from = BankAccount.builder().state(AccountState.ACTIVE.getCode()).id(1L).accountNumber("A001").balance(new BigDecimal("100.00")).build();
        BankAccount to = BankAccount.builder().state(AccountState.ACTIVE.getCode()).id(2L).accountNumber("A002").balance(new BigDecimal("50.00")).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(from));
        when(repository.findByAccountNumber("A002")).thenReturn(Optional.of(to));
        when(trans.transfer(any(), any(), any(), anyLong(), any(), any(), any())).thenReturn(LocalDateTime.now());
        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("A001");
        req.setToAccountNumber("A002");
        req.setAmount(null);
        assertThrows(AccountParamException.class, () -> service.transfer(req));
    }

    @Test
    void transfer_account_not_active() throws AccountError, AccountException {
        BankAccount from = BankAccount.builder().state(AccountState.CLOSED.getCode()).id(1L).accountNumber("A001").balance(new BigDecimal("100.00")).build();
        BankAccount to = BankAccount.builder().state(AccountState.ACTIVE.getCode()).id(2L).accountNumber("A002").balance(new BigDecimal("50.00")).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(from));
        when(repository.findByAccountNumber("A002")).thenReturn(Optional.of(to));
        when(trans.transfer(any(), any(), any(), anyLong(), any(), any(), any())).thenReturn(LocalDateTime.now());
        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("A001");
        req.setToAccountNumber("A002");
        req.setAmount("2333");
        assertThrows(AccountTransferException.class, () -> service.transfer(req));
    }

    @Test
    void transfer_account_not_active2() throws AccountError, AccountException {
        BankAccount from = BankAccount.builder().state(AccountState.ACTIVE.getCode()).id(1L).accountNumber("A001").balance(new BigDecimal("100.00")).build();
        BankAccount to = BankAccount.builder().state(AccountState.CLOSED.getCode()).id(2L).accountNumber("A002").balance(new BigDecimal("50.00")).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(from));
        when(repository.findByAccountNumber("A002")).thenReturn(Optional.of(to));
        when(trans.transfer(any(), any(), any(), anyLong(), any(), any(), any())).thenReturn(LocalDateTime.now());
        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("A001");
        req.setToAccountNumber("A002");
        req.setAmount("1");
        assertThrows(AccountTransferException.class, () -> service.transfer(req));
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
                .ver(verHelper.genId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
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
                .ver(verHelper.genId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(from));
        when(repository.findByAccountNumber("A002")).thenReturn(Optional.of(to));
        assertThrows(InsufficientBalanceException.class, () -> service.transfer(new TransferRequest().setFromAccountNumber("A001").setToAccountNumber("A002").setAmount(BigDecimal.valueOf(300).toString())));
    }

    @Test
    void transfer_fromAccountNotFound() throws AccountError, AccountException {
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.empty());
        when(trans.transfer(any(), any(), any(), anyLong(), any(), any(), any())).thenReturn(LocalDateTime.now());
        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("A001");
        req.setToAccountNumber("A002");
        req.setAmount(new BigDecimal("30.00").toString());
        assertThrows(AccountNotFoundException.class, () -> service.transfer(req));
    }

    @Test
    void transfer_toAccountNotFound() throws AccountError, AccountException {
        BankAccount from = BankAccount.builder().state(AccountState.ACTIVE.getCode()).id(1L).accountNumber("A001").balance(new BigDecimal("100.00")).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(from));
        when(repository.findByAccountNumber("A002")).thenReturn(Optional.empty());
        when(trans.transfer(any(), any(), any(), anyLong(), any(), any(), any())).thenReturn(LocalDateTime.now());
        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("A001");
        req.setToAccountNumber("A002");
        req.setAmount(new BigDecimal("30.00").toString());
        assertThrows(AccountNotFoundException.class, () -> service.transfer(req));
    }

    @Test
    void transfer_insufficientBalance() throws AccountError, AccountException {
        BankAccount from = BankAccount.builder().state(AccountState.ACTIVE.getCode()).id(1L).accountNumber("A001").balance(new BigDecimal("10.00")).build();
        BankAccount to = BankAccount.builder().state(AccountState.ACTIVE.getCode()).id(2L).accountNumber("A002").balance(new BigDecimal("50.00")).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(from));
        when(repository.findByAccountNumber("A002")).thenReturn(Optional.of(to));
        when(trans.transfer(any(), any(), any(), anyLong(), any(), any(), any())).thenReturn(LocalDateTime.now());
        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("A001");
        req.setToAccountNumber("A002");
        req.setAmount(new BigDecimal("30.00").toString());
        assertThrows(InsufficientBalanceException.class, () -> service.transfer(req));
    }

    @Test
    void transfer_invalidParams() throws AccountError, AccountException {
        BankAccount from = BankAccount.builder().state(AccountState.ACTIVE.getCode()).id(1L).accountNumber("A001").balance(new BigDecimal("10.00")).build();
        BankAccount to = BankAccount.builder().state(AccountState.ACTIVE.getCode()).id(2L).accountNumber("A002").balance(new BigDecimal("50.00")).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(from));
        when(repository.findByAccountNumber("A002")).thenReturn(Optional.of(to));
        when(trans.transfer(any(), any(), any(), anyLong(), any(), any(), any())).thenReturn(LocalDateTime.now());
        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("A001");
        req.setToAccountNumber("A002");
        req.setAmount(new BigDecimal("30.00000010").toString());
        assertThrows(AccountParamException.class, () -> service.transfer(req));
    }
}

