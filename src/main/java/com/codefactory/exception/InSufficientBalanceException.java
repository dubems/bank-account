package com.codefactory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Account has Insufficient balance")
public class InSufficientBalanceException extends RuntimeException {
    public InSufficientBalanceException(String s) {
        super(s);
    }

}
