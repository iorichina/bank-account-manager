package iorihuang.bankaccountmanager.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateAccountRequest {
    @NotEmpty(message = "Owner name cannot be empty")
    private String ownerName;
    @NotEmpty(message = "Contact info cannot be empty")
    private String contactInfo;
}
