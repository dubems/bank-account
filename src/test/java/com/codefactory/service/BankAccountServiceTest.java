package com.codefactory.service;

import com.codefactory.domain.entity.Account;
import com.codefactory.domain.entity.AccountType;
import com.codefactory.domain.repository.AccountRepository;
import com.codefactory.exception.AccountAlreadyLockedException;
import com.codefactory.exception.AccountNotLockedException;
import com.codefactory.exception.BankAccountNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankAccountServiceTest {

    private final static String MOCK_IBAN = "DE80801817944420161050";

    @Mock
    private Clock clock;
    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private BankAccountService bankAccountService;

    @ParameterizedTest
    @EnumSource(AccountType.class)
    public void shouldCreateTheRightAccountWhenAccountTypeIsGiven(final AccountType accountType) {
        given(accountRepository.saveAccount(any())).willReturn(MOCK_IBAN);

        bankAccountService.createAccount(accountType);

        if (accountType.equals(AccountType.SAVINGS_ACCOUNT)) {
            InOrder inOrder = inOrder(accountRepository);
            inOrder.verify(accountRepository).saveAccount(argThat(a -> a.getAccountType().equals(AccountType.CHECKING_ACCOUNT)));
            inOrder.verify(accountRepository).saveAccount(argThat(a -> a.getAccountType().equals(accountType)));
            verify(accountRepository, times(2)).getAllBankAccounts();
        } else {
            verify(accountRepository).saveAccount(argThat(a -> a.getAccountType().equals(accountType)));
            verify(accountRepository).getAllBankAccounts();

        }
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void shouldThrowExceptionWhenAccountDoesNotExistWhileGettingAccountBalance() {
        given(accountRepository.getBankAccount(MOCK_IBAN)).willReturn(Optional.empty());

        Throwable throwable = catchThrowable(() -> bankAccountService.getAccountBalance(MOCK_IBAN));

        assertThat(throwable).isInstanceOf(BankAccountNotFoundException.class)
                .hasMessage("BankAccount was not found, IBAN=" + MOCK_IBAN);
    }

    @Test
    public void shouldThrowExceptionWhenAccountDoesNotExistWhileLockingAccount() {
        given(accountRepository.getBankAccount(MOCK_IBAN)).willReturn(Optional.empty());

        Throwable throwable = catchThrowable(() -> bankAccountService.lockAccount(MOCK_IBAN));

        assertThat(throwable).isInstanceOf(BankAccountNotFoundException.class)
                .hasMessage("BankAccount was not found");
    }

    @Test
    public void shouldThrowExceptionWhenAccountIsAlreadyLockedWhileLockingAccount() {
        Account account = buildAccount(AccountType.SAVINGS_ACCOUNT, BigDecimal.valueOf(1000));
        account.setLocked(true);
        given(accountRepository.getBankAccount(MOCK_IBAN)).willReturn(Optional.of(account));

        Throwable throwable = catchThrowable(() -> bankAccountService.lockAccount(MOCK_IBAN));
        assertThat(throwable).isInstanceOf(AccountAlreadyLockedException.class);
    }

    @Test
    public void shouldLockAccountWhenAccountIsNotLocked() {
        Account account = buildAccount(AccountType.SAVINGS_ACCOUNT, BigDecimal.valueOf(1000));
        given(accountRepository.getBankAccount(MOCK_IBAN)).willReturn(Optional.of(account));

        bankAccountService.lockAccount(MOCK_IBAN);

        verify(accountRepository).saveAccount(argThat(Account::isLocked));
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void shouldThrowExceptionWhenAccountDoesNotExistWhileUnLockingAccount() {
        given(accountRepository.getBankAccount(MOCK_IBAN)).willReturn(Optional.empty());

        Throwable throwable = catchThrowable(() -> bankAccountService.unlockAccount(MOCK_IBAN));

        assertThat(throwable).isInstanceOf(BankAccountNotFoundException.class)
                .hasMessage("BankAccount was not found");
    }

    @Test
    public void shouldThrowExceptionWhenAccountIsNotLockedWhileUnlockingAccount() {
        Account account = buildAccount(AccountType.SAVINGS_ACCOUNT, BigDecimal.valueOf(1000));
        given(accountRepository.getBankAccount(MOCK_IBAN)).willReturn(Optional.of(account));

        Throwable throwable = catchThrowable(() -> bankAccountService.unlockAccount(MOCK_IBAN));
        assertThat(throwable).isInstanceOf(AccountNotLockedException.class);
    }

    @Test
    public void shouldUnlockAccountWhenAccountIsLocked() {
        Account account = buildAccount(AccountType.CHECKING_ACCOUNT, BigDecimal.valueOf(1000));
        account.setLocked(true);
        given(accountRepository.getBankAccount(MOCK_IBAN)).willReturn(Optional.of(account));

        bankAccountService.unlockAccount(MOCK_IBAN);

        verify(accountRepository).saveAccount(argThat(x -> !x.isLocked()));
        verifyNoMoreInteractions(accountRepository);
    }

    private Account buildAccount(final AccountType accountType, BigDecimal amount) {
        return Account.builder()
                .accountType(accountType)
                .balance(amount)
                .IBAN(MOCK_IBAN)
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .build();
    }
}
