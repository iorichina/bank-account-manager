package iorihuang.bankaccountmanager.controller;

import io.micrometer.observation.annotation.Observed;
import iorihuang.bankaccountmanager.dto.BankAccountDTO;
import iorihuang.bankaccountmanager.dto.BankAccountListDTO;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Account Information Query Interface
 */
@RestController
@RequestMapping("/info/api/accounts/v1")
@RequiredArgsConstructor
@Slf4j
@Observed(name = "info.controller")
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
    @Observed(name = "bank.account.list")
    public ResponseEntity<?> list(@RequestParam(name = "last_id", required = false) Long lastId, @RequestParam(required = false) Integer size) throws AccountError, AccountException {
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
    @Observed(name = "bank.account.get")
    public ResponseEntity<?> get(@PathVariable String accountNumber) throws AccountError, AccountException {
        BankAccountDTO dto = service.getAccount(accountNumber);
        return buildResponse(dto);
    }

}