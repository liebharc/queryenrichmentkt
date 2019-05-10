package com.github.liebharc.queryenrichment;

import org.junit.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class H2PlanBuilderTest {

    private Connection connection;
    private Statement statement;

    @Before
    public void setupH2() throws SQLException {
        connection = DriverManager.
                getConnection("jdbc:h2:mem:test", "sa", "");
        statement = connection.createStatement();
        statement.execute("CREATE TABLE STUDENT ( ID int(11) NOT NULL, FIRST_NAME varchar(255), LAST_NAME varchar(255), CLASS int(11))");
        statement.execute("CREATE TABLE CLASS ( ID int(11) NOT NULL, DESCRIPTION varchar(255))");
        statement.execute("INSERT INTO CLASS(id, description) VALUES (1, 'Doctor for everything')");
        statement.execute("INSERT INTO STUDENT(id, first_name, last_name, class) VALUES (10, 'David', 'Tenant', 1)");
        statement.execute("INSERT INTO STUDENT(id, first_name, last_name, class) VALUES (11, 'Matt', 'Smith', 1)");
    }

    @After
    public void disposeH2() throws SQLException {
        statement.execute("DROP TABLE CLASS");
        statement.execute("DROP TABLE STUDENT");
        statement.close();
        connection.close();
    }

    @Test
    public void queryTest() {
        final List<Step<?>> steps = this.createDefaultSelectors();
        final PlanBuilder planBuilder = new H2PlanBuilder(connection, steps);

        final Request request = new Request(
                Arrays.asList(Attributes.studentId,
                        Attributes.lastName,
                        Attributes.firstName));
        final Plan plan = planBuilder.build(request);

        final String stringResult = this.resultToString(plan.execute(request));
        Assert.assertEquals(
                "10,Tenant,David\n" +
                "11,Smith,Matt", stringResult);
    }

    @Test
    public void withCriteriaTest() {
        final List<Step<?>> steps = this.createDefaultSelectors();
        final PlanBuilder planBuilder = new H2PlanBuilder(connection, steps);

        final SimpleExpression criterion = SimpleExpression.neq(Attributes.studentId, 11L);
        final Request request = new Request(
                Arrays.asList(Attributes.studentId,
                        Attributes.lastName,
                        Attributes.firstName),
                Collections.singletonList(criterion));
        final Plan plan = planBuilder.build(request);

        final String stringResult = this.resultToString(plan.execute(request));
        Assert.assertEquals(
                "10,Tenant,David", stringResult);
    }

    @Test
    public void replaceSelectorByFilterTest() {
        final List<Step<?>> steps = this.createDefaultSelectors();
        final PlanBuilder planBuilder = new H2PlanBuilder(connection, steps);

        final SimpleExpression criterion = SimpleExpression.eq(Attributes.firstName, "David");
        final Request request = new Request(
                Arrays.asList(Attributes.studentId,
                        Attributes.lastName,
                        Attributes.firstName),
                Collections.singletonList(criterion));
        final Plan plan = planBuilder.build(request);

        Assert.assertEquals(2, plan.getSteps().stream().filter(sel -> !(sel instanceof AddValuesFromFilter)).count());
        final String stringResult = this.resultToString(plan.execute(request));
        Assert.assertEquals(
                "10,Tenant,David", stringResult);
    }

    @Test
    public void executeSimpleQueryTest() {
        final List<Step<?>> steps = this.createDefaultSelectors();
        final PlanBuilder planBuilder = new H2PlanBuilder(connection, steps);

        final SimpleExpression criterion = SimpleExpression.eq(Attributes.firstName, "David");
        final Request request = new Request(
                Arrays.asList(Attributes.studentId,
                        Attributes.lastName,
                        Attributes.firstName),
                Collections.singletonList(criterion));
        final Plan plan = planBuilder.build(request);
        final EnrichedQueryResult result = plan.execute(request);
        Assert.assertEquals(
                "10,Tenant,David", this.resultToString(result));
    }

    @Test
    public void enrichmentWithManualDependencyResolutionTest() {
        final List<Step<?>> steps = this.createDefaultSelectors();
        final PlanBuilder planBuilder = new H2PlanBuilder(connection, steps);

        final Request request = new Request(
                Arrays.asList(Attributes.studentId,
                        Attributes.firstName,
                        Attributes.lastName,
                        Attributes.fullName));
        final Plan plan = planBuilder.build(request);
        final EnrichedQueryResult result = plan.execute(request);
        Assert.assertEquals(
                "10,David,Tenant,David Tenant\n" +
                         "11,Matt,Smith,Matt Smith", this.resultToString(result));
    }

    @Test
    public void enrichmentWithAutomaticDependencyResolutionTest() {
        final List<Step<?>> steps = this.createDefaultSelectors();
        final PlanBuilder planBuilder = new H2PlanBuilder(connection, steps);

        final Request request = new Request(
                Arrays.asList(Attributes.studentId,
                        Attributes.fullName));
        final Plan plan = planBuilder.build(request);
        final EnrichedQueryResult result = plan.execute(request);
        Assert.assertEquals(
                "10,David Tenant\n" +
                         "11,Matt Smith", this.resultToString(result));
    }

    @Test
    public void constantsTest() {
        H2QueryBuilder.classIdStringCalls = 0;
        final List<Step<?>> steps = this.createDefaultSelectors();
        final PlanBuilder planBuilder = new H2PlanBuilder(connection, steps);
        final Request request = new Request(Arrays.asList(Attributes.classIdString), Arrays.asList(SimpleExpression.eq(Attributes.studentClass, 1L)));
        final Plan plan = planBuilder.build(request);
        final EnrichedQueryResult result = plan.execute(request);
        Assert.assertEquals(
                "Class: 1\n" +
                        "Class: 1", this.resultToString(result));
        Assert.assertEquals(1, H2QueryBuilder.classIdStringCalls);
    }

    private String resultToString(EnrichedQueryResult result) {
        return Arrays.stream(result.getResults())
                .map(row -> Arrays.stream(row)
                        .map(obj -> obj != null ? obj.toString() : "<null>").collect(Collectors.joining(",")))
                .collect(Collectors.joining("\n"));
    }

    private List<Step<?>> createDefaultSelectors() {
        return Arrays.asList(
                H2QueryBuilder.studentId,
                H2QueryBuilder.firstName,
                H2QueryBuilder.lastName,
                H2QueryBuilder.fullName,
                H2QueryBuilder.studentClass,
                H2QueryBuilder.classIdString);
    }
}
