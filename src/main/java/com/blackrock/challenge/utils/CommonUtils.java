package com.blackrock.challenge.utils;

import com.blackrock.challenge.dto.KPeriod;
import com.blackrock.challenge.dto.PPeriod;
import com.blackrock.challenge.dto.QPeriod;
import com.blackrock.challenge.dto.request.FilterRequest;
import com.blackrock.challenge.dto.request.ParsedTransaction;
import com.blackrock.challenge.dto.request.Transaction;
import com.blackrock.challenge.dto.response.InvalidTransactionFilter;
import com.blackrock.challenge.dto.response.ProcessedTransaction;
import com.blackrock.challenge.service.TemporalRuleService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.blackrock.challenge.constants.FinancialConstants.HUNDRED;
import static com.blackrock.challenge.constants.FinancialConstants.ZERO;

public class CommonUtils {

    public static void getResponseFromRequest(List<ProcessedTransaction> valid, List<InvalidTransactionFilter> invalid, FilterRequest request){
        List<ParsedTransaction> validDomainParsedTransactions = new ArrayList<>();

        Set<String> seen = new HashSet<>();

        // Step 1: Validate + parse raw parsedTransactions
        for (Transaction txn : request.transactions()) {

            // Negative check
            if (txn.amount() == null ||
                    txn.amount().compareTo(ZERO) < 0) {

                invalid.add(new InvalidTransactionFilter(
                        txn.date(),
                        txn.amount(),
                        "Negative amounts are not allowed"
                ));
                continue;
            }

            // Duplicate check (date + amount)
            String key = txn.date() + "-" + txn.amount();
            if (!seen.add(key)) {
                invalid.add(new InvalidTransactionFilter(
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
        List<QPeriod> qPeriods = request.q() == null ? List.of() :
                request.q().stream()
                        .map(q -> new QPeriod(
                                q.start(),
                                q.end(),
                                q.fixedRemnant()
                        ))
                        .toList();

        List<PPeriod> pPeriods = request.p() == null ? List.of() :
                request.p().stream()
                        .map(p -> new PPeriod(
                                p.start(),
                                p.end(),
                                p.extra()
                        ))
                        .toList();

        List<KPeriod> kPeriods = request.k() == null ? List.of() :
                request.k().stream()
                        .map(k -> new KPeriod(
                                k.start(),
                                k.end()
                        ))
                        .toList();
    }

    public static void getResponseFromRequest(List<InvalidTransactionFilter> invalid, List<PPeriod> pPeriods, List<QPeriod> qPeriods, List<KPeriod> kPeriods,
                                              List<ParsedTransaction> validDomainParsedTransactions,
                                              FilterRequest request, List<Transaction> transactions){

        Set<String> seen = new HashSet<>();

        // Step 1: Validate + parse raw parsedTransactions
        for (Transaction txn : transactions) {

            // Negative check
            if (txn.amount() == null ||
                    txn.amount().compareTo(ZERO) < 0) {

                invalid.add(new InvalidTransactionFilter(
                        txn.date(),
                        txn.amount(),
                        "Negative amounts are not allowed"
                ));
                continue;
            }

            // Duplicate check (date + amount)
            String key = txn.date() + "-" + txn.amount();
            if (!seen.add(key)) {
                invalid.add(new InvalidTransactionFilter(
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
        qPeriods = request.q() == null ? List.of() :
                request.q().stream()
                        .map(q -> new QPeriod(
                                q.start(),
                                q.end(),
                                q.fixedRemnant()
                        ))
                        .toList();

        pPeriods = request.p() == null ? List.of() :
                request.p().stream()
                        .map(p -> new PPeriod(
                                p.start(),
                                p.end(),
                                p.extra()
                        ))
                        .toList();

        kPeriods = request.k() == null ? List.of() :
                request.k().stream()
                        .map(k -> new KPeriod(
                                k.start(),
                                k.end()
                        ))
                        .toList();
    }
}
