package com.example.turgo;

import com.google.firebase.database.DatabaseError;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class MailFirebase implements FirebaseClass<Mail> {
    private String mailID;
    private String from;
    private String to;
    private String fromType;
    private String toType;
    private String timeSent;
    private String timeOpened;
    private String header;
    private String body;
    private String richBody;
    private boolean draft;
    private boolean opened;
    private ArrayList<String> attachments;
    public MailFirebase() {}

    public String getPreview() {
        if (!Tool.boolOf(body)) return "";
        return body.length() > 30 ? body.substring(0, 30) : body;
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

    public String getFromType() {
        return fromType;
    }

    public ArrayList<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<String> attachments) {
        this.attachments = attachments;
    }

    public void setFromType(String fromType) {
        this.fromType = fromType;
    }

    public String getToType() {
        return toType;
    }

    public void setToType(String toType) {
        this.toType = toType;
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

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public String getRichBody() {
        return richBody;
    }

    public void setRichBody(String richBody) {
        this.richBody = richBody;
    }

    @Override
    public void importObjectData(Mail mail) {
        this.mailID = mail.getID();
        this.from = mail.getFromId();
        this.to = mail.getToId();
        this.fromType = mail.getFromType();
        this.toType = mail.getToType();
        this.timeSent = mail.getTimeSent() != null ? mail.getTimeSent().toString() : null;
        this.timeOpened = mail.getTimeOpened() != null ? mail.getTimeOpened().toString() : null;
        this.header = mail.getHeader();
        this.body = mail.getBody();
        this.richBody = mail.getRichBody() != null ? mail.getRichBody().getID() : null;
        this.draft = mail.isDraft();
        this.opened = mail.isOpened();
        this.attachments = mail.getAttachmentIds() != null
                ? new ArrayList<>(mail.getAttachmentIds())
                : new ArrayList<>();
    }

    @Override
    public String getID() {
        return mailID;
    }

    @Override
    public void convertToNormal(ObjectCallBack<Mail> objectCallBack)
            throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Mail.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object)
                    throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Mail) object);
            }

            @Override
            public void onError(DatabaseError error) {
                Log.e("MailFirebase", "convertToNormal failed for mailID=" + getID() + ": " + error.getMessage());
                objectCallBack.onError(error);
            }
        });
    }
}
