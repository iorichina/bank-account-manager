package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.dto.BankAccountDTO;
import iorihuang.bankaccountmanager.dto.CreateAccountRequest;
import iorihuang.bankaccountmanager.exception.exception.DuplicateAccountException;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdHelper;
import iorihuang.bankaccountmanager.model.BankAccount;
import iorihuang.bankaccountmanager.repository.BankAccountRepository;
import iorihuang.bankaccountmanager.repository.BankAccountTrans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BankAccountCreateTest {
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
    void createAccount_success() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setAccountNumber("A001");
        req.setOwnerId("4500003333000x");
        req.setOwnerName("张三");
        req.setContactInfo("13800000000");
        req.setInitialBalance("100.00");
        req.setAccountType(1);
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.empty());
        when(idHelper.genId()).thenReturn(1L);
        when(verHelper.genId()).thenReturn(100L);
        BankAccount account = BankAccount.builder()
                .id(1L)
                .accountNumber("A001")
                .ownerId("4500003333000x")
                .ownerName("张三")
                .contactInfo("13800000000")
                .balance(new BigDecimal("100.00"))
                .accountType(1)
                .state(1)
                .version(100L)
                .build();
        when(trans.createAccount(any())).thenReturn(account);
        BankAccountDTO dto = service.createAccount(req);
        assertEquals("A001", dto.getAccountNumber());
        assertEquals("张三", dto.getOwnerName());
        assertEquals("100.000000", dto.getBalance());
    }

    @Test
    void createAccount_duplicate() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setAccountNumber("A001");
        when(repository.findByAccountNumber("A001")).thenReturn(Optional.of(new BankAccount()));
        assertThrows(DuplicateAccountException.class, () -> service.createAccount(req));
    }

    @Test
    void createAccount_invalidParams() {
        CreateAccountRequest req = new CreateAccountRequest();
        req.setAccountNumber("");
        req.setOwnerId("");
        req.setOwnerName("");
        req.setContactInfo("");
        req.setInitialBalance(null);
        when(repository.findByAccountNumber(any())).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> service.createAccount(req));
    }
}

