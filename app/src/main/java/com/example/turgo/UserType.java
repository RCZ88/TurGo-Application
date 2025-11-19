package com.example.turgo;

public enum UserType {
    STUDENT("Student", Student.class),
    TEACHER("Teacher", Teacher.class),
    PARENT("Parent", Parent.class),
    ADMIN("Admin", Admin.class);

    private String userType;
    private Class<? extends User> userClass;
    UserType(String userType, Class<? extends User> userClass){
        this.userType = userType;
        this.userClass = userClass;
    }
    public String type(){
        return userType;
    }
    public Class<? extends User> getUserClass(){
        return userClass;
    }
}
