package com.github.liebharc.queryenrichment

import java.util.*

class InMemoryQueryBuilder : QueryBuilder {

    override fun build(selectors: List<QuerySelector>, filters: List<QueryFilter>): com.github.liebharc.queryenrichment.Query {
        if (!filters.isEmpty()) {
            throw IllegalArgumentException("This class doesn't support criteria")
        }

        return Query(selectors)
    }

    class Database {
        val students: MutableList<Student> = ArrayList()

        fun clear() {
            students.clear()
        }

        fun add(student: Student) {
            students.add(student)
        }

    }

    inner class Query(private val steps: List<QuerySelector>) : com.github.liebharc.queryenrichment.Query {

        override fun query(request: Request): QueryResult {
            val rows = database.students.map { student ->
                steps.map<QuerySelector, Any?> { selector ->
                    val attribute = selector.attribute
                    when (attribute) {
                        Attributes.studentId -> student.id
                        Attributes.firstName -> student.firstName
                        Attributes.lastName -> student.lastName
                        Attributes.reference -> student
                        else -> throw IllegalArgumentException("Unknown column $selector")
                    }
                }
                .toList()
            }.toList()
            return QueryResult(rows)
        }
    }

    companion object {
        val database = Database()

        val studentId = SelectorBuilder(Attributes.studentId).addColumn("ID").build()
        val reference = SelectorBuilder(Attributes.reference).addColumn("*").build()
        val firstName = SelectorBuilder(Attributes.firstName).addColumn("FIRST_NAME").build()
        val lastName = SelectorBuilder(Attributes.lastName).addColumn("LAST_NAME").build()
        val fullName: ExecutableStep<String, Any?> = object : ParameterlessEnrichment<String>(Attributes.fullName, Dependencies.requireOneOf(Attributes.firstName, Attributes.lastName)) {
            override fun enrich(result: IntermediateResult) {
                val firstName = result[Attributes.firstName]
                val lastName = result[Attributes.lastName]
                if (firstName != null && lastName != null) {
                    result.add(this, "$firstName $lastName")
                } else if (firstName != null) {
                    result.add(this, firstName)
                } else if (lastName != null) {
                    result.add(this, lastName)
                } else {
                    throw RuntimeException("At least one of firstName and lastName must be available")
                }
            }
        }
    }
}
