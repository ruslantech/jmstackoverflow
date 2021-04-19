package com.javamentor.qa.platform.dao.search.predicates;

import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IsSearchOperator extends SearchOperator {

    public IsSearchOperator(@Value("is:question|answer search operator") String description,
                            @Value("10") int order) {
        super(description, order);
    }

    @Override
    public BooleanPredicateClausesStep<?> parse(StringBuilder query, SearchPredicateFactory factory, BooleanPredicateClausesStep<?> booleanPredicate) {
        Pattern pattern = Pattern.compile("is:(question|answer)");
        Matcher matcher = pattern.matcher(query);

        String operator = "";

        while (matcher.find()) {
           operator = matcher.group(1);
        }

        switch (operator) {
            case "question":
                booleanPredicate = booleanPredicate.must(factory.exists().field("id"));
                break;
            case "answer":
                booleanPredicate = booleanPredicate.must(factory.exists().field("answerId"));
                break;
        }

        query.replace(0, query.length(), matcher.replaceAll(""));

        return booleanPredicate;
    }
}
