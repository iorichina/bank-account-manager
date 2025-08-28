package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.config.SnowFlakeIdAutoConfig;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.exception.AccountNotFoundException;
import iorihuang.bankaccountmanager.exception.exception.DeleteAccountException;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdHelper;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdProperties;
import iorihuang.bankaccountmanager.model.BankAccount;
import iorihuang.bankaccountmanager.model.bankaccount.AccountState;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class BankAccountDeleteTest {
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
    void deleteAccount_notFound() {
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> service.deleteAccount("A001"));
    }

    @Test
    void deleteAccount_frozen() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().accountNumber("A001").balance(BigDecimal.ONE).state(1).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc));
        when(verHelper.genId()).thenReturn(100L, 100L, 100L, 100L, 100L, 100L);
        doThrow(new DeleteAccountException("Account not found"))
                .when(trans).deleteAccount(any(), eq(AccountState.FROZEN), anyLong(), any());
        assertThrows(DeleteAccountException.class, () -> service.deleteAccount("A001"));
    }

    @Test
    void deleteAccount_success_in_trans() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().accountNumber("A001").balance(BigDecimal.ZERO).state(1).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc));
        when(verHelper.genId()).thenReturn(100L, 100L, 100L, 100L, 100L, 100L);
        doThrow(new DeleteAccountException("Account not found"))
                .when(trans).deleteAccount(any(), eq(AccountState.CLOSED), anyLong(), any());
        assertThrows(DeleteAccountException.class, () -> service.deleteAccount("A001"));
    }

    @Test
    void deleteAccount_success() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().accountNumber("A001").balance(BigDecimal.ZERO).state(1).updatedAt(LocalDateTime.now()).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc));
        when(verHelper.genId()).thenReturn(100L, 100L, 100L, 100L, 100L, 100L);
        when(trans.deleteAccount(any(), eq(AccountState.fromCodeSafe(acc.getState())), anyLong(), any())).thenReturn(LocalDateTime.now());
        assertDoesNotThrow(() -> service.deleteAccount("A001"));
    }

    @Test
    void deleteAccount_frozen_success() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").balance(BigDecimal.ONE).state(1).updatedAt(LocalDateTime.now()).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc));
        when(verHelper.genId()).thenReturn(100L, 100L, 100L, 100L, 100L, 100L);
        when(trans.deleteAccount(any(), eq(AccountState.fromCodeSafe(acc.getState())), anyLong(), any())).thenReturn(LocalDateTime.now());
        assertDoesNotThrow(() -> service.deleteAccount("A001"));
    }

    @Test
    void deleteAccount_closed() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").balance(BigDecimal.ZERO).state(AccountState.CLOSED.getCode()).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc));
        when(verHelper.genId()).thenReturn(100L, 100L, 100L, 100L, 100L, 100L);
        when(trans.deleteAccount(any(), eq(AccountState.fromCodeSafe(acc.getState())), anyLong(), any())).thenReturn(LocalDateTime.now());
        assertThrows(DeleteAccountException.class, () -> service.deleteAccount("A001"));
    }

    @Test
    void deleteAccount_frozen2() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").balance(BigDecimal.ONE).state(AccountState.FROZEN.getCode()).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc));
        when(verHelper.genId()).thenReturn(100L, 100L, 100L, 100L, 100L, 100L);
        when(trans.deleteAccount(any(), eq(AccountState.fromCodeSafe(acc.getState())), anyLong(), any())).thenReturn(LocalDateTime.now());
        assertThrows(DeleteAccountException.class, () -> service.deleteAccount("A001"));
    }
}
