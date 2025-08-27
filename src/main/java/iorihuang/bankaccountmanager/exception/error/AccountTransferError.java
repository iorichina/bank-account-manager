package iorihuang.bankaccountmanager.exception.error;

import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.ExpCode;

public class AccountTransferError extends AccountError {
    public AccountTransferError(Throwable cause, String message) {
        super(ExpCode.DbTransferBalanceErr.getCode(), cause, message);
    }
}
