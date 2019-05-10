package com.github.liebharc.queryenrichment

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*

class H2QueryBuilder internal constructor(private val connection: Connection) : QueryBuilder {

    override fun build(selectors: List<QuerySelector>, filters: List<QueryFilter>): com.github.liebharc.queryenrichment.Query {
        val select = this.createSelectStatement(selectors)
        val query = StringBuilder()
        query.append("SELECT ")
        query.append(select)
        query.append(" FROM ")
        query.append("student")

        if (!filters.isEmpty()) {
            query.append(" WHERE ")
            query.append(FilterUtils.getSqlCriteria(filters))
        }

        try {
            return Query(connection.prepareStatement(query.toString()), selectors)
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }

    }

    private fun createSelectStatement(steps: List<QuerySelector>): String {
        return if (steps.isEmpty()) {
            "1"
        } else {
            steps.joinToString(", ") { it.column }
        }
    }

    private inner class Query internal constructor(private val query: PreparedStatement, private val selectors: List<QuerySelector>) : com.github.liebharc.queryenrichment.Query {

        override fun query(request: Request): QueryResult {
            try {
                var pos = 1
                for (criterion in request.criteria) {
                    query.setObject(pos, criterion.value)
                    pos++
                }

                val resultSet = query.executeQuery()

                val results = ArrayList<List<Any>>()
                while (resultSet.next()) {
                    val row = ArrayList<Any>()
                    for (i in 1..selectors.size) {
                        row.add(resultSet.getObject(i))
                    }

                    results.add(row)
                }

                return QueryResult(results)

            } catch (e: SQLException) {
                throw RuntimeException(e)
            }

        }
    }

    companion object {

        var classIdStringCalls = 0

        val studentId = SelectorBuilder(Attributes.studentId).addColumn("ID").build()
        val firstName = SelectorBuilder(Attributes.firstName).addColumn("FIRST_NAME").build()
        val lastName = SelectorBuilder(Attributes.lastName).addColumn("LAST_NAME").build()
        val studentClass = SelectorBuilder(Attributes.studentClass).addColumn("CLASS").build()
        val fullName: ExecutableStep<String, Any?> = object : ParameterlessEnrichment<String>(Attributes.fullName, Dependencies.requireAll(Attributes.firstName, Attributes.lastName)) {
            override fun enrich(result: IntermediateResult) =
                    result.add(this, result[Attributes.firstName] + " " + result[Attributes.lastName])
        }

        val classIdString: ExecutableStep<String, Any?> = object : ParameterlessEnrichment<String>(Attributes.classIdString, Dependencies.require(Attributes.studentClass)) {
            override fun enrich(result: IntermediateResult) {
                classIdStringCalls++
                result.add(this, "Class: " + result[Attributes.studentClass]!!)
            }
        }
    }
}
