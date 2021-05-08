package com.codefactory;

import com.codefactory.controller.dto.*;
import com.codefactory.domain.entity.Account;
import com.codefactory.domain.entity.AccountType;
import com.codefactory.domain.entity.Transaction;
import com.codefactory.domain.entity.TransactionType;
import com.codefactory.domain.repository.AccountRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BankAccountToyApplicationTests {


    private final static String BASE_PATH = "/api/v1";
    private final static String ACCOUNT_ENDPOINT = "/account";
    private final static String LOCK_ACCOUNT_ENDPOINT = "/account/lock";
    private final static String ACCOUNT_BALANCE_ENDPOINT = "/account/balance";
    private final static String TRANSACTION_ENDPOINT = "/transaction";
    private final static String TRANSFER_ENDPOINT = "/transaction/transfer";
    private final static String DEPOSIT_ENDPOINT = "/transaction/deposit";
    private final static BigDecimal amount = BigDecimal.valueOf(78000);


    @LocalServerPort
    private int port;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        RestAssured.basePath = BASE_PATH;
        accountRepository.deleteAllAccounts();
    }

    @Test
    public void shouldCreateAccount() {
        given().accept(ContentType.JSON).contentType(ContentType.JSON)
                .when()
                .body(AccountTypeRequestDto.builder().accountType(AccountTypeDto.CHECKING).build())
                .post(ACCOUNT_ENDPOINT)
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    public void shouldFilterAccounts() {
        Set<AccountTypeDto> accountTypes = Arrays.stream(AccountTypeDto.values()).collect(Collectors.toSet());
        accountTypes.forEach(this::createAccount);

        AccountResponseDto responseDto = given().accept(ContentType.JSON).contentType(ContentType.JSON).when()
                .urlEncodingEnabled(false)
                .get(ACCOUNT_ENDPOINT + "?accountTypes=" + AccountTypeDto.PRIVATE_LOAN + "," + AccountTypeDto.SAVINGS)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(AccountResponseDto.class);

        assertThat(responseDto.getAccounts().size()).isEqualTo(2);
        assertThat(responseDto.getAccounts().stream().map(Account::getAccountType))
                .containsExactlyInAnyOrder(AccountType.PRIVATE_LOAN_ACCOUNT, AccountType.SAVINGS_ACCOUNT);
    }

    @Test
    public void shouldGetAccountBalance() {
        final String IBAN = createAccount(AccountTypeDto.CHECKING);

        AccountBalanceResponseDto responseDto = given().accept(ContentType.JSON).contentType(ContentType.JSON).when()
                .get(ACCOUNT_BALANCE_ENDPOINT + "?iban=" + IBAN)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(AccountBalanceResponseDto.class);

        assertThat(responseDto.getBalance().equals(BigDecimal.valueOf(0.00)));
    }

    @Test
    public void shouldLockAccount() {
        final String IBAN = createAccount(AccountTypeDto.SAVINGS);

        given().accept(ContentType.JSON).contentType(ContentType.JSON)
                .when()
                .body(LockAccountRequestDto.builder().IBAN(IBAN).build())
                .post(LOCK_ACCOUNT_ENDPOINT)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void shouldUnLockAccount() {
        final String IBAN = createAccount(AccountTypeDto.SAVINGS);
        lockAccount(IBAN);

        given().accept(ContentType.JSON).contentType(ContentType.JSON).when()
                .body(UnlockAccountRequestDto.builder().IBAN(IBAN).build())
                .put(LOCK_ACCOUNT_ENDPOINT)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void shouldDepositMoney() {
        final String IBAN = createAccount(AccountTypeDto.PRIVATE_LOAN);

        given().accept(ContentType.JSON).contentType(ContentType.JSON)
                .when()
                .body(DepositRequestDto.builder().IBAN(IBAN).amount(amount).build())
                .post(DEPOSIT_ENDPOINT)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void shouldTransferMoney() {
        final String fromIban = createAccount(AccountTypeDto.CHECKING);
        final String toIban = createAccount(AccountTypeDto.PRIVATE_LOAN);
        depositMoney(fromIban);

        given().accept(ContentType.JSON).contentType(ContentType.JSON)
                .when()
                .body(TransferRequestDto.builder().fromIBAN(fromIban).toIBAN(toIban).amount(amount).build())
                .post(TRANSFER_ENDPOINT)
                .then()
                .statusCode(HttpStatus.OK.value());

    }

    @Test
    public void shouldGetTransactionHistory() {
        final String iban = createAccount(AccountTypeDto.CHECKING);
        depositMoney(iban);

        final TransactionHistoryResponseDto responseDto = given().accept(ContentType.JSON).contentType(ContentType.JSON)
                .when()
                .get(TRANSACTION_ENDPOINT + "?iban=" + iban)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(TransactionHistoryResponseDto.class);

        Transaction expectedTransaction = Transaction.builder()
                .amount(amount)
                .transactionType(TransactionType.CREDIT)
                .build();
        assertThat(responseDto.getTransactionHistory().size()).isEqualTo(1);
        assertThat(responseDto.getTransactionHistory()).first().isEqualToIgnoringGivenFields(expectedTransaction, "createdAt");
    }

    private String createAccount(final AccountTypeDto accountType) {
        return given().accept(ContentType.JSON).contentType(ContentType.JSON)
                .when()
                .body(AccountTypeRequestDto.builder().accountType(accountType).build())
                .post(ACCOUNT_ENDPOINT)
                .then()
                .extract()
                .path("iban");
    }

    private void depositMoney(final String IBAN) {
        given().accept(ContentType.JSON).contentType(ContentType.JSON)
                .when()
                .body(DepositRequestDto.builder().IBAN(IBAN).amount(amount).build())
                .post(DEPOSIT_ENDPOINT)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    private void lockAccount(final String IBAN) {
        given().accept(ContentType.JSON).contentType(ContentType.JSON)
                .when()
                .body(LockAccountRequestDto.builder().IBAN(IBAN).build())
                .post(LOCK_ACCOUNT_ENDPOINT)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

}
