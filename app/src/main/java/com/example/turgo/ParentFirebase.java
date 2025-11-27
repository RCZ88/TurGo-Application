package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class ParentFirebase extends UserFirebase implements FirebaseClass<Parent>{
    private ArrayList<String> children;

    public ParentFirebase(String userType) {
        super(userType);
        children = new ArrayList<>();
    }

    @Override
    public void importObjectData(Parent from) {
        setID(from.getID());
        setFullName(from.getFullName());
        setNickname(from.getNickname());
        setBirthDate(from.getBirthDate());
        setAge(from.getAge());
        setEmail(from.getEmail());
        setGender(from.getGender());
        setPhoneNumber(from.getPhoneNumber());
        setLanguage(from.getLanguage().getDisplayName());
        setTheme(from.getTheme().getTheme());
        setInbox(convertToIdList(from.getInbox()));
        setOutbox(convertToIdList(from.getOutbox()));
        setNotifications(convertToIdList(from.getNotifications()));
        
        children = convertToIdList(from.getChildren());
    }
    
    public ArrayList<String> getChildren() {
        return children;
    }
    
    public void setChildren(ArrayList<String> children) {
        this.children = children;
    }

    @Override
    public void convertToNormal(ObjectCallBack<Parent> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {

    }


}
