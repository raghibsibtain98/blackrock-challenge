package com.blackrock.challenge.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record InvestmentsResponse(
        BigDecimal totalTransactionAmount,
        BigDecimal totalCeiling,
        List<SavingsByDate> savingsByDates
) {}
