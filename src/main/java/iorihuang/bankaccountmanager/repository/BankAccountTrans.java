package iorihuang.bankaccountmanager.repository;

import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.AccountExceptions;
import iorihuang.bankaccountmanager.exception.error.*;
import iorihuang.bankaccountmanager.model.BankAccount;
import iorihuang.bankaccountmanager.model.BankAccountBalanceLog;
import iorihuang.bankaccountmanager.model.BankAccountChangeLog;
import iorihuang.bankaccountmanager.model.BankAccountTransferLog;
import iorihuang.bankaccountmanager.model.bankaccount.AccountState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

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
    private final BankAccountChangeLogRepository changeLogRepository;
    private final BankAccountBalanceLogRepository balanceLogRepository;
    private final BankAccountTransferLogRepository transferLogRepository;

    /**
     * Create a bank account with transaction management
     *
     * @param account
     * @param changeLog
     * @param balanceLog
     * @return
     */
    @Transactional(rollbackFor = Throwable.class, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRES_NEW)
    public BankAccount createAccount(BankAccount account, BankAccountChangeLog changeLog, BankAccountBalanceLog balanceLog) throws AccountError, AccountException {
        try {
            BankAccount save = repository.save(account);
            if (changeLog != null) {
                // 保存变更日志
                changeLogRepository.save(changeLog);
            }
            if (balanceLog != null) {
                balanceLogRepository.save(balanceLog);
            }
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
            throw new AccountCreateError(e, "Account create error1: " + account.getAccountNumber());
        } catch (Exception e) {
            throw new AccountCreateError(e, "Account create error2: " + account.getAccountNumber());
        }
    }

    /**
     * Update a bank account with transaction management
     *
     * @param account
     * @param ownerName
     * @param contactInfo
     * @param newVersion
     * @param changeLog
     * @return updated time
     */
    @Transactional(rollbackFor = Throwable.class, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRES_NEW)
    public LocalDateTime updateAccount(BankAccount account, String ownerName, String contactInfo, long newVersion, BankAccountChangeLog changeLog) throws AccountError, AccountException {
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
            if (changeLog != null) {
                changeLogRepository.save(changeLog);
            }
        } catch (Exception e) {
            if (e instanceof AccountException) {
                throw e;
            }
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
     * @param changeLog
     * @return delete timestamp
     */
    @Transactional(rollbackFor = Throwable.class, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRES_NEW)
    public LocalDateTime deleteAccount(BankAccount account, AccountState newState, long newVersion, BankAccountChangeLog changeLog) throws AccountError, AccountException {
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
            if (changeLog != null) {
                changeLogRepository.save(changeLog);
            }
        } catch (Exception e) {
            if (e instanceof AccountException) {
                throw e;
            }
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
     * @param version
     * @param fromBalanceLog
     * @param toBalanceLog
     * @return
     */
    @Transactional(rollbackFor = Throwable.class, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRES_NEW)
    public LocalDateTime transfer(BankAccount from, BankAccount to, BigDecimal amount, long version, BankAccountBalanceLog fromBalanceLog, BankAccountBalanceLog toBalanceLog, BankAccountTransferLog transferLog) throws AccountError, AccountException {
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
            if (transferLog != null) {
                transferLogRepository.save(transferLog);
            }
            //todo move to disruptor
            if (fromBalanceLog != null) {
                balanceLogRepository.save(fromBalanceLog);
            }
            if (toBalanceLog != null) {
                balanceLogRepository.save(toBalanceLog);
            }
        } catch (Exception e) {
            if (e instanceof AccountException) {
                throw e;
            }
            throw new AccountTransferError(e, "Account transfer error from " + from.getAccountNumber() + " to " + to.getAccountNumber());
        }
        return now;
    }

    /**
     * 强制新事务读取，避免JPA一级缓存影响
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<BankAccount> getAccountByAccountNumberWithNewTx(String accountNumber) throws AccountError {
        try {
            return repository.findByAccountNumber(accountNumber);
        } catch (Exception e) {
            throw new AccountReadError(e, "Error retrieving account by account number: " + accountNumber);
        }
    }

}
