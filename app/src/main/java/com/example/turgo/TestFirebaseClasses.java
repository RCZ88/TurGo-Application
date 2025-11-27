package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public class TestFirebaseClasses {
    public static void main(String[]args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, ParseException {
        CourseType ct = new CourseType("Piano");
        Teacher teacher = new Teacher("FEROS", "Male", "10/10/1980", "feros", "feros@gmail.com", "08123456788");

//      Course course = new Course(null, ct, "Idiot", "dont join", teacher);
    }
}
