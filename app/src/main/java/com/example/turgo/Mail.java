package com.example.turgo;

import java.time.LocalDateTime;

public class Mail {
    private User from;
    private User to;
    private LocalDateTime timeSent;
    private LocalDateTime timeOpened;
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

    public User getTo() {
        return to;
    }

    public LocalDateTime getTimeSent() {
        return timeSent;
    }

    public LocalDateTime getTimeOpened() {
        return timeOpened;
    }

    public boolean isOpened() {
        return opened;
    }
}
