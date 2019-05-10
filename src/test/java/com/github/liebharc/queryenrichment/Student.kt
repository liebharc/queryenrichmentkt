package com.github.liebharc.queryenrichment

import java.util.Objects

class Student(val id: Long, val firstName: String, val lastName: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val student = other as Student?
        return id == student!!.id &&
                firstName == student.firstName &&
                lastName == student.lastName
    }

    override fun hashCode(): Int {
        return Objects.hash(id, firstName, lastName)
    }
}
