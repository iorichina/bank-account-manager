package iorihuang.bankaccountmanager.exception;

import iorihuang.bankaccountmanager.exception.exception.*;

import java.math.BigDecimal;

public class AccountExceptions {
    private AccountExceptions() {
    }

    public static AccountNotFoundException accountNotFound(String accountId) {
        return new AccountNotFoundException("Account with ID " + accountId + " not found.");
    }

    public static DuplicateAccountException duplicateAccount(String accountId) {
        return new DuplicateAccountException("Account with ID " + accountId + " already exists.");
    }

    public static DeleteAccountException deleteAccount(String accountId) {
        return new DeleteAccountException("Account with ID " + accountId + " delete fail.");
    }

    public static DeleteAccountException deleteFailWithClosed(String accountId) {
        return new DeleteAccountException("Account with ID " + accountId + " is CLOSED.");
    }

    public static DeleteAccountException deleteFailWithFrozen(String accountId) {
        return new DeleteAccountException("Account with ID " + accountId + " is FROZEN.");
    }

    public static UpdateAccountException updateAccount(String accountId) {
        return new UpdateAccountException("Account with ID " + accountId + " update fail.");
    }

    public static UpdateAccountException updateFailWithClosed(String accountId) {
        return new UpdateAccountException("Account with ID " + accountId + " is CLOSED.");
    }

    public static InsufficientBalanceException insufficientBalance(String accountId, BigDecimal amount) {
        return new InsufficientBalanceException("Insufficient balance in account " + accountId + " for withdrawal of " + amount);
    }

}