package com.example.turgo;

public enum UserType {
    STUDENT("Student"),
    TEACHER("Teacher"),
    PARENT("Parent"),
    ADMIN("Admin");

    private String userType;
    UserType(String userType){
        this.userType = userType;
    }
    public String type(){
        return userType;
    }
}
