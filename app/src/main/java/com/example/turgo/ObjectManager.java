package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class ObjectManager {
    protected static final ArrayList<User> USERS = new ArrayList<>();
    protected static final ArrayList<Course> COURSES = new ArrayList<>();
    protected static final ArrayList<Room> ROOMS = new ArrayList<>();
    protected static final ArrayList<CourseType> COURSE_TYPES = new ArrayList<>();

    public static void ADD_USER(User user) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        USERS.add(user);
        user.updateUserDB();
    }

    public static void ADD_COURSE(Course course){
        COURSES.add(course);

    }
}
