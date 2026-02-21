package com.blackrock.challenge.rule.strategy;



import com.blackrock.challenge.dto.request.ParsedTransaction;

import java.util.List;

/**
 * Strategy interface for temporal period rules.
 * Each rule (Q, P) transforms a list of parsedTransactions by modifying
 * their remnant values according to the period's business logic.
 * This allows new rules to be added without modifying the engine.
 */
public interface PeriodRuleStrategy<T> {

    /**
     * Apply the period rule to the given list of parsedTransactions.
     *
     * @param parsedTransactions input parsedTransactions (may be modified by previous rules)
     * @param periods      list of period definitions for this rule type
     * @return new list of parsedTransactions with remnants updated per this rule
     */
    List<ParsedTransaction> apply(List<ParsedTransaction> parsedTransactions, List<T> periods);
}
