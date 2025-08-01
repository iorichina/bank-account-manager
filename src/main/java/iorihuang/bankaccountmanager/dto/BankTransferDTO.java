package iorihuang.bankaccountmanager.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Data Transfer Object for Bank Account
 */
@Data
@Accessors(chain = true)
public class BankTransferDTO {
    private BankAccountDTO from;
    private BankAccountDTO to;
}