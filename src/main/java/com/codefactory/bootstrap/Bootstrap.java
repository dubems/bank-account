package com.codefactory.bootstrap;

import com.codefactory.domain.entity.Account;
import com.codefactory.domain.entity.AccountType;
import com.codefactory.domain.repository.AccountRepository;
import com.codefactory.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class Bootstrap implements CommandLineRunner {

    private final BankAccountService bankAccountService;
    private final AccountRepository accountRepository;


    @Override
    public void run(String... args) throws Exception {
        final Account savingsAccount = bankAccountService.buildAccount(AccountType.SAVINGS_ACCOUNT, BigDecimal.valueOf(1000.00));
        createAccount(savingsAccount);
        final Account loanAccount = bankAccountService.buildAccount(AccountType.PRIVATE_LOAN_ACCOUNT, BigDecimal.valueOf(1000.00));
        createAccount(loanAccount);
    }

    private void createAccount(Account account) {
        Account checkingAccount = null;
        if (account.getAccountType() == AccountType.SAVINGS_ACCOUNT) {
            checkingAccount = bankAccountService.buildAccount(AccountType.CHECKING_ACCOUNT, BigDecimal.valueOf(1000.00));
            accountRepository.saveAccount(checkingAccount);
            log.info("Account with IBAN = {} and type = {} has been created", checkingAccount.getIBAN(),
                    checkingAccount.getAccountType().toString());
        }

        account.setReferenceAccount(checkingAccount);
        final String IBAN = accountRepository.saveAccount(account);
        log.info("Account with IBAN = {} and type = {} has been created", IBAN, account.getAccountType().toString());
    }
}
