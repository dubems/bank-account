package com.codefactory.service;

import com.codefactory.domain.entity.Account;
import com.codefactory.domain.entity.AccountType;
import com.codefactory.domain.repository.AccountRepository;
import com.codefactory.exception.AccountAlreadyLockedException;
import com.codefactory.exception.AccountNotLockedException;
import com.codefactory.exception.BankAccountNotFoundException;
import com.codefactory.service.utils.IBANUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final Clock clock;
    private final AccountRepository accountRepository;


    public String createAccount(final AccountType accountType) {
        Account checkingAccount = null;
        BigDecimal defaultAmount = BigDecimal.valueOf(0.00);
        if (accountType.equals(AccountType.SAVINGS_ACCOUNT)) {
            checkingAccount = buildAccount(AccountType.CHECKING_ACCOUNT, defaultAmount);
            accountRepository.saveAccount(checkingAccount);
        }

        final Account account = buildAccount(accountType, defaultAmount);
        account.setReferenceAccount(checkingAccount);

        final String IBAN = accountRepository.saveAccount(account);
        log.info("Account with IBAN = {} and type ={} has been created", IBAN, accountType.toString());
        return IBAN;
    }

    public void saveAccount(final Account account) {
        accountRepository.saveAccount(account);
    }

    public Set<Account> filterAccountsBy(Set<AccountType> accountTypes) {
        return accountRepository.getBankAccountsBy(accountTypes);
    }

    public BigDecimal getAccountBalance(String IBAN) {
        return accountRepository.getBankAccount(IBAN)
                .map(Account::getBalance)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount was not found, IBAN=" + IBAN));
    }

    public void lockAccount(final String IBAN) {
        Optional<Account> accountOptional = accountRepository.getBankAccount(IBAN);
        if (accountOptional.isPresent()) {
            Account bankAccount = accountOptional.get();
            if (!bankAccount.isLocked()) {
                bankAccount.setLocked(true);
                accountRepository.saveAccount(bankAccount);
                log.info("BankAccount with IBAN ={} has been locked", IBAN);
            } else {
                log.warn("BankAccount with IBAN = {} is already locked!", IBAN);
                throw new AccountAlreadyLockedException("Account is already locked, IBAN=" + IBAN);
            }
        } else {
            log.warn("BankAccount with IBAN = {} does not exist", IBAN);
            throw new BankAccountNotFoundException("BankAccount was not found");
        }
    }

    public void unlockAccount(final String IBAN) {
        accountRepository.getBankAccount(IBAN)
                .map(account -> {
                    if (account.isLocked()) {
                        account.setLocked(false);
                        accountRepository.saveAccount(account);
                        log.info("BankAccount with IBAN ={} has been unlocked", IBAN);
                    } else {
                        log.warn("BankAccount with IBAN = {} is not locked", IBAN);
                        throw new AccountNotLockedException("Account is not locked and cannot be unlocked, IBAN=" + IBAN);
                    }

                    return account;
                }).orElseThrow(() -> {
            log.warn("BankAccount with IBAN = {} does not exist", IBAN);
            return new BankAccountNotFoundException("BankAccount was not found");
        });
    }

    public Optional<Account> getBankAccount(final String IBAN) {
        return accountRepository.getBankAccount(IBAN);
    }

    public Account buildAccount(final AccountType accountType, BigDecimal amount) {
        Instant now = clock.instant();
        return Account.builder()
                .accountType(accountType)
                .balance(amount)
                .IBAN(generateIBAN())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private String generateIBAN() {
        String IBAN;
        do {
            IBAN = IBANUtility.generateIBAN();
        } while (IBANExists(IBAN));

        return IBAN;
    }

    private boolean IBANExists(final String IBAN) {
        return accountRepository.getAllBankAccounts().containsKey(IBAN);
    }
}
