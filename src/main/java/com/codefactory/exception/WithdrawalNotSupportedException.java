package com.codefactory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE, reason = "Withdrawal not supported for fromAccount")
public class WithdrawalNotSupportedException extends RuntimeException {
    public WithdrawalNotSupportedException(String s) {
        super(s);
    }
}
