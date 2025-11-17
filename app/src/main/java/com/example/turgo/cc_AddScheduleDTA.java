package com.example.turgo;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link cc_AddScheduleDTA#newInstance} factory method to
 * create an instance of this fragment.
 */
public class cc_AddScheduleDTA extends Fragment {


    Button btn_selectEarliest, btn_selectLatest, btn_addDTA;
    Spinner sp_selectDay;
    TimePicker tp_pickTime;
    CheckBox cb_limit;
    Boolean earlyOrLatest;
    LocalTime earliest = null;
    LocalTime latest = null;
    DayOfWeek day = null;
    boolean limitMeetingBool = false;
    EditText et_meetingLimit;
    int meetingLimit = 0;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public cc_AddScheduleDTA() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChooseScheduleStudent.
     */
    // TODO: Rename and change types and number of parameters
    public static cc_AddScheduleDTA newInstance(String param1, String param2) {
        cc_AddScheduleDTA fragment = new cc_AddScheduleDTA();
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
        View view = inflater.inflate(R.layout.fragment_cc_add_schedule_dta, container, false);
        CreateCourse cc = (CreateCourse) requireActivity();
        btn_addDTA = view.findViewById(R.id.btn_CC_AddDTA);
        btn_selectEarliest = view.findViewById(R.id.btn_CC_EarlyTimeDTA);
        btn_selectLatest = view.findViewById(R.id.btn_CC_LatestTimeDTA);
        sp_selectDay = view.findViewById(R.id.sp_CC_DayOfWeek);
        tp_pickTime = view.findViewById(R.id.tp_CC_PickTime);

        btn_selectEarliest.setOnClickListener(view1 -> {
            earlyOrLatest = true;
            showTimePicker();
        });
        btn_selectLatest.setOnClickListener(view2 -> {
            earlyOrLatest = false;
            showTimePicker();
        });
        sp_selectDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = adapterView.getItemAtPosition(i).toString();
                DayOfWeek dow = DayOfWeek.valueOf(selected.toUpperCase());
                day = dow;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        btn_addDTA.setOnClickListener(view3 -> {
            if(earliest != null && latest != null && day != null){
                DayTimeArrangement dta = new DayTimeArrangement(cc.teacher, cc.course, day, earliest, latest, meetingLimit);
                cc.dtas.add(dta);
            }
        });
        cb_limit.setOnCheckedChangeListener((compoundButton, b) -> {
            limitMeetingBool = b;
            if(b){
                et_meetingLimit.setEnabled(false);
                et_meetingLimit.setText("");
                et_meetingLimit.setHint("Unlimited");
                et_meetingLimit.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray));
            }else{
                et_meetingLimit.setEnabled(true);
                et_meetingLimit.setHint("Max students per meeting");
                et_meetingLimit.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.black));
            }
        });
        et_meetingLimit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                meetingLimit = Integer.parseInt(String.valueOf(editable));
            }
        });
        return view;
    }

    public void showTimePicker(){
        tp_pickTime.setVisibility(View.VISIBLE);
        AtomicReference<String> time = new AtomicReference<>("");
        tp_pickTime.setOnTimeChangedListener((timePicker, i, i1) -> {
            time.set(String.format(Locale.getDefault(), "%02d:%02d", i, i1));
            if(earlyOrLatest){
                btn_selectEarliest.setText(time.get());
                earliest = LocalTime.of(i, i1);
            }else{
                btn_selectLatest.setText(time.get());
                latest = LocalTime.of(i, i1);
            }
        });

    }
}