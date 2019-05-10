package com.github.liebharc.queryenrichment

import java.util.Optional

/** A step which executes a filter expression in Java  */
internal abstract class FilterStep<TAttribute, TParameter> protected constructor(
        /** The step which produces the attribute we have to filter for  */
        protected val innerStep: ExecutableStep<TAttribute, TParameter>,
        /** The filter expression  */
        protected val expression: SimpleExpression) : ExecutableStep<TAttribute, TParameter> {

    override val attribute: Attribute<TAttribute>
        get() = innerStep.attribute

    override val dependencies: Dependency
        get() = innerStep.dependencies

    override val isConstant: Boolean
        get() = innerStep.isConstant

    abstract override fun enrich(result: IntermediateResult, parameter: TParameter)

    override fun toString(): String {
        return "FilterStep{" +
                "innerStep=" + innerStep +
                ", expression=" + expression +
                '}'.toString()
    }

    companion object {

        private val serialVersionUID = 5894332892548243458L

        fun <TAttribute, TParameter> createFilter(
                innerStep: ExecutableStep<TAttribute, TParameter>,
                expression: SimpleExpression): FilterStep<TAttribute, TParameter> {
            if (expression.operation != "=") {
                throw IllegalArgumentException("Only equality is supported right now")
            }

            return EqualityFilter(innerStep, expression)
        }
    }
}
