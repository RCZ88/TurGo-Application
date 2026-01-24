package com.example.turgo;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RC_UserInformation#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RC_UserInformation extends Fragment implements checkFragmentCompletion {

    EditText et_school, et_grade, et_reasonForJoining;
    Button btn_saveToStudent, btn_autofill;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    static final String viewTitle = "User Details & Information";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RC_UserInformation() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RC_UserInformation.
     */
    // TODO: Rename and change types and number of parameters
    public static RC_UserInformation newInstance(String param1, String param2) {
        RC_UserInformation fragment = new RC_UserInformation();
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
        View view = inflater.inflate(R.layout.fragment_register__user_information, container, false);
        et_grade = view.findViewById(R.id.et_EducationStudent);
        et_school = view.findViewById(R.id.et_SchoolNameInput);
        et_reasonForJoining = view.findViewById(R.id.etml_ReasonForJoining);
        btn_autofill = view.findViewById(R.id.btn_rui_autofill);
        btn_saveToStudent = view.findViewById(R.id.btn_saveToProfile);
        RegisterCourse rc = (RegisterCourse) getActivity();
        assert rc != null;
        rc.tv_title.setText(viewTitle);
        et_grade.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                rc.setEducationGrade(et_grade.getText().toString());
            }
        });
        et_school.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                rc.setSchool(et_school.getText().toString());
            }
        });
        et_reasonForJoining.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                rc.setReasonForJoining(et_school.getText().toString());
            }
        });
        Student student = rc.getStudent();
        btn_saveToStudent.setOnClickListener(v -> {
            if(et_school.getText().length() != 0 && et_grade.getText().length() != 0){
                new AlertDialog.Builder(requireContext())
                        .setTitle("Confirm")
                        .setMessage("Replace/Set default School & Grade for your Account!")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // ✅ YES ACTION
                            StudentRepository studentRepository = StudentRepository.getInstance(student.getID());
                            student.setSchool(et_school.getText().toString());
                            studentRepository.updateSchool(et_school.getText().toString());
                            student.setGradeLevel(et_grade.getText().toString());
                            studentRepository.updateGradeLevel(et_grade.getText().toString());
                            Toast.makeText(requireContext(), "School & Grade saved Successfully!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }else{
                Toast.makeText(requireContext(), "Please Fill in School & Grades before Saving.", Toast.LENGTH_SHORT).show();
            }
        });
        btn_autofill.setOnClickListener(v->{
            if(Tool.boolOf(student.getSchool()) && Tool.boolOf(student.getGradeLevel())){
                et_school.setText(student.getSchool());
                et_grade.setText(student.getGradeLevel());
            }
        });
        return view;
    }

    @Override
    public boolean checkIfCompleted() {
        return  !(et_grade.getText().toString().isEmpty() ||
                et_school.getText().toString().isEmpty() ||
                et_reasonForJoining.getText().toString().isEmpty());
    }
}