package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TeacherCourseScreen extends Fragment {

    SearchView sv_StudentSearch;
    Button btn_ViewSchedule, btn_AddAgenda, btn_AssignTask, btn_ViewTasks;
    RecyclerView rv_listOfStudent;
    TextView tv_courseName, tv_selectedCount;
    Course course;
    ArrayList<Student> students = new ArrayList<>();
    ArrayList<Student> selectedStudents = new ArrayList<>();
    Set<String> selectedStudentIds = new HashSet<>();
    boolean isSelectionMode = false;
    LinearLayout ll_empty;
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
        btn_ViewTasks = view.findViewById(R.id.btn_TCS_ViewTasks);
        tv_courseName = view.findViewById(R.id.tv_TCS_CourseName);
        tv_selectedCount = view.findViewById(R.id.tv_TCS_SelectedCount);
        ll_empty = view.findViewById(R.id.ll_TCS_EmptyState);
        btn_ViewSchedule.setEnabled(true);
        updateSelectedCountText();

        Bundle args = getArguments();
        if(Tool.boolOf(args)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                course = args.getSerializable(Course.SERIALIZE_KEY_CODE, Course.class);
            } else {
                course = (Course) args.getSerializable(Course.SERIALIZE_KEY_CODE);
            }
        }



        if (course == null) {
            Log.e("TeacherCourseScreen", "Course argument is missing.");
            Tool.handleEmpty(true, rv_listOfStudent, ll_empty);
            return view;
        }

        tv_courseName.setText(course.getCourseName());

        btn_AssignTask.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("presetCourse", course);
            ArrayList<Student> studentsForAction = getStudentsForAction();
            bundle.putSerializable("presetStudent", studentsForAction);
            bundle.putSerializable("studentSelectedMenu", studentsForAction);
            TeacherCreateTask teacherCreateTask = new TeacherCreateTask();
            teacherCreateTask.setArguments(bundle);
            Tool.loadFragment(requireActivity(), TeacherScreen.getContainerId(), teacherCreateTask);
        });

        btn_AddAgenda.setOnClickListener(v ->{
            Bundle bundle = new Bundle();
            bundle.putSerializable("presetCourse", course);
            ArrayList<Student> studentsForAction = getStudentsForAction();
            bundle.putSerializable("presetStudent", studentsForAction);
            bundle.putSerializable("studentSelectedMenu", studentsForAction);
            Tool.loadFragment(requireActivity(), TeacherScreen.getContainerId(), new TeacherAddAgenda(), bundle);
        });

        btn_ViewSchedule.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Course.SERIALIZE_KEY_CODE, course);
            Tool.loadFragment(requireActivity(), TeacherScreen.getContainerId(), new TeacherScheduleOfCourse(), bundle);
        });

        btn_ViewTasks.setOnClickListener(v -> {
            TeacherCourseTasksFragment fragment = TeacherCourseTasksFragment.newInstance(course);
            Tool.loadFragment(requireActivity(), TeacherScreen.getContainerId(), fragment);
        });

        course.getStudents().addOnSuccessListener(students ->{
            Log.d("TeacherCourseScreen", "Students Count: "  + students.size());
            ArrayList<Student>studentsList = students.isEmpty() ? new ArrayList<>() : (ArrayList<Student>) students;
            this.students.clear();
            this.students.addAll(studentsList);
            this.selectedStudents.clear();
            this.selectedStudentIds.clear();
            this.isSelectionMode = false;
            updateSelectedCountText();
            final STCAdapter[] adapterRef = new STCAdapter[1];
            STCAdapter adapter = new STCAdapter(studentsList, getContext(), new OnItemClickListener<>() {
                @Override
                public void onItemClick(Student item) {
                    if (!isSelectionMode) {
                        return;
                    }
                    toggleStudentSelection(item);
                    btn_ViewSchedule.setEnabled(selectedStudents.size() <= 1);
                    updateSelectedCountText();
                    if (adapterRef[0] != null) {
                        adapterRef[0].notifyDataSetChanged();
                    }
                }

                @Override
                public void onItemLongClick(Student item) {
                    if (!isSelectionMode) {
                        isSelectionMode = true;
                    }
                    toggleStudentSelection(item);
                    btn_ViewSchedule.setEnabled(selectedStudents.size() <= 1);
                    updateSelectedCountText();
                    if (adapterRef[0] != null) {
                        adapterRef[0].notifyDataSetChanged();
                    }
                }
            }, selectedStudentIds);
            adapterRef[0] = adapter;

            rv_listOfStudent.setLayoutManager(new LinearLayoutManager(requireContext()));
            rv_listOfStudent.setAdapter(adapter);
            Tool.handleEmpty(studentsList.isEmpty(), rv_listOfStudent,ll_empty );
        }).addOnFailureListener(error -> {
            Log.e("TeacherCourseScreen", "Failed to load students for course: " + course.getID(), error);
            Tool.handleEmpty(true, rv_listOfStudent, ll_empty);
        });
        return view;
    }

    private ArrayList<Student> getStudentsForAction() {
        return selectedStudents.isEmpty() ? new ArrayList<>(students) : new ArrayList<>(selectedStudents);
    }

    private void toggleStudentSelection(Student item) {
        String key = resolveStudentId(item);
        if (!Tool.boolOf(key)) {
            return;
        }
        if (selectedStudentIds.contains(key)) {
            selectedStudentIds.remove(key);
            selectedStudents.removeIf(student -> key.equals(resolveStudentId(student)));
        } else {
            selectedStudentIds.add(key);
            if (!containsStudentById(selectedStudents, key)) {
                selectedStudents.add(item);
            }
        }
        if (selectedStudentIds.isEmpty()) {
            isSelectionMode = false;
        }
        updateSelectedCountText();
    }

    private boolean containsStudentById(ArrayList<Student> studentList, String key) {
        for (Student student : studentList) {
            if (key.equals(resolveStudentId(student))) {
                return true;
            }
        }
        return false;
    }

    private String resolveStudentId(Student student) {
        if (student == null) {
            return "";
        }
        if (Tool.boolOf(student.getUid())) {
            return student.getUid();
        }
        return Tool.boolOf(student.getID()) ? student.getID() : "";
    }

    private void updateSelectedCountText() {
        if (tv_selectedCount == null) {
            return;
        }
        int count = selectedStudents == null ? 0 : selectedStudents.size();
        if (count == 1) {
            tv_selectedCount.setText("1 student selected");
            return;
        }
        tv_selectedCount.setText(count + " students selected");
    }
}
