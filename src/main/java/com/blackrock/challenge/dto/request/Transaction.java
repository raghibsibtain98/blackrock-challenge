package com.blackrock.challenge.dto.request;

import java.math.BigDecimal;

public record Transaction(
        String date,
        BigDecimal amount
) {}
