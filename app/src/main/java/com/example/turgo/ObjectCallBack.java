package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public interface ObjectCallBack<T> {
    void onObjectRetrieved(T object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;
    void onError(DatabaseError error);
}
