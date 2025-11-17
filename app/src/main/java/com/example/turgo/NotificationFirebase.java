package com.example.turgo;

public class NotificationFirebase implements FirebaseClass<Notification<?>> {
    private String notif_ID;
    private String title;
    private String content;
    private String timeSent;
    private String from_ID;
    private String to_ID;

    @Override
    public void importObjectData(Notification<?> from) {
        notif_ID = from.getID();
        title = from.getTitle();
        content = from.getContent();
        timeSent = from.getTimeSent().toString();
        from_ID = ((RequireUpdate<?, ?>) from.getFrom()).getID();
        to_ID = from.getTo().getUID();
    }

    @Override
    public String getID() {
        return notif_ID;
    }

    public String getNotif_ID() {
        return notif_ID;
    }

    public void setNotif_ID(String notif_ID) {
        this.notif_ID = notif_ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(String timeSent) {
        this.timeSent = timeSent;
    }

    public String getFrom_ID() {
        return from_ID;
    }

    public void setFrom_ID(String from_ID) {
        this.from_ID = from_ID;
    }

    public String getTo_ID() {
        return to_ID;
    }

    public void setTo_ID(String to_ID) {
        this.to_ID = to_ID;
    }
}
