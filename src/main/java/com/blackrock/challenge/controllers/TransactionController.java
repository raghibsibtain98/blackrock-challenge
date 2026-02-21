package com.blackrock.challenge.controllers;

import com.blackrock.challenge.dto.KPeriod;
import com.blackrock.challenge.dto.PPeriod;
import com.blackrock.challenge.dto.QPeriod;
import com.blackrock.challenge.dto.request.*;
import com.blackrock.challenge.dto.response.*;
import com.blackrock.challenge.service.InvestmentsService;
import com.blackrock.challenge.service.TemporalRuleService;
import com.blackrock.challenge.service.ParsingService;
import com.blackrock.challenge.service.ValidationService;
import com.blackrock.challenge.utils.CommonUtils;
import com.blackrock.challenge.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.blackrock.challenge.constants.FinancialConstants.HUNDRED;
import static com.blackrock.challenge.constants.FinancialConstants.ZERO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/blackrock/challenge/v1")
public class TransactionController {

    private final ParsingService parsingService;
    private final ValidationService validationService;
    private final TemporalRuleService engine;
    private final InvestmentsService service;

    @PostMapping("/parsedTransactions:parse")
    public ResponseEntity<List<ParseTransactionResponse>> parse(
            @RequestBody List<Transaction> request) {

        return ResponseEntity.ok(parsingService.parse(request));
    }


    @PostMapping("/parsedTransactions:validator")
    public ResponseEntity<ValidatorResponse> validate(
            @RequestBody ValidatorRequest request) {

        return ResponseEntity.ok(validationService.validate(request));
    }

    @PostMapping("/transactions:filter")
    public ResponseEntity<FilterResponse> filter(@RequestBody FilterRequest request) {
        List<InvalidTransactionFilter> invalid = new ArrayList<>();
        List<ProcessedTransaction> valid;
        List<ParsedTransaction> validDomainParsedTransactions = new ArrayList<>();
        List<PPeriod> pPeriods = new ArrayList<>();
        List<QPeriod> qPeriods = new ArrayList<>();
        List<KPeriod> kPeriods = new ArrayList<>();
        CommonUtils.getResponseFromRequest(invalid,pPeriods,qPeriods,kPeriods,validDomainParsedTransactions,request);

        TemporalRuleService.EngineResult result =
                engine.process(
                        validDomainParsedTransactions,
                        qPeriods,
                        pPeriods,
                        kPeriods
                );
        valid = result.transactions().stream()
                        .map(tx -> new ProcessedTransaction(
                                tx.date(),
                                tx.amount(),
                                tx.ceiling(),
                                tx.remnant(),
                                tx.inkPeriod()
                        ))
                        .toList();

        return ResponseEntity.ok(new FilterResponse(valid, invalid));
    }




    @PostMapping("/returns:index")
    public ResponseEntity<InvestmentsResponse> index(
            @RequestBody InvestmentRequest request
    ) {
        return ResponseEntity.ok(service.calculateIndex(request));
    }

    @PostMapping("/returns:nps")
    public ResponseEntity<InvestmentsResponse> nps(@RequestBody InvestmentRequest request) {
        return ResponseEntity.ok(service.calculateNps(request));
    }
}