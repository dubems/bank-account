package com.codefactory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Account is already locked")
public class AccountAlreadyLockedException extends RuntimeException {
    public AccountAlreadyLockedException(String s) {
        super(s);
    }
}
