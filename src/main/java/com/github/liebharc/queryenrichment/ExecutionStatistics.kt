package com.github.liebharc.queryenrichment

import java.io.Serializable

/**
 * Gives rough statistics about execution performance. Rough means that in doubt a detailed benchmarking should
 * still be executed.
 */
class ExecutionStatistics : Serializable {

    /** Number of executed queries  */
    private var queryCount: Long = 0
    /** Total time spent in query  */
    private var queryDuration: Long = 0
    /** Number of executed queries and enrichment, should match number of queries unless there are exceptions thrown  */
    private var totalCount: Long = 0
    /** Total time spent in queries and enrichment  */
    private var totalDuration: Long = 0

    @Synchronized
    fun addQueryTime(duration: Long) {
        queryCount++
        queryDuration += duration
    }

    @Synchronized
    fun addTotal(duration: Long) {
        totalCount++
        totalDuration += duration
    }

    override fun toString(): String {
        return "ExecutionStatistics{" +
                "queryCount=" + queryCount +
                ", queryDuration=" + queryDuration +
                ", queryAverage=" + queryDuration / queryCount +
                ", totalCount=" + totalCount +
                ", totalDuration=" + totalDuration +
                ", totalAverage=" + totalDuration / totalCount +
                '}'.toString()
    }

    companion object {
        private const val serialVersionUID = 5061706759148561282L
    }
}
