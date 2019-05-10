package com.github.liebharc.queryenrichment;

import java.util.Optional;

/** A step which executes a filter expression in Java */
abstract class FilterStep<TAttribute, TParameter> implements ExecutableStep<TAttribute, TParameter> {

    private static final long serialVersionUID = 5894332892548243458L;
    /** The step which produces the attribute we have to filter for */
    protected final ExecutableStep<TAttribute, TParameter> innerStep;
    /** The filter expression */
    protected final SimpleExpression expression;

    static<TAttribute, TParameter> FilterStep<TAttribute, TParameter> createFilter(
            ExecutableStep<TAttribute, TParameter> innerStep,
            SimpleExpression expression) {
        if (!expression.getOperation().equals("=")) {
            throw new IllegalArgumentException("Only equality is supported right now");
        }

        return new EqualityFilter<>(innerStep, expression);
    }

    protected FilterStep(ExecutableStep<TAttribute, TParameter> innerStep, SimpleExpression expression) {
        this.innerStep = innerStep;
        this.expression = expression;
    }

    @Override
    public Optional<String> getColumn() {
        return innerStep.getColumn();
    }

    @Override
    public Attribute<TAttribute> getAttribute() {
        return innerStep.getAttribute();
    }

    @Override
    public abstract void enrich(IntermediateResult result, TParameter parameter);

    @Override
    public Dependency getDependencies() {
        return innerStep.getDependencies();
    }

    @Override
    public boolean isConstant() {
        return innerStep.isConstant();
    }

    @Override
    public String toString() {
        return "FilterStep{" +
                "innerStep=" + innerStep +
                ", expression=" + expression +
                '}';
    }
}
