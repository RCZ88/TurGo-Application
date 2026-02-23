package com.example.turgo;

import java.io.Serializable;

public enum MailType implements Serializable {
    MAIL_TYPE("mailType"),
    DRAFT("drafts"),
    INBOX("inbox"),
    OUTBOX("outbox");

    public String mailType;

    MailType(String mailType) {
        this.mailType = mailType;
    }

    public String getMailType(){
        return mailType;
    }
}
