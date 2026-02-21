package com.blackrock.challenge.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QPeriod(
        String start,
        String end,
        BigDecimal fixedRemnant
) {}
