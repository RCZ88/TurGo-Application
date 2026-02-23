package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDateTime;

public class NotificationFirebase implements FirebaseClass<Notification<?>> {
    private String notif_ID;
    private String title;
    private String content;
    private String timeSent;
    private String from;
    private String to;

    public NotificationFirebase(){}
    @Override
    public void importObjectData(Notification<?> from) {
        notif_ID = from.getID();
        title = from.getTitle();
        content = from.getContent();
        timeSent = from.getTimeSent().toString();
        this.from = ((RequireUpdate<?, ?, ?>) from.getFrom()).getID();
        String toUid = from.getTo() != null ? from.getTo().getUid() : null;
        if (!Tool.boolOf(toUid) && from.getTo() instanceof RequireUpdate<?, ?, ?>) {
            toUid = ((RequireUpdate<?, ?, ?>) from.getTo()).getID();
        }
        to = toUid;
    }

    @Override
    public String getID() {
        return notif_ID;
    }



    @Override
    public void convertToNormal(ObjectCallBack<Notification<?>> callBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {

        if(Tool.getNodeOfID(from).getClazz().newInstance() instanceof User){
            Notification<User>notif = new Notification<>(title, content, LocalDateTime.parse(timeSent), null, null);

            User fromUser = Tool.getUserOfId(from);
            User toUser = Tool.getUserOfId(to);
            notif.setTo(toUser);
            notif.setFrom(fromUser);
            callBack.onObjectRetrieved(notif);
        }else if(Tool.getNodeOfID(from).getClazz().newInstance() instanceof Course){
            Notification<Course>notif = new Notification<>(title, content, LocalDateTime.parse(timeSent), null, null);
            RTDBManager<Course> manager = new RTDBManager<>();
            Course course = new Course();
            course.retrieveOnce(new ObjectCallBack<>() {
                @Override
                public void onObjectRetrieved(CourseFirebase object) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, ParseException {
                    final Course[] from = new Course[1];
                    object.convertToNormal(new ObjectCallBack<Course>() {
                        @Override
                        public void onObjectRetrieved(Course object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                            from[0] = object;
                        }

                        @Override
                        public void onError(DatabaseError error) {

                        }
                    });
                    notif.setFrom(from[0]);
                }

                @Override
                public void onError(DatabaseError error) {

                }
            }, from);
            notif.setTo(Tool.getUserOfId(to));
            callBack.onObjectRetrieved(notif);
        }
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
}
