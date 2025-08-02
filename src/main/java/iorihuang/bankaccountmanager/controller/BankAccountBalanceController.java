package iorihuang.bankaccountmanager.controller;

import io.micrometer.observation.annotation.Observed;
import iorihuang.bankaccountmanager.dto.BankTransferDTO;
import iorihuang.bankaccountmanager.dto.TransferRequest;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;
import iorihuang.bankaccountmanager.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Observed(name = "balance.controller")
public class BankAccountBalanceController extends BaseController {
    private final BankAccountService service;

    @PostMapping("/transfer")
    @Observed(name = "bank.account.transfer")
    public ResponseEntity<?> transfer(@RequestBody @Valid TransferRequest request) throws AccountError, AccountException {
        BankTransferDTO dto = service.transfer(request);
        return buildResponse(dto);
    }
}

