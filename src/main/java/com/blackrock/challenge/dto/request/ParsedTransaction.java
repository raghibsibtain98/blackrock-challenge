package com.blackrock.challenge.dto.request;

import java.math.BigDecimal;

public record ParsedTransaction(
        String date,
        BigDecimal amount,
        BigDecimal ceiling,
        BigDecimal remnant
) {
    public ParsedTransaction withRemnant(BigDecimal newRemnant) {
        return new ParsedTransaction(date, amount, ceiling, newRemnant);
    }
}