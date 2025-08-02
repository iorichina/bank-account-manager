package iorihuang.bankaccountmanager.service;

import iorihuang.bankaccountmanager.dto.*;
import iorihuang.bankaccountmanager.exception.AccountError;
import iorihuang.bankaccountmanager.exception.AccountException;

public interface BankAccountService {
    BankAccountDTO createAccount(CreateAccountRequest request) throws AccountError, AccountException;

    BankAccountDTO deleteAccount(String accountNumber) throws AccountException, AccountError;

    BankAccountDTO updateAccount(String accountNumber, UpdateAccountRequest request) throws AccountException, AccountError;

    BankAccountListDTO listAccounts(Long lastId, Integer size) throws AccountException, AccountError;

    BankAccountDTO getAccount(String accountNumber) throws AccountException, AccountError;

    BankTransferDTO transfer(TransferRequest request) throws AccountException, AccountError;
}

