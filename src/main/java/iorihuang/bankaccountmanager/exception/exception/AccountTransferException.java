package iorihuang.bankaccountmanager.exception.exception;

import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.ExpCode;

public class AccountTransferException extends AccountException {
    public AccountTransferException(ExpCode code, String message) {
        super(code.getCode(), message);
    }
}
