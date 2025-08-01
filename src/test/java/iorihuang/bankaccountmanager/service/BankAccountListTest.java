package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.dto.BankAccountListDTO;
import iorihuang.bankaccountmanager.model.BankAccount;
import iorihuang.bankaccountmanager.model.bankaccount.AccountState;
import iorihuang.bankaccountmanager.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class BankAccountListTest {
    @Mock
    private BankAccountRepository repository;
    @InjectMocks
    private BankAccountServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listAccounts_success() {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").ownerName("张三").balance(new BigDecimal("100.00")).build();
        when(repository.findByState(AccountState.ACTIVE.getCode(), Long.MAX_VALUE, 2)).thenReturn(List.of(acc, acc));
        BankAccountListDTO page = service.listAccounts(null, Integer.valueOf(1));
        assertEquals(1, page.getElements().size());
        assertEquals(1, page.getHasMore());
        assertEquals("1", page.getLastId());
        assertEquals("A001", page.getElements().get(0).getAccountNumber());
    }

    @Test
    void listAccounts_empty() {
        when(repository.findByState(AccountState.ACTIVE.getCode(), Long.MAX_VALUE, 2)).thenReturn(List.of());
        BankAccountListDTO page = service.listAccounts(null, Integer.valueOf(1));
        assertEquals(0, page.getElements().size());
        assertEquals(0, page.getHasMore());
    }
}

