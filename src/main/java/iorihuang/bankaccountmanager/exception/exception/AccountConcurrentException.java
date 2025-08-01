package iorihuang.bankaccountmanager.exception.exception;

import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.ExpCode;

public class AccountConcurrentException extends AccountException {
    public AccountConcurrentException(String message) {
        super(ExpCode.AccountConcurrentLimit.getCode(), message);
    }
}
