package com.blackrock.challenge.service;

import com.blackrock.challenge.dto.KPeriod;
import com.blackrock.challenge.dto.PPeriod;
import com.blackrock.challenge.dto.QPeriod;
import com.blackrock.challenge.dto.request.ParsedTransaction;
import com.blackrock.challenge.dto.response.ProcessedTransaction;
import com.blackrock.challenge.rule.aggregator.KPeriodAggregator;
import com.blackrock.challenge.rule.strategy.PPeriodRuleStrategy;
import com.blackrock.challenge.rule.strategy.QPeriodRuleStrategy;
import com.blackrock.challenge.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class TemporalRuleService {

    private final QPeriodRuleStrategy qStrategy;
    private final PPeriodRuleStrategy pStrategy;
    private final KPeriodAggregator kAggregator;

    public EngineResult process(
            List<ParsedTransaction> parsedTransactions,
            List<QPeriod> q,
            List<PPeriod> p,
            List<KPeriod> k
    ) {

        List<ParsedTransaction> afterQ = qStrategy.apply(parsedTransactions, q);
        List<ParsedTransaction> afterP = pStrategy.apply(afterQ, p);

        List<ProcessedTransaction> finalTx = new ArrayList<>();

        BigDecimal totalTransactionAmount = BigDecimal.ZERO;
        BigDecimal totalCeiling = BigDecimal.ZERO;

        Map<KPeriod, BigDecimal> kTotals = new LinkedHashMap<>();

        for (KPeriod kp : k) {
            kTotals.put(kp, BigDecimal.ZERO);
        }

        for (ParsedTransaction tx : afterP) {

            LocalDateTime date = DateUtils.parse(tx.date());

            boolean isInK = kAggregator.isInK(date, k);

            ProcessedTransaction processed = new ProcessedTransaction(
                            tx.date(),
                            tx.amount(),
                            tx.ceiling(),
                            tx.remnant(),
                            isInK);

            finalTx.add(processed);

            // aggregate totals
            totalTransactionAmount =
                    totalTransactionAmount.add(tx.amount());

            totalCeiling =
                    totalCeiling.add(tx.ceiling());

            // aggregate K totals
            for (KPeriod kp : k) {
                if (DateUtils.isWithin(date, DateUtils.parse(kp.start()), DateUtils.parse(kp.end()))) {
                    kTotals.put(
                            kp,
                            kTotals.get(kp).add(tx.remnant())
                    );
                }
            }
        }

        return new EngineResult(
                finalTx,
                totalTransactionAmount,
                totalCeiling,
                kTotals
        );
    }

    /*public EngineResult process(
            List<ParsedTransaction> parsedTransactions,
            List<QPeriod> q,
            List<PPeriod> p,
            List<KPeriod> k
    ) {


        List<ParsedTransaction> afterQ = qStrategy.apply(parsedTransactions, q);


        List<ParsedTransaction> afterP = pStrategy.apply(afterQ, p);


        List<ProcessedTransaction> finalTx = new ArrayList<>();

        for (ParsedTransaction tx : afterP) {
            LocalDateTime date = DateUtils.parse(tx.date());

            boolean isInk = kAggregator.isInK(date, k);

            finalTx.add(new ProcessedTransaction(
                    tx.date(),
                    tx.amount(),
                    tx.ceiling(),
                    tx.remnant(),
                    isInk)
            );
        }

        return new EngineResult(finalTx);
    }*/

    public record EngineResult(
            List<ProcessedTransaction> transactions,
            BigDecimal totalTransactionAmount,
            BigDecimal totalCeiling,
            Map<KPeriod, BigDecimal> kTotals
    ) {
        public EngineResult(List<ProcessedTransaction> transactions) {
            this(transactions, BigDecimal.ZERO, BigDecimal.ZERO, Map.of());
        }
    }


    public InvestmentEngineResult processForInvestment(
            List<ParsedTransaction> parsedTransactions,
            List<QPeriod> q,
            List<PPeriod> p,
            List<KPeriod> k
    ) {

        // Apply Q and P exactly like filter
        List<ParsedTransaction> afterQ = qStrategy.apply(parsedTransactions, q);
        List<ParsedTransaction> afterP = pStrategy.apply(afterQ, p);

        BigDecimal totalTransactionAmount = BigDecimal.ZERO;
        BigDecimal totalCeiling = BigDecimal.ZERO;

        Map<KPeriod, BigDecimal> kTotals = new LinkedHashMap<>();

        for (KPeriod kp : k) {
            kTotals.put(kp, BigDecimal.ZERO);
        }

        for (ParsedTransaction tx : afterP) {

            LocalDateTime date = DateUtils.parse(tx.date());

            totalTransactionAmount =
                    totalTransactionAmount.add(tx.amount());

            totalCeiling =
                    totalCeiling.add(tx.ceiling());

            for (KPeriod kp : k) {
                if (DateUtils.isWithin(
                        date,
                        DateUtils.parse(kp.start()),
                        DateUtils.parse(kp.end()))
                ) {
                    kTotals.put(
                            kp,
                            kTotals.get(kp).add(tx.remnant())
                    );
                }
            }
        }

        return new InvestmentEngineResult(
                totalTransactionAmount,
                totalCeiling,
                kTotals
        );
    }

    public record InvestmentEngineResult(
            BigDecimal totalTransactionAmount,
            BigDecimal totalCeiling,
            Map<KPeriod, BigDecimal> kTotals
    ) {}

}