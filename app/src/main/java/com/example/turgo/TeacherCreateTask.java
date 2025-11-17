package com.example.turgo;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeacherCreateTask#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeacherCreateTask extends Fragment {
    EditText et_taskTitle, et_taskDescription;
    Spinner sp_taskOfCourse, sp_studentSelect;
    Button btn_selectSubmissionDate, btn_createTask;
    CheckBox cb_openDropbox;
    TextView tv_selectedDate, tv_presetStudentsSelected, tv_presetCourseSelected;
    Teacher teacher;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TeacherCreateTask() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TeacherCreateTask.
     */
    // TODO: Rename and change types and number of parameters
    public static TeacherCreateTask newInstance(String param1, String param2) {
        TeacherCreateTask fragment = new TeacherCreateTask();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Teacher teacher = ((TeacherScreen) requireActivity()).getTeacher();

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teacher_create_task, container, false);
        et_taskTitle = view.findViewById(R.id.et_TCT_TaskTitle);
        et_taskDescription = view.findViewById(R.id.etml_TCT_TaskDescription);
        sp_studentSelect = view.findViewById(R.id.sp_TCT_SelectStudent);
        sp_taskOfCourse = view.findViewById(R.id.sp_TCT_TaskOfCourse);
        btn_selectSubmissionDate = view.findViewById(R.id.btn_TCT_SelectDate);
        cb_openDropbox = view.findViewById(R.id.cb_TCT_OpenDropbox);
        btn_createTask = view.findViewById(R.id.btn_TCT_CreateTask);
        tv_presetStudentsSelected = view.findViewById(R.id.tv_TCK_PresetStudent);
        tv_presetCourseSelected = view.findViewById(R.id.tv_TCK_PresetCourse);

        teacher = ((TeacherScreen) requireActivity()).getTeacher();

        boolean presetTask = false;


        final Course[] courseSelected = {(Course) sp_taskOfCourse.getSelectedItem()};
        final Student[] studentSelected = {(Student) sp_studentSelect.getSelectedItem()};
        assert getActivity() != null;
        ArrayAdapter<Course> courseAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, teacher.getCoursesTeach());

        final ArrayList<Student>studentSelectedList = new ArrayList<>();
        Course coursePreset = null;


        Bundle args = getArguments();
        boolean preset = args != null && args.containsKey("presetStudent") && args.containsKey("presetCourse");
        if (preset) {

            if(args.getSerializable("presetStudent") instanceof ArrayList){
                ArrayList<Student> students = (ArrayList<Student>) args.getSerializable("studentSelectedMenu");
                studentSelectedList.addAll(students);
                StringBuilder listOfStudents = new StringBuilder();
                for(Student s : students){
                    listOfStudents.append(s.getNickname()).append(", ");
                }
                tv_presetStudentsSelected.setText(listOfStudents);
            }else{
                Student studentFrom = (Student) args.getSerializable("presetStudent");
                studentSelectedList.add(studentFrom);
                assert studentFrom != null;
                tv_presetStudentsSelected.setText(studentFrom.getNickname());
            }
            coursePreset = (Course)args.getSerializable("presetCourse");
            tv_presetCourseSelected.setText(coursePreset.getCourseName());
        }

        sp_taskOfCourse.setAdapter(courseAdapter);
        sp_taskOfCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseSelected[0] = (Course) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_studentSelect.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, courseSelected[0].getStudents()));
        sp_studentSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                studentSelected[0] = (Student)parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_selectSubmissionDate.setOnClickListener(v -> openDatePicker());


        Course finalCoursePreset = coursePreset;
        btn_createTask.setOnClickListener(v -> {
            boolean completeForm;
            if(!presetTask){
                completeForm = !et_taskTitle.getText().toString().isEmpty() && !et_taskDescription.getText().toString().isEmpty() && sp_taskOfCourse.getSelectedItem() != null && sp_studentSelect.getSelectedItem() != null;
            }else{
                completeForm = !et_taskTitle.getText().toString().isEmpty() && !et_taskDescription.getText().toString().isEmpty() && sp_taskOfCourse.getSelectedItem() != null;
            }

            if (completeForm) {
                String taskTitle = et_taskTitle.getText().toString();
                String taskDescription = et_taskDescription.getText().toString();
                Course course = (Course) sp_taskOfCourse.getSelectedItem();
                if(course == null){
                    course = finalCoursePreset;
                }
                Student student = (Student) sp_studentSelect.getSelectedItem();
                if(student != null){
                    studentSelectedList.add(student);
                }
                LocalDateTime submissionDate = LocalDateTime.parse(tv_selectedDate.getText().toString());
                studentSelectedList.add(student);
                Task task = new Task(taskTitle, studentSelectedList , taskDescription, submissionDate, course, null, teacher, cb_openDropbox.isChecked());
                for(Student s : studentSelectedList){
                    try {
                        s.assignTask(task);
                    } catch (InvocationTargetException | NoSuchMethodException |
                             IllegalAccessException | java.lang.InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }else{
                Toast.makeText(getActivity(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    tv_selectedDate.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }
    private void toggleView(boolean preset){
        if(preset){
            sp_studentSelect.setVisibility(View.GONE);
            tv_presetStudentsSelected.setVisibility(View.VISIBLE);
            sp_taskOfCourse.setVisibility(View.GONE);
            tv_presetCourseSelected.setVisibility(View.VISIBLE);
        }else{
            sp_studentSelect.setVisibility(View.VISIBLE);
            tv_presetStudentsSelected.setVisibility(View.GONE);
            sp_taskOfCourse.setVisibility(View.VISIBLE);
            tv_presetCourseSelected.setVisibility(View.GONE);
        }
    }
}