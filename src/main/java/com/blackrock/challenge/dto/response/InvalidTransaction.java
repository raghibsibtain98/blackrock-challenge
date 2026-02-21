package com.blackrock.challenge.dto.response;

import java.math.BigDecimal;

public record InvalidTransaction(
        String date,
        BigDecimal amount,
        BigDecimal ceiling,
        BigDecimal remnant,
        String message
) {}