package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TeacherCourseScreen extends Fragment {

    SearchView sv_StudentSearch;
    Button btn_ViewSchedule, btn_AddAgenda, btn_AssignTask;
    RecyclerView rv_listOfStudent;
    TextView tv_courseName;
    Course course;
    ArrayList<Student> students = new ArrayList<>();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public TeacherCourseScreen() {
    }

    public static TeacherCourseScreen newInstance(String param1, String param2) {
        TeacherCourseScreen fragment = new TeacherCourseScreen();
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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_teacher_course_screen, container, false);
        sv_StudentSearch = view.findViewById(R.id.sv_TCS_SearchStudent);
        rv_listOfStudent = view.findViewById(R.id.rv_TCS_StudentList);
        btn_AddAgenda = view.findViewById(R.id.btn_TCS_AddAgenda);
        btn_AssignTask = view.findViewById(R.id.btn_TCS_AssignTask);
        btn_ViewSchedule = view.findViewById(R.id.btn_TCS_ViewSchedule);
        tv_courseName = view.findViewById(R.id.tv_TCS_CourseName);

        btn_ViewSchedule.setEnabled(true);

        Bundle args = getArguments();
        assert args != null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            course = args.getSerializable(Course.SERIALIZE_KEY_CODE, Course.class);
        } else {
            course = (Course) args.getSerializable(Course.SERIALIZE_KEY_CODE);
        }

        assert course != null;
        tv_courseName.setText(course.getCourseName());

        Teacher teacher = ((TeacherScreen) requireActivity()).getTeacher();

        // Load students synchronously using Await
        course.getStudents().addOnSuccessListener(students ->{
            ArrayList<Student> studentSelectedInList = new ArrayList<>();

            STCAdapter adapter = new STCAdapter((ArrayList<Student>) students, getContext(), new OnItemClickListener<>() {
                @Override
                public void onItemClick(Student item) {
                    if (studentSelectedInList.contains(item)) {
                        studentSelectedInList.remove(item);
                    } else {
                        studentSelectedInList.add(item);
                    }

                    btn_ViewSchedule.setEnabled(studentSelectedInList.size() <= 1);
                }

                @Override
                public void onItemLongClick(Student item) {
                }
            });

            rv_listOfStudent.setAdapter(adapter);

            btn_AssignTask.setOnClickListener(v -> {
                TeacherCreateTask teacherCreateTask = new TeacherCreateTask();
                Bundle bundle = new Bundle();
                bundle.putSerializable("presetCourse", course);
                bundle.putSerializable("presetStudents", (ArrayList<Student>) students);
                teacherCreateTask.setArguments(bundle);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nhf_ts_FragmentContainer, teacherCreateTask)
                        .addToBackStack(null)
                        .commit();
            });

            btn_AddAgenda.setOnClickListener(v -> {
                Tool.loadFragment(requireActivity(), TeacherScreen.getContainerId(), new TeacherAddAgenda());
            });

            btn_ViewSchedule.setOnClickListener(v -> {
                Tool.loadFragment(requireActivity(), TeacherScreen.getContainerId(), new TeacherScheduleOfCourse());
            });

        });
        return view;
    }
}
