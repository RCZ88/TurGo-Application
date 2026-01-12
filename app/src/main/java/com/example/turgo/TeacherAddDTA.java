package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeacherAddDTA#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeacherAddDTA extends Fragment {
    Button btn_selectStart, btn_selectEnd, btn_selectDay, btn_addDTA;
    EditText et_maxMeeting;
    Spinner sp_selectCourse;
    TextView tv_start, tv_end, tv_day;
    Teacher teacher;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TeacherAddDTA() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TeacherAddSchedule.
     */
    // TODO: Rename and change types and number of parameters
    public static TeacherAddDTA newInstance(String param1, String param2) {
        TeacherAddDTA fragment = new TeacherAddDTA();
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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teacher_add_dta, container, false);
        btn_selectStart = view.findViewById(R.id.btn_TAD_SelectStartTime);
        btn_selectEnd = view.findViewById(R.id.btn_TAD_SelectEndTime);
        btn_selectDay = view.findViewById(R.id.btn_TAD_SelectDayOfDTA);
        btn_addDTA = view.findViewById(R.id.btn_TAD_AddDTA);
        et_maxMeeting = view.findViewById(R.id.etn_TAD_SelectMaxMeeting);
        sp_selectCourse = view.findViewById(R.id.sp_TAD_SelectCourse);
        tv_start = view.findViewById(R.id.tv_TAD_StartTimeSelected);
        tv_end = view.findViewById(R.id.tv_TAD_EndTimeSelected);
        tv_day = view.findViewById(R.id.tv_TAD_DaySelected);
        final Course[] courseSelected = new Course[1];
        sp_selectCourse.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, teacher.getCoursesTeach()));
        sp_selectCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseSelected[0] = teacher.getCoursesTeach().get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_selectStart.setOnClickListener(view1 -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Start Time")
                    .build();
            picker.addOnPositiveButtonClickListener(view12 ->{
                int hour = picker.getHour();
                int minute = picker.getMinute();
                String formattedTime = String.format("%02d:%02d", hour, minute);
                tv_start.setText(formattedTime);
            });
        });
        btn_selectEnd.setOnClickListener(view13 -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select End Time")
                    .build();
            picker.addOnPositiveButtonClickListener(view14 ->{
                int hour = picker.getHour();
                int minute = picker.getMinute();
                String formattedTime = String.format("%02d:%02d", hour, minute);
                tv_end.setText(formattedTime);
            });
        });

        btn_selectDay.setOnClickListener(view15 -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Day")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                tv_day.setText(datePicker.getHeaderText());
            });
            datePicker.show(getParentFragmentManager(), "tag");
        });
        btn_addDTA.setOnClickListener(view16 -> {
            if(tv_start.getText().toString().equals("") || tv_end.getText().toString().equals("") || tv_day.getText().toString().equals("")){
                //show error
                Toast.makeText(getContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
            }else{
                //add DTA
                DayTimeArrangement dta = new DayTimeArrangement(courseSelected[0], DayOfWeek.valueOf(tv_day.getText().toString()), LocalTime.parse(tv_start.getText().toString()), LocalTime.parse(tv_end.getText().toString()), Integer.parseInt(et_maxMeeting.getText().toString()));
                courseSelected[0].addNewDayTimeArr(dta);
                try {
                    teacher.addDTA(dta);
                    dta.updateDB();
                } catch (InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | java.lang.InstantiationException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        return view;
    }
}