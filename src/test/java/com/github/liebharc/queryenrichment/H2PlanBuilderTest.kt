package com.github.liebharc.queryenrichment

import org.junit.*

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.Arrays
import java.util.Collections
import java.util.stream.Collectors

class H2PlanBuilderTest {

    private var connection: Connection? = null
    private var statement: Statement? = null

    @Before
    @Throws(SQLException::class)
    fun setupH2() {
        connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "")
        statement = connection!!.createStatement()
        statement!!.execute("CREATE TABLE STUDENT ( ID int(11) NOT NULL, FIRST_NAME varchar(255), LAST_NAME varchar(255), CLASS int(11))")
        statement!!.execute("CREATE TABLE CLASS ( ID int(11) NOT NULL, DESCRIPTION varchar(255))")
        statement!!.execute("INSERT INTO CLASS(id, description) VALUES (1, 'Doctor for everything')")
        statement!!.execute("INSERT INTO STUDENT(id, first_name, last_name, class) VALUES (10, 'David', 'Tenant', 1)")
        statement!!.execute("INSERT INTO STUDENT(id, first_name, last_name, class) VALUES (11, 'Matt', 'Smith', 1)")
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
    fun queryTest() {
        val steps = this.createDefaultSelectors()
        val planBuilder = H2PlanBuilder(connection!!, steps)

        val request = Request(
                listOf(Attributes.studentId,
                        Attributes.lastName,
                        Attributes.firstName))
        val plan = planBuilder.build(request)

        val stringResult = this.resultToString(plan.execute(request, null))
        Assert.assertEquals(
                "10,Tenant,David\n" + "11,Smith,Matt", stringResult)
    }

    @Test
    fun withCriteriaTest() {
        val steps = this.createDefaultSelectors()
        val planBuilder = H2PlanBuilder(connection, steps)

        val criterion = SimpleExpression.neq(Attributes.studentId, 11L)
        val request = Request(
                listOf(Attributes.studentId,
                        Attributes.lastName,
                        Attributes.firstName),
                listOf(criterion))
        val plan = planBuilder.build(request)

        val stringResult = this.resultToString(plan.execute(request, null))
        Assert.assertEquals(
                "10,Tenant,David", stringResult)
    }

    @Test
    fun replaceSelectorByFilterTest() {
        val steps = this.createDefaultSelectors()
        val planBuilder = H2PlanBuilder(connection, steps)

        val criterion = SimpleExpression.eq(Attributes.firstName, "David")
        val request = Request(
                listOf(Attributes.studentId,
                        Attributes.lastName,
                        Attributes.firstName),
                listOf(criterion))
        val plan = planBuilder.build(request)

        Assert.assertEquals(2, plan.steps.count { it !is AddValuesFromFilter })
        val stringResult = this.resultToString(plan.execute(request, null))
        Assert.assertEquals(
                "10,Tenant,David", stringResult)
    }

    @Test
    fun executeSimpleQueryTest() {
        val steps = this.createDefaultSelectors()
        val planBuilder = H2PlanBuilder(connection, steps)

        val criterion = SimpleExpression.eq(Attributes.firstName, "David")
        val request = Request(
                listOf(Attributes.studentId,
                        Attributes.lastName,
                        Attributes.firstName),
                listOf(criterion))
        val plan = planBuilder.build(request)
        val result = plan.execute(request, null)
        Assert.assertEquals(
                "10,Tenant,David", this.resultToString(result))
    }

    @Test
    fun enrichmentWithManualDependencyResolutionTest() {
        val steps = this.createDefaultSelectors()
        val planBuilder = H2PlanBuilder(connection, steps)

        val request = Request(
                listOf(Attributes.studentId,
                        Attributes.firstName,
                        Attributes.lastName,
                        Attributes.fullName))
        val plan = planBuilder.build(request)
        val result = plan.execute(request, null)
        Assert.assertEquals(
                "10,David,Tenant,David Tenant\n" + "11,Matt,Smith,Matt Smith", this.resultToString(result))
    }

    @Test
    fun enrichmentWithAutomaticDependencyResolutionTest() {
        val steps = this.createDefaultSelectors()
        val planBuilder = H2PlanBuilder(connection, steps)

        val request = Request(
                listOf(Attributes.studentId,
                        Attributes.fullName))
        val plan = planBuilder.build(request)
        val result = plan.execute(request, null)
        Assert.assertEquals(
                "10,David Tenant\n" + "11,Matt Smith", this.resultToString(result))
    }

    @Test
    fun constantsTest() {
        H2QueryBuilder.classIdStringCalls = 0
        val steps = this.createDefaultSelectors()
        val planBuilder = H2PlanBuilder(connection, steps)
        val request = Request(listOf(Attributes.classIdString), Arrays.asList(SimpleExpression.eq(Attributes.studentClass, 1L)))
        val plan = planBuilder.build(request)
        val result = plan.execute(request, null)
        Assert.assertEquals(
                "Class: 1\n" + "Class: 1", this.resultToString(result))
        Assert.assertEquals(1, H2QueryBuilder.classIdStringCalls.toLong())
    }

    private fun resultToString(result: EnrichedQueryResult): String {
        return result.results.map { row -> row.joinToString(",") { it?.toString() ?: "<null>"  } }
                .joinToString("\n")
    }

    private fun createDefaultSelectors(): List<ExecutableStep<*, Any?>> {
        return listOf(
                H2QueryBuilder.studentId,
                H2QueryBuilder.firstName,
                H2QueryBuilder.lastName,
                H2QueryBuilder.fullName,
                H2QueryBuilder.studentClass,
                H2QueryBuilder.classIdString)
    }
}
