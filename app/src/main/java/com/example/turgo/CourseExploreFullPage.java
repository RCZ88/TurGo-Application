package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class CourseExploreFullPage extends Fragment {

    Button btn_joinCourse;
    TextView tv_courseTitle, tv_courseDescription, tv_teacherName, tv_teacherDescription;
    ViewPager2 vp_courseImages;
    ImageView iv_courseWallpaper;
    LinearLayout ll_ImageIndicators;
    RecyclerView rv_courseDayTimes;
    Course course;
    Teacher teacher;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public CourseExploreFullPage() {
        // Required empty public constructor
    }

    public static CourseExploreFullPage newInstance(String param1, String param2) {
        CourseExploreFullPage fragment = new CourseExploreFullPage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            course = (Course) getArguments().getSerializable(Course.SERIALIZE_KEY_CODE);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_explore_course_full_page, container, false);

        tv_courseTitle = view.findViewById(R.id.tv_ecfp_CourseTitle);
        btn_joinCourse = view.findViewById(R.id.btn_JoinCourse);
        tv_courseDescription = view.findViewById(R.id.tv_CourseDescription);
        tv_teacherDescription = view.findViewById(R.id.tv_TeacherDescription);
        tv_teacherName = view.findViewById(R.id.tv_TeacherName);
        vp_courseImages = view.findViewById(R.id.vp_ecfp_courseImages);
        ll_ImageIndicators = view.findViewById(R.id.ll_ecfp_imageIndicators);
        iv_courseWallpaper = view.findViewById(R.id.iv_ecfp_courseWallpaper);
        rv_courseDayTimes = view.findViewById(R.id.rv_ecfp_availTimes);
        rv_courseDayTimes.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));

        // Static UI from course (synchronous)
        ArrayList<DayTimeArrangement> dtas = course.getDayTimeArrangement();
        ArrayList<String> daysOfDtas = DayTimeArrangement.getDaysOfDtas(dtas);
        ArrayList<String> timesOfDtas = DayTimeArrangement.getTimesOfDtas(dtas);
        Log.d("CEFP", daysOfDtas.toString());
        Log.d("CEFP", timesOfDtas.toString());

        DayTimeAdapter adapter = new DayTimeAdapter(daysOfDtas, timesOfDtas);
        rv_courseDayTimes.setAdapter(adapter);

        Tool.setImageCloudinary(getContext(), course.getBackgroundCloudinary(), iv_courseWallpaper);

        tv_courseTitle.setText(course.getCourseName());
        tv_courseDescription.setText(course.getCourseDescription());

        CourseImageAdapter cia = new CourseImageAdapter(course.getImagesCloudinary());
        setupCarousel(cia);

        btn_joinCourse.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), RegisterCourse.class);
            intent.putExtra("Student", ((StudentScreen) getContext()).getStudent());
            intent.putExtra(Course.SERIALIZE_KEY_CODE, course);
            startActivity(intent);
        });

        // Async load teacher (no Await)
        loadTeacher();

        return view;
    }

    private void loadTeacher() {
        course.getTeacher()
                .addOnSuccessListener(t -> {
                    teacher = t;
                    tv_teacherName.setText(teacher.getFullName());

                    String teacherResume = "Teacher Description not Found";
                    if (Tool.boolOf(teacher.getTeacherResume())) {
                        teacherResume = teacher.getTeacherResume();
                    }
                    tv_teacherDescription.setText(teacherResume);
                })
                .addOnFailureListener(e -> {
                    tv_teacherName.setText("Teacher not found");
                    tv_teacherDescription.setText("Teacher description not available");
                });
    }

    private void setupCarousel(CourseImageAdapter adapter) {
        vp_courseImages.setAdapter(adapter);

        setupIndicators(adapter.getItemCount());
        vp_courseImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
            }
        });
    }

    private void setupIndicators(int imageAmount) {
        ll_ImageIndicators.removeAllViews();
        for (int i = 0; i < imageAmount; i++) {
            ImageView dot = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setImageResource(R.drawable.indicator_dot);
            ll_ImageIndicators.addView(dot);
        }
        updateIndicators(0);
    }

    private void updateIndicators(int currentPosition) {
        for (int i = 0; i < ll_ImageIndicators.getChildCount(); i++) {
            ImageView dot = (ImageView) ll_ImageIndicators.getChildAt(i);
            dot.setSelected(i == currentPosition);
        }
    }
}
