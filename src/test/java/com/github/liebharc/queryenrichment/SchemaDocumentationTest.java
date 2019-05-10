package com.github.liebharc.queryenrichment;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class SchemaDocumentationTest {

    @Test
    public void drawSchemaTest() {
        final String schema = SchemaDocumentation.INSTANCE.drawSchema(Arrays.asList(
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
                Attributes.classDescription));
        final String expected =
                "class:\n" +
                "\t - description: String\n" +
                "\t - id: Long\n" +
                "student:\n" +
                "\t - class: Long\n" +
                "\t - firstName: String\n" +
                "\t - fullName: String\n" +
                "\t - id: Long\n" +
                "\t - lastName: String\n" +
                "teacher:\n" +
                "\t - class: Long\n" +
                "\t - firstName: String\n" +
                "\t - id: Long\n" +
                "\t - lastName: String\n";
        Assert.assertEquals(expected, schema);
    }
}