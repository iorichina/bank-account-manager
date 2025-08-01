package iorihuang.bankaccountmanager.controller;

import iorihuang.bankaccountmanager.dto.BankAccountDTO;
import iorihuang.bankaccountmanager.dto.BankAccountListDTO;
import iorihuang.bankaccountmanager.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Account Information Query Interface
 */
@RestController
@RequestMapping("/info/api/accounts/v1")
@RequiredArgsConstructor
public class BankAccountInfoController extends BaseController {
    private final BankAccountService service;

    /**
     * Query account list, using account ID for pagination to reduce potential performance issues with large pagination
     *
     * @param lastId the account ID of the last record on the previous page, can be omitted for the first page
     * @param size   the number of records to return
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestParam(name = "last_id") Long lastId, @RequestParam Integer size) {
        // TODO: Implement rate limiting to prevent abuse of the API
        BankAccountListDTO dto = service.listAccounts(lastId, size);
        return buildResponse(dto);
    }

    /**
     * Query detailed account information
     *
     * @param accountNumber the account number
     * @return
     */
    @GetMapping("/{accountNumber}")
    public BankAccountDTO get(@PathVariable String accountNumber) {
        return service.getAccount(accountNumber);
    }

}

