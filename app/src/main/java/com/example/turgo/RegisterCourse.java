package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class RegisterCourse extends AppCompatActivity {
    int amtOfRegisFrag = 4;
    Fragment[]registerFragments = new Fragment[amtOfRegisFrag];
    HashMap<TimeSlot, Integer>slotAmount;
    ProgressBar pb_regisProgress;
    ImageButton btn_collapse, btn_goBack;
    Fragment frag_UserInfo, frag_AvailDayTime, frag_PrivateDurationAmount, frag_SelectPaymentConfirm;

    View fc_container;
    Teacher courseTeacher;

    Button btn_next, btn_prev;
    TextView tv_title;
    LinearLayout ll_topNav;
    private int amountOfMeetingPerWeek;
    private Course course;
    private ScheduleQuality sq;
    private int duration;
    private int selectedPrice;
    private ArrayList<DayOfWeek>dowSelected;
    private ArrayList<TimeSlot>selectedTS;
    private ArrayList<DayTimeArrangement>dtaAvailable;
    private String school, educationGrade, reasonForJoining;
    int currentFragIndex;
    private Student student;
    private boolean paymentPreferences;
    private boolean nextJoin = false;
    private boolean topBarOpened = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        this.course = (Course) intent.getSerializableExtra(Course.SERIALIZE_KEY_CODE);
        assert this.course != null;
        Log.d("RegisterCourse", "Course Object:\n " + this.course.getCourseID());
        this.student = (Student) intent.getSerializableExtra("Student");
        fc_container = findViewById(R.id.fcv_RegisterFragment);
        pb_regisProgress = findViewById(R.id.pb_registerCoursePB);
        tv_title = findViewById(R.id.tv_arc_ViewTitle);
        btn_collapse = findViewById(R.id.btn_arc_CollapseTopBar);
        ll_topNav =  findViewById(R.id.ll_arc_topView);
        btn_goBack = findViewById(R.id.ib_RC_backNav);

        btn_goBack.setOnClickListener(v->goBackToCourseFullPage());



        frag_UserInfo = new RC_UserInformation();
        frag_PrivateDurationAmount = new RC_PrivateDurationAmount();
        frag_AvailDayTime = new RC_AvailableDayTime();
        frag_SelectPaymentConfirm = new RC_SelectPaymentConfirm();

        currentFragIndex = 0;
        registerFragments[0] = frag_UserInfo;
        registerFragments[1] = frag_PrivateDurationAmount;
        registerFragments[2] = frag_AvailDayTime;
        registerFragments[3] = frag_SelectPaymentConfirm;

        initializeProgressBar();

        btn_prev = findViewById(R.id.btn_previous_rc);
        btn_next = findViewById(R.id.btn_next_rc);


        btn_collapse.setOnClickListener(view ->{
            //excluding the button itself
            int visibility = topBarOpened? View.GONE : View.VISIBLE;
            int buttonResourceId = topBarOpened ? R.drawable.caret_down:R.drawable.caret;
            for(int i =0; i<ll_topNav.getChildCount()-1; i++){
                ll_topNav.getChildAt(i).setVisibility(visibility);
            }
            btn_collapse.setImageResource(buttonResourceId);
            topBarOpened = !topBarOpened;
        });

        Tool.loadFragment(this, fc_container.getId(), registerFragments[currentFragIndex]);
        btn_prev.setText("Return");

        btn_prev.setOnClickListener(view -> prevFragment());
        btn_next.setOnClickListener(view -> {

            if(nextJoin){
                try {
                    Log.d("Apply", "Applying to Course");
                    apply();
                } catch (InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }else{
                nextFragment();
            }
        });
    }

    public ArrayList<DayTimeArrangement> getDtaAvailable() {
        return dtaAvailable;
    }

    public void setDtaAvailable(ArrayList<DayTimeArrangement> dtaAvailable) {
        this.dtaAvailable = dtaAvailable;
    }

    public void apply() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        AtomicReference<ArrayList<Schedule>> schedules = new AtomicReference<>(new ArrayList<>());
        if(dowSelected == null){
            Log.e("ApplyCourse", "Day of Week Array (dowSelected) is null.");
            return;
        }
        if(!Tool.boolOf(selectedTS)){
            Log.d("ApplyCourse", "Selected TimeSlot is Null or Empty");
            return;
        }
        AlarmPermissionHelper.requestExactAlarmPermissionWithCallback(
                this,
                ()-> course.getTeacher().addOnSuccessListener(teacher->{
                   courseTeacher = teacher;
                    createSchedules(new ObjectCallBack<>() {
                        @Override
                        public void onObjectRetrieved(ArrayList<Schedule> object) {
                            schedules.set(object);
                            ensureStudentLinkedToSchedules(schedules.get());
                            if (course.isAutoAcceptStudent()) {
                                student.joinCourse(course, paymentPreferences,
                                                sq == ScheduleQuality.PRIVATE_ONLY, selectedPrice,
                                                schedules.get(), selectedTS, RegisterCourse.this)
                                        .onSuccessTask(unused -> generateMeetingsNow(schedules.get()))
                                        .addOnSuccessListener(unused -> {
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable(Course.SERIALIZE_KEY_CODE, course);
                                            bundle.putSerializable(Teacher.SERIALIZE_KEY_CODE, courseTeacher);
                                            Intent intent = new Intent(RegisterCourse.this, StudentScreen.class);
                                            intent.putExtra("showCourseJoined", true);
                                            intent.putExtra("bundleToCourseJoined", bundle);
                                            intent.putExtra("Student_RequireUpdate", student);
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("RegisterCourse", "joinCourse failed", e);
                                            Toast.makeText(RegisterCourse.this,
                                                    "Failed to join course. Please try again.",
                                                    Toast.LENGTH_LONG).show();
                                        });
                            } else {
                                MailApplyCourse acm = new MailApplyCourse(student, teacher,
                                        schedules.get(), course, reasonForJoining, school, educationGrade);
                                try {
                                    User.sendMail(acm, student, teacher);
                                } catch (InvocationTargetException |
                                         NoSuchMethodException |
                                         IllegalAccessException |
                                         InstantiationException e) {
                                    throw new RuntimeException(e);
                                }
                                Toast.makeText(RegisterCourse.this, "Apply Course Mail Request Sent!", Toast.LENGTH_SHORT).show();
                                Tool.loadFragment(RegisterCourse.this, R.id.nhf_ss_FragContainer, new Student_Dashboard());
                            }
                        }

                        @Override
                        public void onError(DatabaseError error) {

                        }
                    });
                }),
                ()-> Toast.makeText(this,
                        "Alarm permission is required to receive meeting notifications",
                        Toast.LENGTH_LONG).show()
        );

    }
    private Task<Void> generateMeetingsNow(ArrayList<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return Tasks.forResult(null);
        }
        int weeksAhead = 4;
        LocalDate today = LocalDate.now();
        List<Task<?>> tasks = new ArrayList<>();
        StudentRepository studentRepository = new StudentRepository(student.getID());

        for (Schedule schedule : schedules) {
            if (schedule == null || schedule.getDay() == null) {
                continue;
            }
            for (int week = 0; week < weeksAhead; week++) {
                LocalDate meetingDate = today.plusWeeks(week)
                        .with(TemporalAdjusters.nextOrSame(schedule.getDay()));
                String meetingId = schedule.getID() + "_" + meetingDate;
                MeetingRepository meetingRepo = new MeetingRepository(meetingId);

                Task<Void> task = meetingRepo.getDbReference().get().onSuccessTask(snapshot -> {
                    if (snapshot.exists()) {
                        return meetingRepo.loadAsNormal().onSuccessTask(existingMeeting -> {
                            if (existingMeeting != null) {
                                if (existingMeeting.getUsersRelated() == null) {
                                    existingMeeting.setUsersRelated(new ArrayList<>());
                                }
                                if (!existingMeeting.getUsersRelated().contains(student.getID())) {
                                    existingMeeting.getUsersRelated().add(student.getID());
                                }
                                String legacyMeetingId = existingMeeting.getRawMeetingID();
                                return meetingRepo.saveAsync(existingMeeting)
                                        .onSuccessTask(unused -> studentRepository.addStringToArrayAsync("preScheduledMeetings", meetingId))
                                        .onSuccessTask(unused -> {
                                            if (!meetingId.equals(legacyMeetingId)) {
                                                return studentRepository.removeStringFromArrayAsync("preScheduledMeetings", legacyMeetingId);
                                            }
                                            return Tasks.forResult(null);
                                        })
                                        .onSuccessTask(unused -> existingMeeting.getMeetingOfCourse().onSuccessTask(moc -> {
                                            TeacherRepository teacherRepository = new TeacherRepository(moc.getTeacherId());
                                            Task<Void> addTask = teacherRepository.addStringToArrayAsync("scheduledMeetings", meetingId);
                                            if (!meetingId.equals(legacyMeetingId)) {
                                                return addTask.onSuccessTask(v ->
                                                        teacherRepository.removeStringFromArrayAsync("scheduledMeetings", legacyMeetingId));
                                            }
                                            return addTask;
                                        }));
                            }
                            return studentRepository.addStringToArrayAsync("preScheduledMeetings", meetingId);
                        });
                    }
                    Meeting meeting = new Meeting(meetingId, schedule, meetingDate, student);
                    meeting.assignUser(student);
                    return meetingRepo.saveAsync(meeting)
                            .onSuccessTask(unused -> studentRepository.addStringToArrayAsync("preScheduledMeetings", meetingId))
                            .onSuccessTask(unused -> meeting.getMeetingOfCourse().onSuccessTask(moc -> {
                                TeacherRepository teacherRepository = new TeacherRepository(moc.getTeacherId());
                                return teacherRepository.addStringToArrayAsync("scheduledMeetings", meetingId);
                            }));
                });
                tasks.add(task);
            }
        }
        return tasks.isEmpty() ? Tasks.forResult(null) : Tasks.whenAll(tasks);
    }
    private void createSchedules(ObjectCallBack<ArrayList<Schedule>> callBack) {
        if (dowSelected == null || dowSelected.isEmpty()) {
            try {
                callBack.onObjectRetrieved(new ArrayList<>());
            } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException | InstantiationException e) {
                Log.e("RegisterCourse", "Error returning empty schedule list", e);
            }
            return;
        }

        if (selectedTS == null || selectedTS.size() < dowSelected.size()) {
            Log.e("RegisterCourse", "Invalid selectedTS mapping. selectedTS size="
                    + (selectedTS == null ? 0 : selectedTS.size())
                    + ", dowSelected size=" + dowSelected.size());
            try {
                callBack.onObjectRetrieved(new ArrayList<>());
            } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException | InstantiationException e) {
                Log.e("RegisterCourse", "Error returning empty schedule list for invalid selection", e);
            }
            return;
        }

        new CourseRepository(course.getID()).loadAsNormal()
                .addOnSuccessListener(freshCourse -> {
                    ArrayList<Schedule> existingSchedules = (freshCourse != null && freshCourse.getSchedules() != null)
                            ? freshCourse.getSchedules()
                            : new ArrayList<>();
                    createSchedulesFromExisting(existingSchedules, callBack);
                })
                .addOnFailureListener(e -> {
                    Log.e("RegisterCourse", "Failed loading fresh course schedules, fallback to in-memory list", e);
                    ArrayList<Schedule> fallback = course.getSchedules() == null
                            ? new ArrayList<>()
                            : course.getSchedules();
                    createSchedulesFromExisting(fallback, callBack);
                });
    }

    private void createSchedulesFromExisting(ArrayList<Schedule> existingSchedules, ObjectCallBack<ArrayList<Schedule>> callBack) {
        ArrayList<Schedule> schedules = new ArrayList<>();
        java.util.concurrent.atomic.AtomicInteger completedCount = new java.util.concurrent.atomic.AtomicInteger(0);
        int total = dowSelected.size();
        Runnable maybeComplete = () -> {
            if (completedCount.incrementAndGet() == total) {
                try {
                    callBack.onObjectRetrieved(schedules);
                } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | InstantiationException e) {
                    Log.e("RegisterCourse", "Error returning created schedules", e);
                }
            }
        };

        for (int i = 0; i < total; i++) {
            TimeSlot timeSlot = selectedTS.get(i);
            DayOfWeek dayOfWeek = dowSelected.get(i);

            Schedule matched = findExactSchedule(existingSchedules, dayOfWeek, timeSlot.getStart(), timeSlot.getEnd());
            if (matched != null) {
                schedules.add(matched);
                maybeComplete.run();
                continue;
            }

            try {
                Room.getEmptyRoom(timeSlot.getStart(), timeSlot.getEnd(), dayOfWeek, new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(Room room) {
                        Schedule schedule = new Schedule(timeSlot.getStart(),
                                (int) (timeSlot.getTime().getSeconds() / 60),
                                dayOfWeek,
                                sq == ScheduleQuality.PRIVATE_ONLY, course.getID());
                        if (room != null) {
                            schedule.setRoom(room.getID());
                        }
                        schedule.setOfCourse(course.getID());
                        schedules.add(schedule);
                        existingSchedules.add(schedule);
                        maybeComplete.run();
                    }

                    @Override
                    public void onError(DatabaseError error) {
                        Schedule schedule = new Schedule(timeSlot.getStart(),
                                (int) (timeSlot.getTime().getSeconds() / 60),
                                dayOfWeek,
                                sq == ScheduleQuality.PRIVATE_ONLY, course.getCourseID());
                        schedules.add(schedule);
                        existingSchedules.add(schedule);
                        maybeComplete.run();
                    }
                });
            } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException | InstantiationException e) {
                Log.e("RegisterCourse", "Failed to resolve room for schedule", e);
                Schedule schedule = new Schedule(timeSlot.getStart(),
                        (int) (timeSlot.getTime().getSeconds() / 60),
                        dayOfWeek,
                        sq == ScheduleQuality.PRIVATE_ONLY, course.getCourseID());
                schedules.add(schedule);
                existingSchedules.add(schedule);
                maybeComplete.run();
            }
        }
    }

    private Schedule findExactSchedule(ArrayList<Schedule> existingSchedules, DayOfWeek day, java.time.LocalTime start, java.time.LocalTime end) {
        if (existingSchedules == null || existingSchedules.isEmpty()) {
            return null;
        }
        for (Schedule existing : existingSchedules) {
            if (existing == null || existing.getDay() == null || existing.getMeetingStart() == null || existing.getMeetingEnd() == null) {
                continue;
            }
            if (existing.getDay() == day
                    && start.equals(existing.getMeetingStart())
                    && end.equals(existing.getMeetingEnd())) {
                return existing;
            }
        }
        return null;
    }
    private void initializeProgressBar(){
        pb_regisProgress.setMin(1);
        pb_regisProgress.setMax(amtOfRegisFrag);
        pb_regisProgress.setProgress(currentFragIndex+1);
    }

    private void ensureStudentLinkedToSchedules(ArrayList<Schedule> schedules) {
        if (schedules == null || student == null || !Tool.boolOf(student.getID())) {
            return;
        }
        for (Schedule schedule : schedules) {
            if (schedule == null || !Tool.boolOf(schedule.getID())) {
                continue;
            }
            if (schedule.students == null) {
                schedule.students = new ArrayList<>();
            }
            if (!schedule.students.contains(student.getID())) {
                schedule.students.add(student.getID());
            }
            ScheduleRepository scheduleRepository = new ScheduleRepository(schedule.getID());
            scheduleRepository.addStringToArrayAsync("students", student.getID());
        }
    }

    @SuppressLint("SetTextI18n")
    public void nextFragment(){

        if(!((checkFragmentCompletion)registerFragments[currentFragIndex]).checkIfCompleted()){
            Toast.makeText(this, "Please Fill in the Form Completely to Continue", Toast.LENGTH_SHORT).show();
            return;
        }
        if(currentFragIndex < amtOfRegisFrag){
            currentFragIndex++;
            updateCurrentFragment();
            Log.d("RegisterCourse", "Next button Clicked! Current Frag Index: "+ currentFragIndex + "/" + (amtOfRegisFrag-1));
            if(currentFragIndex == amtOfRegisFrag-1){
                if(course.isAutoAcceptStudent()){
                    btn_next.setText("Join");
                    nextJoin = true;
                }else{
                    btn_next.setText("Request to Join");
                }
            }

        }
    }
    public void prevFragment(){
        if(currentFragIndex > 0){
            if(nextJoin){
                nextJoin = false;
            }
            currentFragIndex--;
            Log.d("RegisterCourse", "Prev button Clicked! Current Frag Index: " + currentFragIndex + "/" + (amtOfRegisFrag-1));
            updateCurrentFragment();
        }else{
            goBackToCourseFullPage();
        }
    }

    private void goBackToCourseFullPage(){
        Bundle bundle = new Bundle();
        bundle.putSerializable(Course.SERIALIZE_KEY_CODE, course);
        Tool.loadFragment(this, R.id.nhf_ss_FragContainer, new CourseExploreFullPage());
    }


    @SuppressLint("SetTextI18n")
    public void updateCurrentFragment(){
        Tool.loadFragment(this, fc_container.getId(), registerFragments[currentFragIndex]);
        pb_regisProgress.setProgress(currentFragIndex+1);
        if(currentFragIndex == 0){
            btn_prev.setText("Return");
        }else{
            btn_prev.setText("Previous");
        }
        if(!nextJoin){
            btn_next.setText("Next");
        }
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public boolean isPaymentPreferences() {
        return paymentPreferences;
    }

    public void setPaymentPreferences(boolean paymentPreferences) {
        this.paymentPreferences = paymentPreferences;
    }

    public ArrayList<TimeSlot> getSelectedTS() {
        if(selectedTS == null){
            selectedTS = new ArrayList<>();
        }
        return selectedTS;
    }

    public void setSelectedTS(ArrayList<TimeSlot> selectedTS) {
        this.selectedTS = selectedTS;
    }



    public void addDowSelected(DayOfWeek dow){
        dowSelected.add(dow);
    }

    public ArrayList<DayOfWeek> getDowSelected() {
        if(dowSelected == null){
            dowSelected = new ArrayList<>();
        }
        return dowSelected;
    }


    public void setDowSelected(ArrayList<DayOfWeek> dowSelected) {
        this.dowSelected = dowSelected;
    }

    public ScheduleQuality getSq() {
        return sq;
    }

    public void setSq(ScheduleQuality sq) {
        this.sq = sq;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public int getAmountOfMeetingPerWeek() {
        return amountOfMeetingPerWeek;
    }

    public void setAmountOfMeetingPerWeek(int amountOfMeetingPerWeek) {
        this.amountOfMeetingPerWeek = amountOfMeetingPerWeek;
    }

    public int getSelectedPrice() {
        return selectedPrice;
    }

    public void setSelectedPrice(int selectedPrice) {
        this.selectedPrice = selectedPrice;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getEducationGrade() {
        return educationGrade;
    }

    public void setEducationGrade(String educationGrade) {
        this.educationGrade = educationGrade;
    }

    public String getReasonForJoining() {
        return reasonForJoining;
    }

    public void setReasonForJoining(String reasonForJoining) {
        this.reasonForJoining = reasonForJoining;
    }
}
