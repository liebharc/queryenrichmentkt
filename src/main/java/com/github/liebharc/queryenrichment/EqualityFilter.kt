package com.github.liebharc.queryenrichment

import java.util.*

internal class EqualityFilter<TAttribute, TParameter>(innerStep: ExecutableStep<TAttribute, TParameter>, expression: SimpleExpression) : FilterStep<TAttribute, TParameter>(innerStep, expression) {
    override val column: String?
        get() = innerStep.column

    override fun enrich(result: IntermediateResult, parameter: TParameter) {
        innerStep.enrich(result, parameter)
        val value = result.get(this.attribute)

        if (value != expression.value) {
            result.stopProcessing()
        }
    }

    override fun toString(): String {
        return "EqualityFilter{" +
                "innerStep=" + innerStep +
                ", expression=" + expression +
                '}'.toString()
    }

    companion object {
        private val serialVersionUID = 6490446172472673292L
    }
}
