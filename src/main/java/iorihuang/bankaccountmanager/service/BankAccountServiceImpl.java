package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.constant.AccountConst;
import iorihuang.bankaccountmanager.dto.*;
import iorihuang.bankaccountmanager.exception.AccountExceptions;
import iorihuang.bankaccountmanager.exception.ExpCode;
import iorihuang.bankaccountmanager.exception.error.AccountReadError;
import iorihuang.bankaccountmanager.exception.error.AccountUpdateError;
import iorihuang.bankaccountmanager.exception.exception.*;
import iorihuang.bankaccountmanager.helper.snowflakeid.SnowFlakeIdHelper;
import iorihuang.bankaccountmanager.model.BankAccount;
import iorihuang.bankaccountmanager.model.bankaccount.AccountState;
import iorihuang.bankaccountmanager.model.bankaccount.AccountType;
import iorihuang.bankaccountmanager.repository.BankAccountRepository;
import iorihuang.bankaccountmanager.repository.BankAccountTrans;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
public class BankAccountServiceImpl implements BankAccountService {
    private final BankAccountRepository repository;
    private final BankAccountTrans trans;
    private final SnowFlakeIdHelper idHelper;
    private final SnowFlakeIdHelper verHelper;

    //todo lock before op

    /**
     * Create a bank account
     *
     * @param request account creation request
     * @return account created
     */
    @Override
    public BankAccountDTO createAccount(CreateAccountRequest request) {
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
                .deleteAt(now)
                .build();
        try {
            account = trans.createAccount(account);
        } catch (Exception e) {
            log.error("createAccount fail with error:{}", accountNumber, e);
            throw e;
        }
        log.info("Account create success:{}", accountNumber);
        return toDTO(account);
    }

    /**
     * Delete a bank account
     *
     * @param accountNumber account number
     * @return account info after deleted
     */
    @Override
    @CacheEvict(value = "account", key = "#accountNumber")
    public BankAccountDTO deleteAccount(String accountNumber) {
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
            log.warn("Account has balance, cannot delete: {}", accountNumber);
//            throw AccountExceptions.accountHasBalance(accountNumber);
            //账号状态不应该为frozen
            if (Objects.equals(AccountState.FROZEN.getCode(), account.getState())) {
                log.warn("Account is frozen, cannot frozen: {}", accountNumber);
                throw new DeleteAccountException("Account is frozen with positive balance, cannot delete: " + accountNumber);
            }
            newState = AccountState.FROZEN;
        }

        try {
            trans.deleteAccount(account, newState, newVersion);
        } catch (Exception e) {
            log.error("Account delete fail with error:{}", accountNumber, e);
            throw e;
        }

        log.info("Account delete success:{}", accountNumber);
        accountOpt = getAccountByAccountNumber(accountNumber);
        return accountOpt.map(this::toDTO).orElse(null);
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
    public BankAccountDTO updateAccount(String accountNumber, UpdateAccountRequest request) {
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
            throw AccountExceptions.deleteFailWithClosed(accountNumber);
        }

        String ownerName = request.getOwnerName();
        String contactInfo = request.getContactInfo();
        if (Objects.isNull(ownerName) || Objects.isNull(contactInfo) || ownerName.isEmpty() || contactInfo.isEmpty()) {
            String message = String.format("Account update fail:%s with invalid params: %s", accountNumber, request);
            log.warn(message);
            throw new AccountParamException(message);
        }
        Long newVersion = verHelper.genId();

        try {
            trans.updateAccount(account, ownerName, contactInfo, newVersion);
        } catch (Exception e) {
            log.error("Account update fail with error:{}", accountNumber, e);
            throw new AccountUpdateError(e, "Account update error: " + account.getAccountNumber());
        }

        log.info("Account update success:{}", accountNumber);
        accountOpt = getAccountByAccountNumber(accountNumber);
        return accountOpt.map(this::toDTO).orElse(null);
    }

    /**
     * Transfer funds between two accounts
     *
     * @param request Transfer request
     */
    @Override
    public BankTransferDTO transfer(TransferRequest request) {
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
        // Save updated accounts
        try {
            trans.transfer(from, to, amount, newVersion);
        } catch (Exception e) {
            log.error("Transfer failed from account {} to account {} with amount {}",
                    fromAccountNumber, toAccountNumber, amount, e);
            throw e;
        }

        log.info("Transfer success from account {} to account {} with amount {}",
                fromAccountNumber, toAccountNumber, amount);
        fromOpt = getAccountByAccountNumber(fromAccountNumber);
        toOpt = getAccountByAccountNumber(toAccountNumber);
        return new BankTransferDTO()
                .setFrom(fromOpt.map(this::toDTO).orElse(null))
                .setTo(toOpt.map(this::toDTO).orElse(null))
                ;
    }

    private Optional<BankAccount> getAccountByAccountNumber(String accountNumber) {
        try {
            return repository.findByAccountNumber(accountNumber);
        } catch (Exception e) {
            throw new AccountReadError(e, "Error retrieving account by account number: " + accountNumber);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountListDTO listAccounts(Long lastId, Integer size) {
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

        return dto;
    }

    /**
     * account info with cache
     *
     * @param accountNumber
     * @return account event if account is closed
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "account", key = "#accountNumber")
    public BankAccountDTO getAccount(String accountNumber) {
        Optional<BankAccount> bankAccount = getAccountByAccountNumber(accountNumber);
        if (bankAccount.isEmpty()) {
            log.warn("Account not found: {}", accountNumber);
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        BankAccount account = bankAccount.get();
        return toDTO(account);
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