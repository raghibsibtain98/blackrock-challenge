package com.blackrock.challenge.dto;

import java.math.BigDecimal;

public record QPeriod(
        String start,
        String end,
        BigDecimal fixed
) {}
