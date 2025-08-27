package iorihuang.bankaccountmanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bank_account_change_log",
        indexes = {
                @Index(name = "idx_account_change_account_number", columnList = "accountNumber,createdAt")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountChangeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 32)
    private String accountNumber;

    @Column(nullable = false, length = 32)
    private String ownerId;

    @Column(nullable = false)
    private Integer changeType;

    @Column(nullable = false, length = 128)
    private String changeDesc;

    @Column(nullable = false)
    private Integer beforeState;

    @Column(nullable = false)
    private Integer afterState;

    @Column(nullable = false, length = 64)
    private String beforeOwnerName;

    @Column(nullable = false, length = 64)
    private String afterOwnerName;

    @Column(nullable = false, length = 64)
    private String beforeContactInfo;

    @Column(nullable = false, length = 64)
    private String afterContactInfo;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
