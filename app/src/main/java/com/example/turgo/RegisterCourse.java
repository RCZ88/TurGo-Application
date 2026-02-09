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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class RegisterCourse extends AppCompatActivity {
    int amtOfRegisFrag = 4;
    Fragment[]registerFragments = new Fragment[amtOfRegisFrag];
    HashMap<TimeSlot, Integer>slotAmount;
    ProgressBar pb_regisProgress;
    ImageButton btn_collapse;
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
                            if (course.isAutoAcceptStudent()) {
                                student.joinCourse(course, paymentPreferences,
                                                sq == ScheduleQuality.PRIVATE_ONLY, selectedPrice,
                                                schedules.get(), selectedTS, RegisterCourse.this)
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
                                    User.sendMail(acm);
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
    private void createSchedules(ObjectCallBack<ArrayList<Schedule>> callBack) {
        ArrayList<Schedule> schedules = new ArrayList<>();

        Log.d("DEBUG", "createSchedules called, dowSelected size: " + dowSelected.size());

        // Check if dowSelected is empty - return empty list immediately
        if (dowSelected.isEmpty()) {
            Log.d("DEBUG", "dowSelected is empty, returning empty list");
            try {
                callBack.onObjectRetrieved(new ArrayList<>());
            } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException | InstantiationException e) {
                Log.e("DEBUG", "Error in empty schedule callback", e);
                try {
                    // Try to report error through callback if possible
                    // Note: This depends on your ObjectCallBack implementation
                    // If there's no onError for exceptions, we need to handle differently
                    callBack.onObjectRetrieved(new ArrayList<>());
                    // You might need to adapt this based on your actual ObjectCallBack class
                    // If it doesn't have onError for exceptions, we need another approach
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return; // IMPORTANT: Return after calling callback
        }

        Log.d("DEBUG", "Processing " + dowSelected.size() + " days");

        // Use AtomicInteger to track completion since we're in async callbacks
        java.util.concurrent.atomic.AtomicInteger completedCount = new java.util.concurrent.atomic.AtomicInteger(0);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.SCHEDULE.getPath());
        ArrayList<Schedule>existingSchedules = new ArrayList<>();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    try {
                        ds.getValue(ScheduleFirebase.class).convertToNormal(new ObjectCallBack<>() {
                            @Override
                            public void onObjectRetrieved(Schedule object) {
                                existingSchedules.add(object);
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        for (int i = 0; i < dowSelected.size(); i++) {
            TimeSlot timeSlot = selectedTS.get(i);
            DayOfWeek dayOfWeek = dowSelected.get(i);

            boolean scheduleFound = false;
            Log.d("DEBUG", "Processing schedule " + i + ": DOW=" + dayOfWeek + ", TimeSlot=" + timeSlot.toStr());
            for(Schedule schedule : existingSchedules){
                if(timeSlot.getStart().equals(schedule.getMeetingStart()) && timeSlot.getEnd().equals(schedule.getMeetingEnd())){
                    schedules.add(schedule);
                    scheduleFound = true;
                    break;
                }
            }
            if(scheduleFound) continue;


            try {
                int finalI = i;
                Room.getEmptyRoom(timeSlot.getStart(), timeSlot.getEnd(), dayOfWeek, new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(Room room) {
                        // Create schedule with the found room

                        Schedule schedule = new Schedule(timeSlot.getStart(),
                                (int) (timeSlot.getTime().getSeconds() / 60),
                                dayOfWeek,
                                sq == ScheduleQuality.PRIVATE_ONLY, course.getID());
                        // If room was found, set it on the schedule
                        if (room != null) {
                            //save room to db
                            Log.d("DEBUG", "Found room for schedule " + finalI + ": " + room.getID());
                            schedule.setRoom(room.getID());
                        } else {
                            Log.d("DEBUG", "No room found for schedule " + finalI);
                        }
                        schedule.setOfCourse(course.getID());


                        schedules.add(schedule);
                        Log.d("DEBUG", "Schedule " + finalI + " created: " + schedule);

                        // Check if all schedules have been created
                        int completed = completedCount.incrementAndGet();
                        if (completed == dowSelected.size()) {
                            Log.d("DEBUG", "All schedules created, returning list of size: " + schedules.size());
                            try {
                                callBack.onObjectRetrieved(schedules);
                            } catch (Exception e) {
                                Log.e("DEBUG", "Error in final callback", e);
                            }
                        }
                    }

                    @Override
                    public void onError(DatabaseError error) {
                        Log.e("DEBUG", "Error getting room for schedule " + finalI + ": " + error.getMessage());

                        // Even if room finding fails, we should still create the schedule (maybe without room)
                        Schedule schedule = new Schedule(timeSlot.getStart(),
                                (int) (timeSlot.getTime().getSeconds() / 60),
                                dayOfWeek,
                                sq == ScheduleQuality.PRIVATE_ONLY, course.getCourseID());
                        schedules.add(schedule);

                        int completed = completedCount.incrementAndGet();
                        if (completed == dowSelected.size()) {
                            Log.d("DEBUG", "All schedules processed (some with errors), returning list");
                            try {
                                callBack.onObjectRetrieved(schedules);
                            } catch (Exception e) {
                                Log.e("DEBUG", "Error in error callback", e);
                            }
                        }
                    }
                });
            } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException | InstantiationException e) {
                Log.e("DEBUG", "Exception in Room.getEmptyRoom for schedule " + i, e);

                // Handle exception - still create schedule but mark as problematic
                Schedule schedule = new Schedule(timeSlot.getStart(),
                        (int) (timeSlot.getTime().getSeconds() / 60),
                        dayOfWeek,
                        sq == ScheduleQuality.PRIVATE_ONLY, course.getCourseID());
                schedules.add(schedule);

                int completed = completedCount.incrementAndGet();
                if (completed == dowSelected.size()) {
                    Log.d("DEBUG", "All schedules processed (some with exceptions), returning list");
                    try {
                        callBack.onObjectRetrieved(schedules);
                    } catch (Exception ex) {
                        Log.e("DEBUG", "Error in exception callback", ex);
                    }
                }
            }
        }
    }
    private void initializeProgressBar(){
        pb_regisProgress.setMin(1);
        pb_regisProgress.setMax(amtOfRegisFrag);
        pb_regisProgress.setProgress(currentFragIndex+1);
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
            Bundle bundle = new Bundle();
            bundle.putSerializable(Course.SERIALIZE_KEY_CODE, course);
            Tool.loadFragment(this, R.id.nhf_ss_FragContainer, new CourseExploreFullPage());
        }
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
