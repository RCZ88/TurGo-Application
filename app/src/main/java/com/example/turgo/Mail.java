package com.example.turgo;

import java.time.LocalDateTime;

public class Mail {
    private User from;
    private User to;
    private LocalDateTime timeSent;
    private LocalDateTime timeOpened;
    private String header, body;
    private boolean opened;

    public Mail(User from, User to) {
        this.from = from;
        this.to = to;
        timeSent = LocalDateTime.now();
    }

    public void open(){
        opened = true;
        timeOpened = LocalDateTime.now();
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
}
