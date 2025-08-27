package iorihuang.bankaccountmanager.controller;

import io.micrometer.observation.annotation.Observed;
import iorihuang.bankaccountmanager.dto.BankAccountDTO;
import iorihuang.bankaccountmanager.dto.CreateAccountRequest;
import iorihuang.bankaccountmanager.dto.UpdateAccountRequest;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * account info management
 */
@RestController
@RequestMapping("/op/api/accounts/v1")
@RequiredArgsConstructor
@Slf4j
@Observed(name = "account.controller")
public class BankAccountController extends BaseController {
    private final BankAccountService service;

    /**
     * create account
     *
     * @return saved account info
     */
    @PostMapping("/create")
    @Observed(name = "bank.account.create")
    public ResponseEntity<?> create(@RequestBody @Valid CreateAccountRequest request) throws AccountError, AccountException {
        BankAccountDTO dto = service.createAccount(request);
        return buildResponse(dto);
    }

    /**
     * Delete account by account number
     *
     * @return saved account info
     */
    @DeleteMapping("/delete/{accountNumber}")
    @Observed(name = "bank.account.delete")
    public ResponseEntity<?> delete(@PathVariable String accountNumber) throws AccountError, AccountException {
        BankAccountDTO dto = service.deleteAccount(accountNumber);
        return buildResponse(dto);
    }

    /**
     * Update account ownerName, contactInfo by account number
     *
     * @return saved account info
     */
    @PutMapping("/update/{accountNumber}")
    @Observed(name = "bank.account.update")
    public ResponseEntity<?> update(@PathVariable String accountNumber, @RequestBody @Valid UpdateAccountRequest request) throws AccountError, AccountException {
        BankAccountDTO dto = service.updateAccount(accountNumber, request);
        return buildResponse(dto);
    }

}

