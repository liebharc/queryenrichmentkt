package com.github.liebharc.queryenrichment

/**
 * Consider a SQL statement like this:
 * <pre>SELECT * FROM STUDENT WHERE CLASS = 123;</pre>
 * If we then ask what class a student belongs to we can say from the WHERE filter condition that the class ID must be
 * 123. This class allows to reflect that knowledge.
 * @param <TAttribute> Attribute type
</TAttribute> */
internal class AddValuesFromFilter<TAttribute, TParameter> private constructor(attribute: Attribute<TAttribute>,
                                                                               /** An equality expression  */
                                                                               private val expression: SimpleExpression<TAttribute>) : Enrichment<TAttribute, TParameter>(attribute, null, Dependencies.noDependencies()) {

    override val canBeConstant: Boolean
        get() = true

    override fun enrich(result: IntermediateResult, parameter: TParameter) {
        result.add(this, expression.value)
    }

    companion object {

        private const val serialVersionUID = 2553654683345913539L

        /**
         * Helper method to create a new instance in a way to easier deal with the Java type checker.
         */
        fun <TAttribute, TParameter> create(
                attribute: Attribute<TAttribute>,
                expression: SimpleExpression<TAttribute>): AddValuesFromFilter<TAttribute, TParameter> {
            return AddValuesFromFilter(attribute, expression)
        }
    }
}
