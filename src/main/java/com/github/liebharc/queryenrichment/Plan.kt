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
            val constantResult = IntermediateResult()

            val batchSize = Math.min(20, rows.size)

            // determine constants once at the beginning
            if (this.processConstants(constantResult, parameter)) {
                return EnrichedQueryResult(attributes, results.toTypedArray())
            }

            val intermediateResults = List(batchSize) { i -> if (i == 0) constantResult else constantResult.copy() }

            val activeResults = MutableList(batchSize, {_ -> constantResult })
            for (batch in rows.asSequence().batch(batchSize)) {
                val zip = batch.zip(intermediateResults)
                for ((row, result) in zip) {
                    result.nextRow(row)
                }

                // An active result is a result which hasn't been filtered out
                activeResults.clear()
                activeResults.addAll(intermediateResults.take(zip.size))
                this.processRow(activeResults, parameter)
                for (result in activeResults) {
                    if (result.isContinueProcessing) {
                        results.add(this.storeResultInObjectArray(result))
                    }
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
        val singleElementBatch = listOf(intermediateResult)
        for (step in constants) {
            step.enrichBatch(singleElementBatch, parameter)
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
    private fun processRow(activeResults: MutableList<IntermediateResult>, parameter: TParameter) {
        for (step in steps) {
            step.enrichBatch(activeResults, parameter)
            activeResults.retainAll { it.isContinueProcessing }
        }
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

    /**
     * Batches a sequence.
     */
    fun <T> Sequence<T>.batch(n: Int): Sequence<List<T>> {
        return BatchingSequence(this, n)
    }

    private class BatchingSequence<T>(val source: Sequence<T>, val batchSize: Int) : Sequence<List<T>> {
        override fun iterator(): Iterator<List<T>> = object : AbstractIterator<List<T>>() {
            val iterate = if (batchSize > 0) source.iterator() else emptyList<T>().iterator()
            override fun computeNext() {
                if (iterate.hasNext()) setNext(iterate.asSequence().take(batchSize).toList())
                else done()
            }
        }
    }
}
