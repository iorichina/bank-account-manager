package iorihuang.bankaccountmanager.controller;

import iorihuang.bankaccountmanager.dto.DTOResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class BaseController {
    /**
     * return success response
     *
     * @param data
     * @param <T>
     * @return
     */
    public <T> ResponseEntity<?> buildResponse(T data) {
        DTOResponse<T> body = new DTOResponse<>();
        body.setCode(1);
        body.setData(data);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}
