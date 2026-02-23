package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

        loadActiveStudentAndExploreCourses();

        return view;
    }

    private void loadActiveStudentAndExploreCourses() {
        if (!isAdded()) {
            return;
        }
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser == null || !Tool.boolOf(fbUser.getUid())) {
            Tool.handleEmpty(true, rv_courses, tv_emptyCourses);
            return;
        }
        String activeUid = fbUser.getUid();

        StudentScreen sc = (StudentScreen) requireActivity();
        Student current = sc.getStudent();
        if (current != null && Tool.boolOf(current.getID()) && current.getID().equals(activeUid)) {
            student = current;
            loadExploreCourses();
            return;
        }

        new StudentRepository(activeUid).loadAsNormal()
                .addOnSuccessListener(freshStudent -> {
                    if (!isUiAlive() || freshStudent == null) {
                        return;
                    }
                    student = freshStudent;
                    sc.setStudent(freshStudent);
                    loadExploreCourses();
                })
                .addOnFailureListener(e -> {
                    if (!isUiAlive()) {
                        return;
                    }
                    Log.e("StudentExploreCourse", "Failed to refresh active student", e);
                    Tool.handleEmpty(true, rv_courses, tv_emptyCourses);
                });
    }

    private void loadExploreCourses() {
        Log.d("StudentExploreCourse", "Fetching Student's Explore Courses...");

        student.getExploreCourse().addOnSuccessListener(courses -> {
            if (!isUiAlive()) {
                return;
            }
            exploreCourses = courses;
            Log.d("StudentExploreCourse", "Retrieved " + exploreCourses.size() + " Courses");

            if (exploreCourses.isEmpty()) {
                Tool.handleEmpty(true, rv_courses, tv_emptyCourses);
                return;
            }

            loadTeachersForCourses();
        }).addOnFailureListener(e -> {
            if (!isUiAlive()) {
                return;
            }
            Log.e("StudentExploreCourse", "Failed to load explore courses", e);
            Tool.handleEmpty(true, rv_courses, tv_emptyCourses);
        });
    }

    private void loadTeachersForCourses() {
        teachers.clear();

        final int total = exploreCourses.size();
        final int[] completed = {0};
        for (int i = 0; i < total; i++) {
            teachers.add(null);
        }

        for (int i = 0; i < total; i++) {
            final int index = i;
            Course course = exploreCourses.get(i);
            course.getTeacher().addOnSuccessListener(teacher -> {
                if (!isUiAlive()) {
                    return;
                }
                teachers.set(index, teacher);
                completed[0]++;

                if (completed[0] == total) {
                    onAllDataLoaded();
                }
            }).addOnFailureListener(e -> {
                if (!isUiAlive()) {
                    return;
                }
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
        if (!isUiAlive()) {
            return;
        }
        Context context = getContext();
        if (context == null) {
            return;
        }
        Tool.handleEmpty(exploreCourses.isEmpty(), rv_courses, tv_emptyCourses);

        CourseAdapter courseAdapter = new CourseAdapter(
                exploreCourses,
                student,
                new OnItemClickListener<>() {
                    @Override
                    public void onItemClick(Course item) {
                        if (!isAdded()) {
                            return;
                        }
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
                context,
                false
        );

        rv_courses.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_courses.setAdapter(courseAdapter);
    }

    private boolean isUiAlive() {
        return isAdded() && getView() != null && rv_courses != null && tv_emptyCourses != null;
    }
}
