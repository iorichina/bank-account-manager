package iorihuang.bankaccountmanager.exception;

import lombok.Getter;

@Getter
public enum ExpCode {
    AccountNotFound(10001, "Account not found"),
    DuplicateAccount(10002, "Duplicate account found"),
    AccountInvalidParam(10003, "Account invalid parameter"),
    DeleteAccountFail(10004, "Delete account failed"),
    UpdateAccountFail(10005, "Update account failed"),
    InsufficientBalance(10006, "Insufficient balance for the operation"),
    TransferAccountLimit(10007, "Account limit to transfer balance "),
    AccountConcurrentLimit(10008, "Account current operation limit"),
    DbCreateAccountErr(20001, "Database error while creating account"),
    DbDeleteAccountErr(20002, "Database error while delete account"),
    DbUpdateAccountErr(20003, "Database error while update account"),
    DbTransferBalanceErr(20004, "Database error while transfer balance"),
    DbReadAccountErr(20005, "Database error while read account"),
    ;
    private final int code;
    private final String message;

    ExpCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
