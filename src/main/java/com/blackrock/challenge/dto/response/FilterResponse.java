package com.blackrock.challenge.dto.response;

import java.util.List;

public record FilterResponse(
        List<ProcessedTransaction> valid,
        List<InvalidTransactionFilter> invalid
) {}