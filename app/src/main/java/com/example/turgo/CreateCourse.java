package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;

public class CreateCourse extends AppCompatActivity {
    protected String courseName, courseDescription,  courseIconCloudinary, courseBannerCloudinary;
    protected CourseType courseType;
    file courseIcon, courseBanner = null;
    protected ArrayList<String> courseImagesCloudinary;
    protected Teacher teacher;
    protected ArrayList<DayTimeArrangement> dtas = new ArrayList<>();
    protected int hourlyCost, baseCost, monthlyDiscount;
    protected boolean [] groupPrivate, acceptedPaymentMethods;
    protected boolean autoAcceptStudent;
    cc_AddScheduleDTA as = new cc_AddScheduleDTA();
    cc_CourseInformation ci = new cc_CourseInformation();
    cc_Media m = new cc_Media();
    cc_SetTeacher st = new cc_SetTeacher();
    cc_PriceEnrollment pe = new cc_PriceEnrollment();
    Fragment[] phases = {ci, st, as, pe, m};
    Course course = new Course();
    FragmentContainerView fcv_phases;
    Button btn_next, btn_prev;
    int currentPhase;
    Admin admin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        groupPrivate = new boolean[2];
        acceptedPaymentMethods = new boolean[2];
        setContentView(R.layout.activity_create_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        admin = (Admin) getIntent().getSerializableExtra(Admin.SERIALIZE_KEY_CODE);
        currentPhase = 0;
        fcv_phases = findViewById(R.id.fcv_CreateCoursePhases);
        btn_next = findViewById(R.id.btn_CC_next);
        btn_prev = findViewById(R.id.btn_CC_back);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fcv_CreateCoursePhases, phases[0])
                .commit();
        if(currentPhase == 0){
            btn_prev.setVisibility(View.GONE);
        }

        btn_next.setOnClickListener(view -> {
            try {
                next();
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                     InstantiationException e) {
                throw new RuntimeException(e);
            }
        });
        btn_prev.setOnClickListener(view -> back());
    }
    @SuppressLint("SetTextI18n")
    private void next() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Log.d("CreateCourse", "CurrentPhase: " + currentPhase);
        if(currentPhase != phases.length-1){
            currentPhase++;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fcv_CreateCoursePhases, phases[currentPhase])
                    .commit();
            if(currentPhase == phases.length-1){
                btn_next.setText("Done");
            }
            if(currentPhase!= 0){
                btn_prev.setVisibility(View.VISIBLE);
            }
        }else{
            createCourse();
        }
    }
    public void back(){
        Log.d("CreateCourse", "CurrentPhase: " + currentPhase);
        if(currentPhase != 0){
            currentPhase--;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fcv_CreateCoursePhases, phases[currentPhase])
                    .commit();
            if(currentPhase == 0){
                btn_prev.setVisibility(View.GONE);
            }
        }
    }
    private void createCourse() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (courseName.isEmpty() || courseDescription.isEmpty() || courseType == null) {
            Toast.makeText(this, "Please fill in all required course information", Toast.LENGTH_LONG).show();
            return;
        }

        String iconUrl = null;
        if (courseIcon != null) {
            iconUrl = courseIcon.getSecureURL();
        } else if (courseIconCloudinary != null && !courseIconCloudinary.isEmpty()) {
            iconUrl = courseIconCloudinary;
        }

        String bannerUrl = null;
        if (courseBanner != null) {
            bannerUrl = courseBanner.getSecureURL();
        } else if (courseBannerCloudinary != null && !courseBannerCloudinary.isEmpty()) {
            bannerUrl = courseBannerCloudinary; 
        }

        if (iconUrl == null || bannerUrl == null) {
            Toast.makeText(this, "Please upload or select course icon and banner", Toast.LENGTH_LONG).show();
            return;
        }

        if (teacher == null) {
            Toast.makeText(this, "Please select a teacher", Toast.LENGTH_LONG).show();
            return;
        }

        course.setCourseName(courseName);
        course.setCourseDescription(courseDescription);
        course.setCourseType(courseType);
        course.setBaseCost(baseCost);
        course.setHourlyCost(hourlyCost);
        course.setMonthlyDiscountPercentage(monthlyDiscount);
        course.setAutoAcceptStudent(autoAcceptStudent);
        course.setPaymentPer(acceptedPaymentMethods);
        course.setPrivateGroup(groupPrivate);
        course.setDayTimeArrangement(dtas);

        course.setLogo(iconUrl);
        course.setBackground(bannerUrl);
        course.setImagesCloudinary(courseImagesCloudinary);

        course.setTeacher(teacher);

//        RTDBManager<Course> courseManager = new RTDBManager<>();
//        String courseId = course.getID();

        course.updateDB();
        Toast.makeText(this, "Course created successfully!", Toast.LENGTH_LONG).show();
    }
}