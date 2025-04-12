package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RC_PrivateDurationAmount#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RC_PrivateDurationAmount extends Fragment {
    RadioGroup rg_privateGroup;
    SeekBar sb_durationSlider, sb_amountSlider;
    TextView tv_minutesDisplay, tv_amountDisplay;
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
        rg_privateGroup = view.findViewById(R.id.rg_PrivateGroup);
        sb_durationSlider = view.findViewById(R.id.sb_DurationSlider);
        sb_amountSlider = view.findViewById(R.id.sb_AmountPerWeek);
        tv_minutesDisplay = view.findViewById(R.id.tv_durationDisplay);
        tv_amountDisplay = view.findViewById(R.id.tv_AmtDisplay);
        final String[] privateGroup = {""};
        rg_privateGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            if(i != -1){
                RadioButton selected = view.findViewById(i);
                privateGroup[0] = selected.getText().toString();
            }
        });
        rc.setPrivate(privateGroup[0].equals("Private"));
        int stepSize = 15;
        int maxMin = 120;
        final int[] minutes = {stepSize};
        sb_durationSlider.setMax(maxMin / stepSize);
        sb_durationSlider.setMin(1);
        sb_durationSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                minutes[0] = i*stepSize;
                String display = minutes[0] + " minutes";
                tv_minutesDisplay.setText(display);
                rc.setDuration(minutes[0]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sb_amountSlider.setMax(rc.getCourse().amountDaysAvail());
        sb_amountSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tv_amountDisplay.setText(i);
                rc.setAmountOfMeetingPerWeek(i);
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
}