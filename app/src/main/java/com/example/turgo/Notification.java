package com.example.turgo;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.UUID;

public class Notification<FromType> implements Serializable, RequireUpdate<Notification<?>, NotificationFirebase, NotificationRepository> {
    private final FirebaseNode fbn = FirebaseNode.NOTIFICATION;
    private final Class<NotificationFirebase> fbc = NotificationFirebase.class;
    private static RTDBManager<Notification<?>>notifRTDB;
    private final String notif_ID;
    private String title;
    private String content;
    private LocalDateTime timeSent;
    private FromType from;
    private User to;

    public Notification(){
        notif_ID = UUID.randomUUID().toString();
    }
    public Notification(String title, String content, LocalDateTime timeSent, FromType from, User to) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        notif_ID = UUID.randomUUID().toString();
        this.title= title;
        this.content = content;
        this.timeSent = timeSent;
        this.from = from;
        this.to = to;
        this.updateDB();
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

    public LocalDateTime getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(LocalDateTime timeSent) {
        this.timeSent = timeSent;
    }

    public FromType getFrom() {
        return from;
    }

    public void setFrom(FromType from) {
        this.from = from;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }
    public static void sendNotification(Notification<?> notification) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        User user = notification.getTo();
        user.recieveNotification(notification);
        if (user instanceof Student) {
            ((Student) user).getRepositoryInstance().save((Student) user);
        } else if (user instanceof Teacher) {
            ((Teacher) user).getRepositoryInstance().save((Teacher) user);
        } else if (user instanceof Parent) {
            ((Parent) user).getRepositoryInstance().save((Parent) user);
        } else if (user instanceof Admin) {
            ((Admin) user).getRepositoryInstance().save((Admin) user);
        }
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<NotificationRepository> getRepositoryClass() {
        return NotificationRepository.class;
    }

    @Override
    public Class<NotificationFirebase> getFirebaseClass() {
        return fbc;
    }


    @Override
    public String getID() {
        return notif_ID;
    }
}
