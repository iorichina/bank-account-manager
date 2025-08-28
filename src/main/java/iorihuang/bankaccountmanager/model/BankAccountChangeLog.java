package iorihuang.bankaccountmanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountChangeLog {
    private Long id;
    private Long accountId;
    private String accountNumber;
    private String ownerId;
    private Integer changeType;
    private String changeDesc;
    private Integer beforeState;
    private Integer afterState;
    private String beforeOwnerName;
    private String afterOwnerName;
    private String beforeContactInfo;
    private String afterContactInfo;
    private LocalDateTime createdAt;
}
