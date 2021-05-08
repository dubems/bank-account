package com.codefactory.controller.dto;

import com.codefactory.domain.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDto {

    private Set<Account> accounts;
}
