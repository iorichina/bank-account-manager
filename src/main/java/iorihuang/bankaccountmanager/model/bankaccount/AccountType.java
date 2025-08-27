package iorihuang.bankaccountmanager.model.bankaccount;

import lombok.Getter;

/**
 * AccountType enum representing different types of bank accounts.
 */
@Getter
public enum AccountType {
    SAVINGS(1, "Savings Account"),
    CURRENT(2, "Current Account"),
    FIXED_DEPOSIT(3, "Fixed Deposit Account");

    private final int code;
    private final String description;

    AccountType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Safely convert code to AccountType, return null for unknown codes
     *
     * @param code the code to convert
     * @return the AccountType or null if code is unknown
     */
    public static AccountType fromCodeSafe(int code) {
        for (AccountType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null; // Return null for unknown codes instead of throwing exception
    }
}