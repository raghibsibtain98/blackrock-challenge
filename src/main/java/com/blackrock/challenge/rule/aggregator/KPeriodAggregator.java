package com.blackrock.challenge.rule.aggregator;

import com.blackrock.challenge.dto.KPeriod;
import com.blackrock.challenge.utils.DateUtils;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;
import java.util.*;

@Component
public class KPeriodAggregator {

    public boolean isInK(LocalDateTime date, List<KPeriod> periods) {
        for (KPeriod k : periods) {
            if (!date.isBefore(DateUtils.parse(k.start())) &&
                    !date.isAfter(DateUtils.parse(k.end()))) {
                return true;
            }
        }
        return false;
    }
}