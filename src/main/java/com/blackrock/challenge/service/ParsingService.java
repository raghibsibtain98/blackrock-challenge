package com.blackrock.challenge.service;

import com.blackrock.challenge.dto.request.Transaction;
import com.blackrock.challenge.dto.response.ParseTransactionResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.blackrock.challenge.constants.FinancialConstants.HUNDRED;

@Service
public class ParsingService {

    public List<ParseTransactionResponse> parse(
            List<Transaction> requests) {

        return requests.parallelStream()
                .map(req -> {

                    BigDecimal amount = req.amount();

                    BigDecimal ceiling = amount
                            .divide(HUNDRED, 0, RoundingMode.CEILING)
                            .multiply(HUNDRED);

                    BigDecimal remnant = ceiling.subtract(amount);

                    return new ParseTransactionResponse(
                            req.date(),
                            amount,
                            ceiling,
                            remnant
                    );
                })
                .toList();
    }
}