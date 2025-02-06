package com.example.turgo;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class Parent extends User implements Serializable {
    private ArrayList<Student> children;
    public Parent(String fullName, String gender, String birthDate, String nickname, String email, String phoneNumber) throws ParseException {
        super("PARENT", gender, fullName, birthDate, nickname, email, phoneNumber, "parObj");
        this.children = new ArrayList<>();
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
