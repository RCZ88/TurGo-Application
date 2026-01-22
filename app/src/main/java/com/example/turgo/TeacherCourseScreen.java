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

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeacherCourseScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeacherCourseScreen extends Fragment implements RequiresDataLoading {
    SearchView sv_StudentSearch;
    Button btn_ViewSchedule, btn_AddAgenda, btn_AssignTask;
    RecyclerView rv_listOfStudent;
    TextView tv_courseName;
    Course course;

    ArrayList<Student>students;
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
        tv_courseName = view.findViewById(R.id.tv_TCS_CourseName);
        btn_ViewSchedule.setEnabled(true);
        Bundle args = getArguments();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            assert args != null;
            course = args.getSerializable(Course.SERIALIZE_KEY_CODE, Course.class);
        }

        tv_courseName.setText(course.getCourseName());
        Teacher teacher = ((TeacherScreen) requireActivity()).getTeacher();
        ArrayList<Student>studentSelectedInList = new ArrayList<>();
        STCAdapter adapter = new STCAdapter(students, getContext(), new OnItemClickListener<>() {
            @Override
            public void onItemClick(Student item) {
                if (studentSelectedInList.contains(item)) {
                    studentSelectedInList.remove(item);
                } else {
                    studentSelectedInList.add(item);
                }
                if (studentSelectedInList.size() > 1) {
                    btn_ViewSchedule.setEnabled(false);
                } else {
                    btn_ViewSchedule.setEnabled(true);
                }
            }

            @Override
            public void onItemLongClick(Student item) {

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
            Bundle bundle = new Bundle();
            bundle.putSerializable(Course.SERIALIZE_KEY_CODE, course);
            bundle.putSerializable(Student.SERIALIZE_KEY_CODE, students);
            DataLoading.loadAndNavigate(requireContext(), TeacherAddAgenda.class, bundle, true, TeacherScreen.class, teacher);
        });
        btn_ViewSchedule.setOnClickListener(v -> {

            Bundle bundle = new Bundle();
            bundle.putSerializable(Teacher.SERIALIZE_KEY_CODE, teacher);
            DataLoading.loadAndNavigate(requireContext(), TeacherScheduleOfCourse.class, bundle, true, TeacherScreen.class, teacher);
        });
        rv_listOfStudent.setAdapter(adapter);
        return view;
    }

    @Override
    public Bundle loadDataInBackground(Bundle input, DataLoading.ProgressCallback log) {
        Course course = (Course)input.getSerializable(Course.SERIALIZE_KEY_CODE);
        assert course != null;
        ArrayList<Student>students = Await.get(course::getStudents);
        Bundle bundle = new Bundle();
        bundle.putSerializable("students", students);
        return bundle;
    }

    @Override
    public void onDataLoaded(Bundle preloadedData) {
        students = (ArrayList<Student>) preloadedData.getSerializable("students");
    }

    @Override
    public void onLoadingError(Exception error) {

    }
}