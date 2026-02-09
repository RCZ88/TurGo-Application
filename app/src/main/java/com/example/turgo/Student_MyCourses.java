package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Student_MyCourses extends Fragment {

    RecyclerView rv_myCourses;
    TextView tv_noCourse;
    Button btn_goToExplore;
    LinearLayout ll_empty;
    Student user;
    ArrayList<Teacher> teachersOfCourse = new ArrayList<>();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Student_MyCourses() {
        // Required empty public constructor
    }

    public static Student_MyCourses newInstance(String param1, String param2) {
        Student_MyCourses fragment = new Student_MyCourses();
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
            // No data loading needed here anymore
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_student_my_courses, container, false);

        StudentScreen activity = (StudentScreen) getActivity();
        assert activity != null;
        user = activity.getStudent();
        Log.d("Student_MyCourse(OnCreate)", "User Retrieved: " + user);

        tv_noCourse = view.findViewById(R.id.tv_smc_NoCoursesJoined);
        btn_goToExplore = view.findViewById(R.id.btn_smc_NavigateExploreCourse);
        rv_myCourses = view.findViewById(R.id.rv_ListOfMyCourses);
        ll_empty = view.findViewById(R.id.ll_smc_EmptyState);

        rv_myCourses.setLayoutManager(new LinearLayoutManager(requireContext()));

        ArrayList<Course> courses = user.getCourseTaken();

        Tool.handleEmpty(courses.isEmpty(), rv_myCourses, ll_empty);

        if (!courses.isEmpty()) {
            loadTeachersAndSetupAdapter(courses);
        }

        btn_goToExplore.setOnClickListener(v -> {
            Tool.loadFragment(requireActivity(), StudentScreen.getContainer(), new Student_ExploreCourse());
        });

        return view;
    }

    private void loadTeachersAndSetupAdapter(ArrayList<Course> courses) {
        teachersOfCourse.clear();

        AtomicInteger remaining = new AtomicInteger(courses.size());

        for (Course course : courses) {
            course.getTeacher()
                    .addOnSuccessListener(teacher -> {
                        teachersOfCourse.add(teacher);
                        if (remaining.decrementAndGet() == 0) {
                            setupAdapter(courses);
                        }
                    })
                    .addOnFailureListener(e -> {
                        teachersOfCourse.add(null);
                        if (remaining.decrementAndGet() == 0) {
                            setupAdapter(courses);
                        }
                    });
        }
    }

    private void setupAdapter(ArrayList<Course> courses) {
        CourseAdapter adapter = new CourseAdapter(courses, user, new OnItemClickListener<>() {
            @Override
            public void onItemClick(Course item) {
                selectCourse(item);
            }

            @Override
            public void onItemLongClick(Course item) {
                // No-op
            }
        }, teachersOfCourse, requireContext());

        rv_myCourses.setAdapter(adapter);
    }

    public void selectCourse(Course course) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Course.SERIALIZE_KEY_CODE, course);
        course.getTeacher().addOnSuccessListener(t ->{
            bundle.putSerializable(Teacher.SERIALIZE_KEY_CODE, t);
            Tool.loadFragment(requireActivity(), StudentScreen.getContainer(), new CourseJoinedFullPage(), bundle);
        });
    }
}
