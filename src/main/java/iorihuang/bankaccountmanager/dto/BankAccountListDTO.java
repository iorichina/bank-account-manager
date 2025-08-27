package iorihuang.bankaccountmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Data Transfer Object for Bank Account List
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankAccountListDTO {
    /**
     * elements of pagination
     */
    private List<BankAccountDTO> elements;
    /**
     * for next  page query
     */
    @JsonProperty("last_id")
    private String lastId;
    /**
     * 1=yes, 0=no
     */
    @JsonProperty("has_more")
    private int hasMore;

    public BankAccountListDTO isHasMore(boolean hasMore) {
        this.hasMore = hasMore ? 1 : 0;
        return this;
    }
}