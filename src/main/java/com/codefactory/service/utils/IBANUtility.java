package com.codefactory.service.utils;

import lombok.experimental.UtilityClass;
import org.iban4j.CountryCode;
import org.iban4j.Iban;

@UtilityClass
public class IBANUtility {

    private static final String BANK_CODE = "12345123";

    public String generateIBAN() {
        return new Iban.Builder()
                .countryCode(CountryCode.DE)
                .bankCode(BANK_CODE)
                .buildRandom()
                .toString();
    }
}
