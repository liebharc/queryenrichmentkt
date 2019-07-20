package com.github.liebharc.cachetable

import org.junit.Assert
import java.sql.ResultSet

open class ResultSetAssertions {
    fun consume(result: ResultSet): List<List<Any>> {
        var list: MutableList<List<Any>> = ArrayList()
        val columnCount = result.metaData.columnCount
        while (result.next()) {
            var row: MutableList<Any> = ArrayList()
            for (i in 1..columnCount) {
                val cell = result.getObject(i)
                row.add(cell)
            }
            list.add(row)
        }

        return list
    }

    fun assertEquals(expected: List<List<Any>>, actual: List<List<Any>>) {
        val expectedString = listToString(expected)
        val actualString = listToString(actual)
        Assert.assertEquals(expectedString, actualString)
    }

    private fun listToString(list: List<List<Any>>): String {
        return list.map { row -> row.joinToString("|") }.joinToString("\n")
    }
}