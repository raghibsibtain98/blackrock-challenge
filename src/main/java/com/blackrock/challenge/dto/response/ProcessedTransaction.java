package com.blackrock.challenge.dto.response;

import java.math.BigDecimal;

public record ProcessedTransaction(
        String date,
        BigDecimal amount,
        BigDecimal ceiling,
        BigDecimal remnant,
        boolean inkPeriod
) {}
