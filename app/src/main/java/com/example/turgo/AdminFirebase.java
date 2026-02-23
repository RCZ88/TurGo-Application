package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public class AdminFirebase extends UserFirebase implements FirebaseClass<Admin>{

    public AdminFirebase() {
        super(UserType.ADMIN.type());
    }


    @Override
    public void importObjectData(Admin from) {
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
        setInbox(from.getInboxIds());
        setOutbox(from.getOutboxIds());
        setNotifications(from.getNotificationsIds());
    }

    @Override
    public String getID() {
        return super.getID();
    }

    @Override
    public void convertToNormal(ObjectCallBack<Admin> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Admin.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Admin) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

}
