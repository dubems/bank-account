package com.codefactory.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Builder
@Getter
public class DepositRequestDto {
    @NotNull
    private final BigDecimal amount;
    @NotNull
    @JsonProperty(value = "iban")
    private final String IBAN;
}
