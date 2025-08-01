package iorihuang.bankaccountmanager.model.bankaccount;

import lombok.Getter;

/**
 * Represents the state of a bank account.
 */
@Getter
public enum AccountState {
    ACTIVE(1, "Active"),
    FROZEN(2, "Frozen"),
    CLOSED(4, "Closed");

    private final int code;
    private final String description;

    AccountState(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Safely convert code to AccountState, return null for unknown codes
     *
     * @param code the code to convert
     * @return the AccountState or null if code is unknown
     */
    public static AccountState fromCodeSafe(int code) {
        for (AccountState state : values()) {
            if (state.getCode() == code) {
                return state;
            }
        }
        return null; // Return null for unknown codes instead of throwing exception
    }
}