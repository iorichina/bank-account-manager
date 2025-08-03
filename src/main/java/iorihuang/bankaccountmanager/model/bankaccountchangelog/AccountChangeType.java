package iorihuang.bankaccountmanager.model.bankaccountchangelog;

import lombok.Getter;

@Getter
public enum AccountChangeType {
    OPEN_ACCOUNT(1, "Open Account"),
    CLOSE_ACCOUNT(2, "Close Account"),
    INFO_CHANGE(3, "Info Change"),
    FROZEN_STATE_CHANGE(4, "FROZEN State Change"),
    REACTIVE_STATE_CHANGE(5, "ReActive State Change");
    private final int code;
    private final String description;

    private AccountChangeType(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
