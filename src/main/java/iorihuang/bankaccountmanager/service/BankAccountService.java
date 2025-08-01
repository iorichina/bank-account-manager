package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.dto.*;

public interface BankAccountService {
    BankAccountDTO createAccount(CreateAccountRequest request);

    BankAccountDTO deleteAccount(String accountNumber);

    BankAccountDTO updateAccount(String accountNumber, UpdateAccountRequest request);

    BankAccountListDTO listAccounts(Long lastId, Integer size);

    BankAccountDTO getAccount(String accountNumber);

    BankTransferDTO transfer(TransferRequest request);
}

