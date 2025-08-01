package iorihuang.bankaccountmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * response DTO for API operations
 *
 * @param <T>
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DTOResponse<T> {
    private String ts;
    /**
     * code of op result, 0 means fail, 1 means success
     */
    private int code;
    private String msg = "";
    /**
     * error code, if any error occurs, this will be set
     * to a non-zero value, otherwise it will be null or zero.
     */
    private int error;
    private T data;

    public DTOResponse() {
        ts = String.valueOf(System.currentTimeMillis());
    }
}
