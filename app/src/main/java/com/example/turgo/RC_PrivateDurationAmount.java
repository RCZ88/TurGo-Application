package com.example.turgo;

import static com.example.turgo.ScheduleQuality.FLEXIBLE;
import static com.example.turgo.ScheduleQuality.GROUP_ONLY;
import static com.example.turgo.ScheduleQuality.PRIVATE_ONLY;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RC_PrivateDurationAmount#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RC_PrivateDurationAmount extends Fragment implements checkFragmentCompletion {
    RadioGroup rg_privateGroup;
    SeekBar sb_durationSlider, sb_amountSlider;
    static final String viewName = "Course Details";
    TextView tv_minutesDisplay, tv_amountDisplay, tv_durationSlotExplanation;
    int currentSessionAvailableAmount;
    int currentAmountSelected;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RC_PrivateDurationAmount() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment register_PrivateDuration.
     */
    // TODO: Rename and change types and number of parameters
    public static RC_PrivateDurationAmount newInstance(String param1, String param2) {
        RC_PrivateDurationAmount fragment = new RC_PrivateDurationAmount();
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
        View view = inflater.inflate(R.layout.fragment_register__private_duration, container, false);
        RegisterCourse rc = (RegisterCourse)getActivity();
        assert rc != null;
        rc.tv_title.setText(viewName);
        rg_privateGroup = view.findViewById(R.id.rg_PrivateGroup);
        sb_durationSlider = view.findViewById(R.id.sb_DurationSlider);
        sb_amountSlider = view.findViewById(R.id.sb_AmountPerWeek);
        tv_minutesDisplay = view.findViewById(R.id.tv_durationDisplay);
        tv_amountDisplay = view.findViewById(R.id.tv_AmtDisplay);
        tv_durationSlotExplanation =  view.findViewById(R.id.tv_SessionLimitInfo);
        ScheduleQuality[]qualities = {PRIVATE_ONLY, GROUP_ONLY, FLEXIBLE};
        rg_privateGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            if(i != -1){
                assert rc != null;
                RadioButton rb = view.findViewById(i);
                int index = rg_privateGroup.indexOfChild(rb);
                rc.setSq(qualities[index]);
            }
        });


        int stepSize = 15;
        int maxMin = rc.getCourse().getMaxMeetingDuration();

        int maxSteps = maxMin / stepSize;
        final int[] minutes = {stepSize};
        sb_durationSlider.setMin(0);
        sb_durationSlider.setMax(maxSteps);
        Log.d("Duration Slider", "Min Max duration: " + 15  + "-" +maxMin );

        sb_amountSlider.setMin(0);
        currentAmountSelected = 1;
        sb_amountSlider.setProgress(currentAmountSelected);
        sb_durationSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                minutes[0] = i*stepSize;
                Log.d("RC", "i = " + i);
                String display = minutes[0] + " minutes";
                tv_minutesDisplay.setText(display);
                rc.setDuration(minutes[0]);
                ArrayList<DayTimeArrangement>dtaAvailForTime =  rc.getCourse().getDtasOfTime(minutes[0]);
                Log.d("RC_PrivateDurationAmount", "DTA AVAILABLE AMOUNT: " + dtaAvailForTime.size());
                for(DayTimeArrangement dta : dtaAvailForTime){
                    Log.d("rc", "- " +dta.getDuration() + " minutes.");
                }
                int amt = dtaAvailForTime.size();
                //check if theres a change in session avail amount;
                if(amt != currentSessionAvailableAmount){
                    sb_amountSlider.setMax(amt);
                    int newProgress = currentAmountSelected > amt ? sb_amountSlider.getMax() : sb_amountSlider.getProgress();
                    sb_amountSlider.setProgress(newProgress);
                    tv_durationSlotExplanation.setText("Based on " +  amt + " available slots for " +  minutes[0] +" minute duration.");
                    tv_amountDisplay.setText(newProgress + "/" + sb_amountSlider.getMax());
                    rc.setDtaAvailable(dtaAvailForTime);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sb_durationSlider.setProgress(rc.getDuration() / 15, true);
        Log.d("PrivateDurationAmount", "Amount Slider Max: "+ rc.getCourse().amountDaysAvail());
        sb_amountSlider.setMax(rc.getCourse().amountDaysAvail());
        sb_amountSlider.setMin(1);
        rc.setAmountOfMeetingPerWeek(sb_amountSlider.getMin());
        sb_amountSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tv_amountDisplay.setText(Integer.toString(i));
                rc.setAmountOfMeetingPerWeek(i);
                currentAmountSelected = i;
                tv_amountDisplay.setText(currentAmountSelected + "/" + sb_amountSlider.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return view;

    }


    @Override
    public boolean checkIfCompleted() {
        return rg_privateGroup.getCheckedRadioButtonId() != -1;
    }
}