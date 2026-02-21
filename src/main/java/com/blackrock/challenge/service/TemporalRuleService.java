package com.blackrock.challenge.service;

import com.blackrock.challenge.dto.KPeriod;
import com.blackrock.challenge.dto.PPeriod;
import com.blackrock.challenge.dto.QPeriod;
import com.blackrock.challenge.dto.request.ParsedTransaction;
import com.blackrock.challenge.dto.request.Transaction;
import com.blackrock.challenge.dto.response.InvalidTransactionFilter;
import com.blackrock.challenge.dto.response.ProcessedTransaction;
import com.blackrock.challenge.rule.aggregator.KPeriodAggregator;
import com.blackrock.challenge.rule.strategy.PPeriodRuleStrategy;
import com.blackrock.challenge.rule.strategy.QPeriodRuleStrategy;
import com.blackrock.challenge.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

import static com.blackrock.challenge.constants.FinancialConstants.HUNDRED;
import static com.blackrock.challenge.constants.FinancialConstants.ZERO;

@Component
@RequiredArgsConstructor
public class TemporalRuleService {

    private final QPeriodRuleStrategy qStrategy;
    private final PPeriodRuleStrategy pStrategy;
    private final KPeriodAggregator kAggregator;

    public EngineResult process(
            List<Transaction> transactions,
            List<QPeriod> q,
            List<PPeriod> p,
            List<KPeriod> k
    ) {
        List<InvalidTransactionFilter> invalidTx = new ArrayList<>();
        List<ParsedTransaction> validDomainParsedTransactions = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        List<ProcessedTransaction> finalTx = new ArrayList<>();

        // Step 1: Validate + parse raw parsedTransactions
        for (Transaction txn : transactions) {

            // Negative check
            if (txn.amount() == null ||
                    txn.amount().compareTo(ZERO) < 0) {

                invalidTx.add(new InvalidTransactionFilter(
                        txn.date(),
                        txn.amount(),
                        "Negative amounts are not allowed"
                ));
                continue;
            }

            // Duplicate check (date + amount)
            String key = txn.date() + "-" + txn.amount();
            if (!seen.add(key)) {
                invalidTx.add(new InvalidTransactionFilter(
                        txn.date(),
                        txn.amount(),
                        "Duplicate transaction"
                ));
                continue;
            }

            // Compute ceiling
            BigDecimal ceiling = txn.amount()
                    .divide(HUNDRED, 0, RoundingMode.CEILING)
                    .multiply(HUNDRED);

            BigDecimal remnant = ceiling.subtract(txn.amount());

            validDomainParsedTransactions.add(
                    new ParsedTransaction(
                            txn.date(),
                            txn.amount(),
                            ceiling,
                            remnant
                    )
            );
        }

        // Sort parsedTransactions by timestamp (important for sweep-line)
        validDomainParsedTransactions.sort(
                Comparator.comparing(tx -> DateUtils.parse(tx.date()))
        );

        // Step 2: Map periods to domain
        List<QPeriod> qPeriods = q == null ? List.of() :
                q.stream()
                        .map(qReq -> new QPeriod(
                                qReq.start(),
                                qReq.end(),
                                qReq.fixed()
                        ))
                        .toList();

        List<PPeriod> pPeriods = p == null ? List.of() :
                p.stream()
                        .map(pReq -> new PPeriod(
                                pReq.start(),
                                pReq.end(),
                                pReq.extra()
                        ))
                        .toList();

        List<KPeriod> kPeriods = k == null ? List.of() :
                k.stream()
                        .map(kReq -> new KPeriod(
                                kReq.start(),
                                kReq.end()
                        ))
                        .toList();

        List<ParsedTransaction> afterQ = qStrategy.apply(validDomainParsedTransactions, qPeriods);
        List<ParsedTransaction> afterP = pStrategy.apply(afterQ, pPeriods);

        BigDecimal totalTransactionAmount = BigDecimal.ZERO;
        BigDecimal totalCeiling = BigDecimal.ZERO;

        Map<KPeriod, BigDecimal> kTotals = new LinkedHashMap<>();

        for (KPeriod kp : kPeriods) {
            kTotals.put(kp, BigDecimal.ZERO);
        }

        for (ParsedTransaction ptx : afterP) {

            LocalDateTime date = DateUtils.parse(ptx.date());

            boolean isInK = kAggregator.isInK(date, kPeriods);

            ProcessedTransaction processed = new ProcessedTransaction(
                            ptx.date(),
                            ptx.amount(),
                            ptx.ceiling(),
                            ptx.remnant(),
                            isInK);

            finalTx.add(processed);

            // aggregate totals
            totalTransactionAmount = totalTransactionAmount.add(ptx.amount());

            totalCeiling = totalCeiling.add(ptx.ceiling());

            // aggregate K totals
            for (KPeriod kp : kPeriods) {
                if (DateUtils.isWithin(date, DateUtils.parse(kp.start()), DateUtils.parse(kp.end()))) {
                    kTotals.put(kp, kTotals.get(kp).add(ptx.remnant()));
                }
            }
        }

        return new EngineResult(
                finalTx,
                invalidTx,
                totalTransactionAmount,
                totalCeiling,
                kTotals
        );
    }

    public record EngineResult(
            List<ProcessedTransaction> transactions,
            List<InvalidTransactionFilter> invalidTransactions,
            BigDecimal totalTransactionAmount,
            BigDecimal totalCeiling,
            Map<KPeriod, BigDecimal> kTotals
    ) {
        public EngineResult(List<ProcessedTransaction> transactions) {
            this(transactions, List.of(), BigDecimal.ZERO, BigDecimal.ZERO, Map.of());
        }
    }

}