package iorihuang.bankaccountmanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_account_transfer_log",
        indexes = {
                @Index(name = "idx_transfer_from_account_number", columnList = "fromAccountNumber,createdAt"),
                @Index(name = "idx_transfer_to_account_number", columnList = "toAccountNumber,createdAt")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountTransferLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long fromAccountId;

    @Column(nullable = false, length = 32)
    private String fromAccountNumber;

    @Column(nullable = false)
    private Long toAccountId;

    @Column(nullable = false, length = 32)
    private String toAccountNumber;

    @Column(nullable = false, precision = 25, scale = 10)
    private BigDecimal amount;

    @Column(nullable = false, precision = 25, scale = 10)
    private BigDecimal beforeBalanceFrom;

    @Column(nullable = false, precision = 25, scale = 10)
    private BigDecimal afterBalanceFrom;

    @Column(nullable = false, precision = 25, scale = 10)
    private BigDecimal beforeBalanceTo;

    @Column(nullable = false, precision = 25, scale = 10)
    private BigDecimal afterBalanceTo;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
