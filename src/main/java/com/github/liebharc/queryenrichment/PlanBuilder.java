package com.github.liebharc.queryenrichment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates a plan for a query. This class is intended to be subclassed so that implementors can provide their
 * query builder.
 */
public abstract class PlanBuilder<TParameter> {
    /** Maps attributes to the step which creates that attribute */
    private final Map<Attribute<?>, ExecutableStep<?, TParameter>> attributeToStep = new HashMap<>();

    public PlanBuilder(List<ExecutableStep<?, TParameter>> steps) {

        // Create lookup tables and check steps
        final StringBuilder errorBuilder = new StringBuilder(0);
        for (ExecutableStep<?, TParameter> step : steps) {
            final ExecutableStep<?, TParameter> previousMapping= this.attributeToStep.put(step.getAttribute(), step);

            if (previousMapping != null) {
                errorBuilder.append(step.getAttribute())
                        .append("  has more than one step; ")
                        .append(previousMapping).append(" and ")
                        .append(step).append("\n");
            }
        }

        final String errors = errorBuilder.toString();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Failed to plan query:\n" + errors);
        }
    }

    /**
     * Builds the plan for a request.
     */
    public Plan build(Request request) {
        if (request.getAttributes().isEmpty()) {
            throw new IllegalArgumentException("At least one attribute must be requested");
        }

        final Map<Boolean, List<SimpleExpression>> groupedByQueryFilter = this.groupByQueryFilter(request.getCriteria());
        final List<ExecutableStep<?, TParameter>> filterSteps =
                this.addDependencies(
                        this.createFilterSteps(
                                groupedByQueryFilter.getOrDefault(false, Collections.emptyList())));
        List<SimpleExpression> sqlQueryExpressions = groupedByQueryFilter.getOrDefault(true, Collections.emptyList());
        final List<ExecutableStep<?, TParameter>> allRequiredSteps =
                this.addStepsForFilters(filterSteps,
                    this.injectConstants(sqlQueryExpressions,
                        this.addDependencies(
                            this.findRequiredSteps(sqlQueryExpressions, request))));
        final List<ExecutableStep<?, TParameter>> orderedSteps = this.orderSelectorsByDependencies(allRequiredSteps);
        final List<QueryFilter> filters =
                this.translatePropertyNames(
                        groupedByQueryFilter.getOrDefault(true, Collections.emptyList()));
        final List<QuerySelector> queryColumns =
                orderedSteps.stream()
                        .filter(sel -> sel.getColumn().isPresent())
                        .map(sel -> new QuerySelector(sel.getAttribute(), sel.getColumn().get()))
                        .collect(Collectors.toList());
        final Map<Boolean, List<ExecutableStep<?, TParameter>>> groupedByConstant = this.groupByConstant(orderedSteps);
        return new Plan<>(
                request.getAttributes(),
                groupedByConstant.get(true),
                groupedByConstant.get(false),
                this.getQueryBuilder().build(queryColumns, filters));
    }

    /**
     * Joins the lists of selector/enrichment and filter steps.
     */
    private List<ExecutableStep<?, TParameter>> addStepsForFilters(List<ExecutableStep<?, TParameter>> filterSteps, List<ExecutableStep<?, TParameter>> requiredSelectors) {
        final List<ExecutableStep<?, TParameter>> result = new ArrayList<>(filterSteps.size() + requiredSelectors.size());
        result.addAll(filterSteps);
        for (ExecutableStep<?, TParameter> selector : requiredSelectors) {
            if (!result.contains(selector)) {
                result.add(selector);
            }
        }

        return result;
    }

    /**
     * Creates Java filters for the given filter expressions.
     */
    private List<ExecutableStep<?, TParameter>> createFilterSteps(List<SimpleExpression> javaFilters) {
        return javaFilters.stream().map(expr -> {
            final ExecutableStep<?, TParameter> step = attributeToStep.get(expr.getAttribute());
            if (step == null) {
                throw new IllegalArgumentException("Failed to find selector for expression " + expr);
            }

            return FilterStep.createFilter(step, expr);
        }).collect(Collectors.toList());
    }

    /**
     * Groups the given list of steps in constant/not-constant.
     */
    private Map<Boolean, List<ExecutableStep<?, TParameter>>> groupByConstant(List<ExecutableStep<?, TParameter>> steps) {
        final List<ExecutableStep<?, TParameter>> constant = new ArrayList<>();
        final Set<Attribute<?>> constantAttributes = new HashSet<>();
        final List<ExecutableStep<?, TParameter>> notConstant = new ArrayList<>();
        for (ExecutableStep<?, TParameter> step : steps) {
            if (step.isConstant() && step.getDependencies().isEmpty()) {
                constant.add(step);
                constantAttributes.add(step.getAttribute());
            }
            else if (!step.getColumn().isPresent() && step.getDependencies().isOkay(constantAttributes)) {
                constant.add(step);
                constantAttributes.add(step.getAttribute());
            }
            else {
                notConstant.add(step);
            }
        }

        final Map<Boolean, List<ExecutableStep<?, TParameter>>> result = new HashMap<>();
        result.put(false, notConstant);
        result.put(true, constant);
        return result;
    }

    /**
     * Replaces steps by constants where possible.
     */
    private List<ExecutableStep<?, TParameter>> injectConstants(List<SimpleExpression> queryFilters, List<ExecutableStep<?, TParameter>> steps) {
        Map<Attribute<?>, SimpleExpression> equalityFilters = queryFilters.stream()
                .filter(this::isEqualityExpression)
                .collect(Collectors.toMap(SimpleExpression::getAttribute, expr -> expr));

        return steps.stream().map(step -> {
            if (step.isConstant()) {
                return step;
            }

            final SimpleExpression filterExpression = equalityFilters.get(step.getAttribute());
            if (filterExpression != null) {
                return AddValuesFromFilter.<TParameter>create(step.getAttribute(), filterExpression);
            }
            else {
                return step;
            }
        }).collect(Collectors.toList());
    }

    /**
     * Find the required steps to implement the given list of filters in Java.
     */
    private List<ExecutableStep<?, TParameter>> findRequiredSteps(List<SimpleExpression> queryExpressions, Request request) {
        Map<Attribute<?>, SimpleExpression> equalityFilters = queryExpressions.stream()
                .filter(this::isEqualityExpression)
                .collect(Collectors.toMap(SimpleExpression::getAttribute, expr -> expr));

        return request.getAttributes().stream().map(attr -> {
            SimpleExpression filterExpression = equalityFilters.get(attr);
            if (filterExpression != null) {
                return AddValuesFromFilter.<TParameter>create(attr, filterExpression);
            }
            else {
                return attributeToStep.get(attr);
            }
        }).collect(Collectors.toList());
    }

    /**
     * Adds the dependencies for all steps.
     */
    private List<ExecutableStep<?, TParameter>> addDependencies(List<ExecutableStep<?, TParameter>> requiredSteps) {
        final List<ExecutableStep<?, TParameter>> result = new ArrayList<>();
        final Set<Attribute<?>> availableAttributes =
                requiredSteps.stream().map(Step::getAttribute).collect(Collectors.toSet());
        for (ExecutableStep<?, TParameter> step : requiredSteps) {
            this.addDependency(result, step, availableAttributes);
        }

        return result;
    }

    /**
     * Adds the dependencies for a step.
     */
    private void addDependency(List<ExecutableStep<?, TParameter>> result, ExecutableStep<?, TParameter> item, Set<Attribute<?>> availableAttributes) {
        if (result.contains(item)) {
            return;
        }

        result.add(item);
        availableAttributes.add(item.getAttribute());

        for (Attribute<?> dependency : item.getDependencies().getMinimalRequiredAttributes(availableAttributes)) {
            final ExecutableStep<?, TParameter> step = attributeToStep.get(dependency);
            if (step == null) {
                throw new IllegalArgumentException("Inconsistent selector tree, a selector contains an dependency which doesn't exist: " + item + " requires " + dependency);
            }

            this.addDependency(result, step, availableAttributes);
        }
    }

    /**
     * Creates a linear execution plan for given list of steps.
     */
    private List<ExecutableStep<?, TParameter>> orderSelectorsByDependencies(List<ExecutableStep<?, TParameter>> steps) {
        final Map<Attribute<?>, ExecutableStep<?, TParameter>> attributeToSelectorsWithConstants = new HashMap<>(attributeToStep);
        for (ExecutableStep<?, TParameter> step : steps) {
            if (step.isConstant()) {
                attributeToSelectorsWithConstants.put(step.getAttribute(), step);
            }
        }

        return TopologicalSort.INSTANCE.sort(steps, attributeToSelectorsWithConstants);
    }

    /**
     * Adds the column names to a filter expression.
     */
    private List<QueryFilter> translatePropertyNames(List<SimpleExpression> criteria) {
        return criteria.stream().map(expr -> {
            final Optional<String> selector = Optional.ofNullable(attributeToStep.get(expr.getAttribute())).flatMap(Step::getColumn);
            return selector.map(s -> new QueryFilter(expr, s)).orElse(new QueryFilter(expr));
        }).collect(Collectors.toList());
    }

    /**
     * Groups filters into one of two groups: Filters which can be executed together with the query and filters
     * which must be executed in Java.
     */
    private Map<Boolean, List<SimpleExpression>> groupByQueryFilter(List<SimpleExpression> criteria) {
        return criteria.stream().collect(Collectors.groupingBy(this::isSupportedByQuery));
    }

    /**
     * Indicates whether or not the given expression is an equality expression.
     */
    private boolean isEqualityExpression(SimpleExpression expr) {
        return expr.getOperation().equals("=");
    }

    /**
     * Intended to be overwritten. Indicates whether or not an expression can be added to the query.
     */
    protected boolean isSupportedByQuery(SimpleExpression criteria) {
        return true;
    }

    /**
     * Intended to be overwritten. Provides the concrete query builder which should be used.
     */
    protected abstract QueryBuilder getQueryBuilder();
}
