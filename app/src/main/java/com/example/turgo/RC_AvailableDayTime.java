package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RC_AvailableDayTime#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RC_AvailableDayTime extends Fragment implements checkFragmentCompletion {
    static final String viewName = "Select Schedule Day & Time Slot";
    TextView tv_daysTitle, tv_timeTitle, tv_dayEmpty, tv_timeEmpty, tv_schedulesEmpty;
    RecyclerView rv_listOfDays, rv_listOfTS, rv_scheduleAdded;
    Button btn_add;
    HashMap<TimeSlot, Integer>tsAmountPeople;
    int amtPerWeek;
    RegisterCourse rc;
    ArrayList<DayOfWeek> daysAlreadySelected;
    SeekBar sb_timeOffset;
    TextView tv_offsetText;
    final int[] cycleCount = {1};
    final TimeSlot[] currentSelectedTS = new TimeSlot[1];
    final int[] currentAmtPpl = new int[1];
    final boolean[] selected = {false};
    TimeSlotViewHolder tsvh;
    DayViewHolder dvh;
    ArrayList<TimeSlot> selectedTimeSlots, timeSlotsOfShiftMinute = new ArrayList<>();
    MutableLiveData<DayOfWeek> currentSelectedDay = new MutableLiveData<>();
    RcScheduleAdapter scheduleAdapter;
    DayAdapter dayAdapter;

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
        rv_listOfDays.setLayoutManager(new LinearLayoutManager(getContext()));
        tv_timeTitle = view.findViewById(R.id.tv_SelectTimeTitle);
        rv_listOfTS = view.findViewById(R.id.rv_ListOfAvailTimes);
        rv_listOfTS.setLayoutManager(new LinearLayoutManager(getContext()));
        sb_timeOffset = view.findViewById(R.id.sb_rcad_TimeOffset);
        tv_offsetText = view.findViewById(R.id.tv_rcad_timeOffsetText);
        rv_scheduleAdded = view.findViewById(R.id.rv_rcad_scheduleSelected);
        rv_scheduleAdded.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        cycleCount[0] = 1;

        selectedTimeSlots = new ArrayList<>();
        scheduleAdapter = new RcScheduleAdapter(selectedTimeSlots);
        scheduleAdapter.setClickListener(new OnItemClickListener<>() {
            @Override
            public void onItemClick(Integer item) {
                daysAlreadySelected.remove(scheduleAdapter.getSlots().get(item).getDay());
                scheduleAdapter.removeSlot(item);
                cycleCount[0]--;
                updateUI();
            }

            @Override
            public void onItemLongClick(Integer item) {

            }
        });
        rv_scheduleAdded.setAdapter(scheduleAdapter);

        tv_schedulesEmpty = view.findViewById(R.id.tv_rcad_scheduleSelectedEmpty);
        tv_dayEmpty = view.findViewById(R.id.tv_rcad_dayNotFound);
        tv_timeEmpty = view.findViewById(R.id.tv_rcad_timeListEmpty);

        btn_add = view.findViewById(R.id.btn_rsad_AddScheduleTS);
        rc = (RegisterCourse) getActivity();
        assert rc != null;
        rc.tv_title.setText(viewName);
        daysAlreadySelected = new ArrayList<>();
        tsAmountPeople = new HashMap<>();
        assert rc != null;

        int maxIncrease = rc.getDuration()/15 - 1;
        sb_timeOffset.setMin(0);
        sb_timeOffset.setMax(maxIncrease);
        Log.d("Slider", "Max: " + sb_timeOffset.getMax());

        Tool.handleEmpty(true, rv_listOfTS, tv_timeEmpty);
        Tool.handleEmpty(false, rv_listOfDays, tv_dayEmpty);
        Tool.handleEmpty(true, rv_scheduleAdded, tv_schedulesEmpty);


        amtPerWeek = rc.getAmountOfMeetingPerWeek();
        updateUI();
        btn_add.setOnClickListener(view1 -> {
            if(selected[0]){
                if(cycleCount[0] <= amtPerWeek){

                    tsAmountPeople.put(currentSelectedTS[0], currentAmtPpl[0]);
                    if(selectedTimeSlots == null){
                        selectedTimeSlots = new ArrayList<>();
                    }
                    scheduleAdapter.addSlot(currentSelectedTS[0]);
                    Tool.handleEmpty(scheduleAdapter.getSlots().isEmpty(), rv_scheduleAdded, tv_schedulesEmpty);

                    cycleCount[0]++;
                    if(cycleCount[0] == amtPerWeek){
                        rc.setSelectedTS(scheduleAdapter.getSlots());
                    }
                    daysAlreadySelected.add(currentSelectedDay.getValue());
                    rc.getDowSelected().add(currentSelectedDay.getValue());
                    Log.d("Days Selected", "Days Selected: " + daysAlreadySelected);
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

    @SuppressLint("NotifyDataSetChanged")
    public void updateUI(){
        Log.d("updateUI", "CycleCount: " + cycleCount[0] + " Amt per Week: "+ amtPerWeek);
        if(cycleCount[0] > amtPerWeek){
            dayAdapter.setDays(new ArrayList<>());
            dayAdapter.notifyDataSetChanged();
            tv_timeEmpty.setText("Selection Complete.");
            tv_dayEmpty.setText("Selection Complete.");
            Tool.handleEmpty(dayAdapter.getDays().isEmpty(), rv_listOfDays, tv_dayEmpty);

            rc.slotAmount = tsAmountPeople;
            return;
        }
        tv_timeEmpty.setText("Please Select a Day First");
        tv_dayEmpty.setText("No Days found with that Amount of Duration.");
        String title = "Select Day " + cycleCount[0] + " of " + amtPerWeek + " Per Week";
        tv_daysTitle.setText(title);
        title = "Select Time For Day " + cycleCount[0];
        tv_timeTitle.setText(title);
        Course course = rc.getCourse();
        ArrayList<DayOfWeek> days = course.filterFullDays(rc.getSq(), rc.getDuration());
        if(!daysAlreadySelected.isEmpty()){
            for(DayOfWeek day : daysAlreadySelected){
                Log.d("updateUI", "Day Already Selected: " + day.toString());
                days.remove(day);
            }
        }
        Tool.handleEmpty(true, rv_listOfTS, tv_timeEmpty);
        TimeSlotAdapter tsa = new TimeSlotAdapter(timeSlotsOfShiftMinute, new OnItemClickListener<SelectedIndicatorHelper<TimeSlot>>() {
            @Override
            public void onItemClick(SelectedIndicatorHelper<TimeSlot> item) {
                currentSelectedTS[0] = item.object;
                Schedule scheduleOfTS = course.getScheduleFromTimeSlot(item.object);
                currentAmtPpl[0] = 1;
                if(scheduleOfTS != null){
                    currentAmtPpl[0] = scheduleOfTS.getNumberOfStudents();
                }

                selected[0] = true;
                Log.d("TimeSlotAdapter", "Time Slot Selected: " + currentSelectedTS[0]);
                if(Tool.boolOf(tsvh)){
                    tsvh.updateBackground();
                }
                ((TimeSlotViewHolder)item.currentViewHolder).tv_timeSlot.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tertiary_text));
                tsvh = (TimeSlotViewHolder) item.currentViewHolder;
            }

            @Override
            public void onItemLongClick(SelectedIndicatorHelper<TimeSlot> item) {

            }
        }, currentSelectedDay.getValue());

        rv_listOfTS.setAdapter(tsa);

        dayAdapter= new DayAdapter(days, new OnItemClickListener<>() {
            @Override
            public void onItemClick(SelectedIndicatorHelper<DayOfWeek> item) {
                Log.d("DayAdapter", "Clicked!");
                currentSelectedDay.setValue(item.object);
                if(Tool.boolOf(dvh)){
                    dvh.tv_Day.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.surface));
                }
                ((DayViewHolder) item.currentViewHolder).tv_Day.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.turgo_gray));
                dvh = (DayViewHolder) item.currentViewHolder;
                Tool.handleEmpty(false, rv_listOfTS, tv_timeEmpty);
                ArrayList<TimeSlot> ts = rc.getCourse().findFreeSpotOfDay(currentSelectedDay.getValue(), rc.getSq(), rc.getCourse().getMaxStudentPerMeeting(), rc.getDuration());
                timeSlotsOfShiftMinute = ts;
                sb_timeOffset.setProgress(0);
                TimeSlot.filterTimesOfIncrement(ts, 0);
                handleTimeListChange(ts, tsa, 0);
                sb_timeOffset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        handleTimeListChange(ts, tsa, progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }

            @Override
            public void onItemLongClick(SelectedIndicatorHelper<DayOfWeek> item) {

            }
        });

        rv_listOfDays.setAdapter(dayAdapter);
    }
    private void handleTimeListChange(ArrayList<TimeSlot>ts, TimeSlotAdapter tsAdapter, int progress){
        int minuteIncrement = progress * 15;
        tv_offsetText.setText(minuteIncrement + " Minutes");
        ArrayList<TimeSlot> filtered = TimeSlot.filterTimesOfIncrement(ts, minuteIncrement);
        Log.d("HandleTimeListener", "Minute Offset Changed to:" + minuteIncrement);
        tsAdapter.setTimeSlots(filtered);
    }
    @Override
    public boolean checkIfCompleted() {
        return scheduleAdapter.getSlots().size() == rc.getAmountOfMeetingPerWeek();
    }
}