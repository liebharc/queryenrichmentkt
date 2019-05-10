package com.github.liebharc.queryenrichment;

/**
 * Consider a SQL statement like this:
 * <pre>SELECT * FROM STUDENT WHERE CLASS = 123;</pre>
 * If we then ask what class a student belongs to we can say from the WHERE filter condition that the class ID must be
 * 123. This class allows to reflect that knowledge.
 * @param <TAttribute> Attribute type
 */
class AddValuesFromFilter<TAttribute, TParameter> extends Enrichment<TAttribute, TParameter> {

    private static final long serialVersionUID = 2553654683345913539L;

    /** An equality expression */
    private final SimpleExpression expression;

    /**
     * Helper method to create a new instance in a way to easier deal with the Java type checker.
     */
    public static<TParameter> AddValuesFromFilter<?, TParameter> create(
            Attribute<?> attribute,
            SimpleExpression expression) {
        return new AddValuesFromFilter<>(attribute, expression);
    }

    private AddValuesFromFilter(Attribute<TAttribute> attribute, SimpleExpression expression) {
        super(attribute, NO_COLUMN, Dependencies.noDependencies());
        this.expression = expression;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void enrich(IntermediateResult result, TParameter parameter) {
        result.add(this, (TAttribute)expression.getValue());
    }

    @Override
    public boolean isConstant() {
        return true;
    }
}
