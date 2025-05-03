package com.example.turgo;

import java.util.ArrayList;

public class ObjectManager {
    protected static final ArrayList<User> USERS = new ArrayList<>();
    protected static final ArrayList<Course> COURSES = new ArrayList<>();
    protected static final ArrayList<Room> ROOMS = new ArrayList<>();
    protected static final ArrayList<CourseType> COURSE_TYPES = new ArrayList<>();

    public static boolean ADD_USER(User user){
        USERS.add(user);
        RTDBManager<User> rm = new RTDBManager<>();
        return rm.storeData(User.SERIALIZE_KEY_CODE, user.getUID(), user, "User", "User");
    }
}
