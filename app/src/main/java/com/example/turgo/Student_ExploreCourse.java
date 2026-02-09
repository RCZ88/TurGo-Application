package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class Student_ExploreCourse extends Fragment {

    private Student student;
    private ArrayList<Teacher> teachers = new ArrayList<>();
    private ArrayList<Course> exploreCourses = new ArrayList<>();
    RecyclerView rv_courses;
    TextView tv_emptyCourses;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Student_ExploreCourse() {}

    public static Student_ExploreCourse newInstance(String param1, String param2) {
        Student_ExploreCourse fragment = new Student_ExploreCourse();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_student_explore_courses, container, false);

        rv_courses = view.findViewById(R.id.rv_exploreCourses);
        tv_emptyCourses = view.findViewById(R.id.tv_exploreCourseEmpty);

        StudentScreen sc = (StudentScreen) requireActivity();
        student = sc.getStudent();

        loadExploreCourses();

        return view;
    }

    private void loadExploreCourses() {
        Log.d("StudentExploreCourse", "Fetching Student's Explore Courses...");

        student.getExploreCourse().addOnSuccessListener(courses -> {
            exploreCourses = courses;
            Log.d("StudentExploreCourse", "Retrieved " + exploreCourses.size() + " Courses");

            if (exploreCourses.isEmpty()) {
                Tool.handleEmpty(true, rv_courses, tv_emptyCourses);
                return;
            }

            loadTeachersForCourses();
        }).addOnFailureListener(e -> {
            Log.e("StudentExploreCourse", "Failed to load explore courses", e);
            Tool.handleEmpty(true, rv_courses, tv_emptyCourses);
        });
    }

    private void loadTeachersForCourses() {
        teachers.clear();

        final int total = exploreCourses.size();
        final int[] completed = {0};

        for (Course course : exploreCourses) {
            course.getTeacher().addOnSuccessListener(teacher -> {
                teachers.add(teacher);
                completed[0]++;

                if (completed[0] == total) {
                    onAllDataLoaded();
                }
            }).addOnFailureListener(e -> {
                Log.e("StudentExploreCourse", "Failed to load teacher for course: " + course, e);
                completed[0]++;
                if (completed[0] == total) {
                    onAllDataLoaded();
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void onAllDataLoaded() {
        Tool.handleEmpty(exploreCourses.isEmpty(), rv_courses, tv_emptyCourses);

        CourseAdapter courseAdapter = new CourseAdapter(
                exploreCourses,
                student,
                new OnItemClickListener<>() {
                    @Override
                    public void onItemClick(Course item) {
                        Log.d("selectedBottomNav", "on Student_ExploreCourse: " + Student_ExploreCourse.class.getSimpleName());
                        CourseExploreFullPage page = new CourseExploreFullPage();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Course.SERIALIZE_KEY_CODE, item);
                        Tool.loadFragment(requireActivity(), StudentScreen.getContainer(), page, bundle);
                    }

                    @Override
                    public void onItemLongClick(Course item) {}
                },
                teachers,
                requireContext()
        );

        rv_courses.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_courses.setAdapter(courseAdapter);
    }
}
