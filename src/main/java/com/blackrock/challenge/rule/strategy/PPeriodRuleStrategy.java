package com.blackrock.challenge.rule.strategy;

import com.blackrock.challenge.dto.PPeriod;
import com.blackrock.challenge.dto.request.ParsedTransaction;
import com.blackrock.challenge.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;
import java.util.*;

/**
 * P-Period Rule Strategy
 * Rule: If tx.date ∈ p-period → remnant += extra (additive, all matches)
 * Unlike Q: ALL overlapping P-periods stack. No override.
 * Algorithm: Sweep line via TreeMap events
 *   - On period start: events[start] += extra
 *   - On period end+1: events[end+1] -= extra
 *   - Per transaction: prefix-sum of events up to tx.date = active extra
 * Complexity: O(p log p) to build + O(n log p) to query = O((n+p) log p)
 */
@Slf4j
@Component
public class PPeriodRuleStrategy implements PeriodRuleStrategy<PPeriod> {

    @Override
    public List<ParsedTransaction> apply(List<ParsedTransaction> parsedTransactions,
                                         List<PPeriod> periods) {

        if (periods == null || periods.isEmpty()) return parsedTransactions;

        List<PPeriod> sorted = periods.stream()
                .sorted(Comparator.comparing(PPeriod::start))
                .toList();

        PriorityQueue<PPeriod> active =
                new PriorityQueue<>(Comparator.comparing(PPeriod::end));

        BigDecimal activeExtra = BigDecimal.ZERO;
        int index = 0;

        List<ParsedTransaction> result = new ArrayList<>();

        for (ParsedTransaction tx : parsedTransactions) {

            LocalDateTime date = DateUtils.parse(tx.date());

            while (index < sorted.size() && !DateUtils.parse(sorted.get(index).start()).isAfter(date)) {
                PPeriod p = sorted.get(index++);
                active.add(p);
                activeExtra = activeExtra.add(p.extra());
            }

            while (!active.isEmpty() && DateUtils.parse(active.peek().end()).isBefore(date)) {
                activeExtra = activeExtra.subtract(active.poll().extra());
            }

            if (activeExtra.compareTo(BigDecimal.ZERO) != 0) {
                result.add(tx.withRemnant(tx.remnant().add(activeExtra)));
            } else {
                result.add(tx);
            }
        }

        return result;
    }
}