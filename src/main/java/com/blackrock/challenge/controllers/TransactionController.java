package com.blackrock.challenge.controllers;

import com.blackrock.challenge.dto.request.*;
import com.blackrock.challenge.dto.response.*;
import com.blackrock.challenge.service.InvestmentsService;
import com.blackrock.challenge.service.TemporalRuleService;
import com.blackrock.challenge.service.ParsingService;
import com.blackrock.challenge.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

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
        List<ProcessedTransaction> validTxns = new ArrayList<>();
        TemporalRuleService.EngineResult result =
                engine.process(
                        request.transactions(),
                        request.q(),
                        request.p(),
                        request.k()
                );
        return ResponseEntity.ok(new FilterResponse(result.transactions(), result.invalidTransactions()));
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