package com.github.liebharc.queryenrichment;

import java.util.Objects;

class EqualityFilter<TAttribute, TParameter> extends FilterStep<TAttribute, TParameter> {
    private static final long serialVersionUID = 6490446172472673292L;

    EqualityFilter(ExecutableStep<TAttribute, TParameter> innerStep, SimpleExpression expression) {
        super(innerStep, expression);
    }

    @Override
    public void enrich(IntermediateResult result, TParameter parameter) {
        innerStep.enrich(result, parameter);
        final TAttribute value = result.get(this.getAttribute());

        if (!Objects.equals(value, expression.getValue())) {
            result.stopProcessing();
        }
    }

    @Override
    public String toString() {
        return "EqualityFilter{" +
                "innerStep=" + innerStep +
                ", expression=" + expression +
                '}';
    }
}
