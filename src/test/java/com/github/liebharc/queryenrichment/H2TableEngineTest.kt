package com.github.liebharc.queryenrichment

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.h2.api.TableEngine
import org.h2.command.ddl.CreateTableData
import org.h2.engine.Session
import org.h2.index.Index
import org.h2.index.IndexType
import org.h2.result.Row
import org.h2.schema.Schema
import org.h2.table.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.ArrayList

class H2TableEngineTest {
    private var connection: Connection? = null
    private var statement: Statement? = null

    @Before
    @Throws(SQLException::class)
    fun setupH2() {
        connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "")
        statement = connection!!.createStatement()
        statement!!.execute("CREATE TABLE CLASS ( ID int(11) NOT NULL, DESCRIPTION varchar(255))\n" +
                "    ENGINE \"com.github.liebharc.cachetable.CacheTableEngine\";")

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
        var result = statement!!.executeQuery("SELECT * FROM User")
        Assert.assertTrue(result.next())
        Assert.assertNotNull(result.getObject(1))

        result = statement!!.executeQuery("SELECT * FROM CLASS WHERE ID = 1")
        Assert.assertTrue(result.next())
    }
}