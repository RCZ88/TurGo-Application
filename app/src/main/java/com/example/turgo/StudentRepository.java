package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentRepository implements RepositoryClass<Student, StudentFirebase>, UserRepositoryClass, Serializable {
    private DatabaseReference studentRef;

    public StudentRepository(String studentId) {
        studentRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.STUDENT.getPath())
                .child(studentId);
    }



    @Override
    public DatabaseReference getDbReference() {
        return studentRef;
    }

    @Override
    public Class<StudentFirebase> getFbClass() {
        return StudentFirebase.class;
    }


    public void delete() {
        studentRef.removeValue();
    }

    public void addCourseTaken(Course item) {
        addStringToArray("courseTaken", item.getID());
    }

    public void removeCourseTaken(String courseId) {
        removeStringFromArray("courseTaken", courseId);
    }

    public void removeCourseTakenCompletely(Course item) {
        removeStringFromArray("courseTaken", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void addStudentCourseTaken(StudentCourse item) {
        addStringToArray("studentCourseTaken", item.getID());
    }

    public void removeStudentCourseTaken(String studentCourseId) {
        removeStringFromArray("studentCourseTaken", studentCourseId);
    }

    public void removeStudentCourseTakenCompletely(StudentCourse item) {
        removeStringFromArray("studentCourseTaken", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void addCourseInterested(String item) {
        addStringToArray("courseInterested", item);
    }

    public void removeCourseInterested(String itemId) {
        removeStringFromArray("courseInterested", itemId);
    }

    public void addCourseRelated(Course item) {
        addStringToArray("courseRelated", item.getID());
    }

    public void removeCourseRelated(String courseId) {
        removeStringFromArray("courseRelated", courseId);
    }

    public void removeCourseRelatedCompletely(Course item) {
        removeStringFromArray("courseRelated", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void addPreScheduledMeeting(Meeting item) {
        addStringToArrayAsync("preScheduledMeetings", item.getID());
    }

    public void removePreScheduledMeeting(String meetingId) {
        removeStringFromArray("preScheduledMeetings", meetingId);
    }

    public void removePreScheduledMeetingCompletely(Meeting item) {
        removeStringFromArray("preScheduledMeetings", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void addMeetingHistory(Meeting item) {
        addStringToArray("meetingHistory", item.getID());
    }

    public void removeMeetingHistory(String meetingId) {
        removeStringFromArray("meetingHistory", meetingId);
    }

    public void removeMeetingHistoryCompletely(Meeting item) {
        removeStringFromArray("meetingHistory", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void addScheduleCompletedThisWeek(Schedule item) {
        addStringToArray("scheduleCompletedThisWeek", item.getID());
    }

    public void removeScheduleCompletedThisWeek(String scheduleId) {
        removeStringFromArray("scheduleCompletedThisWeek", scheduleId);
    }

    public void removeScheduleCompletedThisWeekCompletely(Schedule item) {
        removeStringFromArray("scheduleCompletedThisWeek", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void updatePercentageCompleted(double newPercentageCompleted) {
        studentRef.child("percentageCompleted").setValue(newPercentageCompleted);
    }

    public com.google.android.gms.tasks.Task<Boolean> resetWeeklyMeetingCompletionIfNeeded(String currentWeekKey) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
        studentRef.get().addOnSuccessListener(currentSnapshot -> {
            String storedWeekKey = currentSnapshot.child("completionWeekKey").getValue(String.class);
            if (!Tool.boolOf(currentWeekKey) || currentWeekKey.equals(storedWeekKey)) {
                tcs.setResult(false);
                return;
            }
            Map<String, Object> updates = new HashMap<>();
            updates.put("completionWeekKey", currentWeekKey);
            updates.put("scheduleCompletedThisWeek", new ArrayList<String>());
            updates.put("percentageCompleted", 0d);
            studentRef.updateChildren(updates)
                    .addOnSuccessListener(unused -> tcs.setResult(true))
                    .addOnFailureListener(tcs::setException);
        }).addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public com.google.android.gms.tasks.Task<Void> addScheduleCompletedThisWeekForCurrentWeek(String scheduleId, String currentWeekKey) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (!Tool.boolOf(scheduleId)) {
            tcs.setResult(null);
            return tcs.getTask();
        }
        final String safeWeekKey = Tool.boolOf(currentWeekKey) ? currentWeekKey : Student.getCurrentWeekKey();
        studentRef.get().addOnSuccessListener(currentSnapshot -> {
            String storedWeekKey = currentSnapshot.child("completionWeekKey").getValue(String.class);
            boolean weekChanged = !Tool.boolOf(storedWeekKey) || !storedWeekKey.equals(safeWeekKey);

            ArrayList<String> completed = extractStringList(
                    currentSnapshot.child("scheduleCompletedThisWeek").getValue()
            );
            if (weekChanged) {
                completed.clear();
            }
            if (!completed.contains(scheduleId)) {
                completed.add(scheduleId);
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("scheduleCompletedThisWeek", completed);
            if (weekChanged) {
                updates.put("completionWeekKey", safeWeekKey);
                updates.put("percentageCompleted", 0d);
            }

            studentRef.updateChildren(updates)
                    .addOnSuccessListener(unused -> tcs.setResult(null))
                    .addOnFailureListener(tcs::setException);
        }).addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public com.google.android.gms.tasks.Task<Void> upsertWeeklyCompletionSnapshot(String currentWeekKey, ArrayList<String> scheduleIds, double percentageCompleted) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        String safeWeekKey = Tool.boolOf(currentWeekKey) ? currentWeekKey : Student.getCurrentWeekKey();

        ArrayList<String> safeIds = new ArrayList<>();
        if (scheduleIds != null) {
            for (String id : scheduleIds) {
                if (Tool.boolOf(id) && !safeIds.contains(id)) {
                    safeIds.add(id);
                }
            }
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("completionWeekKey", safeWeekKey);
        updates.put("scheduleCompletedThisWeek", safeIds);
        updates.put("percentageCompleted", Math.max(0d, percentageCompleted));

        studentRef.updateChildren(updates)
                .addOnSuccessListener(unused -> tcs.setResult(null))
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }

    public void updateNextMeeting(Meeting newNextMeeting) {
        studentRef.child("nextMeeting").setValue(newNextMeeting != null ? newNextMeeting.getID() : null);
    }

    public void updateSchool(String newSchool) {
        studentRef.child("school").setValue(newSchool);
    }

    public void updateGradeLevel(String newGradeLevel) {
        studentRef.child("gradeLevel").setValue(newGradeLevel);
    }

    public void addAllSchedule(Schedule item) {
        addStringToArray("allSchedules", item.getID());
    }
    public void replaceAllSchedule(ArrayList<Schedule> oldList, ArrayList<Schedule> newList){
        oldList.forEach(this::removeAllScheduleCompletely);
        newList.forEach(this::addAllSchedule);
    }
    public void removeAllSchedule(String scheduleId) {
        removeStringFromArray("allSchedules", scheduleId);
    }

    public void removeAllScheduleCompletely(Schedule item) {
        removeStringFromArray("allSchedules", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void updateLastScheduled(LocalDate newLastScheduled) {;
        studentRef.child("lastScheduled").setValue(newLastScheduled.toString());
    }

    public void updateHasScheduled(boolean newHasScheduled) {
        studentRef.child("hasScheduled").setValue(newHasScheduled);
    }

    public void updateAutoSchedule(int newAutoSchedule) {
        studentRef.child("autoSchedule").setValue(newAutoSchedule);
    }

    public void updateNotificationEarlyDuration(Duration newNotificationEarlyDuration) {
        studentRef.child("notificationEarlyDuration").setValue(newNotificationEarlyDuration.toString());
    }

    public void addAllTask(Task item) {
        addStringToArray("allTask", item.getID());
    }

    public void removeAllTask(String taskId) {
        removeStringFromArray("allTask", taskId);
    }

    public void removeAllTaskCompletely(Task item) {
        removeStringFromArray("allTask", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void addUncompletedTask(Task item) {
        addStringToArray("uncompletedTask", item.getID());
    }

    public void removeUncompletedTask(String taskId) {
        removeStringFromArray("uncompletedTask", taskId);
    }

    public com.google.android.gms.tasks.Task<Void> removeUncompletedTaskAsync(String taskId) {
        return removeStringFromArrayAsync("uncompletedTask", taskId);
    }

    public void removeUncompletedTaskCompletely(Task item) {
        removeStringFromArray("uncompletedTask", item.getID());
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void addManualCompletedTask(Task item) {
        addStringToArray("manualCompletedTask", item.getID());
    }

    public com.google.android.gms.tasks.Task<Void> addManualCompletedTaskAsync(String taskId) {
        return addStringToArrayAsync("manualCompletedTask", taskId);
    }

    public com.google.android.gms.tasks.Task<Void> removeManualCompletedTaskAsync(String taskId) {
        return removeStringFromArrayAsync("manualCompletedTask", taskId);
    }

    public void addManualMissedTask(Task item) {
        addStringToArray("manualMissedTask", item.getID());
    }

    public com.google.android.gms.tasks.Task<Void> addManualMissedTaskAsync(String taskId) {
        return addStringToArrayAsync("manualMissedTask", taskId);
    }

    public com.google.android.gms.tasks.Task<Void> removeManualMissedTaskAsync(String taskId) {
        return removeStringFromArrayAsync("manualMissedTask", taskId);
    }

    public void addAllAgenda(Agenda item) {
        addStringToArray("allAgendas", item.getID());
    }

    public void removeAllAgenda(String agendaId) {
        removeStringFromArray("allAgendas", agendaId);
    }

    public void removeAllAgendaCompletely(Agenda item) {
        removeStringFromArray("allAgendas", item.getID());
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
