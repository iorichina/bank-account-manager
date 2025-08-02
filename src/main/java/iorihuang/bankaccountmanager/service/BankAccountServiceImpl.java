package iorihuang.bankaccountmanager.service;

import io.micrometer.observation.annotation.Observed;
import iorihuang.bankaccountmanager.constant.AccountConst;
import iorihuang.bankaccountmanager.dto.*;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.AccountExceptions;
import iorihuang.bankaccountmanager.exception.ExpCode;
import iorihuang.bankaccountmanager.exception.error.AccountReadError;
import iorihuang.bankaccountmanager.exception.error.AccountUpdateError;
import iorihuang.bankaccountmanager.exception.exception.*;
import iorihuang.bankaccountmanager.helper.RedisLock;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdHelper;
import iorihuang.bankaccountmanager.model.BankAccount;
import iorihuang.bankaccountmanager.model.BankAccountBalanceLog;
import iorihuang.bankaccountmanager.model.BankAccountChangeLog;
import iorihuang.bankaccountmanager.model.BankAccountTransferLog;
import iorihuang.bankaccountmanager.model.bankaccount.AccountState;
import iorihuang.bankaccountmanager.model.bankaccount.AccountType;
import iorihuang.bankaccountmanager.repository.BankAccountRepository;
import iorihuang.bankaccountmanager.repository.BankAccountTrans;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Bank Account Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Observed(name = "bank.account.service")
public class BankAccountServiceImpl implements BankAccountService {
    // lock before op
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    private final BankAccountRepository repository;
    private final BankAccountTrans trans;
    private final SnowFlakeIdHelper idHelper;
    private final SnowFlakeIdHelper verHelper;

    /**
     * Create a bank account
     *
     * @param request account creation request
     * @return account created
     */
    @Override
    @Observed(name = "bank.account.service.create")
    public BankAccountDTO createAccount(CreateAccountRequest request) throws AccountError, AccountException {
        // Convert String initialBalance to BigDecimal
        BigDecimal balance = request.getInitialBalanceAsBigDecimal();
        if (null == balance) {
            balance = BigDecimal.ZERO;
        }
        // Validate request parameters
        if (balance.scale() > AccountConst.BALANCE_SHOW_DOTS) {
            String message = String.format("Account initial balance scale %d exceeds maximum allowed scale %d by %s", balance.scale(), AccountConst.BALANCE_SHOW_DOTS, request.getAccountNumber());
            log.warn(message);
            throw new AccountParamException(message);
        }

        String accountNumber = request.getAccountNumber();
        Optional<BankAccount> exists = getAccountByAccountNumber(accountNumber);
        if (exists.isPresent()) {
            //or return success with exists account
            DuplicateAccountException err = AccountExceptions.duplicateAccount(accountNumber);
            log.error("createAccount fail with dup:{}", exists.get(), err);
            throw err;
        }

        // Generate ID using Snowflake algorithm if not provided
        Long accountId = idHelper.genId();
        // Generate version using Snowflake algorithm
        Long version = verHelper.genId();

        LocalDateTime now = LocalDateTime.now();

        // Convert Integer accountType to AccountType enum
        Integer accountTypeCode = request.getAccountType();
        // Validate account type - throw exception for invalid values
        if (null != accountTypeCode) {
            AccountType accountTypeEnum = AccountType.fromCodeSafe(accountTypeCode);
            if (null == accountTypeEnum) {
                String message = String.format("Invalid account type: %d while create account %s", accountTypeCode, accountNumber);
                AccountParamException err = new AccountParamException(message);
                log.error(message, err);
                throw err;
            }
        }

        BankAccount account = BankAccount.builder()
                .id(accountId)
                .accountNumber(accountNumber)
                .accountType(accountTypeCode)
                .ownerId(request.getOwnerId())
                .ownerName(request.getOwnerName())
                .contactInfo(request.getContactInfo())
                .balance(balance)
                .balanceAt(now)
                .state(AccountState.ACTIVE.getCode())
                .version(version) // Use custom version instead of default 0
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(now)
                .build();
        BankAccountChangeLog changeLog = BankAccountChangeLog.builder()
                .accountId(accountId)
                .accountNumber(accountNumber)
                .ownerId(request.getOwnerId())
                .changeType(1) // 1:Open Account
                .changeDesc("开户")
                .beforeState(AccountState.NONE.getCode())
                .afterState(AccountState.ACTIVE.getCode())
                .beforeOwnerName("")
                .afterOwnerName(request.getOwnerName())
                .beforeContactInfo("")
                .afterContactInfo(request.getContactInfo())
                .createdAt(now)
                .build();
        BankAccountBalanceLog balanceLog = BankAccountBalanceLog.builder()
                .accountId(accountId)
                .accountNumber(accountNumber)
                .beforeBalance(BigDecimal.ZERO)
                .afterBalance(balance)
                .changeAmount(balance)
                .changeType(1) // 1:开户
                .changeDesc("开户初始入账")
                .createdAt(now)
                .build();
        // Redis distributed lock to prevent concurrent creation of the same account
        try (RedisLock lock = new RedisLock(redisTemplate, getLockKey(accountNumber), 3)) {
            if (!lock.isLocked()) {
                throw new AccountConcurrentException("Failed to acquire account creation lock: " + accountNumber);
            }
            account = trans.createAccount(account, changeLog, balanceLog);
        } catch (Exception e) {
            log.error("createAccount fail with error:{}", accountNumber, e);
            throw e;
        }
        log.info("Account create success:{}", accountNumber);
        return toDTO(account);
    }

