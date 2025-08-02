package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.config.SnowFlakeIdAutoConfig;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdHelper;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdProperties;
import iorihuang.bankaccountmanager.repository.BankAccountRepository;
import iorihuang.bankaccountmanager.repository.BankAccountTrans;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

class BankAccountServiceImplTest {
    @Mock
    private BankAccountRepository repository;
    @Mock
    private BankAccountTrans trans;
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

}

