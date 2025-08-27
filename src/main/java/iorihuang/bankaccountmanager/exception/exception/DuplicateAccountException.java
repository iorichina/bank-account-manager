package iorihuang.bankaccountmanager.exception.exception;

import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.ExpCode;

public class DuplicateAccountException extends AccountException {
    public DuplicateAccountException(String message) {
        super(ExpCode.DuplicateAccount.getCode(), message);
    }
}
