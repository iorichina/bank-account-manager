package iorihuang.bankaccountmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Data Transfer Object for Bank Account
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankTransferDTO {
    private BankAccountDTO from;
    private BankAccountDTO to;
}