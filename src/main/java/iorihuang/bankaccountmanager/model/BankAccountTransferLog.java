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
public class BankAccountTransferLog {
    private Long id;
    private Long fromAccountId;
    private String fromAccountNumber;
    private Long toAccountId;
    private String toAccountNumber;
    private BigDecimal amount;
    private BigDecimal beforeBalanceFrom;
    private BigDecimal afterBalanceFrom;
    private BigDecimal beforeBalanceTo;
    private BigDecimal afterBalanceTo;
    private LocalDateTime createdAt;
}
