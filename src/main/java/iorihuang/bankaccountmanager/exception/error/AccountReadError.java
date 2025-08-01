package iorihuang.bankaccountmanager.exception.error;

import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.ExpCode;

public class AccountReadError extends AccountError {
    public AccountReadError(Throwable cause, String message) {
        super(ExpCode.DbReadAccountErr.getCode(), cause, message);
    }
}
