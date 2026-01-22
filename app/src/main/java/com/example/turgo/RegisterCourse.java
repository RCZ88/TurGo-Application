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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class RegisterCourse extends AppCompatActivity {
    int amtOfRegisFrag = 4;
    Fragment[]registerFragments = new Fragment[amtOfRegisFrag];
    HashMap<TimeSlot, Integer>slotAmount;
    ProgressBar pb_regisProgress;
    ImageButton btn_collapse;
    Fragment frag_UserInfo, frag_AvailDayTime, frag_PrivateDurationAmount, frag_SelectPaymentConfirm;

    View fc_container;

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
        Log.d("RegisterCourse", "Course Object:\n " + this.course.toString());
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

        btn_prev.setOnClickListener(view -> {
            Log.d("RegisterCourse", "Prev button Clicked!");
            prevFragment();
        });
        btn_next.setOnClickListener(view -> {
            Log.d("RegisterCourse", "Next button Clicked! Current Frag Index: "+ currentFragIndex);


            if(nextJoin){
                try {
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



    public void apply() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        ArrayList<Schedule> schedules = new ArrayList<>();
        ArrayList<TimeSlot> timeSlotSelected = new ArrayList<>(selectedTS);
        if(dowSelected == null){
            Log.e("ApplyCourse", "Day of Week Array (dowSelected) is null.");
            return;
        }
        for(int i = 0; i<dowSelected.size(); i++){
            TimeSlot timeSlot = timeSlotSelected.get(i);
            int finalI = i;
            try {
                Room.getEmptyRoom(timeSlot.getStart(), timeSlot.getEnd(), dowSelected.get(i), new ObjectCallBack<Room>() {
                    @Override
                    public void onObjectRetrieved(Room object) {
                        Schedule schedule = new Schedule(timeSlot.getStart(), (int)(timeSlot.getTime().getSeconds()/60), dowSelected.get(finalI), sq == ScheduleQuality.PRIVATE_ONLY);
                        schedules.add(schedule);
                    }

                    @Override
                    public void onError(DatabaseError error) {

                    }
                });
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }
        if(course.isAutoAcceptStudent()){
            Tool.run(this, "Applying to Course",
                    () -> {
                        student.joinCourse(course, paymentPreferences, sq == ScheduleQuality.PRIVATE_ONLY, selectedPrice, schedules, timeSlotSelected);
            }, () -> {
                Toast.makeText(this, "Joined Course Successfully, Welcome!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, StudentScreen.class);
                intent.putExtra("ShowFragment", "CourseJoinedFullPage");
                intent.putExtra("CourseJoined", course);
                startActivity(intent);
                finish();
            }, e -> {
                Log.d("apply", "Error Applying to Course: ", e);
                Toast.makeText(this, "System Fail when Applying to Course.", Toast.LENGTH_SHORT).show();
            });



        }else{
            final Teacher[] teacher = new Teacher[1];
            Tool.run(this, "Loading Teacher...",
                    ()->{
                        teacher[0] = Await.get(course::getTeacher);
                    },
                    ()->{

                        //async - completed
                        MailApplyCourse acm = new MailApplyCourse(student, teacher[0], schedules, course, reasonForJoining, school, educationGrade);
                        try {
                            User.sendMail(acm);
                        } catch (InvocationTargetException | NoSuchMethodException |
                                 IllegalAccessException | InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                        Toast.makeText(this, "Apply Course Mail Request Sent!", Toast.LENGTH_SHORT).show();
                    },
                    e->{

                    });
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
            Log.d("Current Fragment", "Current Frag Index: " + currentFragIndex);
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
        nextJoin = !nextJoin;
        if(currentFragIndex > 0){
            currentFragIndex--;
            updateCurrentFragment();
        }else{
            Bundle bundle = new Bundle();
            bundle.putSerializable(Course.SERIALIZE_KEY_CODE, course);
            DataLoading.loadAndNavigate(this, CourseExploreFullPage.class, bundle, true, StudentScreen.class, student, StudentScreen.getMenuIdForFragment(Student_ExploreCourse.class.getSimpleName()));
        }
    }

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