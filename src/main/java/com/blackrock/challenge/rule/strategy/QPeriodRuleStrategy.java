package com.blackrock.challenge.rule.strategy;

import com.blackrock.challenge.dto.QPeriod;
import com.blackrock.challenge.dto.request.ParsedTransaction;
import com.blackrock.challenge.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Q-Period Rule Strategy
 * Rule: If tx.date ∈ q-period → remnant = fixedRemnant (override)
 * Conflict resolution: if multiple Q-periods overlap, the one with
 * the LATEST start date takes priority.
 * Algorithm: Sort Q-periods by start DESC → findFirst() per transaction
 * Complexity: O(n log q)
 */
@Slf4j
@Component
public class QPeriodRuleStrategy implements PeriodRuleStrategy<QPeriod> {

    @Override
    public List<ParsedTransaction> apply(List<ParsedTransaction> parsedTransactions,
                                         List<QPeriod> periods) {

        if (periods == null || periods.isEmpty()) return parsedTransactions;

        List<QPeriod> sorted = periods.stream().sorted(Comparator.comparing(QPeriod::start)).toList();

        PriorityQueue<QPeriod> active = new PriorityQueue<>((a,b) -> b.start().compareTo(a.start()));

        int index = 0;
        List<ParsedTransaction> result = new ArrayList<>();

        for (ParsedTransaction tx : parsedTransactions) {

            LocalDateTime date = DateUtils.parse(tx.date());

            while (index < sorted.size() && !DateUtils.parse(sorted.get(index).start()).isAfter(date)) {
                active.add(sorted.get(index++));
            }

            while (!active.isEmpty() && DateUtils.parse(active.peek().end()).isBefore(date)) {
                active.poll();
            }

            if (!active.isEmpty()) {
                result.add(tx.withRemnant(active.peek().fixedRemnant()));
            } else {
                result.add(tx);
            }
        }

        return result;
    }
}