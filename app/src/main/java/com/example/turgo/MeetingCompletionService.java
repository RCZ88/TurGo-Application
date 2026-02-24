package com.example.turgo;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MeetingCompletionService {

    private static final String TAG = "MeetingCompletionService";

    private MeetingCompletionService() {
    }

    public static Task<Void> completeMeetingIfDue(String meetingId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (!Tool.boolOf(meetingId)) {
            tcs.setResult(null);
            return tcs.getTask();
        }

        MeetingRepository meetingRepository = new MeetingRepository(meetingId);
        meetingRepository.loadAsNormal()
                .addOnSuccessListener(meeting -> {
                    if (meeting == null) {
                        tcs.setResult(null);
                        return;
                    }
                    if (!isDueAndIncomplete(meeting)) {
                        tcs.setResult(null);
                        return;
                    }

                    completeAndMove(meeting, meetingId)
                            .addOnSuccessListener(unused -> tcs.setResult(null))
                            .addOnFailureListener(tcs::setException);
                })
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }

    public static Task<Void> completeMeetingIfDue(String dbMeetingId, MeetingFirebase meetingFb) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (!Tool.boolOf(dbMeetingId) || meetingFb == null) {
            tcs.setResult(null);
            return tcs.getTask();
        }
        if (meetingFb.isCompleted()) {
            reconcileCompletedMeetingLinks(dbMeetingId, meetingFb)
                    .addOnSuccessListener(unused -> tcs.setResult(null))
                    .addOnFailureListener(tcs::setException);
            return tcs.getTask();
        }
        if (!isDueAndIncomplete(meetingFb)) {
            tcs.setResult(null);
            return tcs.getTask();
        }

        completeAndMoveRaw(dbMeetingId, meetingFb)
                .addOnSuccessListener(unused -> tcs.setResult(null))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public static Task<Void> reconcileCompletedMeetingLinks(String dbMeetingId, MeetingFirebase meetingFb) {
        if (!Tool.boolOf(dbMeetingId) || meetingFb == null) {
            return Tasks.forResult(null);
        }
        Set<String> idsToRemove = buildIdsToRemove(dbMeetingId, meetingFb);
        return moveRelationshipsRaw(meetingFb, idsToRemove, dbMeetingId);
    }

    private static boolean isDueAndIncomplete(Meeting meeting) {
        if (meeting.isCompleted()) {
            return false;
        }
        LocalDate date = meeting.getDateOfMeeting();
        LocalTime end = meeting.getEndTimeChange();
        if (date == null) {
            return false;
        }
        if (end == null) {
            return date.isBefore(LocalDate.now());
        }
        LocalDateTime meetingEnd = LocalDateTime.of(date, end);
        return !LocalDateTime.now().isBefore(meetingEnd);
    }

    private static boolean isDueAndIncomplete(MeetingFirebase meetingFb) {
        if (meetingFb.isCompleted()) {
            return false;
        }
        if (!Tool.boolOf(meetingFb.getDateOfMeeting())) {
            return false;
        }
        LocalDate meetingDate;
        try {
            meetingDate = LocalDate.parse(meetingFb.getDateOfMeeting());
        } catch (Exception e) {
            return false;
        }
        if (!Tool.boolOf(meetingFb.getEndTimeChange())) {
            return meetingDate.isBefore(LocalDate.now());
        }
        try {
            LocalTime end = LocalTime.parse(meetingFb.getEndTimeChange());
            return !LocalDateTime.now().isBefore(LocalDateTime.of(meetingDate, end));
        } catch (Exception e) {
            return meetingDate.isBefore(LocalDate.now());
        }
    }

    private static Task<Void> completeAndMove(Meeting meeting, String dbMeetingId) {
        String listMeetingId = dbMeetingId;
        MeetingRepository meetingRepository = new MeetingRepository(dbMeetingId);
        Task<Void> markCompletedTask = meetingRepository.getDbReference()
                .child("completed")
                .setValue(true);

        Task<Void> relationshipMoves = moveRelationships(meeting, dbMeetingId, listMeetingId);
        return Tasks.whenAll(markCompletedTask, relationshipMoves);
    }

    private static Task<Void> completeAndMoveRaw(String dbMeetingId, MeetingFirebase meetingFb) {
        String listMeetingId = dbMeetingId;
        MeetingRepository meetingRepository = new MeetingRepository(dbMeetingId);
        Task<Void> markCompletedTask = meetingRepository.getDbReference()
                .child("completed")
                .setValue(true);

        Set<String> idsToRemove = buildIdsToRemove(dbMeetingId, meetingFb);
        Task<Void> relationshipMoves = moveRelationshipsRaw(meetingFb, idsToRemove, listMeetingId);
        return Tasks.whenAll(markCompletedTask, relationshipMoves);
    }

    private static Task<Void> moveRelationships(Meeting meeting, String dbMeetingId, String listMeetingId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        if (!Tool.boolOf(meeting.getOfSchedule())) {
            return moveRelationshipsByMembershipScan(buildIdsToRemove(meeting, dbMeetingId), listMeetingId);
        }
        Set<String> idsToRemove = buildIdsToRemove(meeting, dbMeetingId);

        ScheduleRepository scheduleRepository = new ScheduleRepository(meeting.getOfSchedule());
        scheduleRepository.loadAsNormal()
                .addOnSuccessListener(schedule -> {
                    if (schedule == null) {
                        moveRelationshipsByMembershipScan(idsToRemove, listMeetingId)
                                .addOnSuccessListener(unused -> tcs.setResult(null))
                                .addOnFailureListener(tcs::setException);
                        return;
                    }

                    Task<Void> studentsMoveTask = buildStudentsMoveTask(schedule, meeting, idsToRemove, listMeetingId);

                    Task<Void> teacherMoveTask = schedule.getScheduleOfCourse().onSuccessTask(course -> {
                        if (course == null || !Tool.boolOf(course.getTeacherId())) {
                            return Tasks.forResult(null);
                        }
                        TeacherRepository tr = new TeacherRepository(course.getTeacherId());
                        List<Task<?>> teacherTasks = new ArrayList<>();
                        for (String id : idsToRemove) {
                            teacherTasks.add(tr.removeStringFromArrayAsync("scheduledMeetings", id));
                        }
                        teacherTasks.add(tr.addStringToArrayAsync("completedMeetings", listMeetingId));
                        return Tasks.whenAll(teacherTasks);
                    });

                    Task<Void> membershipFallbackTask = moveRelationshipsByMembershipScan(idsToRemove, listMeetingId);
                    Tasks.whenAll(studentsMoveTask, teacherMoveTask, membershipFallbackTask)
                            .addOnSuccessListener(unused -> tcs.setResult(null))
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to move relationships for meetingId=" + meeting.getID(), e);
                                tcs.setException(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load schedule for meetingId=" + meeting.getID(), e);
                    tcs.setException(e);
                });

        return tcs.getTask();
    }

    private static Task<Void> moveRelationshipsRaw(MeetingFirebase meetingFb, Set<String> idsToRemove, String listMeetingId) {
        if (!Tool.boolOf(meetingFb.getOfSchedule())) {
            return moveRelationshipsByMembershipScan(idsToRemove, listMeetingId);
        }

        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        ScheduleRepository scheduleRepository = new ScheduleRepository(meetingFb.getOfSchedule());
        scheduleRepository.loadAsNormal()
                .addOnSuccessListener(schedule -> {
                    if (schedule == null) {
                        moveRelationshipsByMembershipScan(idsToRemove, listMeetingId)
                                .addOnSuccessListener(unused -> tcs.setResult(null))
                                .addOnFailureListener(tcs::setException);
                        return;
                    }

                    Task<Void> studentsMoveTask = buildStudentsMoveTaskRaw(schedule, meetingFb, idsToRemove, listMeetingId);
                    Task<Void> teacherMoveTask = schedule.getScheduleOfCourse().onSuccessTask(course -> {
                        if (course == null || !Tool.boolOf(course.getTeacherId())) {
                            return Tasks.forResult(null);
                        }
                        TeacherRepository tr = new TeacherRepository(course.getTeacherId());
                        List<Task<?>> teacherTasks = new ArrayList<>();
                        for (String id : idsToRemove) {
                            teacherTasks.add(tr.removeStringFromArrayAsync("scheduledMeetings", id));
                        }
                        teacherTasks.add(tr.addStringToArrayAsync("completedMeetings", listMeetingId));
                        return Tasks.whenAll(teacherTasks);
                    });

                    Task<Void> membershipFallbackTask = moveRelationshipsByMembershipScan(idsToRemove, listMeetingId);
                    Tasks.whenAll(studentsMoveTask, teacherMoveTask, membershipFallbackTask)
                            .addOnSuccessListener(unused -> tcs.setResult(null))
                            .addOnFailureListener(tcs::setException);
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    private static Task<Void> buildStudentsMoveTask(Schedule schedule, Meeting meeting, Set<String> idsToRemove, String listMeetingId) {
        String scheduleId = schedule != null ? schedule.getID() : null;
        String currentWeekKey = Student.getCurrentWeekKey();
        return schedule.getStudents().onSuccessTask(students -> {
            Set<String> candidateStudentIds = new LinkedHashSet<>();
            if (students != null) {
                for (Student student : students) {
                    if (student != null && Tool.boolOf(student.getID())) {
                        candidateStudentIds.add(student.getID());
                    }
                }
            }
            if (meeting.getUsersRelated() != null) {
                for (String userId : meeting.getUsersRelated()) {
                    if (Tool.boolOf(userId)) {
                        candidateStudentIds.add(userId);
                    }
                }
            }

            List<Task<?>> studentTasks = new ArrayList<>();
            for (String candidateId : candidateStudentIds) {
                Task<Void> task = FirebaseDatabase.getInstance()
                        .getReference(FirebaseNode.STUDENT.getPath())
                        .child(candidateId)
                        .get()
                        .onSuccessTask(snapshot -> {
                            if (!snapshot.exists()) {
                                return Tasks.forResult(null);
                            }
                            StudentRepository sr = new StudentRepository(candidateId);
                            List<Task<?>> moveTasks = new ArrayList<>();
                            for (String id : idsToRemove) {
                                moveTasks.add(sr.removeStringFromArrayAsync("preScheduledMeetings", id));
                            }
                            moveTasks.add(sr.addStringToArrayAsync("meetingHistory", listMeetingId));
                            if (Tool.boolOf(scheduleId)) {
                                moveTasks.add(sr.addScheduleCompletedThisWeekForCurrentWeek(scheduleId, currentWeekKey));
                            }
                            return Tasks.whenAll(moveTasks);
                        });
                studentTasks.add(task);
            }
            return studentTasks.isEmpty() ? Tasks.forResult(null) : Tasks.whenAll(studentTasks);
        });
    }

    private static Set<String> buildIdsToRemove(Meeting meeting, String dbMeetingId) {
        Set<String> idsToRemove = new LinkedHashSet<>();
        if (Tool.boolOf(dbMeetingId)) {
            idsToRemove.add(dbMeetingId);
        }
        if (Tool.boolOf(meeting.getRawMeetingID())) {
            idsToRemove.add(meeting.getRawMeetingID());
        }
        if (Tool.boolOf(meeting.getID())) {
            idsToRemove.add(meeting.getID());
        }
        return idsToRemove;
    }

    private static Set<String> buildIdsToRemove(String dbMeetingId, MeetingFirebase meetingFb) {
        Set<String> idsToRemove = new LinkedHashSet<>();
        if (Tool.boolOf(dbMeetingId)) {
            idsToRemove.add(dbMeetingId);
        }
        if (Tool.boolOf(meetingFb.getMeetingID())) {
            idsToRemove.add(meetingFb.getMeetingID());
        }
        if (Tool.boolOf(meetingFb.getOfSchedule()) && Tool.boolOf(meetingFb.getDateOfMeeting())) {
            idsToRemove.add(meetingFb.getOfSchedule() + "_" + meetingFb.getDateOfMeeting());
        }
        return idsToRemove;
    }

    private static Task<Void> buildStudentsMoveTaskRaw(Schedule schedule, MeetingFirebase meetingFb, Set<String> idsToRemove, String listMeetingId) {
        String scheduleId = schedule != null ? schedule.getID() : null;
        String currentWeekKey = Student.getCurrentWeekKey();
        return schedule.getStudents().onSuccessTask(students -> {
            Set<String> candidateStudentIds = new LinkedHashSet<>();
            if (students != null) {
                for (Student student : students) {
                    if (student != null && Tool.boolOf(student.getID())) {
                        candidateStudentIds.add(student.getID());
                    }
                }
            }
            if (meetingFb.getUsersRelated() != null) {
                for (String userId : meetingFb.getUsersRelated()) {
                    if (Tool.boolOf(userId)) {
                        candidateStudentIds.add(userId);
                    }
                }
            }

            List<Task<?>> studentTasks = new ArrayList<>();
            for (String candidateId : candidateStudentIds) {
                Task<Void> task = FirebaseDatabase.getInstance()
                        .getReference(FirebaseNode.STUDENT.getPath())
                        .child(candidateId)
                        .get()
                        .onSuccessTask(snapshot -> {
                            if (!snapshot.exists()) {
                                return Tasks.forResult(null);
                            }
                            StudentRepository sr = new StudentRepository(candidateId);
                            List<Task<?>> moveTasks = new ArrayList<>();
                            for (String id : idsToRemove) {
                                moveTasks.add(sr.removeStringFromArrayAsync("preScheduledMeetings", id));
                            }
                            moveTasks.add(sr.addStringToArrayAsync("meetingHistory", listMeetingId));
                            if (Tool.boolOf(scheduleId)) {
                                moveTasks.add(sr.addScheduleCompletedThisWeekForCurrentWeek(scheduleId, currentWeekKey));
                            }
                            return Tasks.whenAll(moveTasks);
                        });
                studentTasks.add(task);
            }
            return studentTasks.isEmpty() ? Tasks.forResult(null) : Tasks.whenAll(studentTasks);
        });
    }

    private static Task<Void> moveRelationshipsByMembershipScan(Set<String> idsToRemove, String listMeetingId) {
        Task<Void> studentsTask = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.STUDENT.getPath())
                .get()
                .onSuccessTask(snapshot -> {
                    List<Task<?>> tasks = new ArrayList<>();
                    for (com.google.firebase.database.DataSnapshot studentSnap : snapshot.getChildren()) {
                        String studentId = studentSnap.getKey();
                        if (!Tool.boolOf(studentId)) {
                            continue;
                        }
                        boolean hasInPreScheduled = containsAnyId(studentSnap.child("preScheduledMeetings"), idsToRemove);
                        if (!hasInPreScheduled) {
                            continue;
                        }
                        StudentRepository sr = new StudentRepository(studentId);
                        for (String id : idsToRemove) {
                            tasks.add(sr.removeStringFromArrayAsync("preScheduledMeetings", id));
                        }
                        tasks.add(sr.addStringToArrayAsync("meetingHistory", listMeetingId));
                    }
                    return tasks.isEmpty() ? Tasks.forResult(null) : Tasks.whenAll(tasks);
                });

        Task<Void> teachersTask = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.TEACHER.getPath())
                .get()
                .onSuccessTask(snapshot -> {
                    List<Task<?>> tasks = new ArrayList<>();
                    for (com.google.firebase.database.DataSnapshot teacherSnap : snapshot.getChildren()) {
                        String teacherId = teacherSnap.getKey();
                        if (!Tool.boolOf(teacherId)) {
                            continue;
                        }
                        boolean hasInScheduled = containsAnyId(teacherSnap.child("scheduledMeetings"), idsToRemove);
                        if (!hasInScheduled) {
                            continue;
                        }
                        TeacherRepository tr = new TeacherRepository(teacherId);
                        for (String id : idsToRemove) {
                            tasks.add(tr.removeStringFromArrayAsync("scheduledMeetings", id));
                        }
                        tasks.add(tr.addStringToArrayAsync("completedMeetings", listMeetingId));
                    }
                    return tasks.isEmpty() ? Tasks.forResult(null) : Tasks.whenAll(tasks);
                });

        return Tasks.whenAll(studentsTask, teachersTask);
    }

    private static boolean containsAnyId(com.google.firebase.database.DataSnapshot arraySnapshot, Set<String> ids) {
        if (arraySnapshot == null || !arraySnapshot.exists() || ids == null || ids.isEmpty()) {
            return false;
        }
        for (com.google.firebase.database.DataSnapshot child : arraySnapshot.getChildren()) {
            String value = child.getValue(String.class);
            if (Tool.boolOf(value) && ids.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
