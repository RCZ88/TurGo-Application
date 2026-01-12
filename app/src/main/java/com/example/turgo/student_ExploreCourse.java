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
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link student_ExploreCourse#newInstance} factory method to
 * create an instance of this fragment.
 */
public class student_ExploreCourse extends Fragment implements RequiresDataLoading {

    private Student student;
    private ArrayList<Teacher> teachers;
    private ArrayList<Course>exploreCourses;
    RecyclerView rv_courses;
    TextView tv_emptyCourses;
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
        onDataLoaded(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_explore_courses, container, false);

        rv_courses = view.findViewById(R.id.rv_exploreCourses);
        tv_emptyCourses = view.findViewById(R.id.tv_exploreCourseEmpty);

        StudentScreen sc = (StudentScreen) requireActivity();
        student = sc.getStudent();

        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public Bundle loadDataInBackground(Bundle input, TextView processLog) {
        Bundle bundle = new Bundle();
        Student student = (Student) input.getSerializable(Student.SERIALIZE_KEY_CODE);
        assert student != null;
        processLog.setText("Fetching Student's Explore Course...");
        ArrayList<Course> exploreCourse = Await.get(student::getExploreCourse);
        processLog.setText("Retrieving Teachers of Explore Courses...");
        ArrayList<Teacher>teachersOfCourse = exploreCourse
                .stream().map(course ->  Await.get(course::getTeacher))
                .collect(Collectors.toCollection(ArrayList::new));
        processLog.setText("Done, preparing to Load " + this.getClass().getSimpleName() + "...");
        bundle.putSerializable(Course.SERIALIZE_KEY_CODE, exploreCourse);
        bundle.putSerializable(Teacher.SERIALIZE_KEY_CODE, teachersOfCourse);
        return bundle;
    }

    @Override
    public void onDataLoaded(Bundle preloadedData) {
        this.exploreCourses = (ArrayList<Course>) preloadedData.getSerializable(Course.SERIALIZE_KEY_CODE);
        this.teachers = (ArrayList<Teacher>) preloadedData.getSerializable(Teacher.SERIALIZE_KEY_CODE);
        Tool.handleEmpty(exploreCourses.isEmpty(), rv_courses, tv_emptyCourses);
        CourseAdapter courseAdapter = new CourseAdapter
                (exploreCourses, student, new OnItemClickListener<>() {
                    @Override
                    public void onItemClick(Course item) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Course.SERIALIZE_KEY_CODE, item);
                        DataLoading.loadAndNavigate(requireContext(), CourseExploreFullPage.class, bundle, true, "StudentScreen");
                    }

                    @Override
                    public void onItemLongClick(Course item) {

                    }
                }, teachers, requireContext());
        rv_courses.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_courses.setAdapter(courseAdapter);
    }

    @Override
    public void onLoadingError(Exception error) {

    }
}