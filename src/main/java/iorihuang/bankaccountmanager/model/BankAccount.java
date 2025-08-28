package iorihuang.bankaccountmanager.model;

import iorihuang.bankaccountmanager.model.bankaccount.AccountState;
import iorihuang.bankaccountmanager.model.bankaccount.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {
    private Long id;

    private String accountNumber;

    private Integer accountType;

    private String ownerId;

    private String ownerName;

    private String contactInfo;

    private BigDecimal balance;

    private LocalDateTime balanceAt;

    private Integer state; // Account state, logical delete usage

    private Long ver; // Version for optimistic locking

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    /**
     * Get account type as enum
     *
     * @return AccountType enum or null if not set
     */
    public AccountType getAccountTypeEnum() {
        if (this.accountType == null) {
            return null;
        }
        return AccountType.fromCodeSafe(this.accountType);
    }

    /**
     * Set account type from enum
     *
     * @param accountType AccountType enum
     */
    public void setAccountTypeEnum(AccountType accountType) {
        this.accountType = accountType != null ? accountType.getCode() : null;
    }

    /**
     * Get state as enum
     *
     * @return AccountState enum or null if not set
     */
    public AccountState getStateEnum() {
        if (this.state == null) {
            return null;
        }
        return AccountState.fromCodeSafe(this.state);
    }

    /**
     * Set state from enum
     *
     * @param state AccountState enum
     */
    public void setStateEnum(AccountState state) {
        this.state = state != null ? state.getCode() : null;
    }
}