package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.dto.BankAccountDTO;
import iorihuang.bankaccountmanager.dto.UpdateAccountRequest;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.exception.AccountNotFoundException;
import iorihuang.bankaccountmanager.exception.exception.AccountParamException;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdHelper;
import iorihuang.bankaccountmanager.model.BankAccount;
import iorihuang.bankaccountmanager.repository.BankAccountRepository;
import iorihuang.bankaccountmanager.repository.BankAccountTrans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    void updateAccount_success() throws AccountError {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").ownerName("张三").contactInfo("13800000000").build();
        BankAccount acc2 = BankAccount.builder().id(1L).accountNumber("A001").ownerName("李四").contactInfo("13900000000").build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc), Optional.of(acc2));
        UpdateAccountRequest req = new UpdateAccountRequest();
        req.setOwnerName("李四");
        req.setContactInfo("13900000000");
        when(trans.updateAccount(any(), any(), any(), anyLong(), any())).thenReturn(LocalDateTime.now());
        BankAccountDTO dto = service.updateAccount("A001", req);
        assertEquals("李四", dto.getOwnerName());
        assertEquals("13900000000", dto.getContactInfo());
    }

    @Test
    void updateAccount_notFound() {
        when(repository.findByAccountNumber("2L")).thenReturn(Optional.empty());
        UpdateAccountRequest req = new UpdateAccountRequest();
        assertThrows(AccountNotFoundException.class, () -> service.updateAccount("2L", req));
    }

    @Test
    void updateAccount_invalidParams() throws AccountError {
        BankAccount acc = BankAccount.builder().id(1L).accountNumber("A001").ownerName("张三").contactInfo("13800000000").build();
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(acc));
        UpdateAccountRequest req = new UpdateAccountRequest();
        req.setOwnerName("");
        req.setContactInfo("");
        when(trans.updateAccount(any(), any(), any(), anyLong(), any())).thenReturn(LocalDateTime.now());
        assertThrows(AccountParamException.class, () -> service.updateAccount("A001", req));
    }
}
