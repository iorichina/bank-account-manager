package iorihuang.bankaccountmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import iorihuang.bankaccountmanager.constant.AccountConst;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Data Transfer Object for Bank Account
 */
@Data
@Accessors(chain = true)
public class BankAccountDTO {
    /**
     * using String instead of Long for id to avoid long overflow issues
     */
    private String id;
    @JsonProperty("account_number")
    private String accountNumber;
    /**
     * @see iorihuang.bankaccountmanager.model.bankaccount.AccountType
     */
    @JsonProperty("account_type")
    private Integer accountType;
    @JsonProperty("owner_id")
    private String ownerId;
    @JsonProperty("owner_name")
    private String ownerName;
    @JsonProperty("contact_info")
    private String contactInfo;
    /**
     * balance keeping 6 decimal places
     */
    private String balance;
    /**
     * @see iorihuang.bankaccountmanager.model.bankaccount.AccountState
     */
    private Integer state;

    /**
     * Set the account id
     *
     * @param id the id to set
     * @return this
     */
    public BankAccountDTO setId(Long id) {
        if (Objects.isNull(id)) {
            return this;
        }
        this.id = id.toString();
        return this;
    }

    /**
     * Set the balance, keeping 6 decimal places
     *
     * @param balance the balance to set
     * @return this
     */
    public BankAccountDTO setBalance(BigDecimal balance) {
        if (Objects.isNull(balance)) {
            return this;
        }
        this.balance = balance.setScale(AccountConst.BALANCE_SHOW_DOTS, RoundingMode.FLOOR).toString();
        return this;
    }

}