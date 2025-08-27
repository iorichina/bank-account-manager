package iorihuang.bankaccountmanager.model;

import iorihuang.bankaccountmanager.model.bankaccount.AccountState;
import iorihuang.bankaccountmanager.model.bankaccount.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_account",
        uniqueConstraints = @UniqueConstraint(name = "uniq_account", columnNames = "accountNumber"),
        indexes = {
                @Index(name = "idx_owner_id", columnList = "ownerId"),
                @Index(name = "idx_state", columnList = "state")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {
    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)  // use snowflake ID generator instead
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String accountNumber;

    @Column(nullable = false)
    private Integer accountType;

    @Column(nullable = false, length = 32)
    private String ownerId;

    @Column(nullable = false, length = 64)
    private String ownerName;

    @Column(length = 64)
    private String contactInfo;

    @Column(nullable = false, precision = 25, scale = 10)
    private BigDecimal balance;

    @Column
    private LocalDateTime balanceAt;

    @Column(nullable = false)
    private Integer state; // Account state, logical delete usage

    @Column(name = "ver", nullable = false)
    private Long version; // Version for optimistic locking

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
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