package com.example.turgo;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
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

    // -------------------------------------------------------------------------
    // Partial / selective loaders
    // -------------------------------------------------------------------------

    /**
     * Load only the profile fields needed by the profile display screen.
     */
    public com.google.android.gms.tasks.Task<UserProfileData> loadProfileData() {
        com.google.android.gms.tasks.TaskCompletionSource<UserProfileData> tcs =
                new com.google.android.gms.tasks.TaskCompletionSource<>();
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

    public com.google.android.gms.tasks.Task<TeacherDashboardData> loadDashboardData() {
        com.google.android.gms.tasks.TaskCompletionSource<TeacherDashboardData> tcs =
                new com.google.android.gms.tasks.TaskCompletionSource<>();
        TeacherDashboardData d = new TeacherDashboardData();

        loadFields("coursesTeach", "latestSubmissions", "nextSchedule").addOnSuccessListener(m -> {
            ArrayList<String> courseIds = extractStringList(m.get("coursesTeach"));
            ArrayList<String> submissionIds = extractStringList(m.get("latestSubmissions"));
            ArrayList<String> scheduledMIds = extractStringList(m.get("nextSchedule"));

            java.util.List<com.google.android.gms.tasks.Task<?>> tasks = new ArrayList<>();

            // 1. Courses and their meetings (Preserve Order)
            if (!courseIds.isEmpty()) {
                java.util.List<com.google.android.gms.tasks.Task<Course>> liteTasks = new ArrayList<>();
                java.util.List<com.google.android.gms.tasks.Task<Meeting>> meetingTasks = new ArrayList<>();

                for (String cid : courseIds) {
                    liteTasks.add(new CourseRepository(cid).loadLite());
                    meetingTasks.add(new CourseRepository(cid).loadAsNormal().continueWithTask(ct -> {
                         if (ct.isSuccessful() && ct.getResult() != null) return ct.getResult().getNextMeetingOfNextSchedule();
                         return com.google.android.gms.tasks.Tasks.forResult(null);
                    }));
                }

                tasks.add(com.google.android.gms.tasks.Tasks.whenAllSuccess(liteTasks).addOnSuccessListener(res -> {
                    d.coursesTeach.clear();
                    for (Object c : res) {
                        Course course = (Course) c;
                        d.coursesTeach.add(course);
                        d.studentCountOfCourses.add(course.getStudentIds() != null ? course.getStudentIds().size() : 0);
                    }
                }));

                tasks.add(com.google.android.gms.tasks.Tasks.whenAllComplete(meetingTasks).addOnSuccessListener(res -> {
                    d.nextMeetingOfCourses.clear();
                    for (com.google.android.gms.tasks.Task<Meeting> mt : meetingTasks) {
                        d.nextMeetingOfCourses.add(mt.isSuccessful() ? mt.getResult() : null);
                    }
                }));
            }

            // 2. Submissions
            if (!submissionIds.isEmpty()) {
                java.util.List<com.google.android.gms.tasks.Task<SubmissionDisplay>> subTasks = new ArrayList<>();
                for (String sid : submissionIds) subTasks.add(new SubmissionDisplayRepository(sid).loadAsNormal());
                tasks.add(com.google.android.gms.tasks.Tasks.whenAllComplete(subTasks).addOnSuccessListener(res -> {
                    for (com.google.android.gms.tasks.Task<SubmissionDisplay> st : subTasks) {
                        if (st.isSuccessful()) d.latestSubmissions.add(st.getResult());
                    }
                }));
            }

            // 3. Next Schedule Display
            if (!scheduledMIds.isEmpty()) {
                tasks.add(getUpcomingMeetingDetails(scheduledMIds.get(0), d));
            }

            com.google.android.gms.tasks.Tasks.whenAllComplete(tasks).addOnCompleteListener(done -> tcs.setResult(d));

        }).addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    private com.google.android.gms.tasks.Task<Void> getUpcomingMeetingDetails(String mid, TeacherDashboardData d) {
        return new MeetingRepository(mid).loadFields("ofSchedule", "startTimeChange").continueWithTask(mt -> {
            if (mt.isSuccessful()) {
                String sid = (String) mt.getResult().get("ofSchedule");
                String startChange = (String) mt.getResult().get("startTimeChange");
                if (com.example.turgo.Tool.boolOf(sid)) {
                    return new ScheduleRepository(sid).loadFields("ofCourse", "room", "meetingStart", "meetingEnd").continueWithTask(sm -> {
                        java.util.Map<String, Object> smm = sm.getResult();
                        String cid = (String) smm.get("ofCourse");
                        String rid = (String) smm.get("room");
                        String sTime = (String) smm.get("meetingStart");
                        String eTime = (String) smm.get("meetingEnd");
                        if (com.example.turgo.Tool.boolOf(startChange)) sTime = startChange;
                        d.nextScheduleTime = sTime + " - " + eTime;

                        java.util.List<com.google.android.gms.tasks.Task<?>> sub = new ArrayList<>();
                        if (com.example.turgo.Tool.boolOf(cid)) {
                            sub.add(new CourseRepository(cid).loadField("courseName").addOnSuccessListener(cs -> d.nextCourseName = (String) cs.getValue()));
                        }
                        if (com.example.turgo.Tool.boolOf(rid)) {
                            sub.add(new RoomRepository(rid).loadField("roomTag").addOnSuccessListener(rs -> d.nextRoomTag = (String) rs.getValue()));
                        }
                        return com.google.android.gms.tasks.Tasks.whenAllComplete(sub).continueWith(v -> (Void)null);
                    });
                }
            }
            return com.google.android.gms.tasks.Tasks.forResult(null);
        });
    }

    public ArrayList<String> extractStringList(Object o) {
        if (o instanceof java.util.ArrayList) {
            return (ArrayList<String>) o;
        } else if (o instanceof java.util.Map) {
            return new ArrayList<>(((java.util.Map<String, String>) o).values());
        }
        return new ArrayList<>();
    }

    public com.google.android.gms.tasks.Task<Teacher> loadLite() {
        return loadFields("fullName", "pfpCloudinary", "profileImageCloudinary")
            .continueWith(task -> {
                java.util.Map<String, Object> m = task.getResult();
                Teacher t = new Teacher();
                t.setUid(getDbReference().getKey());
                t.setFullName((String) m.get("fullName"));
                String pfp = (String) m.get("pfpCloudinary");
                if (!com.example.turgo.Tool.boolOf(pfp)) pfp = (String) m.get("profileImageCloudinary");
                t.setPfpCloudinary(pfp);
                t.setProfileImageCloudinary(pfp);
                return t;
            });
    }

    private static String str(Object o) {
        return o instanceof String ? (String) o : null;
    }

    /**
     * Load only the fields needed by the TeacherScreen activity.
     */
    public com.google.android.gms.tasks.Task<TeacherScreenData> loadScreenData() {
        com.google.android.gms.tasks.TaskCompletionSource<TeacherScreenData> tcs =
                new com.google.android.gms.tasks.TaskCompletionSource<>();
        loadFields(
                "uid", "userType", "fullName", "pfpCloudinary",
                "inbox", "notifications", "notificationIDs", "notitficationIDs"
        ).addOnSuccessListener(m -> {
            TeacherScreenData d = new TeacherScreenData();
            d.uid           = str(m.get("uid"));
            if (!com.example.turgo.Tool.boolOf(d.uid)) {
                d.uid = getDbReference().getKey();
            }
            d.userType      = str(m.get("userType"));
            d.fullName      = str(m.get("fullName"));
            d.pfpCloudinary = str(m.get("pfpCloudinary"));
            d.inboxIds      = extractStringList(m.get("inbox"));
            
            java.util.LinkedHashSet<String> uniqueNotifs = new java.util.LinkedHashSet<>();
            uniqueNotifs.addAll(extractStringList(m.get("notifications")));
            uniqueNotifs.addAll(extractStringList(m.get("notificationIDs")));
            uniqueNotifs.addAll(extractStringList(m.get("notitficationIDs")));
            d.notificationIds = new ArrayList<>(uniqueNotifs);
            
            tcs.setResult(d);
        }).addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }
}