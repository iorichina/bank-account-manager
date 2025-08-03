package iorihuang.bankaccountmanager.model.bankaccountbalancelog;

import lombok.Getter;

@Getter
public enum BalanceChangeType {
    DEPOSIT(1, "Deposit"),
    WITHDRAWAL(2, "Withdrawal"),
    TRANSFER_IN(3, "Transfer In"),
    TRANSFER_OUT(4, "Transfer Out"),
    OTHER(5, "Other");;
    private final int code;
    private final String description;

    private BalanceChangeType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
