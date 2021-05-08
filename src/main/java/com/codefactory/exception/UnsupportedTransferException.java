package com.codefactory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Savings account can only send to reference checking account")
public class UnsupportedTransferException extends RuntimeException {
    public UnsupportedTransferException(String s) {
        super(s);
    }
}
