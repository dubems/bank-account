package com.codefactory.domain.repository;

import com.codefactory.domain.entity.Account;
import com.codefactory.domain.entity.AccountType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface AccountRepository {

    Map<String, Account> getAllBankAccounts();

    String saveAccount(Account account);

    Optional<Account> getBankAccount(String IBAN);

    Set<Account> getBankAccountsBy(Set<AccountType> accountTypes);

    void deleteAllAccounts();
}
