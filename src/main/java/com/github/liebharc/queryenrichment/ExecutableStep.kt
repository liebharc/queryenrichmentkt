package com.github.liebharc.queryenrichment

import java.io.Serializable
import java.util.Optional

/**
 * Definition of the implementation of a step which is executed to get the result for a query.
 * @param <TAttribute> Attribute type
 * @param <TParameter> Parameter type
</TParameter></TAttribute> */
interface ExecutableStep<TAttribute, TParameter> : Step<TAttribute> {

    /**
     * This main method of a step. Works with the result object to get the values from dependencies and sets the
     * value of this step.
     * @param result Gives access to the results of other steps and allows this step to store its results.
     * @param parameter Parameter object
     */
    fun enrich(result: IntermediateResult, parameter: TParameter)
}