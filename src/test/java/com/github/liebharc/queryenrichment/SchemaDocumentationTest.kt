package com.github.liebharc.queryenrichment

import org.junit.Assert
import org.junit.Test

import java.util.Arrays

class SchemaDocumentationTest {

    @Test
    fun drawSchemaTest() {
        val schema = SchemaDocumentation.INSTANCE.drawSchema(Arrays.asList(
                Attributes.studentId,
                Attributes.firstName,
                Attributes.lastName,
                Attributes.studentClass,
                Attributes.fullName,
                Attributes.teacherId,
                Attributes.teacherFirstName,
                Attributes.teacherLastName,
                Attributes.teacherClass,
                Attributes.classId,
                Attributes.classDescription))
        val expected = "class:\n" +
                "\t - description: String\n" +
                "\t - id: long\n" +
                "student:\n" +
                "\t - class: long\n" +
                "\t - firstName: String\n" +
                "\t - fullName: String\n" +
                "\t - id: long\n" +
                "\t - lastName: String\n" +
                "teacher:\n" +
                "\t - class: long\n" +
                "\t - firstName: String\n" +
                "\t - id: long\n" +
                "\t - lastName: String\n"
        Assert.assertEquals(expected, schema)
    }
}