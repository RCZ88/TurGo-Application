package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.time.DayOfWeek;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RC_AvailableDayTime#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RC_AvailableDayTime extends Fragment {
    TextView tv_daysTitle, tv_timeTitle;
    RecyclerView rv_listOfDays, rv_listOfTS;
    Button btn_add;
    HashMap<TimeSlot, Integer>tsAmountPeople;
    int amtPerWeek;
    RegisterCourse rc;
    ArrayList<DayOfWeek> daysAlreadySelected;
    final int[] cycleCount = {1};
    final TimeSlot[] currentSelectedTS = new TimeSlot[1];
    final int[] currentAmtPpl = new int[1];
    final boolean[] selected = {false};


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RC_AvailableDayTime() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RC_AvailableDay.
     */
    // TODO: Rename and change types and number of parameters
    public static RC_AvailableDayTime newInstance(String param1, String param2) {
        RC_AvailableDayTime fragment = new RC_AvailableDayTime();
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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register__available_day, container, false);
        tv_daysTitle = view.findViewById(R.id.tv_SelectDayTitle);
        rv_listOfDays = view.findViewById(R.id.rv_ListOfAvailDays);
        tv_timeTitle = view.findViewById(R.id.tv_SelectTimeTitle);
        rv_listOfTS = view.findViewById(R.id.rv_ListOfAvailTimes);
        btn_add = view.findViewById(R.id.btn_Add);
        rc = (RegisterCourse) getActivity();
        daysAlreadySelected = new ArrayList<>();
        tsAmountPeople = new HashMap<>();
        assert rc != null;

        amtPerWeek = rc.getAmountOfMeetingPerWeek();
        updateUI();
        btn_add.setOnClickListener(view1 -> {
            if(selected[0]){
                if(cycleCount[0] < amtPerWeek){
                    Toast.makeText(getContext(), "Schedule Successfully Added!", Toast.LENGTH_SHORT).show();
                    tsAmountPeople.put(currentSelectedTS[0], currentAmtPpl[0]);
                    cycleCount[0]++;
                    if(cycleCount[0] == amtPerWeek){
                        rc.setTimeSlotPeopleAmountSelected(tsAmountPeople);
                    }
                    updateUI();
                }else{
                    Log.i("Course Day Time Selection", "you have selected all of the days!");
                    btn_add.setActivated(false);
                }
            }
        });
        // Inflate the layout for this fragment
        return view;
    }
    public void updateUI(){
        String title = "Select Day " + cycleCount[0] + " of " + amtPerWeek + " Per Week";
        tv_daysTitle.setText(title);
        title = "Select Time For Day " + cycleCount[0];
        tv_timeTitle.setText(title);
        Course course = rc.getCourse();
        ArrayList<DayOfWeek> days = course.filterFullDays(rc.isPrivate(), rc.getDuration());
        if(!daysAlreadySelected.isEmpty()){
            for(DayOfWeek day : daysAlreadySelected){
                days.remove(day);
            }
        }

        MutableLiveData<DayOfWeek> currentSelectedDay = new MutableLiveData<>();
        DayAdapter dayAdapter = new DayAdapter(days, item -> {
            currentSelectedDay.setValue(item);
            ArrayList<TimeSlot>ts = rc.getCourse().getDTAOfDay(currentSelectedDay.getValue()).splitSlots(rc.getDuration(), rc.getCourse().findFreeSpotOfDay(currentSelectedDay.getValue(), rc.isPrivate(), rc.getCourse().getMaxStudentPerMeeting(), rc.getDuration()));
            TimeSlotAdapter tsa = new TimeSlotAdapter(ts, item1 -> {
                currentSelectedTS[0] = item1;
                currentAmtPpl[0] = course.getScheduleFromTimeSlot(item1).getNumberOfStudents();
                selected[0] = true;
            }, currentSelectedDay.getValue());
            rv_listOfTS.setAdapter(tsa);
        });
        rv_listOfDays.setAdapter(dayAdapter);
    }
}