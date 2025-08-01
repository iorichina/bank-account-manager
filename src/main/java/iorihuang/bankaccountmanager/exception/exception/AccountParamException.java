package iorihuang.bankaccountmanager.exception.exception;

import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.exception.ExpCode;

public class AccountParamException extends AccountException {
    public AccountParamException(String message) {
        super(ExpCode.AccountInvalidParam.getCode(), message);
    }
}
