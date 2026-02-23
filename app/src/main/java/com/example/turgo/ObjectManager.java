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
        if (user instanceof Student) {
            ((Student) user).getRepositoryInstance().save((Student) user);
        } else if (user instanceof Teacher) {
            ((Teacher) user).getRepositoryInstance().save((Teacher) user);
        } else if (user instanceof Parent) {
            ((Parent) user).getRepositoryInstance().save((Parent) user);
        } else if (user instanceof Admin) {
            ((Admin) user).getRepositoryInstance().save((Admin) user);
        }
    }

    public static void ADD_COURSE(Course course){
        COURSES.add(course);

    }
}
