package com.github.liebharc.queryenrichment

import java.util.*

/**
 * Creates a plan for a query. This class is intended to be subclassed so that implementors can provide their
 * query builder.
 */
abstract class PlanBuilder<TParameter>(steps: List<ExecutableStep<*, TParameter>>) {
    /** Maps attributes to the step which creates that attribute  */
    private val attributeToStep = HashMap<Attribute<*>, ExecutableStep<*, TParameter>>()

    /**
     * Intended to be overwritten. Provides the concrete query builder which should be used.
     */
    protected abstract val queryBuilder: QueryBuilder

    init {

        // Create lookup tables and check steps
        val errorBuilder = StringBuilder(0)
        for (step in steps) {
            val previousMapping = this.attributeToStep.put(step.attribute, step)

            if (previousMapping != null) {
                errorBuilder.append(step.attribute)
                        .append("  has more than one step; ")
                        .append(previousMapping).append(" and ")
                        .append(step).append("\n")
            }
        }

        val errors = errorBuilder.toString()
        if (!errors.isEmpty()) {
            throw IllegalArgumentException("Failed to plan query:\n$errors")
        }
    }

    /**
     * Builds the plan for a request.
     */
    fun build(request: Request): Plan<TParameter> {
        if (request.attributes.isEmpty()) {
            throw IllegalArgumentException("At least one attribute must be requested")
        }

        val groupedByQueryFilter = this.groupByQueryFilter(request.criteria)
        val filterSteps = this.addDependencies(
                this.createFilterSteps(
                        groupedByQueryFilter.getOrDefault(false, emptyList())))
        val sqlQueryExpressions = groupedByQueryFilter.getOrDefault(true, emptyList())
        val allRequiredSteps = this.addStepsForFilters(filterSteps,
                this.injectConstants(sqlQueryExpressions,
                        this.addDependencies(
                                this.findRequiredSteps(sqlQueryExpressions, request))))
        val orderedSteps = this.orderSelectorsByDependencies(allRequiredSteps)
        val queryInformation = QueryInformation(request)
        val filters = this.translatePropertyNames(
                queryInformation,
                groupedByQueryFilter.getOrDefault(true, emptyList()))
        val queryColumns = orderedSteps
                .filter { it.column(queryInformation) != null }
                .map { sel -> QuerySelector(sel.attribute, sel.column(queryInformation)!!) }
                .toList()
        val (notConstant, constant) = this.groupByConstant(queryInformation, orderedSteps)
        return Plan(
                request.attributes,
                constant,
                notConstant,
                this.queryBuilder.build(queryColumns, filters))
    }

    /**
     * Joins the lists of selector/enrichment and filter steps.
     */
    private fun addStepsForFilters(filterSteps: List<ExecutableStep<*, TParameter>>, requiredSelectors: List<ExecutableStep<*, TParameter>>): List<ExecutableStep<*, TParameter>> {
        val result = ArrayList<ExecutableStep<*, TParameter>>(filterSteps.size + requiredSelectors.size)
        result.addAll(filterSteps)
        for (selector in requiredSelectors) {
            if (!result.contains(selector)) {
                result.add(selector)
            }
        }

        return result
    }

    /**
     * Creates Java filters for the given filter expressions.
     */
    @Suppress("UNCHECKED_CAST")
    private fun createFilterSteps(javaFilters: List<SimpleExpression<*>>): List<ExecutableStep<*, TParameter>> {
        return javaFilters.map { expr ->
            val step = attributeToStep[expr.attribute]
                    ?: throw IllegalArgumentException("Failed to find selector for expression $expr")
            FilterStep.createFilter(
                    step as ExecutableStep<Any?, TParameter>,
                    expr as SimpleExpression<Any?>
            )
        }
        .toList()
    }

    /**
     * Groups the given list of steps in constant/not-constant.
     */
    private fun groupByConstant(queryInformation: QueryInformation, steps: List<ExecutableStep<*, TParameter>>): Pair<List<ExecutableStep<*, TParameter>>, List<ExecutableStep<*, TParameter>>> {
        val constant = ArrayList<ExecutableStep<*, TParameter>>()
        val constantAttributes = HashSet<Attribute<*>>()
        val notConstant = ArrayList<ExecutableStep<*, TParameter>>()
        for (step in steps) {
            if (step.canBeConstant && step.dependencies.isEmpty) {
                constant.add(step)
                constantAttributes.add(step.attribute)
            } else if (step.column(queryInformation) == null && step.dependencies.canBeConstant(constantAttributes)) {
                constant.add(step)
                constantAttributes.add(step.attribute)
            } else {
                notConstant.add(step)
            }
        }

        return Pair(notConstant, constant)
    }

