package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourseExploreFullPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourseExploreFullPage extends Fragment implements RequiresDataLoading{

    Button btn_joinCourse;
    TextView tv_courseTitle, tv_courseDays, tv_courseDescription, tv_teacherName, tv_teacherDescription;
    Course course;
    Teacher teacher;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CourseExploreFullPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CourseExploreDetails.
     */
    // TODO: Rename and change types and number of parameters
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        onDataLoaded(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore_course_full_page, container, false);
        tv_courseTitle = view.findViewById(R.id.tv_ecfp_CourseTitle);
        btn_joinCourse = view.findViewById(R.id.btn_JoinCourse);
        tv_courseDays = view.findViewById(R.id.tv_CourseAvailableDays);
        tv_courseDescription = view.findViewById(R.id.tv_CourseDescription);
        tv_teacherDescription = view.findViewById(R.id.tv_TeacherDescription);
        tv_teacherName = view.findViewById(R.id.tv_TeacherName);
        assert getActivity() != null;
        Intent intent = getActivity().getIntent();
        course = (Course) intent.getSerializableExtra("Selected Course");
        assert course != null;
        tv_courseTitle.setText(course.getCourseName());
        tv_courseDescription.setText(course.getCourseDescription());
        tv_courseDays.setText(course.getDaysAvailable());
//        course.getTeacher(new ObjectCallBack<>() {
//            @Override
//            public void onObjectRetrieved(Teacher object) {
//
//
//            }
//
//            @Override
//            public void onError(DatabaseError error) {
//
//            }
//        });



        tv_teacherName.setText(teacher.getFullName());
        tv_teacherDescription.setText(teacher.getTeacherResume());

        btn_joinCourse.setOnClickListener(view1 -> {
            Intent intent1 = new Intent(getContext(), RegisterCourse.class);
            intent1.putExtra("Student", ((StudentScreen)getContext()).getStudent());
            intent1.putExtra(Course.SERIALIZE_KEY_CODE, course);
        });

        return view;
    }

    @Override
    public Bundle loadDataInBackground(Bundle input, TextView logLoading) {
        Bundle output = new Bundle();
        Course course =  (Course)input.getSerializable(Course.SERIALIZE_KEY_CODE);
        if(course != null){
            Teacher teacher = Await.get(course::getTeacher);
            output.putSerializable(Teacher.SERIALIZE_KEY_CODE, teacher);
        }
        return output;
    }

    @Override
    public void onDataLoaded(Bundle preloadedData) {
        teacher = (Teacher)preloadedData.getSerializable(Teacher.SERIALIZE_KEY_CODE);
    }

    @Override
    public void onLoadingError(Exception error) {

    }
}