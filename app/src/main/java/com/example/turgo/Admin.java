package com.example.turgo;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

public class Admin extends User implements Serializable  {
    public static String SERIALIZE_KEY_CODE = "adminObj";
    public Admin(String fullName, String gender, String birthDate, String nickname, String email, String phoneNumber) throws ParseException {
        super(UserType.ADMIN, gender, fullName, birthDate, nickname, email, phoneNumber);

    }
    public void createNewCourse(){

    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return FirebaseNode.ADMIN;
    }

    @Override
    public void updateUserDB() {

    }


    public Admin(){}
}
