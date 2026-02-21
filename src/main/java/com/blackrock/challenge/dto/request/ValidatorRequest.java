package com.blackrock.challenge.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record ValidatorRequest(
        BigDecimal wage,
        List<ParsedTransaction> parsedTransactions
) {}