    /**
     * Replaces steps by constants where possible.
     */
    @Suppress("UNCHECKED_CAST")
    private fun injectConstants(queryFilters: List<SimpleExpression<*>>, steps: List<ExecutableStep<*, TParameter>>): List<ExecutableStep<*, TParameter>> {
        val equalityFilters = queryFilters
                .filter { this.isEqualityExpression(it) }
                .groupBy { it.attribute }

        return steps.map { step ->
            val filterExpression = equalityFilters[step.attribute]?.first()
            if (filterExpression != null) {
                AddValuesFromFilter.create(
                        step.attribute as Attribute<Any?>,
                        filterExpression as SimpleExpression<Any?>
                )
            } else {
                step
            }
        }
    }

    /**
     * Find the required steps to implement the given list of filters in Java.
     */
    @Suppress("UNCHECKED_CAST")
    private fun findRequiredSteps(queryExpressions: List<SimpleExpression<*>>, request: Request): List<ExecutableStep<*, TParameter>> {
        val equalityFilters = queryExpressions
                .filter { this.isEqualityExpression(it) }
                .groupBy { it.attribute }

        return request.attributes
                .map { attr ->
                    val filterExpression = equalityFilters[attr]?.first()
                    if (filterExpression != null) {
                        AddValuesFromFilter.create(
                                attr as Attribute<Any?>,
                                filterExpression as SimpleExpression<Any?>
                        )
                    }
                    else {
                        attributeToStep[attr]!!
                } }.toList()
    }

    /**
     * Adds the dependencies for all steps.
     */
    private fun addDependencies(requiredSteps: List<ExecutableStep<*, TParameter>>): List<ExecutableStep<*, TParameter>> {
        val result = ArrayList<ExecutableStep<*, TParameter>>()
        val availableAttributes =
                requiredSteps.map { it.attribute } .toMutableSet()
        for (step in requiredSteps) {
            this.addDependency(result, step, availableAttributes)
        }

        return result
    }

    /**
     * Adds the dependencies for a step.
     */
    private fun addDependency(result: MutableList<ExecutableStep<*, TParameter>>, item: ExecutableStep<*, TParameter>, availableAttributes: MutableSet<Attribute<*>>) {
        if (result.contains(item)) {
            return
        }

        result.add(item)
        availableAttributes.add(item.attribute)

        for (dependency in item.dependencies.getMinimalRequiredAttributes(attributeToStep, availableAttributes)) {
            val step = attributeToStep[dependency]
                    ?: throw IllegalArgumentException("Inconsistent selector tree, a selector contains an dependency which doesn't exist: $item requires $dependency")

            this.addDependency(result, step, availableAttributes)
        }
    }

    /**
     * Creates a linear execution plan for given list of steps.
     */
    private fun orderSelectorsByDependencies(steps: List<ExecutableStep<*, TParameter>>): List<ExecutableStep<*, TParameter>> {
        val attributeToSelectorsWithConstants = HashMap(attributeToStep)
        for (step in steps) {
            attributeToSelectorsWithConstants[step.attribute] = step
        }

        return TopologicalSort.sort(steps, attributeToSelectorsWithConstants)
    }

    /**
     * Adds the column names to a filter expression.
     */
    private fun translatePropertyNames(queryInformation: QueryInformation, criteria: List<SimpleExpression<*>>): List<QueryFilter> {
        return criteria.map { expr ->
            val selector = attributeToStep[expr.attribute]?.column(queryInformation)
            if (selector != null) {
                QueryFilter(expr, selector)
            } else {
                QueryFilter(expr)
            }
        }.toList()
    }

    /**
     * Groups filters into one of two groups: Filters which can be executed together with the query and filters
     * which must be executed in Java.
     */
    private fun groupByQueryFilter(criteria: List<SimpleExpression<*>>): Map<Boolean, List<SimpleExpression<*>>> {
        return criteria.groupBy { isSupportedByQuery(it) }
    }

    /**
     * Indicates whether or not the given expression is an equality expression.
     */
    private fun isEqualityExpression(expr: SimpleExpression<*>): Boolean {
        return expr.operation == "="
    }

    /**
     * Intended to be overwritten. Indicates whether or not an expression can be added to the query.
     */
    protected open fun isSupportedByQuery(criteria: SimpleExpression<*>): Boolean {
        return true
    }
}
