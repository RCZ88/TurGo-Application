package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link signup_UserCategory#newInstance} factory method to
 * create an instance of this fragment.
 */
public class signup_UserCategory extends Fragment implements checkFragmentCompletion{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RadioGroup rg_userCategories;
    boolean segmentComplete = false;
    public signup_UserCategory() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment signup_UserCategory.
     */
    // TODO: Rename and change types and number of parameters
    public static signup_UserCategory newInstance(String param1, String param2) {
        signup_UserCategory fragment = new signup_UserCategory();
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
        View view = inflater.inflate(R.layout.fragment_signup__user_category, container, false);
        rg_userCategories = view.findViewById(R.id.rg_userOptions);
        SignUpPage signUpPage = (SignUpPage) getActivity();
        // Initialize RadioGroup
        rg_userCategories = view.findViewById(R.id.rg_userOptions);


        rg_userCategories.setOnCheckedChangeListener((radioGroup, i) -> {
            assert signUpPage != null;
            Log.d("signup_UserCategory", "This segment is completed!");
            segmentComplete = true;
            RadioButton selectedOption = view.findViewById(i);
            String selected = selectedOption.getText().toString();

            switch(selected){
                case "Student":
                    signUpPage.signUpSegments[SignUpPage.MODSEGMENT1] = new signup__student_selectcourse();
                    signUpPage.userType = UserType.STUDENT;
                    break;
                case "Parent":
                    signUpPage.signUpSegments[SignUpPage.MODSEGMENT1] = new signup__parent_connecttochild();
                    signUpPage.userType = UserType.PARENT;
                    break;
                case "Teacher":
                    signUpPage.signUpSegments[SignUpPage.MODSEGMENT1] = new signup_UserDetails_Teacher();
                    signUpPage.userType = UserType.TEACHER;
                    break;
                case "Admin":
                    signUpPage.signUpSegments[SignUpPage.MODSEGMENT1] = new signup_admin_empty();
                    signUpPage.userType = UserType.ADMIN;
                    break;
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public boolean checkIfCompleted() {
        return segmentComplete;
    }
}