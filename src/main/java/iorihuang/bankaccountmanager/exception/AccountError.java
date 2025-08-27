package iorihuang.bankaccountmanager.exception;

import lombok.Getter;

@Getter
public class AccountError extends Exception implements CodeE {
    private int code;

    public AccountError(int code, Throwable cause, String message) {
        super(message, cause);
        this.code = code;
    }

    public AccountError(int code, Throwable cause, String format, Object... args) {
        super(String.format(format, args), cause);
        this.code = code;
    }
}
