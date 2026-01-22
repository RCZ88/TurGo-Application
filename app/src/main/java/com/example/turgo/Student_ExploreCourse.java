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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Student_ExploreCourse#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Student_ExploreCourse extends Fragment implements RequiresDataLoading {

    private Student student;
    private ArrayList<Teacher> teachers = new ArrayList<>();
    private ArrayList<Course>exploreCourses = new ArrayList<>();
    RecyclerView rv_courses;
    TextView tv_emptyCourses;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Student_ExploreCourse() {
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_explore_courses, container, false);

        rv_courses = view.findViewById(R.id.rv_exploreCourses);
        tv_emptyCourses = view.findViewById(R.id.tv_exploreCourseEmpty);

        StudentScreen sc = (StudentScreen) requireActivity();
        student = sc.getStudent();
        if (getArguments() != null) {
            onDataLoaded(getArguments());
        }
        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public Bundle loadDataInBackground(Bundle input, DataLoading.ProgressCallback processLog) {
        Bundle bundle = new Bundle();
        Student student = (Student) input.getSerializable(Student.SERIALIZE_KEY_CODE);
        assert student != null;
        processLog.onProgress("Fetching Student's Explore Course...");
        ArrayList<Course> exploreCourse = Await.get(student::getExploreCourse);
        Log.d("StudentExploreCourse", "Retrieved "+ exploreCourse.size()+ " Courses");
        for(Course course : exploreCourse){
            Log.d("StudentExploreCourse", "- "+ course.toString());
        }
        processLog.onProgress("Retrieving Teachers of Explore Courses...");
        ArrayList<Teacher> teachersOfCourse = new ArrayList<>();
        for (Course course : exploreCourse) {
            Teacher teacher = Await.get(course::getTeacher);
            Log.d("StudentExploreCourse", "Teacher Retrieved:" + teacher);
            teachersOfCourse.add(teacher);
        }
        Log.d("StudentExploreCourse", "Teachers Mapping on Course: ");
        for(Teacher teacher : teachersOfCourse){
            Log.d("StudentExploreCourse", "- " + teacher);
        }
        processLog.onProgress("Done, preparing to Load " + this.getClass().getSimpleName() + "...");
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
                        Log.d("selectedBottomNav", "on Student_ExploreCourse: "+ Student_ExploreCourse.class.getSimpleName());
                        DataLoading.loadAndNavigate(requireContext(), CourseExploreFullPage.class, bundle, true, StudentScreen.class, student, StudentScreen.getMenuIdForFragment(Student_ExploreCourse.class.getSimpleName()));
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