package com.github.liebharc.cachetable

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.sql.*
import java.util.ArrayList

class CacheTableStringValueTest : ResultSetAssertions() {
    private var connection: Connection? = null
    private var statement: Statement? = null
    private var cache: Cache<Long, String>? = null

    @Before
    @Throws(SQLException::class)
    fun setupH2() {
        cache = CacheBuilder.newBuilder().build<Long, String>()
        CacheTable.register(Long::class, String::class, "CLASS", cache!!)
        cache!!.put(1, "Test")
        cache!!.put(2, "Test2")
        cache!!.put(3, "Test3")
        connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "")
        statement = connection!!.createStatement()
        statement!!.execute("CREATE TABLE CLASS ( ID bigint NOT NULL, DESCRIPTION varchar(255))\n" +
                "    ENGINE \"com.github.liebharc.cachetable.CacheTableEngine\";")
    }

    @After
    @Throws(SQLException::class)
    fun disposeH2() {
        statement!!.close()
        connection!!.close()
    }

    @Test
    fun selectColumnsTest() {
        var result = consume(statement!!.executeQuery("SELECT DESCRIPTION FROM CLASS WHERE ID = 2"))
        assertEquals(listOf(
                listOf("Test2")), result)
    }

    @Test
    fun queryAndSortById() {
        var result = consume(statement!!.executeQuery("SELECT * FROM CLASS WHERE ID = 2"))
        assertEquals(listOf(
                listOf(2L, "Test2")), result)
        result = consume(statement!!.executeQuery("SELECT * FROM CLASS WHERE ID IN (1, 3) ORDER BY ID"))
        assertEquals(listOf(
                listOf(1L, "Test"),
                listOf(3L, "Test3")), result)
        cache!!.put(4, "Test4")
        result = consume(statement!!.executeQuery("SELECT * FROM CLASS ORDER BY ID"))
        assertEquals(listOf(
                listOf(1L, "Test"),
                listOf(2L, "Test2"),
                listOf(3L, "Test3"),
                listOf(4L, "Test4")), result)
        result = consume(statement!!.executeQuery("SELECT * FROM CLASS WHERE ID > 1 ORDER BY ID"))
        assertEquals(listOf(
                listOf(2L, "Test2"),
                listOf(3L, "Test3"),
                listOf(4L, "Test4")), result)
    }

    @Test
    fun queryAndSortByValue() {
        var result = consume(statement!!.executeQuery("SELECT * FROM CLASS WHERE DESCRIPTION = 'Test2'"))
        assertEquals(listOf(
                listOf(2L, "Test2")), result)
        result = consume(statement!!.executeQuery("SELECT * FROM CLASS WHERE DESCRIPTION IN ('Test', 'Test3') ORDER BY DESCRIPTION"))
        assertEquals(listOf(
                listOf(1L, "Test"),
                listOf(3L, "Test3")), result)
        cache!!.put(4, "Test4")
        result = consume(statement!!.executeQuery("SELECT * FROM CLASS WHERE DESCRIPTION LIKE '%2' ORDER BY DESCRIPTION"))
        assertEquals(listOf(
                listOf(2L, "Test2")), result)
        result = consume(statement!!.executeQuery("SELECT * FROM CLASS ORDER BY DESCRIPTION"))
        assertEquals(listOf(
                listOf(1L, "Test"),
                listOf(2L, "Test2"),
                listOf(3L, "Test3"),
                listOf(4L, "Test4")), result)
    }
}