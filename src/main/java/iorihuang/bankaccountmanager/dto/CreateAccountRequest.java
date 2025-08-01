package iorihuang.bankaccountmanager.dto;

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
    private String accountNumber;

    @NotNull(message = "Account type cannot be null")
    private Integer accountType;

    @NotEmpty(message = "Owner ID cannot be empty")
    private String ownerId;

    @NotEmpty(message = "Owner name cannot be empty")
    private String ownerName;

    @NotEmpty(message = "Contact information cannot be empty")
    private String contactInfo;

    /**
     * Initial balance is optional, defaults to 0
     */
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