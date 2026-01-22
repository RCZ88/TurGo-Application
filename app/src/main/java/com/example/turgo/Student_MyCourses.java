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
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Student_MyCourses#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Student_MyCourses extends Fragment implements RequiresDataLoading{

    RecyclerView rv_myCourses;
    TextView tv_noCourse;
    Button btn_goToExplore;
    Student user;
    ArrayList<Teacher> teachersOfCourse = new ArrayList<>();
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
            onDataLoaded(getArguments());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_my_courses, container, false);
        StudentScreen activity = (StudentScreen) getActivity();
        assert activity != null;
        user = activity.getStudent();
        Log.d("Student_MyCourse(OnCreate)", "User Retrieved: "+ user);

        tv_noCourse = view.findViewById(R.id.tv_smc_NoCoursesJoined);
        btn_goToExplore = view.findViewById(R.id.btn_smc_NavigateExploreCourse);

        rv_myCourses = view.findViewById(R.id.rv_ListOfMyCourses);
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
        Tool.handleEmpty(courses.isEmpty(), rv_myCourses, tv_noCourse);
        if(courses.isEmpty()){
            btn_goToExplore.setVisibility(View.VISIBLE);
        }else{
            btn_goToExplore.setVisibility(View.GONE);
        }
        btn_goToExplore.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Student.SERIALIZE_KEY_CODE, user);
            DataLoading.loadAndNavigate(requireContext(), Student_ExploreCourse.class, bundle, true, StudentScreen.class, user);
        });
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

    @SuppressLint("SetTextI18n")
    @Override
    public Bundle loadDataInBackground(Bundle input, DataLoading.ProgressCallback processLog) {
        Bundle bundle = new Bundle();
        Student student = (Student) input.getSerializable(Student.SERIALIZE_KEY_CODE);
        assert student != null;
        processLog.onProgress("Fetching Student's Courses Taken...");
        ArrayList<Course> courseTaken = student.getCourseTaken();
        processLog.onProgress("Retrieving Teachers of Courses Taken...");
        ArrayList<Teacher>teachersOfCourse = courseTaken
                .stream().map(course ->  Await.get(course::getTeacher))
                .collect(Collectors.toCollection(ArrayList::new));
        processLog.onProgress("Done, preparing to Load " + this.getClass().getSimpleName() + "...");
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