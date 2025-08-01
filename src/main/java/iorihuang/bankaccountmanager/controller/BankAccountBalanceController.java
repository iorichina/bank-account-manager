package iorihuang.bankaccountmanager.controller;

import iorihuang.bankaccountmanager.dto.BankTransferDTO;
import iorihuang.bankaccountmanager.dto.TransferRequest;
import iorihuang.bankaccountmanager.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Account Balance Operations API
 */
@RestController
@RequestMapping("/op/api/accounts/balance/v1")
@RequiredArgsConstructor
public class BankAccountBalanceController extends BaseController {
    private final BankAccountService service;

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody @Valid TransferRequest request) {
        BankTransferDTO dto = service.transfer(request);
        return buildResponse(dto);
    }
}

