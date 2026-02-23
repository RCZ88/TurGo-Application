package com.example.turgo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.*;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class MeetingProcessorWorker extends Worker {

    private static final int WEEKS_AHEAD = 4;
    private static final String TAG = "MeetingProcessor";
    public static final String KEY_CLEANUP_ONLY = "cleanup_only";
    private static final String MANUAL_CLEANUP_WORK_NAME = "MeetingProcessorManualCleanup";
    private static final String PREFS_NAME = "meeting_processor_state";
    private static final String PREF_ONE_TIME_PRESCHEDULED_REBUILD_V1 = "one_time_prescheduled_rebuild_v1_done";

    public MeetingProcessorWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Starting daily meeting processor at " + LocalDateTime.now());

            boolean cleanupOnly = getInputData().getBoolean(KEY_CLEANUP_ONLY, false);
            if (cleanupOnly) {
                processCompletedMeetings();
                processOverdueTasks();
                cleanupOldMeetings();
                runOneTimePrescheduledRebuildIfNeeded();
                Log.d(TAG, "Manual cleanup-only run started (includes due-meeting completion)");
                return Result.success();
            }

            // Step 1: Process all active courses
            processAllCourses();

            // Step 2: Complete due meetings and move them to history/completed lists
            processCompletedMeetings();

            // Step 3: Cleanup old meetings
            cleanupOldMeetings();

            // Step 4: Process overdue non-dropbox tasks
            processOverdueTasks();

            // Step 5: Normalize legacy duplicate/random meeting IDs
            cleanupDuplicateMeetings();

            // Step 6: One-time repair of students/{id}/preScheduledMeetings from meetings source of truth
            runOneTimePrescheduledRebuildIfNeeded();

            Log.d(TAG, "Meeting processor completed successfully");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Meeting processor failed: " + e.getMessage(), e);
            return Result.retry(); // Retry on failure
        }
    }

    public static void enqueueCleanupNow(Context context) {
        Data inputData = new Data.Builder()
                .putBoolean(KEY_CLEANUP_ONLY, true)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(MeetingProcessorWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                MANUAL_CLEANUP_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
        );
    }

    private void processCompletedMeetings() {
        DatabaseReference meetingsRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.MEETING.getPath());

        meetingsRef.get()
                .addOnSuccessListener(snapshot -> {
                    for (DataSnapshot meetingSnapshot : snapshot.getChildren()) {
                        String meetingId = meetingSnapshot.getKey();
                        if (!Tool.boolOf(meetingId)) {
                            continue;
                        }
                        MeetingFirebase meetingFb = meetingSnapshot.getValue(MeetingFirebase.class);
                        MeetingCompletionService.completeMeetingIfDue(meetingId, meetingFb)
                                .addOnFailureListener(e -> Log.e(TAG, "Failed completion check for " + meetingId, e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to process completed meetings", e));
    }

    private void processOverdueTasks() {
        TaskDeadlineService.processOverdueNonDropboxTasks()
                .addOnFailureListener(e -> Log.e(TAG, "Failed to process overdue tasks", e));
    }

    private void processAllCourses() {
        DatabaseReference coursesRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.COURSE.getPath());

        coursesRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                CourseFirebase courseFirebase = courseSnapshot.getValue(CourseFirebase.class);
                if (courseFirebase != null) {
                    try {
                        courseFirebase.convertToNormal(new ObjectCallBack<>() {
                            @Override
                            public void onObjectRetrieved(Course object) {
                                processCourseMeetings(object);
                            }

                            @Override
                            public void onError(DatabaseError error) {

                            }
                        });
                    } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                             IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch courses: " + e.getMessage()));
    }

    private void processCourseMeetings(Course course) {
        ArrayList<Schedule> schedules = course.getSchedules();
        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();

        for (Schedule schedule : schedules) {
            // Generate meetings for next 4 weeks
            for (int week = 0; week < WEEKS_AHEAD; week++) {
                LocalDate meetingDate = today.plusWeeks(week)
                        .with(java.time.temporal.TemporalAdjusters.nextOrSame(schedule.getDay()));

                // Check if this meeting already exists
                String meetingId = generateMeetingId(schedule.getID(), meetingDate);
                checkAndCreateMeeting(schedule, meetingDate, meetingId);
            }
        }
    }

    private void checkAndCreateMeeting(Schedule schedule, LocalDate meetingDate, String meetingId) {
        MeetingRepository meetingRepo = new MeetingRepository(meetingId);
        meetingRepo.loadAsNormal().addOnSuccessListener(meeting ->{
            if(meeting == null){
                createNewMeeting(schedule, meetingDate);
            }else{
                normalizeMeetingReferences(schedule, meeting, meetingId);
                assignAlarmsToMeeting(meeting, schedule);
            }
        });
    }

    private void normalizeMeetingReferences(Schedule schedule, Meeting meeting, String canonicalMeetingId) {
        MeetingRepository meetingRepo = new MeetingRepository(canonicalMeetingId);
        String legacyMeetingId = meeting.getRawMeetingID();
        try {
            meetingRepo.save(meeting);
        } catch (Exception e) {
            Log.e(TAG, "Failed to normalize meeting payload for " + canonicalMeetingId, e);
        }

        schedule.getStudents().addOnSuccessListener(students -> {
            if (students == null || students.isEmpty()) {
                return;
            }
            for (Student student : students) {
                if (student == null || !Tool.boolOf(student.getID())) {
                    continue;
                }
                StudentRepository studentRepository = new StudentRepository(student.getID());
                studentRepository.addStringToArrayAsync("preScheduledMeetings", canonicalMeetingId);
                if (!canonicalMeetingId.equals(legacyMeetingId)) {
                    studentRepository.removeStringFromArrayAsync("preScheduledMeetings", legacyMeetingId);
                }
            }
        });

        schedule.getScheduleOfCourse().addOnSuccessListener(course -> {
            if (course == null || !Tool.boolOf(course.getTeacherId())) {
                Log.w(TAG, "Skipping teacher normalization for meeting " + canonicalMeetingId + ": missing course/teacher");
                return;
            }
            TeacherRepository teacherRepository = new TeacherRepository(course.getTeacherId());
            teacherRepository.addStringToArrayAsync("scheduledMeetings", canonicalMeetingId);
            if (!canonicalMeetingId.equals(legacyMeetingId)) {
                teacherRepository.removeStringFromArrayAsync("scheduledMeetings", legacyMeetingId);
            }
        });
    }

    private void createNewMeeting(Schedule schedule, LocalDate meetingDate) {


        // Create meeting WITHOUT student (system-generated)
        String meetingId = generateMeetingId(schedule.getID(), meetingDate);
        Meeting meeting = new Meeting(meetingId, schedule, meetingDate, null);
        schedule.getStudents().addOnSuccessListener(students ->{
            if (students == null || students.isEmpty()) {
                Log.w(TAG, "No students found for schedule " + schedule.getID() + " while creating " + meetingId);
                if (students == null) {
                    return;
                }
            }
            students.forEach(student -> {
                if (student == null || !Tool.boolOf(student.getID())) {
                    return;
                }
                StudentRepository studentRepository = new StudentRepository(student.getID());
                studentRepository.addStringToArrayAsync("preScheduledMeetings", meetingId);
                meeting.getUsersRelated().add(student.getID());
            });
            schedule.getScheduleOfCourse().addOnSuccessListener(course ->{
                if (course == null || !Tool.boolOf(course.getTeacherId())) {
                    Log.w(TAG, "Skipping teacher link for new meeting " + meetingId + ": missing course/teacher");
                } else {
                TeacherRepository teacherRepository = new TeacherRepository(course.getTeacherId());
                teacherRepository.addStringToArrayAsync("scheduledMeetings", meetingId);
                meeting.getUsersRelated().add(course.getTeacherId());
                }

                Log.d(TAG, "Creating new meeting: " + meeting.getMeetingID());
                // Save to Firebase
                MeetingRepository meetingRepo = new MeetingRepository(meetingId);
                try {
                    meetingRepo.save(meeting);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to save meeting: " + e.getMessage());
                }
            });
        });
    }

    private void assignAlarmsToMeeting(Meeting meeting, Schedule schedule) {
        if (meeting.isAlarmAssigned()) {
            return;
        }
        // Get all students in this schedule
        schedule.getStudents().addOnSuccessListener(students ->{
            if (students == null || students.isEmpty()) {
                Log.d(TAG, "No students in schedule " + schedule.getID());
                return;
            }

            Log.d(TAG, "Assigning alarms for meeting " + meeting.getMeetingID() +
                    " to " + students.size() + " students");

            // Assign alarm to each student
            meeting.assignAlarmNotification((ArrayList<Student>) students, getApplicationContext());

            // Mark as assigned
            meeting.setAlarmAssigned(true);
            meeting.setAlarmAssignedAt(LocalDateTime.now());

            // Update in Firebase
            MeetingRepository meetingRepo = new MeetingRepository(meeting.getMeetingID());
            try {
                meetingRepo.updateAlarmAssigned(true);
                meetingRepo.updateAlarmAssignedAt(LocalDateTime.now());
            } catch (Exception e) {
                Log.e(TAG, "Failed to update alarm status: " + e.getMessage());
            }
        });


    }

    private void cleanupOldMeetings() {
        LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);

        DatabaseReference meetingsRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.MEETING.getPath());

        meetingsRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot meetingSnapshot : snapshot.getChildren()) {
                MeetingFirebase meetingFb = meetingSnapshot.getValue(MeetingFirebase.class);
                if (meetingFb != null) {
                    try {
                        meetingFb.convertToNormal(new ObjectCallBack<>() {
                            @Override
                            public void onObjectRetrieved(Meeting meeting) {
                                if (meeting == null || meeting.getDateOfMeeting() == null) {
                                    return;
                                }
                                if (!meeting.getDateOfMeeting().isBefore(oneWeekAgo)) {
                                    return;
                                }
                                cleanupOldMeetingReferences(meeting)
                                        .addOnSuccessListener(unused -> {
                                            meetingSnapshot.getRef().removeValue();
                                            Log.d(TAG, "Deleted old meeting and cleaned references: " + meeting.getMeetingID());
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "Failed reference cleanup for old meeting: " + meeting.getMeetingID(), e));
                            }

                            @Override
                            public void onError(DatabaseError error) {
                                Log.e(TAG, "Failed to convert meeting for cleanup: " + error.getMessage());
                            }
                        });
                    } catch (ParseException | InvocationTargetException |
                             NoSuchMethodException | IllegalAccessException |
                             InstantiationException e) {
                        Log.e(TAG, "Meeting conversion exception during cleanup", e);
                    }
                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch meetings for cleanup", e));
    }

    private Task<Void> cleanupOldMeetingReferences(Meeting meeting) {
        if (meeting == null || !Tool.boolOf(meeting.getID()) || !Tool.boolOf(meeting.getOfSchedule())) {
            return Tasks.forResult(null);
        }

        String meetingId = meeting.getID();
        ScheduleRepository scheduleRepository = new ScheduleRepository(meeting.getOfSchedule());

        Task<Void> studentsCleanupTask = scheduleRepository.loadAsNormal().onSuccessTask(schedule -> {
            if (schedule == null) {
                return Tasks.forResult(null);
            }
            return schedule.getStudents().onSuccessTask(students -> {
                if (students == null || students.isEmpty()) {
                    return Tasks.forResult(null);
                }
                List<Task<?>> studentTasks = new ArrayList<>();
                for (Student student : students) {
                    if (student == null || !Tool.boolOf(student.getID())) {
                        continue;
                    }
                    StudentRepository sr = new StudentRepository(student.getID());
                    studentTasks.add(sr.removeStringFromArrayAsync("preScheduledMeetings", meetingId));
                    studentTasks.add(sr.removeStringFromArrayAsync("meetingHistory", meetingId));
                }
                return studentTasks.isEmpty() ? Tasks.forResult(null) : Tasks.whenAll(studentTasks);
            });
        });

        Task<Void> teacherCleanupTask = scheduleRepository.loadAsNormal().onSuccessTask(schedule -> {
            if (schedule == null) {
                return Tasks.forResult(null);
            }
            return schedule.getScheduleOfCourse().onSuccessTask(course -> {
                if (course == null || !Tool.boolOf(course.getTeacherId())) {
                    return Tasks.forResult(null);
                }
                TeacherRepository tr = new TeacherRepository(course.getTeacherId());
                Task<Void> removeScheduled = tr.removeStringFromArrayAsync("scheduledMeetings", meetingId);
                Task<Void> removeCompleted = tr.removeStringFromArrayAsync("completedMeetings", meetingId);
                return Tasks.whenAll(removeScheduled, removeCompleted);
            });
        });

        return Tasks.whenAll(studentsCleanupTask, teacherCleanupTask);
    }

    private void cleanupDuplicateMeetings() {
        DatabaseReference meetingsRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.MEETING.getPath());

        meetingsRef.get().addOnSuccessListener(snapshot -> {
            Map<String, List<DataSnapshot>> byCanonicalKey = new HashMap<>();

            for (DataSnapshot meetingSnapshot : snapshot.getChildren()) {
                MeetingFirebase meetingFb = meetingSnapshot.getValue(MeetingFirebase.class);
                if (meetingFb == null) {
                    continue;
                }
                String scheduleId = meetingFb.getOfSchedule();
                String date = meetingFb.getDateOfMeeting();
                if (!Tool.boolOf(scheduleId) || !Tool.boolOf(date)) {
                    continue;
                }
                String canonicalKey = scheduleId + "_" + date;
                byCanonicalKey.computeIfAbsent(canonicalKey, k -> new ArrayList<>()).add(meetingSnapshot);
            }

            for (Map.Entry<String, List<DataSnapshot>> entry : byCanonicalKey.entrySet()) {
                String canonicalKey = entry.getKey();
                List<DataSnapshot> variants = entry.getValue();
                if (variants.isEmpty()) {
                    continue;
                }

                Set<String> legacyIdsToRemove = new HashSet<>();
                DataSnapshot canonicalSnapshot = null;

                for (DataSnapshot variant : variants) {
                    MeetingFirebase fb = variant.getValue(MeetingFirebase.class);
                    if (fb == null) {
                        continue;
                    }
                    String dbKey = variant.getKey();
                    String payloadId = fb.getMeetingID();
                    if (Tool.boolOf(payloadId) && !canonicalKey.equals(payloadId)) {
                        legacyIdsToRemove.add(payloadId);
                    }
                    if (canonicalKey.equals(dbKey)) {
                        canonicalSnapshot = variant;
                    } else {
                        legacyIdsToRemove.add(dbKey);
                    }
                }

                if (canonicalSnapshot == null) {
                    continue;
                }

                for (DataSnapshot variant : variants) {
                    if (!canonicalKey.equals(variant.getKey())) {
                        variant.getRef().removeValue();
                    }
                }

                MeetingRepository canonicalRepo = new MeetingRepository(canonicalKey);
                canonicalRepo.loadAsNormal().addOnSuccessListener(meeting -> {
                    if (meeting == null) {
                        return;
                    }
                    normalizeMeetingReferencesFromLegacy(meeting, canonicalKey, legacyIdsToRemove);
                });
            }
        });
    }

    private void runOneTimePrescheduledRebuildIfNeeded() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(PREF_ONE_TIME_PRESCHEDULED_REBUILD_V1, false)) {
            return;
        }
        rebuildStudentsPrescheduledFromMeetings()
                .addOnSuccessListener(unused -> {
                    prefs.edit().putBoolean(PREF_ONE_TIME_PRESCHEDULED_REBUILD_V1, true).apply();
                    Log.d(TAG, "One-time preScheduledMeetings rebuild completed");
                })
                .addOnFailureListener(e -> Log.e(TAG, "One-time preScheduledMeetings rebuild failed", e));
    }

    private Task<Void> rebuildStudentsPrescheduledFromMeetings() {
        Task<DataSnapshot> studentsTask = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.STUDENT.getPath())
                .get();
        Task<DataSnapshot> meetingsTask = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.MEETING.getPath())
                .get();
        Task<DataSnapshot> schedulesTask = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.SCHEDULE.getPath())
                .get();

        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        Tasks.whenAllSuccess(studentsTask, meetingsTask, schedulesTask)
                .addOnSuccessListener(results -> {
                    DataSnapshot studentsSnapshot = studentsTask.getResult();
                    DataSnapshot meetingsSnapshot = meetingsTask.getResult();
                    DataSnapshot schedulesSnapshot = schedulesTask.getResult();
                    if (studentsSnapshot == null || meetingsSnapshot == null || schedulesSnapshot == null) {
                        tcs.setResult(null);
                        return;
                    }

                    HashSet<String> allStudentIds = new HashSet<>();
                    Map<String, LinkedHashSet<String>> currentByStudent = new HashMap<>();
                    for (DataSnapshot studentSnap : studentsSnapshot.getChildren()) {
                        String studentId = studentSnap.getKey();
                        if (!Tool.boolOf(studentId)) {
                            continue;
                        }
                        allStudentIds.add(studentId);
                        currentByStudent.put(studentId, extractStringSet(studentSnap.child("preScheduledMeetings")));
                    }

                    Map<String, LinkedHashSet<String>> scheduleStudents = new HashMap<>();
                    for (DataSnapshot scheduleSnap : schedulesSnapshot.getChildren()) {
                        String scheduleId = scheduleSnap.getKey();
                        if (!Tool.boolOf(scheduleId)) {
                            continue;
                        }
                        scheduleStudents.put(scheduleId, extractStringSet(scheduleSnap.child("students")));
                    }

                    HashSet<String> existingMeetingKeys = new HashSet<>();
                    for (DataSnapshot meetingSnap : meetingsSnapshot.getChildren()) {
                        if (Tool.boolOf(meetingSnap.getKey())) {
                            existingMeetingKeys.add(meetingSnap.getKey());
                        }
                    }

                    Map<String, LinkedHashSet<String>> expectedUpcomingByStudent = new HashMap<>();
                    Map<String, Boolean> isUpcomingByMeetingId = new HashMap<>();
                    Set<String> processedCanonicalIds = new HashSet<>();

                    for (DataSnapshot meetingSnap : meetingsSnapshot.getChildren()) {
                        String dbMeetingId = meetingSnap.getKey();
                        MeetingFirebase meetingFb = meetingSnap.getValue(MeetingFirebase.class);
                        if (!Tool.boolOf(dbMeetingId) || meetingFb == null) {
                            continue;
                        }

                        String canonicalMeetingId = resolveCanonicalMeetingId(dbMeetingId, meetingFb, existingMeetingKeys);
                        if (!Tool.boolOf(canonicalMeetingId) || !processedCanonicalIds.add(canonicalMeetingId)) {
                            continue;
                        }

                        boolean upcoming = isUpcomingMeetingFirebase(meetingFb);
                        isUpcomingByMeetingId.put(canonicalMeetingId, upcoming);

                        if (!upcoming) {
                            continue;
                        }

                        LinkedHashSet<String> targetStudentIds = new LinkedHashSet<>();
                        if (Tool.boolOf(meetingFb.getOfSchedule())) {
                            LinkedHashSet<String> scheduleMemberIds = scheduleStudents.get(meetingFb.getOfSchedule());
                            if (scheduleMemberIds != null) {
                                for (String id : scheduleMemberIds) {
                                    if (Tool.boolOf(id) && allStudentIds.contains(id)) {
                                        targetStudentIds.add(id);
                                    }
                                }
                            }
                        }

                        if (meetingFb.getUsersRelated() != null) {
                            for (String relatedId : meetingFb.getUsersRelated()) {
                                if (Tool.boolOf(relatedId) && allStudentIds.contains(relatedId)) {
                                    targetStudentIds.add(relatedId);
                                }
                            }
                        }

                        for (String studentId : targetStudentIds) {
                            expectedUpcomingByStudent
                                    .computeIfAbsent(studentId, key -> new LinkedHashSet<>())
                                    .add(canonicalMeetingId);
                        }
                    }

                    List<Task<?>> writeTasks = new ArrayList<>();
                    int[] updatedStudents = {0};
                    for (String studentId : allStudentIds) {
                        LinkedHashSet<String> current = currentByStudent.getOrDefault(studentId, new LinkedHashSet<>());
                        LinkedHashSet<String> finalPrescheduled = new LinkedHashSet<>();

                        LinkedHashSet<String> expected = expectedUpcomingByStudent.getOrDefault(studentId, new LinkedHashSet<>());
                        finalPrescheduled.addAll(expected);

                        for (String existingId : current) {
                            if (!Tool.boolOf(existingId) || finalPrescheduled.contains(existingId)) {
                                continue;
                            }
                            if (!existingMeetingKeys.contains(existingId)) {
                                continue;
                            }
                            boolean isUpcoming = Boolean.TRUE.equals(isUpcomingByMeetingId.get(existingId));
                            if (isUpcoming) {
                                finalPrescheduled.add(existingId);
                            }
                        }

                        if (!current.equals(finalPrescheduled)) {
                            updatedStudents[0]++;
                            writeTasks.add(FirebaseDatabase.getInstance()
                                    .getReference(FirebaseNode.STUDENT.getPath())
                                    .child(studentId)
                                    .child("preScheduledMeetings")
                                    .setValue(new ArrayList<>(finalPrescheduled)));
                        }
                    }

                    if (writeTasks.isEmpty()) {
                        Log.d(TAG, "PreScheduled rebuild: no student updates needed (" + allStudentIds.size() + " students scanned)");
                        tcs.setResult(null);
                        return;
                    }
                    Tasks.whenAll(writeTasks)
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "PreScheduled rebuild: updated " + updatedStudents[0] + " / " + allStudentIds.size() + " students");
                                tcs.setResult(null);
                            })
                            .addOnFailureListener(tcs::setException);
                })
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }

    private LinkedHashSet<String> extractStringSet(DataSnapshot snapshot) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (snapshot == null || !snapshot.exists()) {
            return values;
        }
        for (DataSnapshot child : snapshot.getChildren()) {
            Object rawValue = child.getValue();
            if (rawValue instanceof String && Tool.boolOf((String) rawValue)) {
                values.add((String) rawValue);
                continue;
            }
            String key = child.getKey();
            if (Tool.boolOf(key) && !isNumericArrayKey(key) && isTruthyMembershipFlag(rawValue)) {
                values.add(key);
            }
        }
        return values;
    }

    private boolean isNumericArrayKey(String key) {
        if (!Tool.boolOf(key)) {
            return false;
        }
        for (int i = 0; i < key.length(); i++) {
            if (!Character.isDigit(key.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isTruthyMembershipFlag(Object rawValue) {
        if (rawValue == null) {
            return true;
        }
        if (rawValue instanceof Boolean) {
            return (Boolean) rawValue;
        }
        if (rawValue instanceof Number) {
            return ((Number) rawValue).intValue() != 0;
        }
        String value = rawValue.toString();
        return "1".equals(value) || "true".equalsIgnoreCase(value);
    }

    private String resolveCanonicalMeetingId(String dbMeetingId, MeetingFirebase meetingFb, Set<String> existingMeetingKeys) {
        if (meetingFb != null
                && Tool.boolOf(meetingFb.getOfSchedule())
                && Tool.boolOf(meetingFb.getDateOfMeeting())) {
            String computed = meetingFb.getOfSchedule() + "_" + meetingFb.getDateOfMeeting();
            if (existingMeetingKeys.contains(computed)) {
                return computed;
            }
        }
        if (Tool.boolOf(dbMeetingId) && existingMeetingKeys.contains(dbMeetingId)) {
            return dbMeetingId;
        }
        if (meetingFb != null && Tool.boolOf(meetingFb.getMeetingID()) && existingMeetingKeys.contains(meetingFb.getMeetingID())) {
            return meetingFb.getMeetingID();
        }
        return dbMeetingId;
    }

    private boolean isUpcomingMeetingFirebase(MeetingFirebase meetingFb) {
        if (meetingFb == null || meetingFb.isCompleted() || !Tool.boolOf(meetingFb.getDateOfMeeting())) {
            return false;
        }
        LocalDate meetingDate;
        try {
            meetingDate = LocalDate.parse(meetingFb.getDateOfMeeting());
        } catch (Exception e) {
            return false;
        }

        LocalDate today = LocalDate.now();
        if (meetingDate.isAfter(today)) {
            return true;
        }
        if (meetingDate.isBefore(today)) {
            return false;
        }

        if (!Tool.boolOf(meetingFb.getEndTimeChange())) {
            return true;
        }
        try {
            return LocalTime.parse(meetingFb.getEndTimeChange()).isAfter(LocalTime.now());
        } catch (Exception e) {
            return true;
        }
    }

    private void normalizeMeetingReferencesFromLegacy(Meeting meeting, String canonicalMeetingId, Set<String> legacyIdsToRemove) {
        if (meeting == null || !Tool.boolOf(canonicalMeetingId) || !Tool.boolOf(meeting.getOfSchedule())) {
            Log.w(TAG, "Skipping legacy normalization due to missing meeting/schedule data for " + canonicalMeetingId);
            return;
        }

        MeetingRepository meetingRepo = new MeetingRepository(canonicalMeetingId);
        try {
            meetingRepo.save(meeting);
        } catch (Exception e) {
            Log.e(TAG, "Failed to normalize canonical meeting " + canonicalMeetingId, e);
        }

        ScheduleRepository scheduleRepository = new ScheduleRepository(meeting.getOfSchedule());
        scheduleRepository.loadAsNormal().addOnSuccessListener(schedule -> {
            if (schedule == null) {
                Log.w(TAG, "Schedule not found for legacy normalization: " + meeting.getOfSchedule());
                return;
            }

            schedule.getStudents().addOnSuccessListener(students -> {
                if (students == null || students.isEmpty()) {
                    return;
                }
                for (Student student : students) {
                    if (student == null || !Tool.boolOf(student.getID())) {
                        continue;
                    }
                    StudentRepository studentRepository = new StudentRepository(student.getID());
                    studentRepository.addStringToArrayAsync("preScheduledMeetings", canonicalMeetingId);
                    for (String legacyId : legacyIdsToRemove) {
                        studentRepository.removeStringFromArrayAsync("preScheduledMeetings", legacyId);
                    }
                }
            });

            schedule.getScheduleOfCourse().addOnSuccessListener(course -> {
                if (course == null || !Tool.boolOf(course.getTeacherId())) {
                    Log.w(TAG, "Skipping teacher legacy normalization for " + canonicalMeetingId + ": missing course/teacher");
                    return;
                }
                TeacherRepository teacherRepository = new TeacherRepository(course.getTeacherId());
                teacherRepository.addStringToArrayAsync("scheduledMeetings", canonicalMeetingId);
                for (String legacyId : legacyIdsToRemove) {
                    teacherRepository.removeStringFromArrayAsync("scheduledMeetings", legacyId);
                }
            });
        }).addOnFailureListener(e ->
                Log.e(TAG, "Failed loading schedule for legacy normalization: " + meeting.getOfSchedule(), e)
        );
    }

    private String generateMeetingId(String scheduleId, LocalDate date) {
        return scheduleId + "_" + date.toString();
    }
}
