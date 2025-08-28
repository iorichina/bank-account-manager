package iorihuang.bankaccountmanager.constant;

import java.time.format.DateTimeFormatter;

public final class AccountConst {
    private AccountConst() {
    }

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Balance save decimal places, only used for saving to database
     */
    public static final int BALANCE_SAVE_DOTS = 10;
    /**
     * Balance display decimal places
     */
    public static final int BALANCE_SHOW_DOTS = 6;

    /**
     * Account/Balance change lock time in seconds.
     * This is used to prevent concurrent modifications to the same account or balance.
     */
    public static final int ACCOUNT_CHANGE_LOCK_SEC = 3;

    /**
     * JPA unique constraint error message
     */
    public static final String UNIQUE_CONSTRAINT = "unique constraint";
    public static final String DUPLICATE_KEY = "duplicate key";
}
