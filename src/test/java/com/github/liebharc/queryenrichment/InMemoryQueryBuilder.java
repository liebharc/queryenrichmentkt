package com.github.liebharc.queryenrichment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryQueryBuilder implements QueryBuilder {
    public static final Database database = new Database();

    public static final Step<Long> studentId = new SelectorBuilder<>(Attributes.studentId).addColumn("ID").build();
    public static final Step<String> firstName = new SelectorBuilder<>(Attributes.firstName).addColumn("FIRST_NAME").build();
    public static final Step<String> lastName = new SelectorBuilder<>(Attributes.lastName).addColumn("LAST_NAME").build();
    public static final Step<String> fullName = new ParameterlessEnrichment<String>(Attributes.fullName, Dependencies.requireOneOf(Attributes.firstName, Attributes.lastName)) {
        @Override
        public void enrich(IntermediateResult result) {
            final String firstName = result.get(Attributes.firstName);
            final String lastName = result.get(Attributes.lastName);
            if (firstName != null && lastName != null) {
                result.add(this, firstName + " " + lastName);
            } else if (firstName != null) {
                result.add(this, firstName);
            } else if (lastName != null) {
                result.add(this, lastName);
            } else {
                throw new RuntimeException("At least one of firstName and lastName must be available");
            }
        }
    };

    public InMemoryQueryBuilder() {
    }

    @Override
    public com.github.liebharc.queryenrichment.Query build(List<QuerySelector> selectors, List<QueryFilter> filters) {
        if (!filters.isEmpty()) {
            throw new IllegalArgumentException("This class doesn't support criteria");
        }

        return new Query(selectors);
    }

    public static class Database {
        public final List<Student> students = new ArrayList<>();

        public void clear() {
            students.clear();
        }

        public void add(Student student) {
            students.add(student);
        }

        public void setup(Student... students) {
            this.clear();
            for (Student student : students) {
                this.add(student);
            }

        }
    }

    public class Query implements com.github.liebharc.queryenrichment.Query {

        private final List<QuerySelector> steps;

        public Query(List<QuerySelector> steps) {
            this.steps = steps;
        }

        @Override
        public QueryResult query(Request request) {
            List<List<Object>> rows = database.students.stream().map(student ->
                    steps.stream().map(selector -> {
                        Attribute<?> attribute = selector.getAttribute();
                        if (attribute.equals(Attributes.studentId)) {
                            return (Object)student.getId();
                        } else if (attribute.equals(Attributes.firstName)) {
                            return (Object)student.getFirstName();
                        } else if (attribute.equals(Attributes.lastName)) {
                            return (Object)student.getLastName();
                        }

                        throw new IllegalArgumentException("Unknown column " + selector);
                    }).collect(Collectors.toList())).collect(Collectors.toList());
            return new QueryResult(rows);
        }
    }
}
