package com.github.liebharc.queryenrichment;

import java.util.List;

public class InMemoryPlanBuilder extends PlanBuilder {

    public InMemoryPlanBuilder(List<Step<?>> steps) {
        super(steps);
    }

    @Override
    protected QueryBuilder getQueryBuilder() {
        return new InMemoryQueryBuilder();
    }

    @Override
    protected boolean isSupportedByQuery(SimpleExpression criteria) {
        return false;
    }
}
