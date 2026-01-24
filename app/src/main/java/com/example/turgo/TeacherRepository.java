package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;

public class TeacherRepository {
    private static TeacherRepository instance;
    private DatabaseReference teacherRef;

    public static TeacherRepository getInstance(String teacherId) {
        if (instance == null) {
            instance = new TeacherRepository(teacherId);
        }
        return instance;
    }

    private TeacherRepository(String teacherId) {
        teacherRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.TEACHER.getPath())
                .child(teacherId);
    }

    public void save(Teacher object) {
        try {
            TeacherFirebase firebaseObj = object.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(object);
            teacherRef.setValue(firebaseObj);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to save Teacher", e);
        }
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

    /**
     * Adds a Course to this Teacher.
     * Saves the full Course object to its Firebase path and stores only the ID reference here.
     */
    public void addCourseTeach(Course item) {
        try {
            CourseFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            teacherRef.child("coursesTeach").child(item.getID()).setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add CourseTeach", e);
        }
    }

    public void removeCourseTeach(String courseId) {
        teacherRef.child("coursesTeach").child(courseId).removeValue();
    }

    public void removeCourseTeachCompletely(Course item) {
        teacherRef.child("coursesTeach").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds a SubmissionDisplay to this Teacher.
     * Saves the full object to its Firebase path and stores only the ID reference here.
     */
    public void removeLatestSubmission(String submissionId) {
        teacherRef.child("latestSubmission").child(submissionId).removeValue();
    }

    /**
     * Adds a CourseTypeTeach (String) to this Teacher.
     */
    public void addCourseTypeTeach(String item) {
        teacherRef.child("courseTypeTeach").push().setValue(item);
    }

    public void removeCourseTypeTeach(String itemId) {
        teacherRef.child("courseTypeTeach").child(itemId).removeValue();
    }

    /**
     * Adds a Meeting to this Teacher's scheduledMeetings.
     * Saves the full Meeting object to its Firebase path and stores only the ID reference here.
     */
    public void addScheduledMeeting(Meeting item) {
        try {
            MeetingFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            teacherRef.child("scheduledMeetings").child(item.getID()).setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add ScheduledMeeting", e);
        }
    }

    public void removeScheduledMeeting(String meetingId) {
        teacherRef.child("scheduledMeetings").child(meetingId).removeValue();
    }

    public void removeScheduledMeetingCompletely(Meeting item) {
        teacherRef.child("scheduledMeetings").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds a Meeting to this Teacher's completedMeetings.
     */
    public void addCompletedMeeting(Meeting item) {
        try {
            MeetingFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            teacherRef.child("completedMeetings").child(item.getID()).setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add CompletedMeeting", e);
        }
    }

    public void removeCompletedMeeting(String meetingId) {
        teacherRef.child("completedMeetings").child(meetingId).removeValue();
    }

    public void removeCompletedMeetingCompletely(Meeting item) {
        teacherRef.child("completedMeetings").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds an Agenda to this Teacher.
     */
    public void addAgenda(Agenda item) {
        try {
            AgendaFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            teacherRef.child("agendas").child(item.getID()).setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add Agenda", e);
        }
    }

    public void removeAgenda(String agendaId) {
        teacherRef.child("agendas").child(agendaId).removeValue();
    }

    public void removeAgendaCompletely(Agenda item) {
        teacherRef.child("agendas").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds a DayTimeArrangement to this Teacher.
     */
    public void addTimeArrangement(DayTimeArrangement item) {
        try {
            DTAFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            teacherRef.child("timeArrangements").child(item.getID()).setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add TimeArrangement", e);
        }
    }

    public void removeTimeArrangement(String arrangementId) {
        teacherRef.child("timeArrangements").child(arrangementId).removeValue();
    }

    public void removeTimeArrangementCompletely(DayTimeArrangement item) {
        teacherRef.child("timeArrangements").child(item.getID()).removeValue();
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
}
