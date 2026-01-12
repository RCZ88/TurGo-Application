package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Student_MyCourses#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Student_MyCourses extends Fragment implements RequiresDataLoading{

    RecyclerView rv_myCourses;
    Student user;
    ArrayList<Teacher> teachersOfCourse;
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
        onDataLoaded(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_my_courses, container, false);
        StudentScreen activity = (StudentScreen) getActivity();
        assert activity != null;
        user = activity.getStudent();
        Log.d("Student_MyCourse(OnCreate)", "User Retrieved: "+ user);


        RecyclerView rv_myCourses = view.findViewById(R.id.rv_ListOfMyCourses);
        rv_myCourses.setLayoutManager(new LinearLayoutManager(requireContext()));

        ArrayList<Course> courses =  user.getCourseTaken();// Your method to get courses


        CourseAdapter adapter = new CourseAdapter(courses, user, new OnItemClickListener<>() {
            @Override
            public void onItemClick(Course item) {
                selectCourse(item);
            }

            @Override
            public void onItemLongClick(Course item) {

            }
        }, teachersOfCourse, requireContext());
        rv_myCourses.setAdapter(adapter);

        return view;
    }

    public void selectCourse(Course course){
//        Intent intent = new Intent(requireContext(), CourseJoinedFullPage.class);
//        intent.putExtra("SelectedCourse", course);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Course", course);
        user.getStudentCourseFromCourse(course, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(StudentCourse object) {
                bundle.putSerializable("StudentCourse", object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });


        CourseJoinedFullPage fragment = new CourseJoinedFullPage();
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nhf_ss_FragContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

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
        this.teachersOfCourse = (ArrayList<Teacher>)preloadedData.getSerializable(Teacher.SERIALIZE_KEY_CODE);

    }

    @Override
    public void onLoadingError(Exception error) {

    }
}