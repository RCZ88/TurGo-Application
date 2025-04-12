package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class RegisterCourse extends AppCompatActivity {
    int amtOfRegisFrag = 3;
    Fragment[]registerFragments = new Fragment[amtOfRegisFrag];


    ProgressBar pb_regisProgress;

    Fragment frag_AvailDayTime, frag_PrivateDurationAmount;

    View fc_container;

    Button btn_next, btn_prev;


    private int amountOfMeetingPerWeek;
    private Course course;
    private boolean isPrivate;
    private int duration;
    private ArrayList<DayOfWeek>dowSelected;
    private HashMap<TimeSlot, Integer> timeSlotPeopleAmountSelected;
    int currentFragIndex;

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

        fc_container = findViewById(R.id.fcv_RegisterFragment);

        RC_PrivateDurationAmount rc_privateDurationAmount = new RC_PrivateDurationAmount();
        RC_AvailableDayTime rc_availableDayTime = new RC_AvailableDayTime();
        RC_SelectPaymentConfirm rc_selectPaymentConfirm = new RC_SelectPaymentConfirm();

        registerFragments[0] = rc_privateDurationAmount;
        registerFragments[1] = rc_availableDayTime;
        registerFragments[2] = rc_selectPaymentConfirm;

        btn_prev = findViewById(R.id.btn_previous_rc);
        btn_next = findViewById(R.id.btn_next_rc);

        btn_prev.setOnClickListener(view -> prevFragment());
        btn_next.setOnClickListener(view -> {
            if(currentFragIndex < amtOfRegisFrag){
                nextFragment();
            }else{

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
    public void apply(){
        ArrayList<Schedule> schedules = new ArrayList<>();
        ArrayList<TimeSlot> timeSlotSelected = new ArrayList<>();
        timeSlotSelected.addAll(timeSlotPeopleAmountSelected.keySet());
        for(int i = 0; i<dowSelected.size(); i++){
            TimeSlot timeSlot = timeSlotSelected.get(i);
            Schedule schedule = new Schedule(course, timeSlot.getStart(), (int)(timeSlot.getTime().getSeconds()/60), dowSelected.get(i), Room.getEmptyRoom(timeSlot.getStart(), timeSlot.getEnd(), dowSelected.get(i)), isPrivate);
        }
        if(course.isAutoAcceptStudent()){

        }else{

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
}