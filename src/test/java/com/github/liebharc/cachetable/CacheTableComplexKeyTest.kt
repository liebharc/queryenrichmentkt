package com.github.liebharc.cachetable

import com.github.liebharc.queryenrichment.InMemoryQueryBuilder
import com.github.liebharc.queryenrichment.Student
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.Serializable
import java.sql.*
import java.util.*
import java.util.stream.Stream

data class StudentClass(val studentId: Long, val classId: Long, val firstName: String, val lastName: String) : Serializable

data class CacheKey(val classId: Long, val studentId: Long)

data class CacheKeyMataInfo(val cache: Cache<CacheKey, StudentClass>) : ICacheMetaInfo {
    override val key: Class<out Any> = CacheKey::class.java
    override val value: Class<out Any> = StudentClass::class.java

    override fun size(): Long {
        return cache.size();
    }

    override fun getOrNull(key: List<Any?>): List<Any?> {
        if (key.get(1) == null) {
            return cache.asMap().entries.filter { entry -> entry.key.classId == key.get(0) as Long}.map { entry -> entry.value }
        }

        val result = cache.getIfPresent(CacheKey(key.get(0) as Long, key.get(1) as Long))
        if (result == null) {
            return Collections.emptyList()
        }

        return Collections.singletonList(result)
    }

    override fun getAll(): Stream<out MutableMap.MutableEntry<out Any, out Any>> {
        return cache.asMap().entries.stream();
    }

    override fun getNumberOfIndexColumns(): Long {
        return 2;
    }

}

class CacheTableComplexKeyTest : ResultSetAssertions() {
    private var connection: Connection? = null
    private var statement: Statement? = null
    private var cache: Cache<CacheKey, StudentClass>? = null

    @Before
    @Throws(SQLException::class)
    fun setupH2() {
        cache = CacheBuilder.newBuilder().build<CacheKey, StudentClass>()
        CacheTable.register("STUDENT", CacheKeyMataInfo(cache!!))
        cache!!.put(CacheKey(3, 10), StudentClass(10,  3,"David", "Tenant"))
        cache!!.put(CacheKey(3, 11), StudentClass(11, 3, "Matt", "Smith"))
        connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "")
        statement = connection!!.createStatement()
        statement!!.execute("CREATE TABLE STUDENT ( CLASSID bigint NOT NULL, STUDENTID bigint NOT NULL, FIRSTNAME varchar(255), LASTNAME varchar(255))\n" +
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
        var result = consume(statement!!.executeQuery("SELECT * FROM STUDENT WHERE STUDENTID = 10 AND CLASSID = 3"))
        assertEquals(listOf(
                listOf("3", "10", "David", "Tenant")), result)

        result = consume(statement!!.executeQuery("SELECT * FROM STUDENT WHERE CLASSID = 3"))
        assertEquals(listOf(
                listOf("3", "10", "David", "Tenant"),
                listOf("3", "11", "Matt", "Smith")), result)
    }
}