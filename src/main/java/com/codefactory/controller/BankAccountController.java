package com.codefactory.controller;

import com.codefactory.controller.dto.*;
import com.codefactory.domain.entity.AccountType;
import com.codefactory.service.BankAccountService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1")
public class BankAccountController {

    private final static String ACCOUNT_ENDPOINT = "/account";
    private final static String LOCK_ACCOUNT_ENDPOINT = "/account/lock";
    private final static String ACCOUNT_BALANCE_ENDPOINT = "/account/balance";

    private final BankAccountService bankAccountService;

    @ApiOperation(value = "Create Bank Account")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "created the account successfully")
    })
    @PostMapping(value = ACCOUNT_ENDPOINT)
    @ResponseStatus(value = HttpStatus.CREATED)
    public CreateAccountResponseDto createAccount(@RequestBody @Valid final AccountTypeRequestDto accountTypeDto) {
        return CreateAccountResponseDto.builder()
                .IBAN(bankAccountService.createAccount(AccountType.of(accountTypeDto.getAccountType())))
                .build();
    }

    @ApiOperation(value = "Filter Accounts by accountTypes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "filtered accounts by accountTypes successfully")
    })
    @GetMapping(value = ACCOUNT_ENDPOINT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public AccountResponseDto filterAccounts(@RequestParam(value = "accountTypes") final Set<AccountTypeDto> accountTypes) {
        final Set<AccountType> types = accountTypes.stream().map(AccountType::of).collect(Collectors.toSet());
        return AccountResponseDto.builder()
                .accounts(bankAccountService.filterAccountsBy(types))
                .build();
    }

    @ApiOperation(value = "Get Account Balance for an IBAN")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Account balance returned successfully"),
            @ApiResponse(code = 404, message = "Bank Account with IBAN is not found"),
    })
    @GetMapping(value = ACCOUNT_BALANCE_ENDPOINT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public AccountBalanceResponseDto getAccountBalance(@RequestParam(value = "iban") final String IBAN) {
        return AccountBalanceResponseDto.builder()
                .balance(bankAccountService.getAccountBalance(IBAN))
                .build();
    }

    @ApiOperation(value = "Lock Bank Account")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Bank account locked successfully"),
            @ApiResponse(code = 404, message = "Bank Account with IBAN not found"),
            @ApiResponse(code = 409, message = "Bank Account is already locked"),
    })
    @PostMapping(value = LOCK_ACCOUNT_ENDPOINT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public void lockAccount(@RequestBody @Valid final LockAccountRequestDto lockAccountRequestDto) {
        bankAccountService.lockAccount(lockAccountRequestDto.getIBAN());
    }

    @ApiOperation(value = "Unlock Bank Account")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Bank Account Unlocked successfully"),
            @ApiResponse(code = 404, message = "Bank Account with IBAN not found"),
            @ApiResponse(code = 409, message = "Bank Account is not locked"),
    })
    @PutMapping(value = LOCK_ACCOUNT_ENDPOINT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public void unlockAccount(@RequestBody @Valid final UnlockAccountRequestDto unlockAccountRequestDto) {
        bankAccountService.unlockAccount(unlockAccountRequestDto.getIBAN());
    }
}
