package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public class ParentFirebase extends UserFirebase implements FirebaseClass<Parent>{

    public ParentFirebase(String userType) {
        super(userType);
    }

    @Override
    public void importObjectData(Parent from) {

    }

    @Override
    public Parent convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return null;
    }
}
