package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.time.DayOfWeek;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeacherScheduleOfCourse#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeacherScheduleOfCourse extends Fragment {
    RecyclerView rv_schedules;
    Spinner sp_dayOptions;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TeacherScheduleOfCourse() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TeacherScheduleOfCourse.
     */
    // TODO: Rename and change types and number of parameters
    public static TeacherScheduleOfCourse newInstance(String param1, String param2) {
        TeacherScheduleOfCourse fragment = new TeacherScheduleOfCourse();
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
        View view = inflater.inflate(R.layout.fragment_teacher_schedule_of_course, container, false);
        rv_schedules = view.findViewById(R.id.rv_TSOC_Schedules);
        sp_dayOptions = view.findViewById(R.id.sp_TSOC_FilterDay);
        Teacher teacher =((TeacherScreen) getActivity()).getTeacher();

        TeacherScheduleAdapter adapter = new TeacherScheduleAdapter(teacher.getAllSchedule());
        rv_schedules.setAdapter(adapter);

        ArrayList<DayOfWeek>days = new ArrayList<>();
        for(DayTimeArrangement dta : teacher.getTimeArrangements()){
            days.add(dta.getDay());
        }
        sp_dayOptions.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, days));
        sp_dayOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adapter.scheduleList = teacher.getSchedulesOfDay(days.get(position));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                adapter.scheduleList = teacher.getAllSchedule();
            }
        });

        return view;
    }
}