    private static String getLockKey(String accountNumber) {
        String lockKey = "account:op:lock:" + accountNumber;
        return lockKey;
    }

    /**
     * Delete a bank account
     *
     * @param accountNumber account number
     * @return account info after deleted
     */
    @Override
    @CacheEvict(value = "account", key = "#accountNumber")
    @Observed(name = "bank.account.service.delete")
    public BankAccountDTO deleteAccount(String accountNumber) throws AccountException, AccountError {
        Optional<BankAccount> accountOpt = getAccountByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            log.warn("Account not found: {}", accountNumber);
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        BankAccount account = accountOpt.get();
        //账号当前状态应该不能为closed
        if (Objects.equals(AccountState.CLOSED.getCode(), account.getState())) {
            log.warn("Account is closed: {}", accountNumber);
            throw AccountExceptions.deleteFailWithClosed(accountNumber);
        }
        // Generate new version using Snowflake algorithm
        long newVersion = verHelper.genId();
        // If the account has a balance, it cannot be directly deleted.
        // Instead, it should be frozen to prevent further transactions.
        // This ensures data integrity and prevents accidental loss of funds.
        AccountState newState = AccountState.CLOSED;
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            // log.warn("Account has balance, cannot delete: {}", accountNumber);
            // throw AccountExceptions.accountHasBalance(accountNumber);
            //账号状态不应该为frozen
            if (Objects.equals(AccountState.FROZEN.getCode(), account.getState())) {
                log.warn("Account is frozen, cannot delete and frozen: {}", accountNumber);
                throw new DeleteAccountException("Account is frozen with positive balance, cannot delete: " + accountNumber);
            }
            log.warn("Account has balance, frozen instead of delete : {}", accountNumber);
            newState = AccountState.FROZEN;
        }
        BankAccountChangeLog changeLog = BankAccountChangeLog.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .ownerId(account.getOwnerId())
                .changeType(newState == AccountState.CLOSED ? 2 : 4) // 2:Close, 4:Frozen
                .changeDesc(newState == AccountState.CLOSED ? "销户" : "有余额无法销户，仅冻结")
                .beforeState(account.getState())
                .afterState(newState.getCode())
                .beforeOwnerName(account.getOwnerName())
                .afterOwnerName(account.getOwnerName())
                .beforeContactInfo(account.getContactInfo())
                .afterContactInfo(account.getContactInfo())
                .createdAt(LocalDateTime.now())
                .build();
        // Redis distributed lock to prevent concurrent creation of the same account
        try (RedisLock lock = new RedisLock(redisTemplate, getLockKey(accountNumber), 3)) {
            if (!lock.isLocked()) {
                throw new AccountConcurrentException("Failed to acquire account creation lock: " + accountNumber);
            }
            trans.deleteAccount(account, newState, newVersion, changeLog);
        } catch (Exception e) {
            log.error("Account delete fail with error:{}", accountNumber, e);
            throw e;
        }

