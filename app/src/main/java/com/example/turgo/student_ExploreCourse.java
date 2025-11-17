package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link student_ExploreCourse#newInstance} factory method to
 * create an instance of this fragment.
 */
public class student_ExploreCourse extends Fragment {

    private Student student;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public student_ExploreCourse() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment navigation_explore.
     */
    // TODO: Rename and change types and number of parameters
    public static student_ExploreCourse newInstance(String param1, String param2) {
        student_ExploreCourse fragment = new student_ExploreCourse();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_explore_courses, container, false);
        StudentScreen sc = (StudentScreen) requireActivity();
        StudentFirebase sf = sc.getStudent();
        try {
            student = sf.convertToNormal();
        } catch (NoSuchMethodException | java.lang.InstantiationException | IllegalAccessException |
                 InvocationTargetException | ParseException e) {
            throw new RuntimeException(e);
        }
        ArrayList<Course> coursesInterested = student.getExploreCourse();
        CourseAdapter courseAdapter = new CourseAdapter(coursesInterested, student, item -> {
            CourseExploreFullPage fragment = new CourseExploreFullPage();
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nhf_ss_FragContainer, fragment).addToBackStack(null).commit();
        }, requireContext());
        RecyclerView rv_courses = view.findViewById(R.id.rv_exploreCourses);
        rv_courses.setAdapter(courseAdapter);
        return view;
    }
}