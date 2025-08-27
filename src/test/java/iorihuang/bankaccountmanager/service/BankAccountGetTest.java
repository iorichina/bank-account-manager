package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.dto.BankAccountDTO;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.exception.AccountNotFoundException;
import iorihuang.bankaccountmanager.model.BankAccount;
import iorihuang.bankaccountmanager.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class BankAccountGetTest {
    @Mock
    private BankAccountRepository repository;
    @InjectMocks
    private BankAccountServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAccount_success() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").ownerName("张三").balance(new BigDecimal("100.00")).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc));
        BankAccountDTO dto = service.getAccount("A001");
        assertEquals("A001", dto.getAccountNumber());
        assertEquals("张三", dto.getOwnerName());
        assertEquals("100.000000", dto.getBalance());
    }

    @Test
    void getAccount_notFound() {
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> service.getAccount("A001"));
    }
}

