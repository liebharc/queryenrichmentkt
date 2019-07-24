package com.github.liebharc.cachetableperf

import com.github.liebharc.cachetable.CacheTable
import com.github.liebharc.cachetable.ICacheMetaInfo
import com.github.liebharc.cachetable.ResultSetAssertions
import com.github.liebharc.cachetable.SingleIdCacheMetaInfo
import com.github.liebharc.queryenrichment.Student
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.h2.jdbcx.JdbcDataSource
import org.junit.*
import org.springframework.jdbc.core.JdbcTemplate

import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.util.HashMap
import java.util.function.Function
import java.util.stream.Stream


class LoadIndicationTest : ResultSetAssertions() {

    class StudentSpecializedMetaDData(private val cache: Map<out Any, out Any>) : ICacheMetaInfo {
        override val key: Class<out Any> = Long::class.java
        override val value: Class<out Any> = Student::class.java


        override fun size(): Long {
            return cache.size.toLong();
        }

        override fun getValueOrNull(key: Array<Any?>): Any? {
            val result = cache.get(key.get(0)!!);
            if (result == null) {
                return null;
            }

            return result;
        }

        override fun getAllValues(): Stream<out Map.Entry<out Any, out Any>> {
            return cache.entries.stream();
        }

        override fun getNumberOfIndexColumns(): Int {
            return 1;
        }


        override fun createFieldAccessor(colName: String): Function<Any, Any>? {
            if (colName == "ID") {
                return Function { obj: Any -> (obj as Student).id as Any };
            }

            if (colName == "FIRSTNAME") {
                return Function{ obj: Any -> (obj as Student).firstName as Any};
            }

            if (colName == "LASTNAME") {
                return Function { obj: Any -> (obj as Student).lastName as Any};
            }

            return null;
        }
    }
    private var connection: Connection? = null
    private var statement: Statement? = null
    private var dataSource: JdbcDataSource? = null
    private var jdbcTemplate: JdbcTemplate? = null
    private var cache: MutableMap<Long, Student>? = null

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

    private val rangeQuery = "SELECT id, lastName FROM STUDENT WHERE ID >= 100 AND ID < 150"
    private val lookupQuery = "SELECT id, lastName FROM STUDENT WHERE ID = 100"
    private val scanQuery = "SELECT id, lastName FROM STUDENT"

    private val rangeSize = 50L

    @Test
    fun rangeReference() {
        fillTable()

        val start = System.currentTimeMillis()
        for (i in 0 until ITERATIONS) {
            val students =  consume(statement!!.executeQuery(rangeQuery))
            Assert.assertEquals(rangeSize, students.size.toLong())
        }

        println("Ref Range Duration [ms]: " + (System.currentTimeMillis() - start))
    }

    @Test
    fun rangeActual() {
        fillCache()

        val start = System.currentTimeMillis()
        for (i in 0 until ITERATIONS) {
            val students =  consume(statement!!.executeQuery(rangeQuery))
            Assert.assertEquals(rangeSize, students.size.toLong())
        }

        println("Act Range Duration [ms]: " + (System.currentTimeMillis() - start))
    }

    @Test
    fun lookupReference() {
        fillTable()

        val start = System.currentTimeMillis()
        for (i in 0 until ITERATIONS) {
            val students =  consume(statement!!.executeQuery(lookupQuery))
            Assert.assertEquals(1L, students.size.toLong())
        }

        println("Ref Lookup Duration [ms]: " + (System.currentTimeMillis() - start))
    }

    @Test
    fun lookupActual() {
        fillCache()

        val start = System.currentTimeMillis()
        for (i in 0 until ITERATIONS) {
            val students =  consume(statement!!.executeQuery(lookupQuery))
            Assert.assertEquals(1L, students.size.toLong())
        }

        println("Act Lookup Duration [ms]: " + (System.currentTimeMillis() - start))
    }

    @Test
    fun scanReference() {
        fillTable()

        val start = System.currentTimeMillis()
        for (i in 0 until ITERATIONS) {
            val students =  consume(statement!!.executeQuery(scanQuery))
            Assert.assertEquals(RESULT_SIZE, students.size.toLong())
        }

        println("Ref Scan Duration [ms]: " + (System.currentTimeMillis() - start))
    }

    @Test
    fun scanActual() {
        fillCache()

        val start = System.currentTimeMillis()
        for (i in 0 until ITERATIONS) {
            val students =  consume(statement!!.executeQuery(scanQuery))
            Assert.assertEquals(RESULT_SIZE, students.size.toLong())
        }

        println("Act Scan Duration [ms]: " + (System.currentTimeMillis() - start))
    }

    private fun fillTable() {
        statement = connection!!.createStatement()
        statement!!.execute("CREATE TABLE STUDENT ( ID bigint primary key auto_increment not null, firstName varchar(255), lastName varchar(255))")
        statement!!.execute("CREATE INDEX STUDENT_OD ON STUDENT(ID)")
        for (i in 0 until RESULT_SIZE) {
            statement!!.execute("INSERT INTO STUDENT(id, firstName, lastName) VALUES ($i, 'John', 'Smith')")
        }
    }

    private fun fillCache() {
        statement = connection!!.createStatement()
        cache = HashMap()
        CacheTable.register("STUDENT", StudentSpecializedMetaDData(cache!!))
        for (i in 0 until RESULT_SIZE) {
            cache!!.put(i, Student(i, "John", "Smith"))
            cache!!.put(11, Student(11, "Matt", "Smith"))
        }
        statement!!.execute("CREATE TABLE STUDENT ( ID bigint NOT NULL, FIRSTNAME varchar(255), LASTNAME varchar(255))\n" +
                "    ENGINE \"com.github.liebharc.cachetable.CacheTableEngine\";")
    }

    companion object {
        private const val RESULT_SIZE: Long = 10000
        private const val ITERATIONS: Long = 20000
    }
}
