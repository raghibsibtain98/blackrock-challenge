package com.blackrock.challenge.dto.response;

import java.math.BigDecimal;

public record SavingsByDate(
        String start,
        String end,
        BigDecimal amount,
        BigDecimal profit,
        BigDecimal taxBenefit
) {}