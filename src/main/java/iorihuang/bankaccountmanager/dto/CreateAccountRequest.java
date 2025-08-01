package iorihuang.bankaccountmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * Request parameters for creating an account
 */
@Data
@Accessors(chain = true)
public class CreateAccountRequest {
    @NotEmpty(message = "Account number cannot be empty")
    @JsonProperty("account_number")
    private String accountNumber;

    /**
     * @see iorihuang.bankaccountmanager.model.bankaccount.AccountType
     */
    @NotNull(message = "Account type cannot be null")
    @JsonProperty("account_type")
    private Integer accountType;

    @NotEmpty(message = "Owner ID cannot be empty")
    @JsonProperty("owner_id")
    private String ownerId;

    @NotEmpty(message = "Owner name cannot be empty")
    @JsonProperty("owner_name")
    private String ownerName;

    @NotEmpty(message = "Contact information cannot be empty")
    @JsonProperty("contact_info")
    private String contactInfo;

    /**
     * Initial balance is optional, defaults to 0
     */
    @JsonProperty("initial_balance")
    private String initialBalance;

    /**
     * Convert initial balance to BigDecimal
     *
     * @return
     */
    public BigDecimal getInitialBalanceAsBigDecimal() {
        if (initialBalance == null || initialBalance.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(initialBalance);
        } catch (NumberFormatException e) {
            return null; // Return null if amount is not a valid number
        }
    }
}