package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public class AdminFirebase extends UserFirebase implements FirebaseClass<Admin>{
    public AdminFirebase(String userType) {
        super(userType);
    }

    @Override
    public void importObjectData(Admin from) {

    }

    @Override
    public String getID() {
        return "";
    }

    @Override
    public Admin convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return null;
    }
}
