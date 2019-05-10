package com.github.liebharc.queryenrichment;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

public class H2PlanBuilder extends PlanBuilder {

    private final Connection connection;

    public H2PlanBuilder(Connection connection, List<Step<?>> steps) {
        super(steps);
        this.connection = connection;
    }

    @Override
    protected QueryBuilder getQueryBuilder() {
        return new H2QueryBuilder(connection);
    }
}
