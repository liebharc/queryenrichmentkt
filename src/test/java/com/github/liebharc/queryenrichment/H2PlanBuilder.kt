package com.github.liebharc.queryenrichment

import java.sql.Connection
import java.sql.Statement

class H2PlanBuilder(private val connection: Connection?, steps: List<ExecutableStep<*, Any?>>) : PlanBuilder<Any?>(steps) {

    protected override val queryBuilder: QueryBuilder
        get() = H2QueryBuilder(connection!!)
}
