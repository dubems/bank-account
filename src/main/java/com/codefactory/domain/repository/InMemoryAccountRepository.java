package com.codefactory.domain.repository;

import com.codefactory.domain.entity.Account;
import com.codefactory.domain.entity.AccountType;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryAccountRepository implements AccountRepository {

    private Map<String, Account> bankAccounts;

    @Override
    public Map<String, Account> getAllBankAccounts() {
        return bankAccounts;
    }

    @Override
    public synchronized String saveAccount(final Account account) {
        final String IBAN = account.getIBAN();
        bankAccounts.put(IBAN, account);
        return IBAN;
    }

    @Override
    public synchronized Optional<Account> getBankAccount(String IBAN) {
        return Optional.ofNullable(bankAccounts.get(IBAN));
    }

    @Override
    public synchronized Set<Account> getBankAccountsBy(Set<AccountType> accountTypes) {
        return bankAccounts.values().stream()
                .filter(account -> accountTypes.contains(account.getAccountType()))
                .collect(Collectors.toSet());
    }

    @Override
    public void deleteAllAccounts() {
        bankAccounts.clear();
    }

    @PostConstruct
    private void init() {
        bankAccounts = new ConcurrentHashMap<>();
    }
}
