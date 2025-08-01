package iorihuang.bankaccountmanager.exception;

import lombok.Getter;

@Getter
public class AccountException extends RuntimeException implements CodeE {
    private int code;

    public AccountException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AccountException(int code, String format, Object... args) {
        super(String.format(format, args));
        this.code = code;
    }
}
