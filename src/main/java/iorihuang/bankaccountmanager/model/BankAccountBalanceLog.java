package iorihuang.bankaccountmanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_account_balance_log",
        indexes = {
                @Index(name = "idx_balance_account_number", columnList = "accountNumber,createdAt")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountBalanceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 32)
    private String accountNumber;

    @Column(nullable = false, precision = 25, scale = 10)
    private BigDecimal beforeBalance;

    @Column(nullable = false, precision = 25, scale = 10)
    private BigDecimal afterBalance;

    @Column(nullable = false, precision = 25, scale = 10)
    private BigDecimal changeAmount;

    @Column(nullable = false)
    private Integer changeType;

    @Column(nullable = false, length = 128)
    private String changeDesc;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
