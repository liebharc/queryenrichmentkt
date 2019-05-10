package com.github.liebharc.queryenrichment

class InMemoryPlanBuilder(steps: List<ExecutableStep<*, Any?>>) : PlanBuilder<Any?>(steps) {

    override val queryBuilder: QueryBuilder
        get() = InMemoryQueryBuilder()

    override fun isSupportedByQuery(criteria: SimpleExpression<*>): Boolean {
        return false
    }
}
