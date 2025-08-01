package iorihuang.bankaccountmanager.exception.error;

import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.ExpCode;

public class AccountDeleteError extends AccountError {
    public AccountDeleteError(Throwable cause, String message) {
        super(ExpCode.DbDeleteAccountErr.getCode(), cause, message);
    }
}
