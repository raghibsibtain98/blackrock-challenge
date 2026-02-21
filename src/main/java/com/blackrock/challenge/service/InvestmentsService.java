package com.blackrock.challenge.service;

import com.blackrock.challenge.dto.KPeriod;
import com.blackrock.challenge.dto.request.InvestmentRequest;
import com.blackrock.challenge.dto.response.InvestmentsResponse;
import com.blackrock.challenge.dto.response.SavingsByDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static com.blackrock.challenge.constants.FinancialConstants.*;

@Service
@RequiredArgsConstructor
public class InvestmentsService {

    private final TemporalRuleService service;

    public InvestmentsResponse calculateIndex(InvestmentRequest request) {
        return calculate(request, INDEX_RATE, false);
    }

    public InvestmentsResponse calculateNps(InvestmentRequest request) {
        return calculate(request, NPS_RATE, true);
    }

    private InvestmentsResponse calculate(
            InvestmentRequest request,
            BigDecimal rate,
            boolean npsMode
    ) {

        var result = service.process(
                request.transactions(),
                request.q(),
                request.p(),
                request.k()
        );

        BigDecimal totalAmount = result.totalTransactionAmount();
        BigDecimal totalCeiling = result.totalCeiling();

        int years = 60 - request.age();
        if (years < 0) years = 5;

        BigDecimal annualIncome =
                request.wage().multiply(new BigDecimal("12"));

        int finalYears = years;
        List<SavingsByDate> savings = result.kTotals()
                .entrySet()
                .stream()
                .map(entry -> {

                    BigDecimal invested = entry.getValue();

                    BigDecimal profit = computeReturns(
                            invested,
                            rate,
                            request.inflation(),
                            finalYears
                    );

                    BigDecimal taxBenefit = BigDecimal.ZERO;

                    if (npsMode) {

                        BigDecimal deduction = invested
                                .min(annualIncome.multiply(new BigDecimal("0.10")))
                                .min(new BigDecimal("200000"));

                        taxBenefit = calculateTaxBenefit(
                                annualIncome,
                                deduction
                        );
                    }

                    return new SavingsByDate(
                            entry.getKey().start(),
                            entry.getKey().end(),
                            invested.setScale(2, RoundingMode.HALF_UP),
                            profit.setScale(2, RoundingMode.HALF_UP),
                            taxBenefit.setScale(2, RoundingMode.HALF_UP)
                    );
                })
                .toList();

        return new InvestmentsResponse(
                totalAmount,
                totalCeiling,
                savings
        );
    }

    private BigDecimal computeReturns(
            BigDecimal principal,
            BigDecimal rate,
            BigDecimal inflation,
            int years
    ) {

        if (principal.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        MathContext mc = MathContext.DECIMAL64;

        BigDecimal futureValue = principal.multiply(BigDecimal.ONE.add(rate).pow(years, mc), mc);

        BigDecimal inflationFactor = BigDecimal.ONE.add(inflation.divide(HUNDRED, mc)).pow(years, mc);

        BigDecimal realValue = futureValue.divide(inflationFactor, mc);

        return realValue.subtract(principal);
    }

    private BigDecimal calculateTaxBenefit(
            BigDecimal income,
            BigDecimal deduction
    ) {
        BigDecimal taxBefore = calculateTax(income);
        BigDecimal taxAfter = calculateTax(income.subtract(deduction));
        return taxBefore.subtract(taxAfter);
    }

    private BigDecimal calculateTax(BigDecimal income) {

        if (income.compareTo(SEVEN_LAKH) <= 0)
            return BigDecimal.ZERO;

        if (income.compareTo(TEN_LAKH) <= 0)
            return income.subtract(SEVEN_LAKH)
                    .multiply(TEN_PERCENT);

        if (income.compareTo(TWELVE_LAKH) <= 0)
            return new BigDecimal("30000").add(
                    income.subtract(TEN_LAKH)
                            .multiply(FIFTEEN_PERCENT)
            );

        if (income.compareTo(FIFTEEN_LAKH) <= 0)
            return new BigDecimal("60000").add(
                    income.subtract(TWELVE_LAKH)
                            .multiply(TWENTY_PERCENT)
            );

        return new BigDecimal("120000").add(income.subtract(FIFTEEN_LAKH).multiply(THIRTY_PERCENT));
    }
}