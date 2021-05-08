package com.codefactory.controller.dto;

import com.codefactory.domain.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistoryResponseDto {

    private Set<Transaction> transactionHistory;
}
