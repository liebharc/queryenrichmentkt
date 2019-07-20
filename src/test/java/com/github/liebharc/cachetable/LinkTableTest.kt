package com.github.liebharc.cachetable

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.sql.*
import java.util.ArrayList

class LinkTableTest : ResultSetAssertions() {
    private var connection: Connection? = null
    private var statement: Statement? = null

    @Before
    @Throws(SQLException::class)
    fun setupH2() {
        connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "")
        statement = connection!!.createStatement()

        // User table: https://rnacentral.org/help/public-database
        statement!!.execute("CREATE LINKED TABLE User('org.postgresql.Driver', 'jdbc:postgresql://hh-pgsql-public.ebi.ac.uk:5432/pfmegrnargs', 'reader', 'NWDMCE5xdipIjRrp', 'User');")
    }

    @After
    @Throws(SQLException::class)
    fun disposeH2() {
        statement!!.close()
        connection!!.close()
    }


    @Test
    fun queryTest() {
        var result = consume(statement!!.executeQuery("SELECT * FROM User"))
        assertEquals(result, listOf(listOf("reader")))
    }

}