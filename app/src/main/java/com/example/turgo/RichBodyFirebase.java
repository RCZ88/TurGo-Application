package com.example.turgo;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class RichBodyFirebase implements FirebaseClass<RichBody> {
    private String id;
    private String text;
    private ArrayList<String>spans;
    @Override
    public void importObjectData(RichBody from) {
        this.id = from.getID();
        this.text = from.text;
        spans = convertToIdList(from.spans);
    }

    @Override
    @Exclude
    public String getID() {
        return id;
    }

    @Override
    public void convertToNormal(ObjectCallBack<RichBody> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(RichBody.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((RichBody) object);
            }

            @Override
            public void onError(DatabaseError error) {}
        });
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArrayList<String> getSpans() {
        return spans;
    }

    public void setSpans(ArrayList<String> spans) {
        this.spans = spans;
    }
}
