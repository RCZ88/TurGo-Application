package com.example.turgo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Mail implements RequireUpdate<Mail, MailFirebase, MailRepository>, Serializable {
    private final String mailID;
    private final FirebaseNode fbn = FirebaseNode.MAIL;
    public static final String SERIALIZE_KEY_CODE = "mailObj";
    private User from;
    private User to;
    private LocalDateTime timeSent;
    private LocalDateTime timeOpened;
    private String header, body;
    private boolean draft;
    private boolean opened;
    public Mail(){
        this.mailID = UUID.randomUUID().toString();
    }

    public Mail(User from, User to, String header, String body){
        this.mailID = UUID.randomUUID().toString();
        this.from = from;
        this.to = to;
        timeSent = LocalDateTime.now();
        this.header = header;
        this.body = body;
        opened = false;
    }
    public Mail( User from, User to) {
        this.mailID = UUID.randomUUID().toString();
        this.from = from;
        this.to = to;
        timeSent = LocalDateTime.now();
    }


    @Override
    public FirebaseNode getFirebaseNode() {
        return FirebaseNode.MAIL;
    }

    @Override
    public Class<MailFirebase> getFirebaseClass() {
        return MailFirebase.class;
    }

    @Override
    public String getID() {
        return mailID;
    }

    @Override
    public Class<MailRepository> getRepositoryClass() {
        return MailRepository.class;
    }

    public String getMailID() {
        return mailID;
    }

    public FirebaseNode getFbn() {
        return fbn;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public LocalDateTime getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(LocalDateTime timeSent) {
        this.timeSent = timeSent;
    }

    public LocalDateTime getTimeOpened() {
        return timeOpened;
    }

    public void setTimeOpened(LocalDateTime timeOpened) {
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
    public String getPreview(){
        return body.substring(0, 30);
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }
}
