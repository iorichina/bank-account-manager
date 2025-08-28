package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.config.SnowFlakeIdAutoConfig;
import iorihuang.bankaccountmanager.dto.BankAccountDTO;
import iorihuang.bankaccountmanager.dto.UpdateAccountRequest;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.error.AccountUpdateError;
import iorihuang.bankaccountmanager.exception.exception.AccountNotFoundException;
import iorihuang.bankaccountmanager.exception.exception.AccountParamException;
import iorihuang.bankaccountmanager.exception.exception.UpdateAccountException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class BankAccountUpdateTest {
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
    void updateAccount_success() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").ownerName("张三").contactInfo("13800000000").updatedAt(LocalDateTime.now()).build();
        BankAccount acc2 = BankAccount.builder().id(1L).accountNumber("A001").ownerName("李四").contactInfo("13900000000").updatedAt(LocalDateTime.now()).build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc), Optional.of(acc2));
        UpdateAccountRequest req = new UpdateAccountRequest();
        req.setOwnerName("李四");
        req.setContactInfo("13900000000");
        when(trans.updateAccount(any(), any(), any(), anyLong(), any())).thenReturn(LocalDateTime.now());
        BankAccountDTO dto = service.updateAccount("A001", req);
        assertEquals("1", dto.getId());
        assertEquals("A001", dto.getAccountNumber());
    }

    @Test
    void updateAccount_success_without_update() throws AccountError, AccountException {
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
                .ver(verHelper.genId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc));
        when(trans.updateAccount(any(), any(), any(), anyLong(), any())).thenReturn(LocalDateTime.now());
        UpdateAccountRequest req = new UpdateAccountRequest();
        req.setOwnerName("张三");
        req.setContactInfo("13800000000");
        BankAccountDTO dto = service.updateAccount("A001", req);
        assertEquals(acc.getId().toString(), dto.getId());
        assertEquals("A001", dto.getAccountNumber());
    }

    @Test
    void updateAccount_closed() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().state(AccountState.CLOSED.getCode()).id(1L).accountNumber("A001").ownerName("张三").contactInfo("13800000000").build();
        BankAccount acc2 = BankAccount.builder().id(1L).accountNumber("A001").ownerName("李四").contactInfo("13900000000").build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc), Optional.of(acc2));
        UpdateAccountRequest req = new UpdateAccountRequest();
        req.setOwnerName("李四");
        req.setContactInfo("13900000000");
        when(trans.updateAccount(any(), any(), any(), anyLong(), any())).thenReturn(LocalDateTime.now());
        assertThrows(UpdateAccountException.class, () -> service.updateAccount("A001", req));
    }

    @Test
    void updateAccount_trans_throws() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").ownerName("张三").contactInfo("13800000000").build();
        BankAccount acc2 = BankAccount.builder().id(1L).accountNumber("A001").ownerName("李四").contactInfo("13900000000").build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc), Optional.of(acc2));
        UpdateAccountRequest req = new UpdateAccountRequest();
        req.setOwnerName("李四");
        req.setContactInfo("13900000000");
        when(trans.updateAccount(any(), any(), any(), anyLong(), any())).thenThrow(new RuntimeException("trans error"));
        assertThrows(AccountUpdateError.class, () -> service.updateAccount("A001", req));
    }

    @Test
    void updateAccount_notFound() {
        when(repository.findByAccountNumber("2L")).thenReturn(Optional.empty());
        UpdateAccountRequest req = new UpdateAccountRequest();
        assertThrows(AccountNotFoundException.class, () -> service.updateAccount("2L", req));
    }

    @Test
    void updateAccount_invalidParams() throws AccountError, AccountException {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").ownerName("张三").contactInfo("13800000000").build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc));
        UpdateAccountRequest req = new UpdateAccountRequest();
        req.setOwnerName("");
        req.setContactInfo("");
        when(trans.updateAccount(any(), any(), any(), anyLong(), any())).thenReturn(LocalDateTime.now());
        assertThrows(AccountParamException.class, () -> service.updateAccount("A001", req));
    }
}
