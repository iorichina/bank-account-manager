package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.dto.BankAccountListDTO;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.exception.AccountParamException;
import iorihuang.bankaccountmanager.model.BankAccount;
import iorihuang.bankaccountmanager.model.bankaccount.AccountState;
import iorihuang.bankaccountmanager.model.bankaccount.AccountType;
import iorihuang.bankaccountmanager.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    void listAccounts_success() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").ownerName("张三").balance(new BigDecimal("100.00")).updatedAt(LocalDateTime.now()).build();
        when(repository.findByState(AccountState.ACTIVE.getCode(), Long.MAX_VALUE, 11)).thenReturn(List.of(acc, acc));
        BankAccountListDTO page = service.listAccounts(null, null);
        assertEquals(2, page.getElements().size());
        assertEquals(0, page.getHasMore());
        assertEquals("1", page.getLastId());
        assertEquals("A001", page.getElements().get(0).getAccountNumber());
    }

    @Test
    void listAccounts_null_list() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").ownerName("张三").balance(new BigDecimal("100.00")).build();
        when(repository.findByState(AccountState.ACTIVE.getCode(), Long.MAX_VALUE, 2)).thenReturn(null);
        assertDoesNotThrow(() -> service.listAccounts(null, Integer.valueOf(100)));
    }

    @Test
    void listAccounts_size_exceed_max() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").ownerName("张三").balance(new BigDecimal("100.00")).build();
        when(repository.findByState(AccountState.ACTIVE.getCode(), Long.MAX_VALUE, 2)).thenReturn(List.of(acc, acc));
        assertThrows(AccountParamException.class, () -> service.listAccounts(null, Integer.valueOf(10000)));
    }

    @Test
    void listAccounts_success_last_id() throws AccountError, AccountException {
        BankAccount from = BankAccount.builder()
                .id(4L)
                .accountNumber("A001")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("张三")
                .contactInfo("13800000000")
                .balance(BigDecimal.valueOf(100))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .ver(2L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
        BankAccount to = BankAccount.builder()
                .id(3L)
                .accountNumber("A002")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("张四")
                .contactInfo("13900000000")
                .balance(BigDecimal.valueOf(101))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .ver(4L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
        when(repository.findByState(anyInt(), anyLong(), eq(2))).thenReturn(List.of(to));
        BankAccountListDTO page = service.listAccounts(4L, 1);
        assertEquals(0, page.getHasMore());
        assertEquals("A002", page.getElements().get(0).getAccountNumber());
    }

    @Test
    void listAccounts_success_pagination() throws AccountError, AccountException {
        BankAccount from = BankAccount.builder()
                .id(4L)
                .accountNumber("A001")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("张三")
                .contactInfo("13800000000")
                .balance(BigDecimal.valueOf(100))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .ver(2L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
        BankAccount to = BankAccount.builder()
                .id(3L)
                .accountNumber("A002")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003333000x")
                .ownerName("张四")
                .contactInfo("13900000000")
                .balance(BigDecimal.valueOf(101))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .ver(4L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
        BankAccount target = BankAccount.builder()
                .id(1L)
                .accountNumber("A007")
                .accountType(AccountType.SAVINGS.getCode())
                .ownerId("4500003313000x")
                .ownerName("王五")
                .contactInfo("13700000000")
                .balance(BigDecimal.valueOf(101))
                .balanceAt(LocalDateTime.now())
                .state(AccountState.ACTIVE.getCode())
                .ver(4L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
        when(repository.findByState(anyInt(), anyLong(), eq(2))).thenReturn(List.of(to, target));
        BankAccountListDTO page = service.listAccounts(4L, 1);
        assertEquals(1, page.getHasMore());
        assertEquals("A002", page.getElements().get(0).getAccountNumber());
    }

    @Test
    void listAccounts_empty() throws AccountError, AccountException {
        when(repository.findByState(AccountState.ACTIVE.getCode(), Long.MAX_VALUE, 2)).thenReturn(List.of());
        BankAccountListDTO page = service.listAccounts(null, Integer.valueOf(1));
        assertEquals(0, page.getElements().size());
        assertEquals(0, page.getHasMore());
    }

}

