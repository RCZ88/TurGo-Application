package com.example.turgo;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;

public class TeacherRepository implements RepositoryClass<Teacher, TeacherFirebase>, UserRepositoryClass{
    private DatabaseReference teacherRef;

    public TeacherRepository(String teacherId) {
        teacherRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.TEACHER.getPath())
                .child(teacherId);
    }
    public TeacherRepository(){}

    @Override
    public DatabaseReference getDbReference() {
        return teacherRef;
    }

    @Override
    public Class<TeacherFirebase> getFbClass() {
        return TeacherFirebase.class;
    }


    public void delete() {
        teacherRef.removeValue();
    }

    public void updateProfileImageCloudinary(String newProfileImageCloudinary) {
        teacherRef.child("profileImageCloudinary").setValue(newProfileImageCloudinary);
    }

    public void updateTeacherResume(String newTeacherResume) {
        teacherRef.child("teacherResume").setValue(newTeacherResume);
    }

    public void updateTeachYearExperience(int newTeachYearExperience) {
        teacherRef.child("teachYearExperience").setValue(newTeachYearExperience);
    }

    public void incrementTeachYearExperience(int amount) {
        teacherRef.child("teachYearExperience").setValue(ServerValue.increment(amount));
    }

    public void decrementTeachYearExperience(int amount) {
        teacherRef.child("teachYearExperience").setValue(ServerValue.increment(-amount));
    }

    public void addCourseTeach(Course item) {
        addStringToArray("coursesTeach", item.getID());
    }

    public void removeCourseTeach(String courseId) {
        removeStringFromArray("coursesTeach", courseId);
    }

    public void removeCourseTeachCompletely(Course item) {
        removeStringFromArray("coursesTeach", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void removeLatestSubmission(String submissionId) {
        removeStringFromArray("latestSubmission", submissionId);
    }

    public void addCourseTypeTeach(String item) {
        addStringToArray("courseTypeTeach", item);
    }

    public void removeCourseTypeTeach(String itemId) {
        removeStringFromArray("courseTypeTeach", itemId);
    }

    public void addScheduledMeeting(Meeting item) {
        addStringToArrayAsync("scheduledMeetings", item.getID());
    }

    public void removeScheduledMeeting(String meetingId) {
        removeStringFromArray("scheduledMeetings", meetingId);
    }

    public void removeScheduledMeetingCompletely(Meeting item) {
        removeStringFromArray("scheduledMeetings", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void addCompletedMeeting(Meeting item) {
        addStringToArray("completedMeetings", item.getID());
    }

    public void removeCompletedMeeting(String meetingId) {
        removeStringFromArray("completedMeetings", meetingId);
    }

    public void removeCompletedMeetingCompletely(Meeting item) {
        removeStringFromArray("completedMeetings", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void addAgenda(Agenda item) {
        addStringToArray("agendas", item.getID());
    }

    public void removeAgenda(String agendaId) {
        removeStringFromArray("agendas", agendaId);
    }

    public void removeAgendaCompletely(Agenda item) {
        removeStringFromArray("agendas", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void addTimeArrangement(DayTimeArrangement item) {
        addStringToArray("timeArrangements", item.getID());
    }

    public void removeTimeArrangement(String arrangementId) {
        removeStringFromArray("timeArrangements", arrangementId);
    }

    public void removeTimeArrangementCompletely(DayTimeArrangement item) {
        removeStringFromArray("timeArrangements", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Updates multiple fields atomically with a lastModified timestamp.
     */
    public void updateMultipleFields(Map<String, Object> updates) {
        Map<String, Object> childUpdates = new HashMap<>();
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            childUpdates.put(entry.getKey(), entry.getValue());
        }
        childUpdates.put("lastModified", ServerValue.TIMESTAMP);
        teacherRef.updateChildren(childUpdates);
    }

    public com.google.android.gms.tasks.Task<Void> addNotificationId(String notificationId) {
        if (!Tool.boolOf(notificationId)) {
            return Tasks.forResult(null);
        }
        return Tasks.whenAll(
                addStringToArrayAsync("notitficationIDs", notificationId),
                addStringToArrayAsync("notificationIDs", notificationId),
                addStringToArrayAsync("notifications", notificationId)
        );
    }

    @Override
    public void sendMail(Mail mail) {
        addStringToArray("outbox", mail.getMailID());
    }

    @Override
    public void recieveMail(Mail mail) {
        addStringToArray("inbox", mail.getMailID());
    }

    @Override
    public void draftMail(Mail mail) {
        addStringToArray("draftMails", mail.getMailID());
    }
}
