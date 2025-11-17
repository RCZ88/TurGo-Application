package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeacherScheduleList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeacherScheduleList extends Fragment {
    SwitchCompat sc_Modes, sc_TimeFrame;
    RecyclerView rv_schedules;
    TextView tv_noSchedules;

    TeacherScreen ts = (TeacherScreen) getActivity();
    Teacher teacher = ts.getTeacher();

    DayScheduleAdapter dsa;
    DayMeetingAdapter dma;
    AtomicReference<RecyclerView.Adapter> adapter;
    AtomicBoolean currentMode;
    private static final boolean WEEKLY = false;
    private static final boolean DAILY = true;
    private static final boolean SCHEDULE = false;
    private static final boolean MEETING = true;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TeacherScheduleList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TeacherAllCourse.
     */
    // TODO: Rename and change types and number of parameters
    public static TeacherScheduleList newInstance(String param1, String param2) {
        TeacherScheduleList fragment = new TeacherScheduleList();
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

        boolean isMeetingMode = savedInstanceState.getBoolean("isMeetingMode");
        boolean isDaily = savedInstanceState.getBoolean("isDaily");

        sc_Modes.setChecked(isMeetingMode);
        sc_TimeFrame.setChecked(isDaily);

        updateRecyclerView(isMeetingMode, isDaily);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teacher_schedule_list, container, false);
/*        rg_weeklyDailyOptions = view.findViewById(R.id.rg_TSL_DailyWeeklySelect);
        cb_meetingMode = view.findViewById(R.id.cb_TSL_ShowAll);*/
        rv_schedules = view.findViewById(R.id.rv_TSL_DaySchedules);
        tv_noSchedules = view.findViewById(R.id.tv_TSL_ScheduleEmpty);
        sc_Modes = view.findViewById(R.id.sc_TSL_Mode);
        sc_TimeFrame = view.findViewById(R.id.sc_TSL_Timeframe);
        currentMode = new AtomicBoolean(sc_Modes.isChecked());



//        rg_weeklyDailyOptions.check(R.id.rb_TSL_WeeklyOpt);
//        cb_meetingMode.setChecked(false);



        dsa = new DayScheduleAdapter(teacher, sc_TimeFrame.isChecked());
        dma = new DayMeetingAdapter(teacher, sc_TimeFrame.isChecked());
        adapter = new AtomicReference<>(dsa);


        sc_Modes.setOnCheckedChangeListener((compoundButton, b) -> {
            updateRecyclerView(b, sc_TimeFrame.isChecked());
        });
        sc_TimeFrame.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateRecyclerView(currentMode.get(), isChecked);
        });

        rv_schedules.setAdapter(adapter.get());

        return view;
    }
    public static boolean isThisWeek(LocalDate date) {
        LocalDate now = LocalDate.now();

        // Get the Monday of this week
        LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);

        // Get the Sunday of this week
        LocalDate endOfWeek = now.with(DayOfWeek.SUNDAY);

        // Check if date is within that range (inclusive)
        return !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek);
    }

    public void updateRecyclerView(boolean isMeetingMode, boolean isDaily){
        if(isMeetingMode == SCHEDULE){
            rv_schedules.setAdapter(dsa);
            adapter.set(dsa);
            currentMode.set(SCHEDULE);
        }else if(isMeetingMode == MEETING){
            rv_schedules.setAdapter(dma);
            adapter.set(dma);
            currentMode.set(MEETING);
        }

        ModeUpdatable mu = (ModeUpdatable) adapter.get();
        mu.updateMode(teacher, isDaily);
        updateEmptyView(mu);

        updateEmptyView((ModeUpdatable)adapter.get());
    }
    private void updateEmptyView(ModeUpdatable adapter) {
        if (adapter.isEmpty()) {
            rv_schedules.setVisibility(View.INVISIBLE);
            tv_noSchedules.setVisibility(View.VISIBLE);
        } else {
            rv_schedules.setVisibility(View.VISIBLE);
            tv_noSchedules.setVisibility(View.GONE);
        }
    }

}