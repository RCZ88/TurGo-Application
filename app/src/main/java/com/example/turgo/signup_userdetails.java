package com.example.turgo;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.Calendar;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link signup_userdetails#newInstance} factory method to
 * create an instance of this fragment.
 */
public class signup_userdetails extends Fragment implements checkFragmentCompletion{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    EditText et_fullName, et_nickname, et_dateOfBirth;
    Spinner sp_latestEducation;
    Switch sw_gender;
    EditText[]userDetailsText;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public signup_userdetails() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_signup_userdetails.
     */
    // TODO: Rename and change types and number of parameters
    public static signup_userdetails newInstance(String param1, String param2) {
        signup_userdetails fragment = new signup_userdetails();
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
        View view = inflater.inflate(R.layout.fragment_signup_userdetails, container, false);
        et_fullName = view.findViewById(R.id.ti_FullName);
        et_nickname = view.findViewById(R.id.ti_Nickname);
        et_dateOfBirth = view.findViewById(R.id.ti_DateOfBirth);
        userDetailsText = new EditText[]{et_fullName, et_nickname, et_dateOfBirth};
        sw_gender = view.findViewById(R.id.switch_Gender);
        sp_latestEducation = view.findViewById(R.id.spin_LatEdu);
        setupSPLE();

        et_dateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int Year, int Month, int Day) { //1 = Year 2 = Month 3 = Day
                        String formattedDate = Day + "/" + (Month + 1) + "/" + Year;
                        et_dateOfBirth.setText(formattedDate);
                    }
                }, year, month, day);

                datePickerDialog.show();
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    private void setupSPLE(){ //setup sp_latestEducation
        ArrayAdapter<CharSequence>adapter = ArrayAdapter.createFromResource(requireContext(), R.array.latest_education,  androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        sp_latestEducation.setAdapter(adapter);
    }

    @Override
    public boolean checkIfCompleted() {
        for(EditText et : userDetailsText){
            if(et.getText().toString().isEmpty()){
                return false;
            }
        }
        final boolean[] nothingSelected = {false};
        sp_latestEducation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                nothingSelected[0] = true;
            }
        });
        return !nothingSelected[0];
    }
}