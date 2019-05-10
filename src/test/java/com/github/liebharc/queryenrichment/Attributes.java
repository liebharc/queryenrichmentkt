package com.github.liebharc.queryenrichment;

public class Attributes {

    public static final Attribute<Long> studentId = new Attribute<>(Long.class,"student", "id");
    public static final Attribute<String> firstName = new Attribute<>(String.class, "student", "firstName");
    public static final Attribute<String> lastName = new Attribute<>(String.class, "student", "lastName");
    public static final Attribute<Long> studentClass = new Attribute<>(Long.class, "student", "class");
    public static final Attribute<String> fullName = new Attribute<>(String.class, "student", "fullName");
    public static final Attribute<String> classIdString = new Attribute<>(String.class, "student", "idString");


    public static final Attribute<Long> teacherId = new Attribute<>(Long.class, "teacher", "id");
    public static final Attribute<String> teacherFirstName = new Attribute<>(String.class, "teacher", "firstName");
    public static final Attribute<String> teacherLastName = new Attribute<>(String.class, "teacher", "lastName");
    public static final Attribute<Long> teacherClass = new Attribute<>(Long.class, "teacher", "class");

    public static final Attribute<Long> classId = new Attribute<>(Long.class, "class", "id");
    public static final Attribute<String> classDescription = new Attribute<>(String.class, "class", "description");

    private Attributes() {

    }
}
