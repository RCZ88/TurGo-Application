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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeacherAddAgenda#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeacherAddAgenda extends Fragment {

    Spinner sp_AgendaOfCourse, sp_SelectStudent, sp_SelectMeeting;
    EditText etml_AgendaContents;
    Button btn_UploadImageAgenda, btn_SendAgenda;
    ToggleButton tb_AgendaFormat;
    ImageView iv_AgendaUploadedPreview;
    TextView tv_presetStudents, tv_presetCourses;
    Teacher teacher;

    private OnFileSelectedListener currentUploadCallback;
    private static final int FILE_PICKER_REQUEST_CODE = 100;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TeacherAddAgenda() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TeacherAddAgenda.
     */
    // TODO: Rename and change types and number of parameters
    public static TeacherAddAgenda newInstance(String param1, String param2) {
        TeacherAddAgenda fragment = new TeacherAddAgenda();
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
        // Inflate the layout for this fragment
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


        teacher = ((TeacherScreen)requireActivity()).getTeacher();
        AtomicReference<Uri> selectedFileUri = new AtomicReference<>();

        ArrayList<Student>presetStudents = new ArrayList<>();
        Course presetCourse;

        Bundle bundle = getArguments();
        boolean presetStudentCourse = bundle != null && bundle.containsKey("presetStudent") && bundle.containsKey("presentCourse");
        toggleView(presetStudentCourse);
        if(presetStudentCourse){
            presetStudents = (ArrayList<Student>) bundle.getSerializable("presetUser");
            StringBuilder studentsString = new StringBuilder();
            for(Student student : presetStudents){
                studentsString.append(student.getNickname()+", ");
            }
            tv_presetStudents.setText(studentsString.toString());
            presetCourse = (Course) bundle.getSerializable("presentCourse");
            tv_presetCourses.setText(presetCourse.getCourseName());
        } else {
            presetCourse = null;
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
            if(!isChecked){
                etml_AgendaContents.setVisibility(View.GONE);
                btn_UploadImageAgenda.setVisibility(View.VISIBLE);
                iv_AgendaUploadedPreview.setVisibility(View.VISIBLE);
            }else{
                btn_UploadImageAgenda.setVisibility(View.GONE);
                iv_AgendaUploadedPreview.setVisibility(View.GONE);
                etml_AgendaContents.setVisibility(View.VISIBLE);
            }
        });


        final Course[] courseSelected = new Course[1];
        sp_AgendaOfCourse.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, teacher.getCoursesTeach()));
        sp_AgendaOfCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseSelected[0] = (Course) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        final Student[] studentSelected = new Student[1];
        sp_SelectStudent.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, courseSelected[0].getStudents()));
        sp_SelectStudent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                studentSelected[0] = (Student) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        courseSelected[0].getSCofStudent(studentSelected[0], new ObjectCallBack<StudentCourse>() {
            @Override
            public void onObjectRetrieved(StudentCourse object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, java.lang.InstantiationException {
                sp_SelectMeeting.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, object.getAgendas()));
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });

        final Meeting[] selectedMeeting = new Meeting[1];
        sp_SelectMeeting.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMeeting[0] = (Meeting) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btn_UploadImageAgenda.setOnClickListener(v -> {
           openFilePicker();
        });


        ArrayList<Student> finalPresetStudents = presetStudents;
        btn_SendAgenda.setOnClickListener(v -> {
            Agenda agenda = null;
            if(tb_AgendaFormat.isChecked()){
                File file;

                try {
                    file = Tool.uriToFile(selectedFileUri.get(), requireActivity());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                final String[] secureUrl = {""};
                Tool.uploadToCloudinary(file, new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(String object) {
                        secureUrl[0] = object;
                    }

                    @Override
                    public void onError(DatabaseError error) {

                    }
                });
                file fileObj = new file(Tool.getFileName(requireActivity(), selectedFileUri.get()), secureUrl[0], teacher, LocalDateTime.now());
                if(presetStudentCourse){

                    for(Student student : finalPresetStudents){
                        agenda = new Agenda(fileObj, LocalDate.now(), selectedMeeting[0], teacher, student, presetCourse);
                        student.assignAgenda(agenda);
                        try {
                            agenda.updateDB();
                        } catch (NoSuchMethodException | InvocationTargetException |
                                 IllegalAccessException | java.lang.InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                        // todo: find a way to do this efficiently.
                    }
                }else{
                    agenda = new Agenda(fileObj, LocalDate.now(), selectedMeeting[0], teacher, studentSelected[0], courseSelected[0]);
                    studentSelected[0].assignAgenda(agenda);
                }


            }else{
                if(presetStudentCourse){
                    for(Student student : finalPresetStudents){
                        agenda = new Agenda(etml_AgendaContents.getText().toString(), LocalDate.now(), selectedMeeting[0], teacher, student, presetCourse);
                        student.assignAgenda( agenda);
                        try {
                            agenda.updateDB();
                        } catch (NoSuchMethodException | InvocationTargetException |
                                 IllegalAccessException | java.lang.InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                agenda = new Agenda(etml_AgendaContents.getText().toString(), LocalDate.now(), selectedMeeting[0], teacher, studentSelected[0], courseSelected[0]);
                studentSelected[0].assignAgenda(agenda);
            }
            try {
                if(!presetStudentCourse){
                    agenda.updateDB();
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                     java.lang.InstantiationException e) {
                throw new RuntimeException(e);
            }

        });

        return view;
    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // You can limit to "image/*" if only images
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"));
    }
    public void toggleView(boolean preset){
        if(preset){
            tv_presetStudents.setVisibility(View.VISIBLE);
            tv_presetCourses.setVisibility(View.VISIBLE);
            sp_SelectStudent.setVisibility(View.GONE);
            sp_AgendaOfCourse.setVisibility(View.GONE);
        }else{
            tv_presetStudents.setVisibility(View.GONE);
            tv_presetCourses.setVisibility(View.GONE);
            sp_SelectStudent.setVisibility(View.VISIBLE);
            sp_AgendaOfCourse.setVisibility(View.VISIBLE);
        }
    }


}