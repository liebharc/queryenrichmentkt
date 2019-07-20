package com.github.liebharc.cachetable

import com.github.liebharc.queryenrichment.InMemoryQueryBuilder
import com.github.liebharc.queryenrichment.Student
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.sql.*
import java.util.ArrayList

class CacheTableComplexValueTest : ResultSetAssertions() {
    private var connection: Connection? = null
    private var statement: Statement? = null
    private var cache: Cache<Long, Student>? = null

    @Before
    @Throws(SQLException::class)
    fun setupH2() {
        cache = CacheBuilder.newBuilder().build<Long, Student>()
        CacheTable.register(Long::class, Student::class, "STUDENT", cache!!)
        cache!!.put(10, Student(10, "David", "Tenant"))
        cache!!.put(11, Student(11, "Matt", "Smith"))
        connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "")
        statement = connection!!.createStatement()
        statement!!.execute("CREATE TABLE STUDENT ( ID bigint NOT NULL, FIRSTNAME varchar(255), LASTNAME varchar(255))\n" +
                "    ENGINE \"com.github.liebharc.cachetable.CacheTableEngine\";")
    }

    @After
    @Throws(SQLException::class)
    fun disposeH2() {
        statement!!.close()
        connection!!.close()
    }

    @Test
    fun queryByIdTest() {
        var result = consume(statement!!.executeQuery("SELECT * FROM STUDENT WHERE ID = 10"))
        assertEquals(listOf(
                listOf("10", "David", "Tenant")), result)
    }

    @Test
    fun queryByColumnTest() {
        var result = consume(statement!!.executeQuery("SELECT * FROM STUDENT WHERE FIRSTNAME = 'David'"))
        assertEquals(listOf(
                listOf("10", "David", "Tenant")), result)
    }
}