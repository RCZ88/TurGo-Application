package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeacherAddDTA#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeacherAddDTA extends Fragment {
    Button btn_selectStart, btn_selectEnd, btn_selectDay, btn_addDTA;
    EditText et_maxMeeting;
    Spinner sp_selectCourse;
    TextView tv_start, tv_end, tv_day, tv_limitStatus;
    SwitchMaterial sw_noLimit;
    Teacher teacher;
    private LocalTime selectedStartTime;
    private LocalTime selectedEndTime;
    private DayOfWeek selectedDay;

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
        tv_limitStatus = view.findViewById(R.id.tv_TAD_LimitStatus);
        sw_noLimit = view.findViewById(R.id.sw_TAD_NoLimitToggle);
        final Course[] courseSelected = new Course[1];
        teacher = ((TeacherScreen)requireActivity()).getTeacher();
        selectedStartTime = null;
        selectedEndTime = null;
        selectedDay = null;
        tv_start.setText("--:--");
        tv_end.setText("--:--");
        tv_day.setText("Not selected");
        updateMaxMeetingUi(false);

        ArrayList<Course> teacherCourses = teacher != null && teacher.getCoursesTeach() != null
                ? teacher.getCoursesTeach()
                : new ArrayList<>();
        SimpleSpinnerAdapter<Course> courseAdapter = new SimpleSpinnerAdapter<>(
                requireContext(),
                teacherCourses,
                course -> {
                    if (course == null || !Tool.boolOf(course.getCourseName())) {
                        return "Unnamed Course";
                    }
                    return course.getCourseName();
                }
        );
        sp_selectCourse.setAdapter(courseAdapter);
        sp_selectCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseSelected[0] = (Course) parent.getSelectedItem();
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
                selectedStartTime = LocalTime.of(hour, minute);
                tv_start.setText(formattedTime);
            });
            picker.show(getParentFragmentManager(), "start_time_picker");
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
                selectedEndTime = LocalTime.of(hour, minute);
                tv_end.setText(formattedTime);
            });
            picker.show(getParentFragmentManager(), "end_time_picker");
        });

        btn_selectDay.setOnClickListener(view15 -> {
            DayOfWeek[] days = DayOfWeek.values();
            String[] labels = new String[days.length];
            for (int i = 0; i < days.length; i++) {
                String raw = days[i].name().toLowerCase();
                labels[i] = Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Select Day")
                    .setItems(labels, (dialog, which) -> {
                        selectedDay = days[which];
                        tv_day.setText(labels[which]);
                    })
                    .show();
        });
        btn_addDTA.setOnClickListener(view16 -> {
            if (courseSelected[0] == null || selectedStartTime == null || selectedEndTime == null || selectedDay == null) {
                Toast.makeText(getContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!selectedEndTime.isAfter(selectedStartTime)) {
                Toast.makeText(getContext(), "End time must be after start time", Toast.LENGTH_SHORT).show();
                return;
            }

            int maxMeeting = Integer.MAX_VALUE;
            if (!sw_noLimit.isChecked()) {
                if (!Tool.boolOf(et_maxMeeting.getText().toString().trim())) {
                    Toast.makeText(getContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    maxMeeting = Integer.parseInt(et_maxMeeting.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Max meeting must be a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (maxMeeting <= 0) {
                    Toast.makeText(getContext(), "Max meeting must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            DayTimeArrangement dta = new DayTimeArrangement(courseSelected[0], selectedDay, selectedStartTime, selectedEndTime, maxMeeting);
            courseSelected[0].addNewDayTimeArr(dta);
            try {
                teacher.addDTA(dta);
                dta.updateDB();
                Toast.makeText(getContext(), "DTA added successfully", Toast.LENGTH_SHORT).show();
            } catch (InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException | java.lang.InstantiationException e) {
                throw new RuntimeException(e);
            }
        });

        sw_noLimit.setOnCheckedChangeListener((buttonView, isChecked) -> updateMaxMeetingUi(isChecked));

        return view;
    }

    private void updateMaxMeetingUi(boolean unlimited) {
        if (tv_limitStatus != null) {
            tv_limitStatus.setText(unlimited ? "ON" : "OFF");
        }
        if (et_maxMeeting != null) {
            et_maxMeeting.setEnabled(!unlimited);
            et_maxMeeting.setFocusable(!unlimited);
            et_maxMeeting.setFocusableInTouchMode(!unlimited);
            et_maxMeeting.setAlpha(unlimited ? 0.5f : 1.0f);
            if (unlimited) {
                et_maxMeeting.setText("");
                et_maxMeeting.setHint("Unlimited");
            } else {
                et_maxMeeting.setHint("5");
            }
        }
    }
}
