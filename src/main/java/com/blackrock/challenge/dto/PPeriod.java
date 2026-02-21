package com.blackrock.challenge.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * P-Period definition: within [start, end], add `extra` to remnant.
 * All matching P-periods are additive — no override, no priority.
 */
public record PPeriod(
        String start,
        String end,
        BigDecimal extra
) {}
