package com.github.liebharc.queryenrichment;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class H2QueryBuilder implements QueryBuilder {

    public static int classIdStringCalls = 0;

    public static final Step<Long> studentId = new SelectorBuilder<>(Attributes.studentId).addColumn("ID").build();
    public static final Step<String> firstName = new SelectorBuilder<>(Attributes.firstName).addColumn("FIRST_NAME").build();
    public static final Step<String> lastName = new SelectorBuilder<>(Attributes.lastName).addColumn("LAST_NAME").build();
    public static final Step<Long> studentClass = new SelectorBuilder<>(Attributes.studentClass).addColumn("CLASS").build();
    public static final Step<String> fullName = new ParameterlessEnrichment<String>(Attributes.fullName, Dependencies.requireAll(Attributes.firstName, Attributes.lastName)) {
        @Override
        public void enrich(IntermediateResult result) {
            result.add(this, result.get(Attributes.firstName) + " " + result.get(Attributes.lastName));
        }
    };

    public static final Step<String> classIdString = new ParameterlessEnrichment<String>(Attributes.classIdString, Dependencies.require(Attributes.studentClass)) {
        @Override
        public void enrich(IntermediateResult result) {
            classIdStringCalls++;
            result.add(this, "Class: " + result.get(Attributes.studentClass));
        }
    };

    private final Connection connection;

    H2QueryBuilder(Connection connection) {
        this.connection = connection;
    }

    @Override
    public com.github.liebharc.queryenrichment.Query build(List<QuerySelector> selectors, List<QueryFilter> filters) {
        final String select = this.createSelectStatement(selectors);
        final StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append(select);
        query.append(" FROM ");
        query.append("student");

        if (!filters.isEmpty()) {
            query.append(" WHERE ");
            query.append(FilterUtils.getSqlCriteria(filters));
        }

        try {
            return new Query(connection.prepareStatement(query.toString()), selectors);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String createSelectStatement(List<QuerySelector> steps) {
        if (steps.isEmpty()) {
            return "1";
        }

        return steps.stream().map(QuerySelector::getColumn).collect(Collectors.joining(", "));
    }

    private class Query implements com.github.liebharc.queryenrichment.Query {

        private final PreparedStatement query;
        private final List<QuerySelector> selectors;

        Query(PreparedStatement query, List<QuerySelector> selectors) {
            this.query = query;
            this.selectors = selectors;
        }

        @Override
        public QueryResult query(Request request) {
            try {
                int pos = 1;
                for (SimpleExpression criterion : request.getCriteria()) {
                    query.setObject(pos, criterion.getValue());
                    pos++;
                }

                ResultSet resultSet = query.executeQuery();

                final List<List<Object>> results = new ArrayList<>();
                while (resultSet.next()) {
                    final List<Object> row = new ArrayList<>();
                    for (int i = 1; i <= selectors.size(); i++) {
                        row.add(resultSet.getObject(i));
                    }

                    results.add(row);
                }

                return new QueryResult(results);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
