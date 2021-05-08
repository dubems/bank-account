package com.codefactory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Account is not locked and cannot be unlocked")
public class AccountNotLockedException extends RuntimeException {
    public AccountNotLockedException(String s) {
    }
}
