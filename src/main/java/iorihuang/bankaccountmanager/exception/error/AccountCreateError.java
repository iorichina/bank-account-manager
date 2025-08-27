package iorihuang.bankaccountmanager.exception.error;

import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.ExpCode;

public class AccountCreateError extends AccountError {
    public AccountCreateError(Throwable cause, String message) {
        super(ExpCode.DbCreateAccountErr.getCode(), cause, message);
    }
}
