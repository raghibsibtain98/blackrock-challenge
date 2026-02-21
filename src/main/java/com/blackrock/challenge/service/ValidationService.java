package com.blackrock.challenge.service;

import com.blackrock.challenge.dto.request.ParsedTransaction;
import com.blackrock.challenge.dto.request.ValidatorRequest;
import com.blackrock.challenge.dto.response.InvalidTransaction;
import com.blackrock.challenge.dto.response.ValidTransaction;
import com.blackrock.challenge.dto.response.ValidatorResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static com.blackrock.challenge.constants.FinancialConstants.MONTHS_IN_YEAR;

@Service
public class ValidationService {

    public ValidatorResponse validate(ValidatorRequest request) {

        List<ValidTransaction> valid = new ArrayList<>();
        List<InvalidTransaction> invalid = new ArrayList<>();

        /*Rule 1: Wage must be positive*/
        if (request.wage() == null || request.wage().compareTo(BigDecimal.ZERO) <= 0) {
            invalid.add(new InvalidTransaction(
                    null, null, null, null,
                    "Wage must be a positive value"
            ));
            return new ValidatorResponse(Collections.emptyList(), invalid);
        }

        BigDecimal annualIncome = request.wage().multiply(MONTHS_IN_YEAR);

        Set<String> seen = new HashSet<>();
        BigDecimal runningTotalRemnant = BigDecimal.ZERO;

        for (ParsedTransaction tx : request.parsedTransactions()) {

            /*Rule 2: Negative amount*/
            if (tx.amount() == null || tx.amount().compareTo(BigDecimal.ZERO) < 0) {
                invalid.add(new InvalidTransaction(
                        tx.date(),
                        tx.amount(),
                        tx.ceiling(),
                        tx.remnant(),
                        "Negative amounts are not allowed"
                ));
                continue;
            }

            /*Rule 3: Duplicate check*/
            String key = tx.date() + "-" + tx.amount();
            if (!seen.add(key)) {
                invalid.add(new InvalidTransaction(
                        tx.date(),
                        tx.amount(),
                        tx.ceiling(),
                        tx.remnant(),
                        "Duplicate transaction"
                ));
                continue;
            }

            /*Rule 4: Annual wage investment cap*/
            BigDecimal tempTotalRemnant = runningTotalRemnant.add(tx.remnant());

            if (tempTotalRemnant.compareTo(annualIncome) > 0) {
                invalid.add(new InvalidTransaction(
                        tx.date(),
                        tx.amount(),
                        tx.ceiling(),
                        tx.remnant(),
                        "Total investment exceeds annual wage limit"
                ));
                continue;
            }
            runningTotalRemnant = tempTotalRemnant;
            valid.add(new ValidTransaction(
                    tx.date(),
                    tx.amount(),
                    tx.ceiling(),
                    tx.remnant()
            ));
        }
        return new ValidatorResponse(valid, invalid);
    }
}