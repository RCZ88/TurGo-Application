package com.example.turgo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link cc_CourseInformation#newInstance} factory method to
 * create an instance of this fragment.
 */
public class cc_CourseInformation extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private EditText et_CourseName, etml_CourseDescription;
    private Spinner sp_CourseTypes;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public cc_CourseInformation() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateCourse_1.
     */
    // TODO: Rename and change types and number of parameters
    public static cc_CourseInformation newInstance(String param1, String param2) {
        cc_CourseInformation fragment = new cc_CourseInformation();
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
        View view = inflater.inflate(R.layout.fragment_cc_course_information, container, false);
        CreateCourse cc = (CreateCourse) requireActivity();
        et_CourseName = view.findViewById(R.id.et_CC_courseName);
        etml_CourseDescription = view.findViewById(R.id.etml_CC_CourseDescription);
        sp_CourseTypes = view.findViewById(R.id.sp_CC_CourseType);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference(FirebaseNode.COURSETYPE.getPath());
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<CourseTypeFirebase> courseTypes = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    courseTypes.add(dataSnapshot.getValue(CourseTypeFirebase.class));
                }
                sp_CourseTypes.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, courseTypes));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(!cc.courseName.isEmpty()){
            et_CourseName.setText(cc.courseName);
        }
        if(!cc.courseDescription.isEmpty()){
            etml_CourseDescription.setText(cc.courseDescription);
        }
//        if(!cc.courseType.isEmpty()){
//
//        }
        et_CourseName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                cc.courseName = et_CourseName.getText().toString();
            }
        });
        etml_CourseDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                cc.courseDescription = etml_CourseDescription.getText().toString();
            }
        });
        sp_CourseTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cc.courseType = (CourseType) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        return view;
    }
}