package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public class MailFirebase implements FirebaseClass<Mail> {
    private String mailID;
    private String from;
    private String to;
    private String timeSent;    // stored as epoch millis
    private String timeOpened;  // nullable, epoch millis
    private String header;
    private String body;
    private boolean opened;

    public MailFirebase(){}

    public String getPreview(){
        return body.substring(0, 30);
    }

    public String getMailID() {
        return mailID;
    }

    public void setMailID(String mailID) {
        this.mailID = mailID;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(String timeSent) {
        this.timeSent = timeSent;
    }

    public String getTimeOpened() {
        return timeOpened;
    }

    public void setTimeOpened(String timeOpened) {
        this.timeOpened = timeOpened;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    @Override
    public void importObjectData(Mail from) {
        this.mailID = from.getID();
        this.from = (from.getFrom() != null) ? from.getFrom().getUid() : null;
        this.to   = (from.getTo()   != null) ? from.getTo().getUid()   : null;
        this.timeSent = from.getTimeSent().toString();
        this.timeOpened = from.getTimeOpened().toString();
        this.header = from.getHeader();
        this.body = from.getBody();
        this.opened = from.isOpened();
    }

    @Override
    public String getID() {
        return mailID;
    }

    @Override
    public void convertToNormal(ObjectCallBack<Mail> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Mail.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Mail) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }
}
