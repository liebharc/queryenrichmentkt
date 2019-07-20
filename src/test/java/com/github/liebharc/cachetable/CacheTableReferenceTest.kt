package com.github.liebharc.cachetable

import com.github.liebharc.queryenrichment.Student
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.junit.*
import java.sql.*
import java.util.ArrayList

class CacheTableReferenceTest : ResultSetAssertions() {
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
    @Ignore
    fun selectReference() {
    }
}