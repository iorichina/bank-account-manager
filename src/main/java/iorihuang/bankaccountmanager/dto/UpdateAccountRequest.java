package iorihuang.bankaccountmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateAccountRequest {
    @NotEmpty(message = "Owner name cannot be empty")
    @JsonProperty("owner_name")
    private String ownerName;

    @NotEmpty(message = "Contact info cannot be empty")
    @JsonProperty("contact_info")
    private String contactInfo;
}
