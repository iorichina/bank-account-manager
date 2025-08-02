package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.dto.TransferRequest;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.exception.AccountNotFoundException;
import iorihuang.bankaccountmanager.exception.exception.AccountParamException;
import iorihuang.bankaccountmanager.exception.exception.InsufficientBalanceException;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdHelper;
import iorihuang.bankaccountmanager.model.BankAccount;
import iorihuang.bankaccountmanager.model.bankaccount.AccountState;
import iorihuang.bankaccountmanager.repository.BankAccountRepository;
import iorihuang.bankaccountmanager.repository.BankAccountTrans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class BankAccountTransferTest {
    @Mock
    private BankAccountRepository repository;
    @Mock
    private BankAccountTrans trans;
    @Mock
    private SnowFlakeIdHelper idHelper;
    @Mock
    private SnowFlakeIdHelper verHelper;
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

