package com.codefactory.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Builder
@Getter
public class TransferRequestDto {

    @NotNull
    @JsonProperty(value = "iban")
    private final String toIBAN;
    @NotNull
    @JsonProperty(value = "fromIban")
    private final String fromIBAN;
    @NotNull
    private final BigDecimal amount;
}
