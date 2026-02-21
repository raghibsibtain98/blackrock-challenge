package com.blackrock.challenge.dto.response;

import java.util.List;

public record ValidatorResponse(
        List<ValidTransaction> valid,
        List<InvalidTransaction> invalid
) {}