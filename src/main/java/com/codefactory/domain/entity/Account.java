package com.codefactory.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Builder
@Getter
@Setter
public class Account {

    @JsonProperty("iban")
    private final String IBAN;
    private BigDecimal balance;
    private final AccountType accountType;
    @JsonIgnore
    private Account referenceAccount;
    private final Instant createdAt;
    private Instant updatedAt;
    @Builder.Default
    private boolean isLocked = false;
    @Builder.Default
    @JsonIgnore
    private Set<Transaction> transactions = new HashSet<>();

    public Optional<Account> getReferenceAccount() {
        return Optional.ofNullable(referenceAccount);
    }
}
