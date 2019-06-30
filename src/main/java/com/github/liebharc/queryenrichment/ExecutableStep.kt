package com.github.liebharc.queryenrichment

/**
 * Definition of the implementation of a step which is executed to get the result for a query.
 * @param <TAttribute> Attribute type
 * @param <TParameter> Parameter type
</TParameter></TAttribute> */
interface ExecutableStep<TAttribute, TParameter> : Step<TAttribute> {

    /**
     * Works with the result object to get the values from dependencies and sets the
     * value of this step.
     * @param result Gives access to the results of other steps and allows this step to store its results.
     * @param parameter Parameter object
     */
    fun enrich(result: IntermediateResult, parameter: TParameter)

    /**
    * This main method of a step. Same as @see enrich but processes results in batches.
    * By default this method calls @see enrich.
    * @param results Batch of result objects.
    * @param parameter Parameter object
    */
    fun enrichBatch(results: List<IntermediateResult>, parameter: TParameter) {
        for (result in results) {
            enrich(result, parameter)
        }
    }
}