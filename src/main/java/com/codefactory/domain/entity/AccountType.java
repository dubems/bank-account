package com.codefactory.domain.entity;

import com.codefactory.controller.dto.AccountTypeDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountType {

    CHECKING_ACCOUNT(true, "ANY"),
    SAVINGS_ACCOUNT(true, "REFERENCE"),
    PRIVATE_LOAN_ACCOUNT(false, null);

    private final boolean withdrawAble;

    private final String transferTo;

    public static AccountType of(AccountTypeDto accountTypeDto) {
        switch (accountTypeDto) {
            case SAVINGS:
                return AccountType.SAVINGS_ACCOUNT;
            case CHECKING:
                return AccountType.CHECKING_ACCOUNT;
            case PRIVATE_LOAN:
                return AccountType.PRIVATE_LOAN_ACCOUNT;
            default:
                throw new IllegalArgumentException("AccountType should be either of SAVINGS, CHECKING or PRIVATE_LOAN");
        }
    }

}
