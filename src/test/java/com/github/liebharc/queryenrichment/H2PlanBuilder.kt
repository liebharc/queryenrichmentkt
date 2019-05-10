package com.github.liebharc.queryenrichment

import java.sql.Connection

class H2PlanBuilder(private val connection: Connection?, steps: List<ExecutableStep<*, Any?>>) : PlanBuilder<Any?>(steps) {

    override val queryBuilder: QueryBuilder
        get() = H2QueryBuilder(connection!!)
}
