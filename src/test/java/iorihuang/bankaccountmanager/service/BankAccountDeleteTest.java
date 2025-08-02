package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.exception.AccountNotFoundException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class BankAccountDeleteTest {
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
    void deleteAccount_notFound() {
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> service.deleteAccount("A001"));
    }

    @Test
    void deleteAccount_success() throws AccountError {
        BankAccount acc = BankAccount.builder().accountNumber("A001").balance(BigDecimal.ZERO).state(1).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc));
        when(verHelper.genId()).thenReturn(100L);
        when(trans.deleteAccount(any(), eq(AccountState.fromCodeSafe(acc.getState())), eq(100L), any())).thenReturn(LocalDateTime.now());
        assertDoesNotThrow(() -> service.deleteAccount("A001"));
    }

    @Test
    void deleteAccount_byId_success() throws AccountError {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").balance(BigDecimal.ZERO).state(1).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc));
        when(verHelper.genId()).thenReturn(100L);
        when(trans.deleteAccount(any(), eq(AccountState.fromCodeSafe(acc.getState())), eq(100L), any())).thenReturn(LocalDateTime.now());
        assertDoesNotThrow(() -> service.deleteAccount("A001"));
    }

    @Test
    void deleteAccount_byId_notFound() {
        when(repository.findByAccountNumber("2L")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> service.deleteAccount("2L"));
    }
}
