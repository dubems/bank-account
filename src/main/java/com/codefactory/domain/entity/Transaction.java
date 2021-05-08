package com.codefactory.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class Transaction {

    private final BigDecimal amount;
    private final TransactionType transactionType;
    private final Instant createdAt;
}
