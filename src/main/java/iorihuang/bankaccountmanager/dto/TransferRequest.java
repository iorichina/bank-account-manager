package iorihuang.bankaccountmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class TransferRequest {
    @NotEmpty(message = "From account number cannot be empty")
    @JsonProperty("from_account_number")
    private String fromAccountNumber;

    @NotEmpty(message = "To account number cannot be empty")
    @JsonProperty("to_account_number")
    private String toAccountNumber;

    @NotEmpty(message = "Amount cannot be empty")
    private String amount;

    /**
     * Get amount as BigDecimal
     *
     * @return amount as BigDecimal, or null if amount is null or empty
     */
    public BigDecimal getAmountAsBigDecimal() {
        if (amount == null || amount.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(amount);
        } catch (NumberFormatException e) {
            return null; // Return null if amount is not a valid number
        }
    }
}