package com.codefactory.service;

import com.codefactory.domain.entity.Account;
import com.codefactory.domain.entity.AccountType;
import com.codefactory.domain.entity.Transaction;
import com.codefactory.domain.entity.TransactionType;
import com.codefactory.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    private final static String MOCK_IBAN = "DE80801817944420161050";
    private final static String ANOTHER_IBAN = "DE80811817954420161158";
    private final static String IBAN = "DE80811817954420162390";
    private final static BigDecimal MOCK_AMOUNT = BigDecimal.valueOf(2000);
    private final static BigDecimal LESSER_AMOUNT = BigDecimal.valueOf(1000);
    private final static BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(0);
    private final static String CREATED_AT = "createdAt";

    @Mock
    private Clock clock;
    @Mock
    private BankAccountService bankAccountService;
    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void shouldThrowExceptionWhenAccountIsLockedWhileCreditingAccount() {
        final Account account = buildAccount(AccountType.CHECKING_ACCOUNT, MOCK_AMOUNT, MOCK_IBAN);
        account.setLocked(true);
        given(bankAccountService.getBankAccount(MOCK_IBAN)).willReturn(Optional.of(account));

        Throwable throwable = catchThrowable(() -> transactionService.creditAccount(MOCK_AMOUNT, MOCK_IBAN));

        assertThat(throwable).isInstanceOf(BankAccountIsLockedException.class);
    }

    @Test
    public void shouldThrowExceptionWhenAccountDoesNotExistWhileGettingBankAccount() {
        given(bankAccountService.getBankAccount(MOCK_IBAN)).willReturn(Optional.empty());

        Throwable throwable = catchThrowable(() -> transactionService.creditAccount(MOCK_AMOUNT, MOCK_IBAN));

        assertThat(throwable).isInstanceOf(BankAccountNotFoundException.class)
                .hasMessage("BankAccount not found, IBAN= " + MOCK_IBAN);
    }

    @Test
    public void shouldCreditAccountWhenAllValidationPasses() {
        final Account account = buildAccount(AccountType.CHECKING_ACCOUNT, MOCK_AMOUNT, MOCK_IBAN);
        given(bankAccountService.getBankAccount(MOCK_IBAN)).willReturn(Optional.of(account));

        transactionService.creditAccount(MOCK_AMOUNT, MOCK_IBAN);

        verify(bankAccountService).getBankAccount(MOCK_IBAN);
        BigDecimal expectedBalance = MOCK_AMOUNT.add(MOCK_AMOUNT);
        verify(bankAccountService).saveAccount(argThat(acct -> acct.getBalance().equals(expectedBalance)));
        verifyNoMoreInteractions(bankAccountService);
    }

    @Test
    public void shouldThrowExceptionWhenAnyAccountDoesNotExistWhileTransferringMoney() {
        given(bankAccountService.getBankAccount(MOCK_IBAN)).willReturn(Optional.empty());

        Throwable throwable = catchThrowable(() -> transactionService.transferMoney(MOCK_AMOUNT, MOCK_IBAN, ANOTHER_IBAN));

        assertThat(throwable).isInstanceOf(BankAccountNotFoundException.class)
                .hasMessage("BankAccount not found, IBAN= " + MOCK_IBAN);
    }

    @Test
    public void shouldThrowExceptionWhenAnyAccountIsLockedWhileTransferringMoney() {
        final Account checkAccount = buildAccount(AccountType.CHECKING_ACCOUNT, MOCK_AMOUNT, MOCK_IBAN);
        final Account savingsAccount = buildAccount(AccountType.SAVINGS_ACCOUNT, MOCK_AMOUNT, ANOTHER_IBAN);
        checkAccount.setLocked(true);
        given(bankAccountService.getBankAccount(MOCK_IBAN)).willReturn(Optional.of(checkAccount));
        given(bankAccountService.getBankAccount(ANOTHER_IBAN)).willReturn(Optional.of(savingsAccount));

        Throwable throwable = catchThrowable(() -> transactionService.transferMoney(MOCK_AMOUNT, MOCK_IBAN, ANOTHER_IBAN));

        assertThat(throwable).isInstanceOf(BankAccountIsLockedException.class);
    }

    @Test
    public void shouldThrowExceptionWhenFromAccountHasInsufficientBalance() {
        final Account checkAccount = buildAccount(AccountType.CHECKING_ACCOUNT, LESSER_AMOUNT, MOCK_IBAN);
        final Account savingsAccount = buildAccount(AccountType.SAVINGS_ACCOUNT, MOCK_AMOUNT, ANOTHER_IBAN);
        given(bankAccountService.getBankAccount(MOCK_IBAN)).willReturn(Optional.of(checkAccount));
        given(bankAccountService.getBankAccount(ANOTHER_IBAN)).willReturn(Optional.of(savingsAccount));

        Throwable thrown = catchThrowable(()
                -> transactionService.transferMoney(MOCK_AMOUNT, checkAccount.getIBAN(), savingsAccount.getIBAN()));

        verify(bankAccountService).getBankAccount(MOCK_IBAN);
        verify(bankAccountService).getBankAccount(ANOTHER_IBAN);
        verifyNoMoreInteractions(bankAccountService);
        assertThat(thrown).isInstanceOf(InSufficientBalanceException.class);
    }

    @Test
    public void shouldTransferMoneyFromCheckingToSavingsAccount() {
        final Account checkAccount = buildAccount(AccountType.CHECKING_ACCOUNT, MOCK_AMOUNT, MOCK_IBAN);
        final Account savingsAccount = buildAccount(AccountType.SAVINGS_ACCOUNT, MOCK_AMOUNT, ANOTHER_IBAN);
        given(bankAccountService.getBankAccount(MOCK_IBAN)).willReturn(Optional.of(checkAccount));
        given(bankAccountService.getBankAccount(ANOTHER_IBAN)).willReturn(Optional.of(savingsAccount));
        Transaction debitTransaction = buildTransaction(MOCK_AMOUNT, TransactionType.DEBIT);
        Transaction creditTransaction = buildTransaction(MOCK_AMOUNT, TransactionType.CREDIT);

        transactionService.transferMoney(MOCK_AMOUNT, checkAccount.getIBAN(), savingsAccount.getIBAN());

        verify(bankAccountService).getBankAccount(MOCK_IBAN);
        verify(bankAccountService).getBankAccount(ANOTHER_IBAN);
        verify(bankAccountService, times(2)).saveAccount(any());
        verifyNoMoreInteractions(bankAccountService);

        assertThat(checkAccount.getBalance()).isEqualTo(MOCK_AMOUNT.subtract(MOCK_AMOUNT));
        assertThat(checkAccount.getTransactions()).first().isEqualToIgnoringGivenFields(debitTransaction, CREATED_AT);

        assertThat(savingsAccount.getBalance()).isEqualTo(MOCK_AMOUNT.add(MOCK_AMOUNT));
        assertThat(savingsAccount.getTransactions()).first().isEqualToIgnoringGivenFields(creditTransaction, CREATED_AT);
    }

    @Test
    public void shouldTransferMoneyFromSavingsToReferenceCheckingAccount() {
        final Account savingsAccount = buildAccount(AccountType.SAVINGS_ACCOUNT, MOCK_AMOUNT, ANOTHER_IBAN);
        final Account checkAccount = savingsAccount.getReferenceAccount().get();
        given(bankAccountService.getBankAccount(ANOTHER_IBAN)).willReturn(Optional.of(savingsAccount));
        given(bankAccountService.getBankAccount(checkAccount.getIBAN())).willReturn(Optional.of(checkAccount));
        Transaction debitTransaction = buildTransaction(MOCK_AMOUNT, TransactionType.DEBIT);
        Transaction creditTransaction = buildTransaction(MOCK_AMOUNT, TransactionType.CREDIT);

        transactionService.transferMoney(MOCK_AMOUNT, savingsAccount.getIBAN(), checkAccount.getIBAN());

        verify(bankAccountService).getBankAccount(ANOTHER_IBAN);
        verify(bankAccountService).getBankAccount(checkAccount.getIBAN());
        verify(bankAccountService, times(2)).saveAccount(any());
        verifyNoMoreInteractions(bankAccountService);

        assertThat(savingsAccount.getBalance()).isEqualTo(MOCK_AMOUNT.subtract(MOCK_AMOUNT));
        assertThat(savingsAccount.getTransactions()).first().isEqualToIgnoringGivenFields(debitTransaction, CREATED_AT);

        assertThat(checkAccount.getBalance()).isEqualTo(DEFAULT_AMOUNT.add(MOCK_AMOUNT));
        assertThat(checkAccount.getTransactions()).first().isEqualToIgnoringGivenFields(creditTransaction, CREATED_AT);
    }

    @Test
    public void shouldNotTransferMoneyFromSavingsToNonReferenceCheckingAccount() {
        final Account savingsAccount = buildAccount(AccountType.SAVINGS_ACCOUNT, MOCK_AMOUNT, ANOTHER_IBAN);
        final Account checkAccount = buildAccount(AccountType.CHECKING_ACCOUNT, LESSER_AMOUNT, IBAN);
        given(bankAccountService.getBankAccount(ANOTHER_IBAN)).willReturn(Optional.of(savingsAccount));
        given(bankAccountService.getBankAccount(IBAN)).willReturn(Optional.of(checkAccount));

        Throwable throwable = catchThrowable(()
                -> transactionService.transferMoney(MOCK_AMOUNT, savingsAccount.getIBAN(), checkAccount.getIBAN()));

        assertThat(throwable).isInstanceOf(UnsupportedTransferException.class)
                .hasMessage("Savings account can only send to reference checking account");

    }

    @Test
    public void shouldNotTransferMoneyFromPrivateLoanToAnyAccount() {
        final Account loanAccount = buildAccount(AccountType.PRIVATE_LOAN_ACCOUNT, MOCK_AMOUNT, ANOTHER_IBAN);
        final Account checkAccount = buildAccount(AccountType.CHECKING_ACCOUNT, LESSER_AMOUNT, IBAN);
        given(bankAccountService.getBankAccount(ANOTHER_IBAN)).willReturn(Optional.of(loanAccount));
        given(bankAccountService.getBankAccount(IBAN)).willReturn(Optional.of(checkAccount));

        Throwable throwable = catchThrowable(() -> transactionService.transferMoney(MOCK_AMOUNT, ANOTHER_IBAN, IBAN));

        assertThat(throwable).isInstanceOf(WithdrawalNotSupportedException.class)
                .hasMessage("Withdrawal not supported for fromAccount, IBAN= " + ANOTHER_IBAN);
    }

    @Test
    public void shouldTransferMoneyFromCheckingToPrivateLoanAccount() {
        final Account checkAccount = buildAccount(AccountType.CHECKING_ACCOUNT, MOCK_AMOUNT, ANOTHER_IBAN);
        final Account loanAccount = buildAccount(AccountType.PRIVATE_LOAN_ACCOUNT, DEFAULT_AMOUNT, MOCK_IBAN);
        given(bankAccountService.getBankAccount(ANOTHER_IBAN)).willReturn(Optional.of(checkAccount));
        given(bankAccountService.getBankAccount(MOCK_IBAN)).willReturn(Optional.of(loanAccount));
        Transaction debitTransaction = buildTransaction(MOCK_AMOUNT, TransactionType.DEBIT);
        Transaction creditTransaction = buildTransaction(MOCK_AMOUNT, TransactionType.CREDIT);

        transactionService.transferMoney(MOCK_AMOUNT, checkAccount.getIBAN(), loanAccount.getIBAN());

        verify(bankAccountService).getBankAccount(ANOTHER_IBAN);
        verify(bankAccountService).getBankAccount(MOCK_IBAN);
        verify(bankAccountService, times(2)).saveAccount(any());
        verifyNoMoreInteractions(bankAccountService);

        assertThat(checkAccount.getBalance()).isEqualTo(MOCK_AMOUNT.subtract(MOCK_AMOUNT));
        assertThat(checkAccount.getTransactions()).first().isEqualToIgnoringGivenFields(debitTransaction, CREATED_AT);

        assertThat(loanAccount.getBalance()).isEqualTo(DEFAULT_AMOUNT.add(MOCK_AMOUNT));
        assertThat(loanAccount.getTransactions()).first().isEqualToIgnoringGivenFields(creditTransaction, CREATED_AT);
    }

    private Transaction buildTransaction(BigDecimal amount, TransactionType type) {
        return Transaction.builder()
                .amount(amount)
                .transactionType(type)
                .createdAt(clock.instant())
                .build();
    }

    private Account buildAccount(final AccountType accountType, BigDecimal amount, final String IBAN) {
        Account referenceAccount = null;
        if (accountType.equals(AccountType.SAVINGS_ACCOUNT)) {
            referenceAccount = Account.builder()
                    .accountType(AccountType.CHECKING_ACCOUNT)
                    .balance(DEFAULT_AMOUNT)
                    .IBAN(MOCK_IBAN)
                    .createdAt(clock.instant())
                    .updatedAt(clock.instant())
                    .build();
        }
        return Account.builder()
                .accountType(accountType)
                .balance(amount)
                .referenceAccount(referenceAccount)
                .IBAN(IBAN)
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .build();
    }

}
