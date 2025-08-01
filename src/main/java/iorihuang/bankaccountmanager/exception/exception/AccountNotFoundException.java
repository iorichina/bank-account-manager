package iorihuang.bankaccountmanager.exception.exception;

import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.ExpCode;

public class AccountNotFoundException extends AccountException {
    public AccountNotFoundException(String message) {
        super(ExpCode.AccountNotFound.getCode(), message);
    }
}
