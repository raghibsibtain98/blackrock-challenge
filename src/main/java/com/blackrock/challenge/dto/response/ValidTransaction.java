package com.blackrock.challenge.dto.response;

import java.math.BigDecimal;

public record ValidTransaction(
        String date,
        BigDecimal amount,
        BigDecimal ceiling,
        BigDecimal remnant
) {}