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

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeacherCourseScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeacherCourseScreen extends Fragment {
    SearchView sv_StudentSearch;
    Button btn_ViewSchedule, btn_AddAgenda, btn_AssignTask;
    RecyclerView rv_listOfStudent;
    Course course;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TeacherCourseScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TeacherCourseScreen.
     */
    // TODO: Rename and change types and number of parameters
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teacher_course_screen, container, false);
        sv_StudentSearch = view.findViewById(R.id.sv_TCS_SearchStudent);
        rv_listOfStudent = view.findViewById(R.id.rv_TCS_StudentList);
        btn_AddAgenda = view.findViewById(R.id.btn_TCS_AddAgenda);
        btn_AssignTask = view.findViewById(R.id.btn_TCS_AssignTask);
        btn_ViewSchedule = view.findViewById(R.id.btn_TCS_ViewSchedule);
        btn_ViewSchedule.setEnabled(true);
        Bundle args = getArguments();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            assert args != null;
            course = args.getSerializable("course", Course.class);
        }
        assert course != null;
        ArrayList<Student>students = course.getStudents();
        ArrayList<Student>studentSelectedInList = new ArrayList<>();
        STCAdapter adapter = new STCAdapter(students, getContext(), item -> {
            if(studentSelectedInList.contains(item)){
                studentSelectedInList.remove(item);
            }else{
                studentSelectedInList.add(item);
            }
            if(studentSelectedInList.size() > 1){
                btn_ViewSchedule.setEnabled(false);
            }else{
                btn_ViewSchedule.setEnabled(true);
            }
        });
        btn_AssignTask.setOnClickListener(v -> {
            TeacherCreateTask teacherCreateTask = new TeacherCreateTask();
            Bundle bundle = new Bundle();
            bundle.putSerializable("presetCourse", course);
            bundle.putSerializable("presetStudents", students);
            teacherCreateTask.setArguments(bundle);
            getParentFragmentManager().beginTransaction().replace(R.id.nhf_ts_FragmentContainer , teacherCreateTask).addToBackStack(null).commit();
        });
        btn_AddAgenda.setOnClickListener(v -> {
            TeacherAddAgenda teacherAddAgenda = new TeacherAddAgenda();
            Bundle bundle = new Bundle();
            bundle.putSerializable("presetCourse", course);
            bundle.putSerializable("presetStudents", students);
            teacherAddAgenda.setArguments(bundle);

        });
        btn_ViewSchedule.setOnClickListener(v -> {
            
        });
        rv_listOfStudent.setAdapter(adapter);
        return view;
    }
}