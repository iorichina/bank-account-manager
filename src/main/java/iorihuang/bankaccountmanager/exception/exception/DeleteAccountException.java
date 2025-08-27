package iorihuang.bankaccountmanager.exception.exception;

import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.ExpCode;

public class DeleteAccountException extends AccountException {
    public DeleteAccountException(String message) {
        super(ExpCode.DeleteAccountFail.getCode(), message);
    }
}
