package com.blackrock.challenge.dto.request;


import com.blackrock.challenge.dto.KPeriod;
import com.blackrock.challenge.dto.PPeriod;
import com.blackrock.challenge.dto.QPeriod;

import java.math.BigDecimal;
import java.util.List;

public record FilterRequest(

        List<QPeriod> q,
        List<PPeriod> p,
        List<KPeriod> k,
        BigDecimal wage,
        List<Transaction> transactions
) {}