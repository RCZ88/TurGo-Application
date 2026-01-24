package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentRepository {
    private static StudentRepository instance;
    private DatabaseReference studentRef;

    public static StudentRepository getInstance(String studentId) {
        if (instance == null) {
            instance = new StudentRepository(studentId);
        }
        return instance;
    }

    private StudentRepository(String studentId) {
        studentRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.STUDENT.getPath())
                .child(studentId);
    }

    public void save(Student object) {
        try {
            StudentFirebase firebaseObj = object.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(object);
            studentRef.setValue(firebaseObj);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to save Student", e);
        }
    }


    public void delete() {
        studentRef.removeValue();
    }

    /**
     * Adds a Course to this Student.
     * Saves the full Course object to its Firebase path and stores only the ID reference here.
     */
    public void addCourseTaken(Course item) {
        try {
            CourseFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            studentRef.child("courseTaken").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add CourseTaken", e);
        }
    }

    public void removeCourseTaken(String courseId) {
        studentRef.child("courseTaken").child(courseId).removeValue();
    }

    public void removeCourseTakenCompletely(Course item) {
        studentRef.child("courseTaken").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds a StudentCourse to this Student.
     * Saves the full StudentCourse object to its Firebase path and stores only the ID reference here.
     */
    public void addStudentCourseTaken(StudentCourse item) {
        try {
            StudentCourseFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            studentRef.child("studentCourseTaken").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add StudentCourseTaken", e);
        }
    }

    public void removeStudentCourseTaken(String studentCourseId) {
        studentRef.child("studentCourseTaken").child(studentCourseId).removeValue();
    }

    public void removeStudentCourseTakenCompletely(StudentCourse item) {
        studentRef.child("studentCourseTaken").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void addCourseInterested(String item) {
        studentRef.child("courseInterested").push().setValue(item);
    }

    public void removeCourseInterested(String itemId) {
        studentRef.child("courseInterested").child(itemId).removeValue();
    }

    /**
     * Adds a Course to this Student.
     * Saves the full Course object to its Firebase path and stores only the ID reference here.
     */
    public void addCourseRelated(Course item) {
        try {
            CourseFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            studentRef.child("courseRelated").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add CourseRelated", e);
        }
    }

    public void removeCourseRelated(String courseId) {
        studentRef.child("courseRelated").child(courseId).removeValue();
    }

    public void removeCourseRelatedCompletely(Course item) {
        studentRef.child("courseRelated").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds a Meeting to this Student.
     * Saves the full Meeting object to its Firebase path and stores only the ID reference here.
     */
    public void addPreScheduledMeeting(Meeting item) {
        try {
            MeetingFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            studentRef.child("preScheduledMeetings").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add PreScheduledMeeting", e);
        }
    }

    public void removePreScheduledMeeting(String meetingId) {
        studentRef.child("preScheduledMeetings").child(meetingId).removeValue();
    }

    public void removePreScheduledMeetingCompletely(Meeting item) {
        studentRef.child("preScheduledMeetings").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds a Meeting to this Student.
     * Saves the full Meeting object to its Firebase path and stores only the ID reference here.
     */
    public void addMeetingHistory(Meeting item) {
        try {
            MeetingFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            studentRef.child("meetingHistory").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add MeetingHistory", e);
        }
    }

    public void removeMeetingHistory(String meetingId) {
        studentRef.child("meetingHistory").child(meetingId).removeValue();
    }

    public void removeMeetingHistoryCompletely(Meeting item) {
        studentRef.child("meetingHistory").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds a Schedule to this Student.
     * Saves the full Schedule object to its Firebase path and stores only the ID reference here.
     */
    public void addScheduleCompletedThisWeek(Schedule item) {
        try {
            ScheduleFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            studentRef.child("scheduleCompletedThisWeek").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add ScheduleCompletedThisWeek", e);
        }
    }

    public void removeScheduleCompletedThisWeek(String scheduleId) {
        studentRef.child("scheduleCompletedThisWeek").child(scheduleId).removeValue();
    }

    public void removeScheduleCompletedThisWeekCompletely(Schedule item) {
        studentRef.child("scheduleCompletedThisWeek").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void updatePercentageCompleted(double newPercentageCompleted) {
        studentRef.child("percentageCompleted").setValue(newPercentageCompleted);
    }

    public void updateNextMeeting(Meeting newNextMeeting) {
        studentRef.child("nextMeeting").setValue(newNextMeeting);
    }

    public void updateSchool(String newSchool) {
        studentRef.child("school").setValue(newSchool);
    }

    public void updateGradeLevel(String newGradeLevel) {
        studentRef.child("gradeLevel").setValue(newGradeLevel);
    }

    /**
     * Adds a Schedule to this Student.
     * Saves the full Schedule object to its Firebase path and stores only the ID reference here.
     */
    public void addAllSchedule(Schedule item) {
        try {
            ScheduleFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            studentRef.child("allSchedules").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add AllSchedule", e);
        }
    }
    public void replaceAllSchedule(ArrayList<Schedule> oldList, ArrayList<Schedule> newList){
        oldList.forEach(this::removeAllScheduleCompletely);
        newList.forEach(this::addAllSchedule);
    }
    public void removeAllSchedule(String scheduleId) {
        studentRef.child("allSchedules").child(scheduleId).removeValue();
    }

    public void removeAllScheduleCompletely(Schedule item) {
        studentRef.child("allSchedules").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void updateLastScheduled(LocalDate newLastScheduled) {
        studentRef.child("lastScheduled").setValue(newLastScheduled);
    }

    public void updateHasScheduled(boolean newHasScheduled) {
        studentRef.child("hasScheduled").setValue(newHasScheduled);
    }

    public void updateAutoSchedule(int newAutoSchedule) {
        studentRef.child("autoSchedule").setValue(newAutoSchedule);
    }

    public void updateNotificationEarlyDuration(Duration newNotificationEarlyDuration) {
        studentRef.child("notificationEarlyDuration").setValue(newNotificationEarlyDuration);
    }

    /**
     * Adds a Task to this Student.
     * Saves the full Task object to its Firebase path and stores only the ID reference here.
     */
    public void addAllTask(Task item) {
        try {
            TaskFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            studentRef.child("allTask").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add AllTask", e);
        }
    }

    public void removeAllTask(String taskId) {
        studentRef.child("allTask").child(taskId).removeValue();
    }

    public void removeAllTaskCompletely(Task item) {
        studentRef.child("allTask").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds a Task to this Student.
     * Saves the full Task object to its Firebase path and stores only the ID reference here.
     */
    public void addUncompletedTask(Task item) {
        try {
            TaskFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            studentRef.child("uncompletedTask").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add UncompletedTask", e);
        }
    }

    public void removeUncompletedTask(String taskId) {
        studentRef.child("uncompletedTask").child(taskId).removeValue();
    }

    public void removeUncompletedTaskCompletely(Task item) {
        studentRef.child("uncompletedTask").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds an Agenda to this Student.
     * Saves the full Agenda object to its Firebase path and stores only the ID reference here.
     */
    public void addAllAgenda(Agenda item) {
        try {
            AgendaFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            studentRef.child("allAgendas").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add AllAgenda", e);
        }
    }

    public void removeAllAgenda(String agendaId) {
        studentRef.child("allAgendas").child(agendaId).removeValue();
    }

    public void removeAllAgendaCompletely(Agenda item) {
        studentRef.child("allAgendas").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void updateLateAttendance(int newLateAttendance) {
        studentRef.child("lateAttendance").setValue(newLateAttendance);
    }

    public void updateLateSubmissions(int newLateSubmissions) {
        studentRef.child("lateSubmissions").setValue(newLateSubmissions);
    }

    public void updateMultipleFields(Map<String, Object> updates) {
        Map<String, Object> childUpdates = new HashMap<>();
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            childUpdates.put(entry.getKey(), entry.getValue());
        }
        childUpdates.put("lastModified", ServerValue.TIMESTAMP);
        studentRef.updateChildren(childUpdates);
    }

    public void incrementAutoSchedule(int amount) {
        studentRef.child("autoSchedule").setValue(ServerValue.increment(amount));
    }

    public void decrementAutoSchedule(int amount) {
        studentRef.child("autoSchedule").setValue(ServerValue.increment(-amount));
    }

    public void incrementLateAttendance(int amount) {
        studentRef.child("lateAttendance").setValue(ServerValue.increment(amount));
    }

    public void decrementLateAttendance(int amount) {
        studentRef.child("lateAttendance").setValue(ServerValue.increment(-amount));
    }

    public void incrementLateSubmissions(int amount) {
        studentRef.child("lateSubmissions").setValue(ServerValue.increment(amount));
    }

    public void decrementLateSubmissions(int amount) {
        studentRef.child("lateSubmissions").setValue(ServerValue.increment(-amount));
    }
}