        switch (newState) {
            case FROZEN:
                log.info("Account try to delete but just frozen success:{}", accountNumber);
                break;
            case CLOSED:
                log.info("Account delete success:{}", accountNumber);
                break;
        }

        return accountOpt.map(this::toSimpleDTO).orElse(null);
    }

    /**
     * Update an existing bank account
     *
     * @param accountNumber The account number of the account to update
     * @param request       Update request containing new owner name and contact info
     * @return account after updated
     */
    @Override
    @CachePut(value = "account", key = "#accountNumber")
    @Observed(name = "bank.account.service.update")
    public BankAccountDTO updateAccount(String accountNumber, UpdateAccountRequest request) throws AccountException, AccountError {
        Optional<BankAccount> accountOpt = getAccountByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            log.warn("Account not found: {}", accountNumber);
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        BankAccount account = accountOpt.get();
        // should not update account if it is closed
        if (Objects.equals(account.getState(), AccountState.CLOSED.getCode())) {
            String message = String.format("Account is closed: %s", accountNumber);
            log.warn(message);
            throw AccountExceptions.updateFailWithClosed(accountNumber);
        }

        // Validate request parameters
        String ownerName = request.getOwnerName();
        String contactInfo = request.getContactInfo();
        if (Objects.isNull(ownerName) || Objects.isNull(contactInfo) || ownerName.isEmpty() || contactInfo.isEmpty()) {
            String message = String.format("Account update fail:%s with invalid params: %s", accountNumber, request);
            log.warn(message);
            throw new AccountParamException(message);
        }

        // no change
        if (Objects.equals(ownerName, account.getOwnerName()) && Objects.equals(contactInfo, account.getContactInfo())) {
            log.info("Account update with no change:{}", accountNumber);
            return toSimpleDTO(account);
        }

        long newVersion = verHelper.genId();
        BankAccountChangeLog changeLog = BankAccountChangeLog.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .ownerId(account.getOwnerId())
                .changeType(3) // 3:Info Change
                .changeDesc("信息变更")
                .beforeState(account.getState())
                .afterState(account.getState())
                .beforeOwnerName(account.getOwnerName())
                .afterOwnerName(ownerName)
                .beforeContactInfo(account.getContactInfo())
                .afterContactInfo(contactInfo)
                .createdAt(LocalDateTime.now())
                .build();
        // Redis distributed lock to prevent concurrent creation of the same account
        try (RedisLock lock = new RedisLock(redisTemplate, getLockKey(accountNumber), 3)) {
            if (!lock.isLocked()) {
                throw new AccountConcurrentException("Failed to acquire account creation lock: " + accountNumber);
            }
            trans.updateAccount(account, ownerName, contactInfo, newVersion, changeLog);
        } catch (Exception e) {
            log.error("Account update fail with error:{}", accountNumber, e);
            throw new AccountUpdateError(e, "Account update error: " + account.getAccountNumber());
        }

        log.info("Account update success:{}", accountNumber);
        return accountOpt.map(this::toSimpleDTO).orElse(null);
    }

    /**
     * Transfer funds between two accounts
     *
     * @param request Transfer request
     */
    @Override
    @Observed(name = "bank.account.service.transfer")
    public BankTransferDTO transfer(TransferRequest request) throws AccountException, AccountError {
        // Validate that source and destination accounts are different
        String fromAccountNumber = request.getFromAccountNumber();
        String toAccountNumber = request.getToAccountNumber();
        if (fromAccountNumber.equals(toAccountNumber)) {
            String message = String.format("Cannot transfer to the same account: %s", fromAccountNumber);
            log.warn(message);
            throw new AccountParamException(message);
        }

        // Validate transfer amount
        BigDecimal amount = request.getAmountAsBigDecimal();
        if (amount == null) {
            String message = String.format("Transfer amount cannot be null from: %s to: %s", fromAccountNumber, toAccountNumber);
            log.warn(message);
            throw new AccountParamException(message);
        }
        if (amount.scale() > AccountConst.BALANCE_SHOW_DOTS) {
            String message = String.format("Transfer amount scale %d exceeds maximum allowed scale %d from %s to %s", amount.scale(), AccountConst.BALANCE_SHOW_DOTS, fromAccountNumber, toAccountNumber);
            log.warn(message);
            throw new AccountParamException(message);
        }

        // Retrieve source account
        Optional<BankAccount> fromOpt = getAccountByAccountNumber(fromAccountNumber);
        if (fromOpt.isEmpty()) {
            log.warn("Transfer from account not found: {}", fromAccountNumber);
            throw new AccountNotFoundException("Source account not found: " + fromAccountNumber);
        }
        BankAccount from = fromOpt.get();
        //validate state
        if (!Objects.equals(from.getState(), AccountState.ACTIVE.getCode())) {
            log.warn("Transfer from account is not active: {}", fromAccountNumber);
            throw new AccountTransferException(ExpCode.TransferAccountLimit, "Source account is not active: " + fromAccountNumber);
        }

        // Check sufficient balance
        if (from.getBalance().compareTo(amount) < 0) {
            String message = String.format("Insufficient balance for transfer between %s and %s. Available: %s, Required: %s",
                    fromAccountNumber, toAccountNumber, from.getBalance(), amount);
            log.warn(message);
            throw new InsufficientBalanceException(message);
        }

        // Retrieve destination account
        Optional<BankAccount> toOpt = getAccountByAccountNumber(toAccountNumber);
        if (toOpt.isEmpty()) {
            log.warn("Transfer to account not found: {}", toAccountNumber);
            throw new AccountNotFoundException("Destination account not found: " + toAccountNumber);
        }
        BankAccount to = toOpt.get();
        //validate state
        if (!Objects.equals(to.getState(), AccountState.ACTIVE.getCode())) {
            log.warn("Transfer to account is not active: {}", toAccountNumber);
            throw new AccountTransferException(ExpCode.TransferAccountLimit, "Destination account is not active: " + fromAccountNumber);
        }

        long newVersion = verHelper.genId();
        // 构建余额变动日志
        BankAccountBalanceLog fromBalanceLog = BankAccountBalanceLog.builder()
                .accountId(from.getId())
                .accountNumber(from.getAccountNumber())
                .beforeBalance(from.getBalance())
                .afterBalance(from.getBalance().subtract(amount))
                .changeAmount(amount.negate())
                .changeType(5) // 5:转出
                .changeDesc("转账转出")
                .createdAt(LocalDateTime.now())
                .build();
        BankAccountBalanceLog toBalanceLog = BankAccountBalanceLog.builder()
                .accountId(to.getId())
                .accountNumber(to.getAccountNumber())
                .beforeBalance(to.getBalance())
                .afterBalance(to.getBalance().add(amount))
                .changeAmount(amount)
                .changeType(6) // 6:转入
                .changeDesc("转账转入")
                .createdAt(LocalDateTime.now())
                .build();
        BankAccountTransferLog transferLog = BankAccountTransferLog.builder()
                .fromAccountId(from.getId())
                .fromAccountNumber(from.getAccountNumber())
                .toAccountId(to.getId())
                .toAccountNumber(to.getAccountNumber())
                .amount(amount)
                .beforeBalanceFrom(from.getBalance())
                .afterBalanceFrom(from.getBalance().subtract(amount))
                .beforeBalanceTo(to.getBalance())
                .afterBalanceTo(to.getBalance().add(amount))
                .createdAt(LocalDateTime.now())
                .build();
        // Save updated accounts
        // Redis distributed lock to prevent concurrent creation of the same account
        try (RedisLock lock1 = new RedisLock(redisTemplate, getLockKey(fromAccountNumber), 3);
             RedisLock lock2 = new RedisLock(redisTemplate, getLockKey(toAccountNumber), 3)) {
            if (!lock1.isLocked()) {
                throw new AccountConcurrentException("Failed to acquire account creation lock: " + fromAccountNumber);
            }
            if (!lock2.isLocked()) {
                throw new AccountConcurrentException("Failed to acquire account creation lock: " + toAccountNumber);
            }
            trans.transfer(from, to, amount, newVersion, fromBalanceLog, toBalanceLog, transferLog);
        } catch (Exception e) {
            log.error("Transfer failed from account {} to account {} with amount {}",
                    fromAccountNumber, toAccountNumber, amount, e);
            throw e;
        }

        log.info("Transfer success from account {} to account {} with amount {}",
                fromAccountNumber, toAccountNumber, amount);
        BankAccountDTO fromDto = fromOpt.map(this::toSimpleDTO).orElse(null)
                .setBalance(from.getBalance().subtract(amount));
        BankAccountDTO toDto = toOpt.map(this::toSimpleDTO).orElse(null)
                .setBalance(to.getBalance().add(amount));
        return new BankTransferDTO()
                .setFrom(fromDto)
                .setTo(toDto)
                ;
    }

    private Optional<BankAccount> getAccountByAccountNumber(String accountNumber) throws AccountError {
        try {
            return repository.findByAccountNumber(accountNumber);
        } catch (Exception e) {
            throw new AccountReadError(e, "Error retrieving account by account number: " + accountNumber);
        }
    }

    /**
     * List accounts with pagination
     *
     * @param lastId
     * @param size
     * @return
     * @throws AccountException
     * @throws AccountError
     */
    @Override
    @Transactional(readOnly = true)
    @Observed(name = "bank.account.service.list")
    public BankAccountListDTO listAccounts(Long lastId, Integer size) throws AccountException, AccountError {
        if (null == lastId) {
            lastId = Long.MAX_VALUE; // Default to max value if lastId is negative
        }
        if (null == size) {
            size = 10;
        }
        if (size > 100) {
            throw new AccountParamException("size too large:" + size);
        }

        List<BankAccount> accounts = null;
        try {
            accounts = repository.findByState(AccountState.ACTIVE.getCode(), lastId, size + 1);
        } catch (Exception e) {
            log.error("read accounts error with lastId:" + lastId, e);
            throw new AccountReadError(e, "read accounts error with lastId:" + lastId);
        }
        if (null == accounts) {
            log.warn("read accounts error with lastId:" + lastId);
            return new BankAccountListDTO()
                    .setElements(new ArrayList<>())
                    .isHasMore(false)
                    ;
        }
        // Convert List to Page
        List<BankAccountDTO> elements = accounts.stream().limit(size).map(this::toDTO).collect(Collectors.toList());

        //pagination
        BankAccountListDTO dto = new BankAccountListDTO();
        dto.isHasMore(accounts.size() > size);
        dto.setElements(elements);
        dto.setLastId(elements.isEmpty() ? null : elements.get(elements.size() - 1).getId());

        log.info("List accounts with lastId: {}, size: {}, hasMore: {}", lastId, size, dto.getHasMore());
        return dto;
    }

    /**
     * account info with cache, should return account info even if account is closed
     *
     * @param accountNumber
     * @return account event if account is closed
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "account", key = "#accountNumber")
    @Observed(name = "bank.account.service.get")
    public BankAccountDTO getAccount(String accountNumber) throws AccountException, AccountError {
        Optional<BankAccount> bankAccount = getAccountByAccountNumber(accountNumber);
        if (bankAccount.isEmpty()) {
            log.warn("Account not found: {}", accountNumber);
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        BankAccount account = bankAccount.get();
        log.info("Get account : {}", accountNumber);
        return toDTO(account);
    }

    private BankAccountDTO toSimpleDTO(BankAccount account) {
        return new BankAccountDTO()
                .setId(account.getId())
                .setAccountNumber(account.getAccountNumber());
    }

    private BankAccountDTO toDTO(BankAccount account) {
        return new BankAccountDTO()
                .setId(account.getId())
                .setAccountNumber(account.getAccountNumber())
                .setAccountType(account.getAccountType())
                .setOwnerId(account.getOwnerId())
                .setOwnerName(account.getOwnerName())
                .setContactInfo(account.getContactInfo())
                .setBalance(account.getBalance())
                .setState(account.getState());
    }
}
