package com.blackrock.challenge.service;
import com.blackrock.challenge.dto.request.InvestmentRequest;
import com.blackrock.challenge.dto.response.InvestmentsResponse;
import com.blackrock.challenge.dto.response.SavingsByDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

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



        var result = service.process();

        BigDecimal totalAmount = result.totalTransactionAmount();
        BigDecimal totalCeiling = result.totalCeiling();

        int years = 60 - request.age();
        if (years < 0) years = 5;

        List<SavingsByDate> savings = result.kSummaries().stream()
                .map(k -> computeReturns(
                        k.start(),
                        k.end(),
                        k.amount(),
                        rate,
                        request.inflation(),
                        years,
                        npsMode,
                        request.wage()
                ))
                .toList();

        return new InvestmentsResponse(
                totalAmount,
                totalCeiling,
                savings
        );
    }

    private SavingsByDate computeReturns(
            String start,
            String end,
            BigDecimal principal,
            BigDecimal rate,
            BigDecimal inflation,
            int years,
            boolean npsMode,
            BigDecimal wage
    ) {

        if (principal.compareTo(BigDecimal.ZERO) == 0) {
            return new SavingsByDate(
                    start, end,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        MathContext mc = MathContext.DECIMAL64;

        BigDecimal growthFactor =
                BigDecimal.ONE.add(rate).pow(years, mc);

        BigDecimal futureValue =
                principal.multiply(growthFactor, mc);

        BigDecimal inflationFactor =
                BigDecimal.ONE.add(inflation.divide(HUNDRED, mc))
                        .pow(years, mc);

        BigDecimal realValue =
                futureValue.divide(inflationFactor, mc);

        BigDecimal profit =
                realValue.subtract(principal, mc);

        BigDecimal taxBenefit = BigDecimal.ZERO;

        if (npsMode) {
            BigDecimal annualIncome =
                    wage.multiply(new BigDecimal("12"));

            BigDecimal tenPercent =
                    annualIncome.multiply(new BigDecimal("0.10"));

            BigDecimal maxDeduction =
                    new BigDecimal("200000");

            BigDecimal deduction =
                    principal.min(tenPercent).min(maxDeduction);

            taxBenefit = calculateTaxBenefit(annualIncome, deduction);
        }

        return new SavingsByDate(
                start,
                end,
                principal,
                profit,
                taxBenefit
        );
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

        BigDecimal tax = BigDecimal.ZERO;

        if (income.compareTo(new BigDecimal("700000")) <= 0) {
            return BigDecimal.ZERO;
        }

        if (income.compareTo(new BigDecimal("1000000")) <= 0) {
            return income.subtract(new BigDecimal("700000"))
                    .multiply(new BigDecimal("0.10"));
        }

        if (income.compareTo(new BigDecimal("1200000")) <= 0) {
            tax = new BigDecimal("30000");
            tax = tax.add(
                    income.subtract(new BigDecimal("1000000"))
                            .multiply(new BigDecimal("0.15"))
            );
            return tax;
        }

        if (income.compareTo(new BigDecimal("1500000")) <= 0) {
            tax = new BigDecimal("60000");
            tax = tax.add(
                    income.subtract(new BigDecimal("1200000"))
                            .multiply(new BigDecimal("0.20"))
            );
            return tax;
        }

        tax = new BigDecimal("120000");
        tax = tax.add(
                income.subtract(new BigDecimal("1500000"))
                        .multiply(new BigDecimal("0.30"))
        );

        return tax;
    }
}
