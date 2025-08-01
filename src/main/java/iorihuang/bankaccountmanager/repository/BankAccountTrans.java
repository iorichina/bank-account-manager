package iorihuang.bankaccountmanager.repository;

import iorihuang.bankaccountmanager.exception.AccountExceptions;
import iorihuang.bankaccountmanager.exception.error.AccountCreateError;
import iorihuang.bankaccountmanager.exception.error.AccountDeleteError;
import iorihuang.bankaccountmanager.exception.error.AccountTransferError;
import iorihuang.bankaccountmanager.exception.error.AccountUpdateError;
import iorihuang.bankaccountmanager.model.BankAccount;
import iorihuang.bankaccountmanager.model.bankaccount.AccountState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static iorihuang.bankaccountmanager.constant.AccountConst.DUPLICATE_KEY;
import static iorihuang.bankaccountmanager.constant.AccountConst.UNIQUE_CONSTRAINT;

/**
 * transactional for bank account
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountTrans {
    private final BankAccountRepository repository;

    /**
     * Create a bank account with transaction management
     *
     * @param account
     * @return
     */
    @Transactional
    public BankAccount createAccount(BankAccount account) {
        try {
            BankAccount save = repository.save(account);
            //todo 账户信息流水表
            //todo 余额变更流水表
            return save;
        } catch (DataIntegrityViolationException e) {
            // Determine if the root cause is a unique constraint violation in H2
            if (e.getRootCause() instanceof org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException) {
                String message = e.getRootCause().getMessage();
                // Further determine if it is a duplicate key (H2 exception messages typically contain "unique constraint" or "duplicate key")
                if (message.contains(UNIQUE_CONSTRAINT) || message.contains(DUPLICATE_KEY)) {
                    //or return success with exists account
                    throw AccountExceptions.duplicateAccount(account.getAccountNumber());
                }
            }
            throw new AccountCreateError(e, "Account create error: " + account.getAccountNumber());
        } catch (Exception e) {
            throw new AccountCreateError(e, "Account create error: " + account.getAccountNumber());
        }
    }

    /**
     * Update a bank account with transaction management
     *
     * @param account
     * @param ownerName
     * @param contactInfo
     * @param newVersion
     * @return updated time
     */
    @Transactional
    public LocalDateTime updateAccount(BankAccount account, String ownerName, String contactInfo, Long newVersion) {
        LocalDateTime now = LocalDateTime.now();
        String accountNumber = account.getAccountNumber();
        try {
            int updatedRows = repository.updateAccountByIdAndVersion(
                    account.getId(),
                    account.getAccountNumber(),
                    account.getState(),
                    account.getVersion(),
                    ownerName,
                    contactInfo,
                    now,
                    newVersion
            );
            if (updatedRows == 0) {
                log.error("Account update fail with no rows updated:{}", accountNumber);
                throw AccountExceptions.updateAccount(accountNumber);
            }
            //todo 账户信息流水表
        } catch (Exception e) {
            throw new AccountUpdateError(e, "Account update error: " + account.getAccountNumber());
        }
        return now;
    }

    /**
     * Delete a bank account with transaction management
     *
     * @param account    account
     * @param newState   new state
     * @param newVersion new version
     * @return delete timestamp
     */
    @Transactional
    public LocalDateTime deleteAccount(BankAccount account, AccountState newState, long newVersion) {
        String accountNumber = account.getAccountNumber();
        LocalDateTime now = LocalDateTime.now();
        try {
            int updatedRows = 0;
            switch (newState) {
                case CLOSED:
                    updatedRows = repository.closeAccountByIdAndVersion(
                            account.getId(),
                            account.getAccountNumber(),
                            account.getState(),
                            account.getVersion(),
                            newState.getCode(), // Set new state to CLOSED
                            now, // Deletion timestamp
                            newVersion // New version number
                    );
                    break;
                case FROZEN:
                    updatedRows = repository.frozenAccountByIdAndVersion(
                            account.getId(),
                            account.getAccountNumber(),
                            account.getState(),
                            account.getVersion(),
                            newState.getCode(), // Set new state to FROZEN
                            now, // update timestamp
                            newVersion // New version number
                    );
                    break;
            }
            if (updatedRows <= 0) {
                throw AccountExceptions.deleteAccount(accountNumber);
            }
            //todo 账户信息流水表
        } catch (Exception e) {
            throw new AccountDeleteError(e, "Account delete error: " + account.getAccountNumber());
        }
        return now;
    }

    /**
     * Transfer money between two accounts
     *
     * @param from
     * @param to
     * @param amount
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public LocalDateTime transfer(BankAccount from, BankAccount to, BigDecimal amount, long version) {
        LocalDateTime now = LocalDateTime.now();
        try {
            // Update the 'from' account balance
            int updateFrom = repository.reduceBalanceByIdAndVersion(from.getId(), from.getAccountNumber(), from.getState(), from.getVersion(), from.getBalance(), amount, now, version);
            if (updateFrom <= 0) {
                throw AccountExceptions.insufficientBalance(from.getAccountNumber(), amount);
            }
            // Update the 'to' account balance
            int updateTo = repository.increaseBalanceByIdAndVersion(to.getId(), to.getAccountNumber(), to.getState(), to.getVersion(), to.getBalance(), amount, now, version);
            if (updateTo <= 0) {
                throw AccountExceptions.updateAccount(from.getAccountNumber());
            }
            //todo 转账流水表
            //todo 余额变更流水表
        } catch (Exception e) {
            throw new AccountTransferError(e, "Account transfer error from " + from.getAccountNumber() + " to " + to.getAccountNumber());
        }
        return now;
    }
}
