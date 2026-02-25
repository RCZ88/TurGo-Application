package com.example.turgo;

import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.android.gms.tasks.Tasks;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


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
    // -------------------------------------------------------------------------
    // Partial / selective loaders
    // -------------------------------------------------------------------------

    public com.google.android.gms.tasks.Task<StudentECData> loadExploreCourseData() {
        StudentECData data = new StudentECData();
        // Load meetings and courses in parallel for best performance
        com.google.android.gms.tasks.Task<ArrayList<Meeting>> meetingTask = getMeetingExploreCourse();
        com.google.android.gms.tasks.Task<ArrayList<Course>> courseTask = getCoursesExploreCourse();

        return Tasks.whenAll(meetingTask, courseTask).continueWith(task -> {
            data.prescheduledMeetings = meetingTask.getResult();
            data.exploreCourses = courseTask.getResult();
            return data;
        });
    }
    private com.google.android.gms.tasks.Task<ArrayList<Course>> getCoursesExploreCourse() {
        return loadField("courseInterested").continueWithTask(task -> {
            DataSnapshot snapshot = task.getResult();
            List<com.google.android.gms.tasks.Task<Map<String, Object>>> detailTasks = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                CourseRepository repo = RequireUpdate.getRepositoryInstance(Course.class, child.getKey());
                detailTasks.add(repo.loadFields("courseName", "logoCloudinary", "schedules", "teacher", "courseID"));
            }
            return Tasks.whenAllSuccess(detailTasks);
        }).continueWith(task -> {
            ArrayList<Course> result = new ArrayList<>();
            List<Object> list = task.getResult();
            for (Object item : list) {
                Map<String, Object> m = (Map<String, Object>) item;
                Course c = new Course();
                c.setCourseID(str(m.get("courseID")));
                c.setCourseName(str(m.get("courseName")));
                c.setLogo(str(m.get("logoCloudinary")));
                c.setTeacher(str(m.get("teacher")));
                ArrayList<String> schedules = extractStringList(m.get("schedules"));
                if (!schedules.isEmpty()) {
                    c.setSchedules(Tool.streamToArray(schedules.stream().map(Schedule::new)));
                }
                result.add(c);
            }
            return result;
        });
    }
    private com.google.android.gms.tasks.Task<ArrayList<Meeting>> getMeetingExploreCourse() {
        return loadField("preScheduledMeetings").continueWithTask(task -> {
            DataSnapshot snapshot = task.getResult();
            List<com.google.android.gms.tasks.Task<Map<String, Object>>> detailTasks = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                MeetingRepository repo = RequireUpdate.getRepositoryInstance(Meeting.class, child.getKey());
                detailTasks.add(repo.loadFields("ofSchedule", "dateOfMeeting", "startTimeChange", "meetingID"));
            }
            return Tasks.whenAllSuccess(detailTasks);
        }).continueWith(task -> {
            ArrayList<Meeting> result = new ArrayList<>();
            List<Object> list = task.getResult();
            for (Object item : list) {
                Map<String, Object> m = (Map<String, Object>) item;
                Meeting meeting = new Meeting(str(m.get("meetingID")));
                meeting.setOfSchedule(str(m.get("ofSchedule")));
                String dateStr = str(m.get("dateOfMeeting"));
                if (Tool.boolOf(dateStr)) meeting.setDateOfMeeting(LocalDate.parse(dateStr));
                String timeStr = str(m.get("startTimeChange"));
                if (Tool.boolOf(timeStr)) meeting.setStartTimeChange(LocalTime.parse(timeStr));
                result.add(meeting);
            }
            return result;
        });
    }
    /**
     * Load only the fields needed by the Student Dashboard screen.
     * <p>
     * Step 1: reads the 5 scalar/list fields from the student node in one parallel batch.
     * Step 2: for each uncompleted task ID, calls {@link TaskRepository#loadPartialForDashboard()}
     *         which loads only the 5 fields the task card renders (no constructClass overhead).
     * </p>
     */
    public com.google.android.gms.tasks.Task<StudentDashboardData> loadDashboardData() {
        TaskCompletionSource<StudentDashboardData> tcs =
                new TaskCompletionSource<>();

        loadFields(
                "percentageCompleted",
                "scheduleCompletedThisWeek",
                "nextMeeting",
                "uncompletedTask",
                "completionWeekKey",
                "allSchedules"
        ).addOnSuccessListener(m -> {
            StudentDashboardData d = new StudentDashboardData();

            Object pct = m.get("percentageCompleted");
            if (pct instanceof Number) {
                d.percentageCompleted = ((Number) pct).doubleValue();
            }
            d.scheduleCompletedThisWeekIds = extractStringList(m.get("scheduleCompletedThisWeek"));
            Object nm = m.get("nextMeeting");
            d.nextMeetingId = nm instanceof String ? (String) nm : null;
            Object wk = m.get("completionWeekKey");
            d.completionWeekKey = wk instanceof String ? (String) wk : null;
            java.util.ArrayList<String> allSchedIds = extractStringList(m.get("allSchedules"));

            // Chain 1: Load Upcoming Meeting details (if any)
            com.google.android.gms.tasks.Task<Void> meetingTask;
            if (com.example.turgo.Tool.boolOf(d.nextMeetingId)) {
                meetingTask = fetchUpcomingMeetingDetails(d, allSchedIds);
            } else {
                meetingTask = com.google.android.gms.tasks.Tasks.forResult(null);
            }

            // Chain 2: Load Tasks (independently of meeting)
            meetingTask.addOnCompleteListener(doneMeeting -> {
                List<String> taskIds = extractStringList(m.get("uncompletedTask"));
                if (taskIds.isEmpty()) {
                    tcs.setResult(d);
                    return;
                }

                List<com.google.android.gms.tasks.Task<Task>> partialLoads = new ArrayList<>();
                for (String taskId : taskIds) {
                    if (Tool.boolOf(taskId)) {
                        partialLoads.add(new TaskRepository(taskId).loadPartialForDashboard());
                    }
                }

                com.google.android.gms.tasks.Tasks.whenAllComplete(partialLoads)
                        .addOnCompleteListener(doneTasks -> {
                            for (com.google.android.gms.tasks.Task<Task> t : partialLoads) {
                                if (t.isSuccessful() && t.getResult() != null) {
                                    d.uncompletedTasks.add(t.getResult());
                                }
                            }
                            tcs.setResult(d);
                        });
            });

        }).addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    private com.google.android.gms.tasks.Task<Void> fetchUpcomingMeetingDetails(StudentDashboardData d, java.util.List<String> allSchedIds) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        // 1. Fetch Meeting basic fields
        new MeetingRepository(d.nextMeetingId).loadFields("ofSchedule", "dateOfMeeting", "startTimeChange")
            .addOnSuccessListener(mm -> {
                String scheduleId = (String) mm.get("ofSchedule");
                d.nextMeetingDate = (String) mm.get("dateOfMeeting");
                String startChange = (String) mm.get("startTimeChange");

                if (!com.example.turgo.Tool.boolOf(scheduleId)) {
                    tcs.setResult(null);
                    return;
                }

                // 2. Fetch Schedule fields
                new ScheduleRepository(scheduleId).loadFields("ofCourse", "meetingStart", "meetingEnd", "duration", "room")
                    .addOnSuccessListener(ss -> {
                        d.nextCourseId = (String) ss.get("ofCourse");
                        Object dur = ss.get("duration");
                        d.nextCourseDuration = dur instanceof Number ? ((Number) dur).intValue() : 0;
                        String roomId = (String) ss.get("room");
                        String sTime = (String) ss.get("meetingStart");
                        String eTime = (String) ss.get("meetingEnd");
                        if (com.example.turgo.Tool.boolOf(startChange)) sTime = startChange;
                        d.nextMeetingTime = sTime + " - " + eTime;

                        if (!com.example.turgo.Tool.boolOf(d.nextCourseId)) {
                            tcs.setResult(null);
                            return;
                        }

                        // 3. Parallel fetch Course, Room, and Days
                        java.util.List<com.google.android.gms.tasks.Task<?>> parallel = new java.util.ArrayList<>();

                        // Course & Teacher
                        parallel.add(new CourseRepository(d.nextCourseId).loadFields("courseName", "logoCloudinary", "teacher")
                            .continueWithTask(ct -> {
                                if (ct.isSuccessful()) {
                                    Map<String, Object> cc = ct.getResult();
                                    d.nextCourseName = (String) cc.get("courseName");
                                    d.nextCourseLogo = (String) cc.get("logoCloudinary");
                                    String teacherId = (String) cc.get("teacher");
                                    if (com.example.turgo.Tool.boolOf(teacherId)) {
                                        return new TeacherRepository(teacherId).loadFields("fullName")
                                            .addOnSuccessListener(tt -> d.nextTeacherName = (String) tt.get("fullName"));
                                    }
                                }
                                return com.google.android.gms.tasks.Tasks.forResult(null);
                            }));

                        // Room
                        if (com.example.turgo.Tool.boolOf(roomId)) {
                            parallel.add(new RoomRepository(roomId).loadField("roomTag")
                                .addOnSuccessListener(snap -> d.nextRoomTag = snap.getValue(String.class)));
                        }

                        // Days (load only ofCourse and day field for all student's schedules to identify course days)
                        if (allSchedIds != null && !allSchedIds.isEmpty()) {
                            java.util.List<com.google.android.gms.tasks.Task<Map<String, Object>>> dayTasks = new java.util.ArrayList<>();
                            for (String sid : allSchedIds) {
                                if (com.example.turgo.Tool.boolOf(sid)) {
                                    dayTasks.add(new ScheduleRepository(sid).loadFields("ofCourse", "day"));
                                }
                            }
                            parallel.add(com.google.android.gms.tasks.Tasks.whenAllComplete(dayTasks).addOnSuccessListener(res -> {
                                java.util.Set<String> uniqueDays = new java.util.LinkedHashSet<>();
                                for (com.google.android.gms.tasks.Task<Map<String, Object>> dt : dayTasks) {
                                    if (dt.isSuccessful()) {
                                        Map<String, Object> r = dt.getResult();
                                        if (d.nextCourseId.equals(r.get("ofCourse"))) {
                                            uniqueDays.add(String.valueOf(r.get("day")));
                                        }
                                    }
                                }
                                d.nextCourseDays = String.join(", ", uniqueDays);
                            }));
                        }

                        com.google.android.gms.tasks.Tasks.whenAllComplete(parallel)
                            .addOnCompleteListener(done -> tcs.setResult(null));
                    }).addOnFailureListener(tcs::setException);
            }).addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }

    /**b
     * Load only the profile fields needed by the profile display screen.
     */
    public com.google.android.gms.tasks.Task<UserProfileData> loadProfileData() {
        TaskCompletionSource<UserProfileData> tcs =
                new TaskCompletionSource<>();
        loadFields(
                "uid", "userType", "fullName", "nickname",
                "email", "phoneNumber", "birthDate",
                "gender", "language", "theme", "pfpCloudinary"
        ).addOnSuccessListener(m -> {
            UserProfileData p = new UserProfileData();
            p.uid           = str(m.get("uid"));
            p.userType      = str(m.get("userType"));
            p.fullName      = str(m.get("fullName"));
            p.nickname      = str(m.get("nickname"));
            p.email         = str(m.get("email"));
            p.phoneNumber   = str(m.get("phoneNumber"));
            p.birthDate     = str(m.get("birthDate"));
            p.gender        = str(m.get("gender"));
            p.language      = str(m.get("language"));
            p.theme         = str(m.get("theme"));
            p.pfpCloudinary = str(m.get("pfpCloudinary"));
            tcs.setResult(p);
        }).addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    private static String str(Object o) {
        return o instanceof String ? (String) o : null;
    }


    /**
     * Load only the fields needed by the StudentScreen activity.
     * Consolidates legacy notification fields and avoids constructClass overhead.
     */
    public com.google.android.gms.tasks.Task<StudentScreenData> loadScreenData() {
        TaskCompletionSource<StudentScreenData> tcs =
                new TaskCompletionSource<>();
        loadFields(
                "uid", "userType", "fullName", "pfpCloudinary",
                "inbox", "notifications", "notificationIDs", "notitficationIDs",
                "completionWeekKey", "percentageCompleted", "allSchedules"
        ).addOnSuccessListener(m -> {
            StudentScreenData d = new StudentScreenData();
            d.uid           = str(m.get("uid"));
            if (!com.example.turgo.Tool.boolOf(d.uid)) {
                d.uid = getDbReference().getKey();
            }
            d.userType      = str(m.get("userType"));
            d.fullName      = str(m.get("fullName"));
            d.pfpCloudinary = str(m.get("pfpCloudinary"));
            d.inboxIds      = extractStringList(m.get("inbox"));
            d.scheduleCompletedThisWeekIds = extractStringList(m.get("scheduleCompletedThisWeek"));
            
            // Consolidate notifications from all possible legacy paths
            java.util.LinkedHashSet<String> uniqueNotifs = new java.util.LinkedHashSet<>();
            uniqueNotifs.addAll(extractStringList(m.get("notifications")));
            uniqueNotifs.addAll(extractStringList(m.get("notificationIDs")));
            uniqueNotifs.addAll(extractStringList(m.get("notitficationIDs")));
            d.notificationIds = new java.util.ArrayList<>(uniqueNotifs);
            
            d.completionWeekKey = str(m.get("completionWeekKey"));
            Object pct = m.get("percentageCompleted");
            if (pct instanceof Number) {
                d.percentageCompleted = ((Number) pct).doubleValue();
            }
            
            java.util.ArrayList<String> allScheds = extractStringList(m.get("allSchedules"));
            d.allSchedulesCount = allScheds.size();
            
            tcs.setResult(d);
        }).addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }
}