package com.example.turgo;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class Parent extends User implements Serializable, RequireUpdate<Parent, ParentFirebase> {
    private ArrayList<Student> children;
    public static String SERIALIZE_KEY_CODE = "parentObj";
    public Parent(String fullName, String gender, String birthDate, String nickname, String email, String phoneNumber) throws ParseException {
        super(UserType.PARENT, gender, fullName, birthDate, nickname, email, phoneNumber);
        this.children = new ArrayList<>();
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return FirebaseNode.PARENT;
    }

    @Override
    public Class<ParentFirebase> getFirebaseClass() {
        return ParentFirebase.class;
    }

    @Override
    public String getID() {
        return super.getUID();
    }

    @Override
    public void updateUserDB() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        this.updateDB();
    }

    public Parent(){}

    @Override
    public String toString() {
        return super.toString() + "Parent{" +
                "children=" + children +
                '}';
    }
    public void addChild(Student child){
        children.add(child);
    }
    public ArrayList<Student> getChildren(){
        return children;
    }
}
