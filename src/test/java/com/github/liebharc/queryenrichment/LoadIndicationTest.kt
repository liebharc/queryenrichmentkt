package com.github.liebharc.queryenrichment

import org.h2.jdbcx.JdbcDataSource
import org.junit.*
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate

import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.util.Arrays

@Ignore
class LoadIndicationTest {
    private var connection: Connection? = null
    private var statement: Statement? = null
    private var dataSource: JdbcDataSource? = null
    private var jdbcTemplate: JdbcTemplate? = null

    @Before
    @Throws(SQLException::class)
    fun setupH2() {
        dataSource = JdbcDataSource()
        dataSource!!.setURL("jdbc:h2:mem:test")
        dataSource!!.user = "sa"
        connection = dataSource!!.connection
        jdbcTemplate = JdbcTemplate(dataSource!!)

        statement = connection!!.createStatement()
        statement!!.execute("CREATE TABLE STUDENT ( ID int(11) NOT NULL, firstName varchar(255), lastName varchar(255), classId int(11))")
        statement!!.execute("CREATE TABLE CLASS ( ID int(11) NOT NULL, DESCRIPTION varchar(255))")
        statement!!.execute("INSERT INTO CLASS(id, description) VALUES (1, 'Group a')")
        statement!!.execute("INSERT INTO CLASS(id, description) VALUES (2, 'Group b')")
        statement!!.execute("INSERT INTO CLASS(id, description) VALUES (3, 'Group c')")
        for (i in 0 until RESULT_SIZE) {
            statement!!.execute("INSERT INTO STUDENT(id, firstName, lastName, classId) VALUES ($i, 'John', 'Smith', 1)")
            statement!!.execute("INSERT INTO STUDENT(id, firstName, lastName, classId) VALUES ($i, 'John', 'Smith', 2)")
            statement!!.execute("INSERT INTO STUDENT(id, firstName, lastName, classId) VALUES ($i, 'John', 'Smith', 3)")
        }
    }

    @After
    @Throws(SQLException::class)
    fun disposeH2() {
        statement!!.execute("DROP TABLE CLASS")
        statement!!.execute("DROP TABLE STUDENT")
        statement!!.close()
        connection!!.close()
    }

    @Test
    fun jdbcTemplateReference() {
        val start = System.currentTimeMillis()
        for (i in 0 until ITERATIONS) {
            val students = jdbcTemplate!!.query("SELECT id, lastName, classId FROM STUDENT WHERE classId = 1", BeanPropertyRowMapper(StudentDAO::class.java))
            Assert.assertEquals(RESULT_SIZE, students.size.toLong())
        }

        println("Duration [ms]: " + (System.currentTimeMillis() - start))
    }

    @Test
    fun planIndication() {
        val start = System.currentTimeMillis()
        val planBuilder = H2PlanBuilder(connection, Arrays.asList(studentId, firstName, lastName, classId))
        val planCache = PlanCache(10, planBuilder)
        for (i in 0 until ITERATIONS) {
            val request = Request(Arrays.asList(Attributes.studentId, Attributes.lastName, Attributes.studentClass),
                    Arrays.asList(SimpleExpression.eq(Attributes.studentClass, 1L)))
            val plan = planCache.getOrBuildPlan(request)
            val result = plan.execute(request, null)
            Assert.assertEquals(RESULT_SIZE, result.results.size.toLong())
        }

        println("Duration [ms]: " + (System.currentTimeMillis() - start))
    }

    // Getters/setters are required by JDBC template
    class StudentDAO {
        var id: Long = 0
        var firstName: String? = null
        var lastName: String? = null
        var classId: Long = 0
    }

    companion object {
        private val RESULT_SIZE: Long = 10000
        private val ITERATIONS: Long = 200

        val studentId = SelectorBuilder(Attributes.studentId).addColumn("ID").build()
        val firstName = SelectorBuilder(Attributes.firstName).addColumn("firstName").build()
        val lastName = SelectorBuilder(Attributes.lastName).addColumn("lastName").build()
        val classId = SelectorBuilder(Attributes.studentClass).addColumn("classId").build()
    }
}
