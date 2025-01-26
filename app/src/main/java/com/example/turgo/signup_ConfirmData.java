package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.ParseException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link signup_ConfirmData#newInstance} factory method to
 * create an instance of this fragment.
 */
public class signup_ConfirmData extends Fragment implements checkFragmentCompletion{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Button btn_SignUp;
    TextView tv_userData;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public signup_ConfirmData() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment signup_ConfirmData.
     */
    // TODO: Rename and change types and number of parameters
    public static signup_ConfirmData newInstance(String param1, String param2) {
        signup_ConfirmData fragment = new signup_ConfirmData();
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
        View view = inflater.inflate(R.layout.fragment_signup__confirm_data, container, false);
        SignUpPage sup = (SignUpPage) getActivity();
        tv_userData = view.findViewById(R.id.tv_userInfoSummary);
        assert sup != null;
        tv_userData.setText(sup.createUser().toString());
        btn_SignUp = view.findViewById(R.id.btn_SignUp);
        btn_SignUp.setOnClickListener(v ->{
            try {
                sup.signUp();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public boolean checkIfCompleted() {
        return true;
    }
}