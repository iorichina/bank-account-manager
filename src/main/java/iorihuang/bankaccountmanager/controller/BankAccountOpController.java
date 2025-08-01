package iorihuang.bankaccountmanager.controller;

import iorihuang.bankaccountmanager.dto.BankAccountDTO;
import iorihuang.bankaccountmanager.dto.CreateAccountRequest;
import iorihuang.bankaccountmanager.dto.Empty;
import iorihuang.bankaccountmanager.dto.UpdateAccountRequest;
import iorihuang.bankaccountmanager.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 账户变更接口
 */
@RestController
@RequestMapping("/op/api/accounts/v1")
@RequiredArgsConstructor
public class BankAccountOpController extends BaseController {
    private final BankAccountService service;

    /**
     * create account
     *
     * @return saved account info
     */
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody @Valid CreateAccountRequest request) {
        BankAccountDTO dto = service.createAccount(request);
        return buildResponse(dto);
    }

    /**
     * Delete account by account number
     *
     * @return saved account info
     */
    @DeleteMapping("/delete/{accountNumber}")
    public ResponseEntity<?> delete(@PathVariable String accountNumber) {
        service.deleteAccount(accountNumber);
        return buildResponse(new Empty());
    }

    /**
     * Update account ownerName, contactInfo by account number
     *
     * @return saved account info
     */
    @PutMapping("/update/{accountNumber}")
    public ResponseEntity<?> update(@PathVariable String accountNumber, @RequestBody @Valid UpdateAccountRequest request) {
        BankAccountDTO dto = service.updateAccount(accountNumber, request);
        return buildResponse(dto);
    }

}

