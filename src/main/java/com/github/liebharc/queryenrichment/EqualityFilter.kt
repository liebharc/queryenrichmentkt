package com.github.liebharc.queryenrichment

internal class EqualityFilter<TAttribute, TParameter>(innerStep: ExecutableStep<TAttribute, TParameter>, expression: SimpleExpression<TAttribute>) : FilterStep<TAttribute, TParameter>(innerStep, expression) {

    override fun column(queryInformation: QueryInformation): String? = innerStep.column(queryInformation)

    override fun enrich(result: IntermediateResult, parameter: TParameter) {
        innerStep.enrich(result, parameter)
        val value = result[this.attribute]

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
        private const val serialVersionUID = 6490446172472673292L
    }
}
