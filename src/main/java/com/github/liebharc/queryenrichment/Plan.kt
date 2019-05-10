package com.github.liebharc.queryenrichment

import java.util.ArrayList
import java.util.Collections

/**
 * A plan holds an execution plan which consists of a query and steps. It allows to execute the plan which results
 * in a [EnrichedQueryResult].
 */
class Plan<TParameter>(
        /** Ordered list of attributes which are queried  */
        private val attributes: List<Attribute<*>>,
        /** Ordered list of constant steps  */
        private val constants: List<ExecutableStep<*, TParameter>>,
        steps: List<ExecutableStep<*, TParameter>>,
        /** Query for this plan  */
        private val query: Query) {
    /** Ordered list of per row steps  */
    val steps: List<ExecutableStep<*, TParameter>> = Collections.unmodifiableList(steps)
    /** Execution statistics  */
    private val statistics = ExecutionStatistics()

    fun execute(request: Request, parameter: TParameter): EnrichedQueryResult {
        val start = System.currentTimeMillis()
        try {
            val queryResult = query.query(request)
            statistics.addQueryTime(System.currentTimeMillis() - start)
            val rows = queryResult.rows
            val results = ArrayList<Array<Any?>>()
            val intermediateResult = IntermediateResult()

            // determine constants once at the beginning
            if (this.processConstants(intermediateResult, parameter)) {
                return EnrichedQueryResult(attributes, results.toTypedArray())
            }

            for (row in rows) {
                intermediateResult.nextRow(row)
                if (this.processRow(intermediateResult, parameter)) {
                    results.add(this.storeResultInObjectArray(intermediateResult))
                }
            }

            return EnrichedQueryResult(attributes, results.toTypedArray())
        } finally {
            statistics.addTotal(System.currentTimeMillis() - start)
        }
    }

    /**
     * Executes all constants steps and then marks the result as constant.
     */
    private fun processConstants(intermediateResult: IntermediateResult, parameter: TParameter): Boolean {
        for (step in constants) {
            step.enrich(intermediateResult, parameter)
            if (!intermediateResult.isContinueProcessing) {
                return true
            }
        }

        intermediateResult.markCurrentResultAsConstant()
        return false
    }

    /**
     * Processes all per row steps. Returns false if the row should be filtered out.
     */
    private fun processRow(intermediateResult: IntermediateResult, parameter: TParameter): Boolean {
        for (step in steps) {
            step.enrich(intermediateResult, parameter)
            if (!intermediateResult.isContinueProcessing) {
                return false
            }
        }

        return true
    }

    /**
     * Stores the results in an object array.
     */
    private fun storeResultInObjectArray(intermediateResult: IntermediateResult): Array<Any?> {
        val row = arrayOfNulls<Any?>(attributes.size)
        for ((pos, attribute) in attributes.withIndex()) {
            row[pos] = intermediateResult[attribute]
        }

        return row
    }

}
