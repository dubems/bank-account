package com.codefactory.service;

import com.codefactory.domain.entity.Account;
import com.codefactory.domain.entity.Transaction;
import com.codefactory.domain.entity.TransactionType;
import com.codefactory.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final String REFERENCE = "REFERENCE";

    private final Clock clock;
    private final BankAccountService bankAccountService;

    public void creditAccount(BigDecimal amount, String IBAN) {
        final Account account = getBankAccount(IBAN);
        validateAccountNotLocked(account);
        depositMoney(account, amount);
    }

    public void transferMoney(BigDecimal amount, String fromIBAN, String toIBAN) {
        final Account fromAccount = getBankAccount(fromIBAN);
        final Account toAccount = getBankAccount(toIBAN);

        validateTransfer(fromAccount, toAccount, amount);
        withDrawMoney(fromAccount, amount);
        depositMoney(toAccount, amount);
    }

    public Set<Transaction> getTransactionHistory(String IBAN) {
        return bankAccountService.getBankAccount(IBAN)
                .map(Account::getTransactions)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank Account not found, IBAN " + IBAN));
    }

    private Account getBankAccount(final String IBAN) {
        return bankAccountService.getBankAccount(IBAN)
                .orElseThrow(() -> {
                    log.error("BankAccount not found, IBAN={}", IBAN);
                    return new BankAccountNotFoundException("BankAccount not found, IBAN= " + IBAN);
                });
    }

    private void validateTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (!fromAccount.getAccountType().isWithdrawAble()) {
            log.warn("Withdrawal not supported for fromAccount, IBAN= {}", fromAccount.getIBAN());
            throw new WithdrawalNotSupportedException("Withdrawal not supported for fromAccount, IBAN= " + fromAccount.getIBAN());
        }

        validateAccountNotLocked(fromAccount);
        validateAccountNotLocked(toAccount);

        if (!hasSufficientBalance(fromAccount, amount)) {
            log.warn("Account with IBAN ={} has insufficient balance {}", fromAccount.getIBAN(), fromAccount.getAccountType());
            throw new InSufficientBalanceException("Account has insufficient balance, IBAN= " + fromAccount.getIBAN());
        }

        if (fromAccount.getReferenceAccount().isPresent() && fromAccount.getAccountType().getTransferTo().equals(REFERENCE)
                && !fromAccount.getReferenceAccount().get().getIBAN().equals(toAccount.getIBAN())) {
            log.warn("Savings account  with IBAN = {} can only send to reference checking account", fromAccount.getIBAN());
            throw new UnsupportedTransferException("Savings account can only send to reference checking account");
        }
    }

    private void withDrawMoney(final Account account, BigDecimal amount) {
        final BigDecimal newBalance = account.getBalance().subtract(amount);
        final Transaction transaction = buildTransaction(amount, TransactionType.DEBIT);
        updateAccount(account, newBalance, transaction);
        log.info("Bank Account with IBAN = {} has been debited", account.getIBAN());
    }

    private void depositMoney(final Account account, BigDecimal amount) {
        final BigDecimal newBalance = account.getBalance().add(amount);
        final Transaction transaction = buildTransaction(amount, TransactionType.CREDIT);
        updateAccount(account, newBalance, transaction);
        log.info("Bank Account with IBAN = {} has been credited", account.getIBAN());
    }

    private void updateAccount(Account account, BigDecimal balance, Transaction transaction) {
        account.setBalance(balance);
        account.setUpdatedAt(clock.instant());

        Set<Transaction> transactions = account.getTransactions();
        transactions.add(transaction);
        account.setTransactions(transactions);
        bankAccountService.saveAccount(account);
    }

    private boolean hasSufficientBalance(final Account account, final BigDecimal amount) {
        return (account.getBalance().compareTo(amount) >= 0);
    }

    private void validateAccountNotLocked(Account account) {
        if (account.isLocked()) {
            log.warn("Account with IBAN ={} is locked", account.getIBAN());
            throw new BankAccountIsLockedException("BankAccount with is locked, IBAN = " + account.getIBAN());
        }
    }

    private Transaction buildTransaction(final BigDecimal amount, TransactionType type) {
        return Transaction.builder()
                .amount(amount)
                .transactionType(type)
                .createdAt(clock.instant())
                .build();
    }
}
