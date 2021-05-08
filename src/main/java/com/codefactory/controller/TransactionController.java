package com.codefactory.controller;

import com.codefactory.controller.dto.DepositRequestDto;
import com.codefactory.controller.dto.TransactionHistoryResponseDto;
import com.codefactory.controller.dto.TransferRequestDto;
import com.codefactory.service.TransactionService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1")
public class TransactionController {

    private final static String TRANSACTION_ENDPOINT = "/transaction";
    private final static String TRANSFER_ENDPOINT = "/transaction/transfer";
    private final static String DEPOSIT_ENDPOINT = "/transaction/deposit";

    private final TransactionService transactionService;

    @ApiOperation(value = "Deposit money into an account")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Money deposited successfully"),
            @ApiResponse(code = 403, message = "Bank Account is locked"),
            @ApiResponse(code = 404, message = "Bank Account with IBAN not found"),
    })
    @PostMapping(value = DEPOSIT_ENDPOINT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public void depositMoney(@RequestBody @Valid final DepositRequestDto depositRequestDto) {
        transactionService.creditAccount(depositRequestDto.getAmount(), depositRequestDto.getIBAN());
    }

    @ApiOperation(value = "Transfer money from one account to another")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Money transferred successfully"),
            @ApiResponse(code = 400, message = "Account has insufficient balance"),
            @ApiResponse(code = 403, message = "Bank Account is locked"),
            @ApiResponse(code = 403, message = "Savings account can only send to reference checking account"),
            @ApiResponse(code = 404, message = "Bank Account with IBAN not found"),
            @ApiResponse(code = 406, message = "Withdrawal not supported for bank account"),
    })
    @PostMapping(value = TRANSFER_ENDPOINT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public void transferMoney(@RequestBody @Valid final TransferRequestDto dto) {
        transactionService.transferMoney(dto.getAmount(), dto.getFromIBAN(), dto.getToIBAN());
    }

    @ApiOperation(value = "Get transaction history")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Transaction history returned successfully"),
            @ApiResponse(code = 404, message = "Bank Account with IBAN not found"),
    })
    @GetMapping(value = TRANSACTION_ENDPOINT, produces = APPLICATION_JSON_VALUE)
    public TransactionHistoryResponseDto getTransactionHistory(@RequestParam("iban") final String IBAN) {
        return TransactionHistoryResponseDto.builder()
                .transactionHistory(transactionService.getTransactionHistory(IBAN))
                .build();
    }
}
