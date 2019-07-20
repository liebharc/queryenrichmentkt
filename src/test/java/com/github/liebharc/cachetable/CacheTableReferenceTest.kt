package com.github.liebharc.cachetable

import com.github.liebharc.queryenrichment.Student
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.junit.*
import java.sql.*
import java.util.ArrayList
import java.io.ObjectInputStream
import java.io.ObjectInput
import java.io.ByteArrayInputStream



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
        statement!!.execute("CREATE TABLE STUDENT ( ID bigint NOT NULL, REF OTHER)\n" +
                "    ENGINE \"com.github.liebharc.cachetable.CacheTableEngine\";")
    }

    @After
    @Throws(SQLException::class)
    fun disposeH2() {
        statement!!.close()
        connection!!.close()
    }

    @Test
    fun selectReference() {
        var result = consume(statement!!.executeQuery("SELECT * FROM STUDENT WHERE ID = 10"))
        Assert.assertEquals(1, result.size);
        Assert.assertEquals(10L, result.get(0).get(0));
        val bis = ByteArrayInputStream(result.get(0).get(1) as ByteArray);
        val input = ObjectInputStream(bis)
        val student = input.readObject() as Student
        Assert.assertEquals(Student(10, "David", "Tenant"), student);
    }
}