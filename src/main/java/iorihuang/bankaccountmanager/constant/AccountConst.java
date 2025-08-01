package iorihuang.bankaccountmanager.constant;

public final class AccountConst {
    private AccountConst() {
    }

    /**
     * Balance save decimal places, only used for saving to database
     */
    public static final int BALANCE_SAVE_DOTS = 10;
    /**
     * Balance display decimal places
     */
    public static final int BALANCE_SHOW_DOTS = 6;

    public static final String UNIQUE_CONSTRAINT = "unique constraint";
    public static final String DUPLICATE_KEY = "duplicate key";
}
