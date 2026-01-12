package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
    int amtOfRegisFrag = 3;
    Fragment[]registerFragments = new Fragment[amtOfRegisFrag];


    ProgressBar pb_regisProgress;

    Fragment frag_UserInfo, frag_AvailDayTime, frag_PrivateDurationAmount, frag_SelectPaymentConfirm;

    View fc_container;

    Button btn_next, btn_prev;


    private int amountOfMeetingPerWeek;
    private Course course;
    private boolean isPrivate;
    private int duration;
    private int selectedPrice;
    private ArrayList<DayOfWeek>dowSelected;
    private HashMap<TimeSlot, Integer> timeSlotPeopleAmountSelected;
    private String school, educationGrade, reasonForJoining;
    int currentFragIndex;
    private Student student;
    private boolean paymentPreferences;
    private boolean nextJoin = false;

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
        this.student = (Student) intent.getSerializableExtra("Student");
        fc_container = findViewById(R.id.fcv_RegisterFragment);

        frag_UserInfo = new RC_UserInformation();
        frag_PrivateDurationAmount = new RC_PrivateDurationAmount();
        frag_AvailDayTime = new RC_AvailableDayTime();
        frag_SelectPaymentConfirm = new RC_SelectPaymentConfirm();

        registerFragments[0] = frag_UserInfo;
        registerFragments[1] = frag_PrivateDurationAmount;
        registerFragments[2] = frag_AvailDayTime;
        registerFragments[3] = frag_SelectPaymentConfirm;

        btn_prev = findViewById(R.id.btn_previous_rc);
        btn_next = findViewById(R.id.btn_next_rc);

        btn_prev.setOnClickListener(view -> prevFragment());
        btn_next.setOnClickListener(view -> {
            if(currentFragIndex < amtOfRegisFrag){
                nextFragment();
                if(nextJoin){
                    try {
                        apply();
                    } catch (InvocationTargetException | NoSuchMethodException |
                             IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }else{
                Log.e("Frag Limited", "Fragment End Reached");
            }
        });
    }

    public HashMap<TimeSlot, Integer> getTimeSlotPeopleAmountSelected() {
        return timeSlotPeopleAmountSelected;
    }

    public void setTimeSlotPeopleAmountSelected(HashMap<TimeSlot, Integer> timeSlotPeopleAmountSelected) {
        this.timeSlotPeopleAmountSelected = timeSlotPeopleAmountSelected;
    }

    public void addDowSelected(DayOfWeek dow){
        dowSelected.add(dow);
    }

    public ArrayList<DayOfWeek> getDowSelected() {
        return dowSelected;
    }

    public void setDowSelected(ArrayList<DayOfWeek> dowSelected) {
        this.dowSelected = dowSelected;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
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

    public void apply() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        ArrayList<Schedule> schedules = new ArrayList<>();
        ArrayList<TimeSlot> timeSlotSelected = new ArrayList<>(timeSlotPeopleAmountSelected.keySet());
        for(int i = 0; i<dowSelected.size(); i++){
            TimeSlot timeSlot = timeSlotSelected.get(i);
            int finalI = i;
            try {
                Room.getEmptyRoom(timeSlot.getStart(), timeSlot.getEnd(), dowSelected.get(i), new ObjectCallBack<Room>() {
                    @Override
                    public void onObjectRetrieved(Room object) {
                        Schedule schedule = new Schedule(timeSlot.getStart(), (int)(timeSlot.getTime().getSeconds()/60), dowSelected.get(finalI), isPrivate);
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
                        student.joinCourse(course, paymentPreferences, isPrivate, selectedPrice, schedules);
            }, () -> {
                Toast.makeText(this, "Joined Course Successfully, Welcome!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, StudentScreen.class);
                intent.putExtra("ShowFragment", "CourseJoinedFullPage");
                intent.putExtra("CourseJoined", course);
                startActivity(intent);
                finish();
            }, e -> {
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

    @SuppressLint("SetTextI18n")
    public void nextFragment(){
        if(currentFragIndex < amtOfRegisFrag){
            currentFragIndex++;
            updateCurrentFragment();
            if(currentFragIndex == amtOfRegisFrag){
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
            currentFragIndex--;
            updateCurrentFragment();
        }
    }

    public void updateCurrentFragment(){
        getSupportFragmentManager().beginTransaction().replace(fc_container.getId(), registerFragments[currentFragIndex]);
        btn_prev.setEnabled(currentFragIndex > 0);
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
}