package com.github.liebharc.queryenrichment;

import java.io.Serializable;

/**
 * Gives rough statistics about execution performance. Rough means that in doubt a detailed benchmarking should
 * still be executed.
 */
public class ExecutionStatistics implements Serializable {
    private static final long serialVersionUID = 5061706759148561282L;

    /** Number of executed queries */
    private long queryCount = 0;
    /** Total time spent in query */
    private long queryDuration = 0;
    /** Number of executed queries and enrichment, should match number of queries unless there are exceptions thrown */
    private long totalCount = 0;
    /** Total time spent in queries and enrichment */
    private long totalDuration = 0;

    public synchronized void addQueryTime(long duration) {
        queryCount++;
        queryDuration += duration;
    }

    public synchronized void addTotal(long duration) {
        totalCount++;
        totalDuration += duration;
    }

    @Override
    public String toString() {
        return "ExecutionStatistics{" +
                "queryCount=" + queryCount +
                ", queryDuration=" + queryDuration +
                ", queryAverage=" + queryDuration / queryCount +
                ", totalCount=" + totalCount +
                ", totalDuration=" + totalDuration +
                ", totalAverage=" + totalDuration / totalCount +
                '}';
    }
}
