package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.ZoneId;

public class MailFirebase implements FirebaseClass<Mail> {
    private String mailID;
    private String fromId;
    private String toId;
    private String timeSent;    // stored as epoch millis
    private String timeOpened;  // nullable, epoch millis
    private String header;
    private String body;
    private boolean opened;


    public String getPreview(){
        return body.substring(0, 30);
    }

    public String getMailID() {
        return mailID;
    }

    public void setMailID(String mailID) {
        this.mailID = mailID;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
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
        this.fromId = (from.getFrom() != null) ? from.getFrom().getUID() : null;
        this.toId   = (from.getTo()   != null) ? from.getTo().getUID()   : null;
        this.timeSent = from.getTimeSent().toString();
        this.timeOpened = from.getTimeOpened().toString();
        this.header = from.getHeader();
        this.body = from.getBody();
        this.opened = from.isOpened();
    }

    @Override
    public void importObjectData(F from) {

    }

    @Override
    public String getID() {
        return mailID;
    }

    @Override
    public Mail convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return null;
    }
}
