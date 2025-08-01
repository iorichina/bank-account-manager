package iorihuang.bankaccountmanager.exception.error;

import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.ExpCode;

public class AccountUpdateError extends AccountError {
    public AccountUpdateError(Throwable cause, String message) {
        super(ExpCode.DbUpdateAccountErr.getCode(), cause, message);
    }
}
