package com.example.turgo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turgo.databinding.ActivityAdminViewAllCourseBinding;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class AdminViewAllCourses extends AppCompatActivity {
    ActivityAdminViewAllCourseBinding binding;
    RecyclerView rv_allCourses;
    EditText et_searchCourse;
    MaterialCardView mcv_backButton;
    FloatingActionButton fab_createNewCourse;
    ArrayList<Course>coursesList = new ArrayList<>();
    CourseAdminAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_view_all_course);
        binding = ActivityAdminViewAllCourseBinding.inflate(getLayoutInflater());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rv_allCourses = findViewById(R.id.rv_AVAC_allCourses);
        et_searchCourse = findViewById(R.id.et_AVAC_searchInput);
        mcv_backButton = findViewById(R.id.cv_AVAC_backBtnContainer);
        fab_createNewCourse = findViewById(R.id.fab_AVAC_addCourse);
        RequireUpdate.getAllObjects(Course.class).addOnSuccessListener(courses->{
            ArrayList<Course>courseArrayList = new ArrayList<>();
            if(!courses.isEmpty()){
                courseArrayList = (ArrayList<Course>) courses;
                coursesList = courseArrayList;
            }
            adapter = new CourseAdminAdapter(courseArrayList, course -> {

            });
            rv_allCourses.setLayoutManager(new LinearLayoutManager(AdminViewAllCourses.this));
            rv_allCourses.setAdapter(adapter);
        });
        et_searchCourse.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0){
                    adapter.setCourseList(coursesList);
                    return;
                }
                String search = s.toString();
                adapter.setCourseList(Tool.streamToArray(coursesList.stream().filter(course -> course.getCourseName().toLowerCase().contains(search))));
            }
        });


    }
}