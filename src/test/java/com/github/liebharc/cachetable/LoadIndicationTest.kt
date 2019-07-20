package com.github.liebharc.cachetable

import com.github.liebharc.queryenrichment.LoadIndicationTest
import com.github.liebharc.queryenrichment.Student
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.h2.jdbcx.JdbcDataSource
import org.junit.*
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate

import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.util.Arrays

class LoadIndicationTest : ResultSetAssertions() {
    private var connection: Connection? = null
    private var statement: Statement? = null
    private var dataSource: JdbcDataSource? = null
    private var jdbcTemplate: JdbcTemplate? = null
    private var cache: Cache<Long, Student>? = null

    @Before
    @Throws(SQLException::class)
    fun setupH2() {
        dataSource = JdbcDataSource()
        dataSource!!.setURL("jdbc:h2:mem:test")
        dataSource!!.user = "sa"
        connection = dataSource!!.connection
        jdbcTemplate = JdbcTemplate(dataSource!!)
    }

    @After
    @Throws(SQLException::class)
    fun disposeH2() {
        statement!!.close()
        connection!!.close()
    }

    private val query = "SELECT id, lastName FROM STUDENT WHERE ID >= 100 AND ID < 200"

    private val rangeSize = 100L

    @Test
    fun referenceRun() {
        statement = connection!!.createStatement()
        statement!!.execute("CREATE TABLE STUDENT ( ID bigint NOT NULL, firstName varchar(255), lastName varchar(255))")
        for (i in 0 until RESULT_SIZE) {
            statement!!.execute("INSERT INTO STUDENT(id, firstName, lastName) VALUES ($i, 'John', 'Smith')")
        }

        val start = System.currentTimeMillis()
        for (i in 0 until ITERATIONS) {
            val students =  consume(statement!!.executeQuery(query))
            Assert.assertEquals(rangeSize, students.size.toLong())
        }

        println("Duration [ms]: " + (System.currentTimeMillis() - start))
    }

    @Test
    fun actualRun() {
        statement = connection!!.createStatement()
        cache = CacheBuilder.newBuilder().build<Long, Student>()
        CacheTable.register(Long::class, Student::class, "STUDENT", cache!!)
        for (i in 0 until RESULT_SIZE) {
            cache!!.put(i, Student(i, "John", "Smith"))
            cache!!.put(11, Student(11, "Matt", "Smith"))
        }
        statement!!.execute("CREATE TABLE STUDENT ( ID bigint NOT NULL, FIRSTNAME varchar(255), LASTNAME varchar(255))\n" +
                "    ENGINE \"com.github.liebharc.cachetable.CacheTableEngine\";")

        val start = System.currentTimeMillis()
        for (i in 0 until ITERATIONS) {
            val students =  consume(statement!!.executeQuery(query))
            Assert.assertEquals(rangeSize, students.size.toLong())
        }

        println("Duration [ms]: " + (System.currentTimeMillis() - start))
    }

    companion object {
        private const val RESULT_SIZE: Long = 10000
        private const val ITERATIONS: Long = 2000
    }
}
