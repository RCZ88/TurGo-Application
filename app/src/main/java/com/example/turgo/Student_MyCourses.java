package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Student_MyCourses#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Student_MyCourses extends Fragment {

    RecyclerView rv_myCourses;
    Student user;
    Course courseClicked = null;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Student_MyCourses() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment student_myCourses.
     */
    // TODO: Rename and change types and number of parameters
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_my_courses, container, false);
        StudentScreen activity = (StudentScreen) getActivity();
        assert activity != null;
        StudentFirebase studentFirebase = activity.getStudent();

        try {
            this.user = studentFirebase.convertToNormal();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 java.lang.InstantiationException | ParseException e) {
            throw new RuntimeException(e);
        }
        RecyclerView recyclerView = view.findViewById(R.id.rv_ListOfMyCourses);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ArrayList<Course> courses =  user.getCourseTaken();// Your method to get courses
        CourseAdapter adapter = new CourseAdapter(courses, user, this::selectCourse, requireContext());
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void selectCourse(Course course){
//        Intent intent = new Intent(requireContext(), CourseJoinedFullPage.class);
//        intent.putExtra("SelectedCourse", course);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Course", course);
        bundle.putSerializable("StudentCourse", user.getStudentCourseFromCourse(course));

        CourseJoinedFullPage fragment = new CourseJoinedFullPage();
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nhf_ss_FragContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }
}