package com.example.turgo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.database.DatabaseError;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class TeacherAddAgenda extends Fragment {

    Spinner sp_AgendaOfCourse, sp_SelectStudent, sp_SelectMeeting;
    EditText etml_AgendaContents;
    Button btn_UploadImageAgenda, btn_SendAgenda;
    ToggleButton tb_AgendaFormat;
    ImageView iv_AgendaUploadedPreview;
    TextView tv_presetStudents, tv_presetCourses;
    Teacher teacher;

    ArrayList<Student> finalPresetStudents = new ArrayList<>();
    Course presetCourse;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    public TeacherAddAgenda() {}

    public static TeacherAddAgenda newInstance(String param1, String param2) {
        TeacherAddAgenda fragment = new TeacherAddAgenda();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_teacher_add_agenda, container, false);

        sp_AgendaOfCourse = view.findViewById(R.id.sp_TAA_AgendaOfCourse);
        sp_SelectStudent = view.findViewById(R.id.sp_TAA_StudentSelect);
        sp_SelectMeeting = view.findViewById(R.id.sp_TAA_SelectMeeting);
        etml_AgendaContents = view.findViewById(R.id.etml_TAA_AgendaContent);
        btn_UploadImageAgenda = view.findViewById(R.id.btn_TAA_UploadAgendaImage);
        btn_SendAgenda = view.findViewById(R.id.btn_TAA_SendAgenda);
        tb_AgendaFormat = view.findViewById(R.id.tb_TAA_AgendaFormat);
        iv_AgendaUploadedPreview = view.findViewById(R.id.iv_TAA_AgendaUploadedPreview);
        tv_presetStudents = view.findViewById(R.id.tv_TAA_PresetStudents);
        tv_presetCourses = view.findViewById(R.id.tv_TAA_PresetCourse);

        teacher = ((TeacherScreen) requireActivity()).getTeacher();
        AtomicReference<Uri> selectedFileUri = new AtomicReference<>();

        Bundle bundle = getArguments();
        boolean presetStudentCourse = bundle != null
                && bundle.containsKey("presetStudent")
                && bundle.containsKey("presetCourse");

        toggleView(presetStudentCourse);

        if (presetStudentCourse) {
            presetCourse = (Course) bundle.getSerializable("presetCourse");
            finalPresetStudents = (ArrayList<Student>) bundle.getSerializable("presetStudent");

            StringBuilder studentsString = new StringBuilder();
            for (Student student : finalPresetStudents) {
                studentsString.append(student.getNickname()).append(", ");
            }
            tv_presetStudents.setText(studentsString.toString());
            tv_presetCourses.setText(presetCourse.getCourseName());
        }

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedFileUri.set(result.getData().getData());
                        iv_AgendaUploadedPreview.setImageURI(selectedFileUri.get());
                    }
                });

        btn_UploadImageAgenda.setVisibility(View.GONE);
        tb_AgendaFormat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                etml_AgendaContents.setVisibility(View.GONE);
                btn_UploadImageAgenda.setVisibility(View.VISIBLE);
                iv_AgendaUploadedPreview.setVisibility(View.VISIBLE);
            } else {
                btn_UploadImageAgenda.setVisibility(View.GONE);
                iv_AgendaUploadedPreview.setVisibility(View.GONE);
                etml_AgendaContents.setVisibility(View.VISIBLE);
            }
        });

        final Course[] courseSelected = new Course[1];
        sp_AgendaOfCourse.setAdapter(new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                teacher.getCoursesTeach()));

        sp_AgendaOfCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseSelected[0] = (Course) parent.getSelectedItem();
                loadStudentsForCourse(courseSelected[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        final Student[] studentSelected = new Student[1];
        sp_SelectStudent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                studentSelected[0] = (Student) parent.getSelectedItem();
                loadMeetings(courseSelected[0], studentSelected[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        final Meeting[] selectedMeeting = new Meeting[1];
        sp_SelectMeeting.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMeeting[0] = (Meeting) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btn_UploadImageAgenda.setOnClickListener(v -> openFilePicker());

        btn_SendAgenda.setOnClickListener(v -> {

            if (selectedMeeting[0] == null) return;

            if (tb_AgendaFormat.isChecked()) {
                handleFileAgenda(selectedFileUri.get(), selectedMeeting[0], studentSelected[0], courseSelected[0], presetStudentCourse);
            } else {
                handleTextAgenda(selectedMeeting[0], studentSelected[0], courseSelected[0], presetStudentCourse);
            }
        });

        return view;
    }

    private void loadStudentsForCourse(Course course) {
        course.getStudents().addOnSuccessListener(students -> {
            finalPresetStudents = (ArrayList<Student>) students;
            sp_SelectStudent.setAdapter(new ArrayAdapter<>(requireActivity(),
                    android.R.layout.simple_spinner_dropdown_item,
                    finalPresetStudents));
        });
    }

    private void loadMeetings(Course course, Student student) {
        course.getSCofStudent(student, new ObjectCallBack<StudentCourse>() {
            @Override
            public void onObjectRetrieved(StudentCourse object) {
                sp_SelectMeeting.setAdapter(new ArrayAdapter<>(requireActivity(),
                        android.R.layout.simple_spinner_dropdown_item,
                        object.getAgendas()));
            }

            @Override
            public void onError(DatabaseError error) {}
        });
    }

    private void handleFileAgenda(Uri uri, Meeting meeting, Student student, Course course, boolean presetStudentCourse) {
        if (uri == null) return;

        File file;
        try {
            file = Tool.uriToFile(uri, requireActivity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Tool.uploadToCloudinary(file, new ObjectCallBack<String>() {
            @Override
            public void onObjectRetrieved(String secureUrl) {

                file fileObj = new file(
                        Tool.getFileName(requireActivity(), uri),
                        secureUrl,
                        teacher,
                        LocalDateTime.now()
                );

                if (presetStudentCourse) {
                    for (Student s : finalPresetStudents) {
                        Agenda agenda = new Agenda(fileObj, LocalDate.now(), meeting, teacher, s, presetCourse.getID());
                        s.assignAgenda(agenda);
                        updateAgenda(agenda);
                    }
                } else {
                    Agenda agenda = new Agenda(fileObj, LocalDate.now(), meeting, teacher, student, course.getCourseID());
                    student.assignAgenda(agenda);
                    updateAgenda(agenda);
                }
            }

            @Override
            public void onError(DatabaseError error) {}
        });
    }

    private void handleTextAgenda(Meeting meeting, Student student, Course course, boolean presetStudentCourse) {

        String content = etml_AgendaContents.getText().toString();

        if (presetStudentCourse) {
            for (Student s : finalPresetStudents) {
                Agenda agenda = new Agenda(content, LocalDate.now(), meeting, teacher, s, presetCourse.getCourseID());
                s.assignAgenda(agenda);
                updateAgenda(agenda);
            }
        } else {
            Agenda agenda = new Agenda(content, LocalDate.now(), meeting, teacher, student, course.getCourseID());
            student.assignAgenda(agenda);
            updateAgenda(agenda);
        }
    }

    private void updateAgenda(Agenda agenda) {
        try {
            agenda.updateDB();
        } catch (NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException | java.lang.InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"));
    }

    public void toggleView(boolean preset) {
        if (preset) {
            tv_presetStudents.setVisibility(View.VISIBLE);
            tv_presetCourses.setVisibility(View.VISIBLE);
            sp_SelectStudent.setVisibility(View.GONE);
            sp_AgendaOfCourse.setVisibility(View.GONE);
        } else {
            tv_presetStudents.setVisibility(View.GONE);
            tv_presetCourses.setVisibility(View.GONE);
            sp_SelectStudent.setVisibility(View.VISIBLE);
            sp_AgendaOfCourse.setVisibility(View.VISIBLE);
        }
    }
}
