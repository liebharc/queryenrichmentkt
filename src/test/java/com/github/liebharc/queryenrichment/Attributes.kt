package com.github.liebharc.queryenrichment

object Attributes {

    val studentId = Attribute(Long::class.java, "student", "id")
    val firstName = Attribute(String::class.java, "student", "firstName")
    val lastName = Attribute(String::class.java, "student", "lastName")
    val reference = Attribute(Student::class.java, "student", Attribute.reference)
    val studentClass = Attribute(Long::class.java, "student", "class")
    val fullName = Attribute(String::class.java, "student", "fullName")
    val classIdString = Attribute(String::class.java, "student", "idString")


    val teacherId = Attribute(Long::class.java, "teacher", "id")
    val teacherFirstName = Attribute(String::class.java, "teacher", "firstName")
    val teacherLastName = Attribute(String::class.java, "teacher", "lastName")
    val teacherClass = Attribute(Long::class.java, "teacher", "class")

    val classId = Attribute(Long::class.java, "class", "id")
    val classDescription = Attribute(String::class.java, "class", "description")
}
