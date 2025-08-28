package iorihuang.bankaccountmanager.model;

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
public class BankAccountBalanceLog {
    private Long id;
    private Long accountId;
    private String accountNumber;
    private BigDecimal beforeBalance;
    private BigDecimal afterBalance;
    private BigDecimal changeAmount;
    private Integer changeType;
    private String changeDesc;
    private LocalDateTime createdAt;
}
