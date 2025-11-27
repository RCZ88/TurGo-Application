package com.example.turgo;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;

public class Admin extends User implements Serializable, RequireUpdate<Admin, AdminFirebase>  {
    public static final String SERIALIZE_KEY_CODE = "adminObj";
    public Admin(String fullName, String gender, String birthDate, String nickname, String email, String phoneNumber) throws ParseException {
        super(UserType.ADMIN, gender, fullName, birthDate, nickname, email, phoneNumber);

    }


    @Override
    public FirebaseNode getFirebaseNode() {
        return FirebaseNode.ADMIN;
    }

    @Override
    public Class<AdminFirebase> getFirebaseClass() {
        return AdminFirebase.class;
    }

    @Override
    public String getID() {
        return super.getUid();
    }

    @Override
    public void updateUserDB() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        updateDB();
    }


    public Admin(){}

    @Override
    public String getSerializeCode() {
        return SERIALIZE_KEY_CODE;
    }
}
