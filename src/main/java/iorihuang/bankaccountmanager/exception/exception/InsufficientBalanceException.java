package iorihuang.bankaccountmanager.exception.exception;

import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.ExpCode;

public class InsufficientBalanceException extends AccountException {
    public InsufficientBalanceException(String message) {
        super(ExpCode.InsufficientBalance.getCode(), message);
    }
}

