package com.codefactory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN,  reason = "Bank account is locked")
public class BankAccountIsLockedException extends RuntimeException {
    public BankAccountIsLockedException(String message) {
        super(message);
    }
}
