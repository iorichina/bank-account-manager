package iorihuang.bankaccountmanager.exception.exception;

import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.ExpCode;

public class UpdateAccountException extends AccountException {
    public UpdateAccountException(String message) {
        super(ExpCode.UpdateAccountFail.getCode(), message);
    }
}
