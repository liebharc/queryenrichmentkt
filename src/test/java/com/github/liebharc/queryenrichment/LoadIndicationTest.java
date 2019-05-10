package com.github.liebharc.queryenrichment;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.*;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public class LoadIndicationTest {
    private final static long RESULT_SIZE = 10000;
    private final static long ITERATIONS = 200;
    private Connection connection;
    private Statement statement;
    private JdbcDataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public static final Step<Long> studentId = new SelectorBuilder<>(Attributes.studentId).addColumn("ID").build();
    public static final Step<String> firstName = new SelectorBuilder<>(Attributes.firstName).addColumn("firstName").build();
    public static final Step<String> lastName = new SelectorBuilder<>(Attributes.lastName).addColumn("lastName").build();
    public static final Step<Long> classId = new SelectorBuilder<>(Attributes.studentClass).addColumn("classId").build();

    @Before
    public void setupH2() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test");
        dataSource.setUser("sa");
        connection = dataSource.getConnection();
        jdbcTemplate = new JdbcTemplate(dataSource);

        statement = connection.createStatement();
        statement.execute("CREATE TABLE STUDENT ( ID int(11) NOT NULL, firstName varchar(255), lastName varchar(255), classId int(11))");
        statement.execute("CREATE TABLE CLASS ( ID int(11) NOT NULL, DESCRIPTION varchar(255))");
        statement.execute("INSERT INTO CLASS(id, description) VALUES (1, 'Group a')");
        statement.execute("INSERT INTO CLASS(id, description) VALUES (2, 'Group b')");
        statement.execute("INSERT INTO CLASS(id, description) VALUES (3, 'Group c')");
        for (int i = 0; i < RESULT_SIZE; i++) {
            statement.execute("INSERT INTO STUDENT(id, firstName, lastName, classId) VALUES ("+i+", 'John', 'Smith', 1)");
            statement.execute("INSERT INTO STUDENT(id, firstName, lastName, classId) VALUES ("+i+", 'John', 'Smith', 2)");
            statement.execute("INSERT INTO STUDENT(id, firstName, lastName, classId) VALUES ("+i+", 'John', 'Smith', 3)");
        }
    }

    @After
    public void disposeH2() throws SQLException {
        statement.execute("DROP TABLE CLASS");
        statement.execute("DROP TABLE STUDENT");
        statement.close();
        connection.close();
    }

    @Test
    public void jdbcTemplateReference() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            List<StudentDAO> students = jdbcTemplate.query("SELECT id, lastName, classId FROM STUDENT WHERE classId = 1", new BeanPropertyRowMapper<>(StudentDAO.class));
            Assert.assertEquals(RESULT_SIZE, students.size());
        }

        System.out.println("Duration [ms]: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void planIndication() {
        long start = System.currentTimeMillis();
        final PlanBuilder planBuilder = new H2PlanBuilder(connection, Arrays.asList(studentId, firstName, lastName, classId));
        final PlanCache planCache = new PlanCache(10, planBuilder);
        for (int i = 0; i < ITERATIONS; i++) {
            final Request request =
                    new Request(Arrays.asList(Attributes.studentId, Attributes.lastName, Attributes.studentClass),
                            Arrays.asList(SimpleExpression.eq(Attributes.studentClass, 1L)));
            Plan plan = planCache.getOrBuildPlan(request);
            EnrichedQueryResult result = plan.execute(request);
            Assert.assertEquals(RESULT_SIZE, result.getResults().length);
        }

        System.out.println("Duration [ms]: " + (System.currentTimeMillis() - start));
    }

    // Getters/setters are required by JDBC template
    @SuppressWarnings("unused")
    public static class StudentDAO {
        private long id;
        private String firstName;
        private String lastName;
        private long classId;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public long getClassId() {
            return classId;
        }

        public void setClassId(long classId) {
            this.classId = classId;
        }
    }
}
