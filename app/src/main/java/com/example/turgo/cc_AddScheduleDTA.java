package com.example.turgo;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link cc_AddScheduleDTA#newInstance} factory method to
 * create an instance of this fragment.
 */
public class cc_AddScheduleDTA extends Fragment implements checkFragmentCompletion {


    TextView btn_selectEarliest, btn_selectLatest;
    Button btn_addDTA;
    AutoCompleteTextView sp_selectDay, sp_selectRoom;
    SwitchMaterial cb_limit;
    Boolean earlyOrLatest;
    RecyclerView rv_DTASelected;
    LocalTime earliest = null;
    LocalTime latest = null;
    DayOfWeek day = null;
    boolean limitMeetingBool = false;
    EditText et_meetingLimit;
    CreateCourse cc;
    int meetingLimit = 0;
    private ArrayList<Room>roomsAvailable = new ArrayList<>();

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
        cc = (CreateCourse) requireActivity();
        btn_addDTA = view.findViewById(R.id.btn_CC_AddDTA);
        btn_selectEarliest = view.findViewById(R.id.btn_CC_EarlyTimeDTA);
        btn_selectLatest = view.findViewById(R.id.btn_CC_LatestTimeDTA);
        sp_selectDay = view.findViewById(R.id.sp_CC_DayOfWeek);
        sp_selectRoom = view.findViewById(R.id.sp_CC_RoomSelection);
        cb_limit = view.findViewById(R.id.cb_CC_MeetingLimit);
        et_meetingLimit = view.findViewById(R.id.etn_CC_maxMeetingOfDay);
        rv_DTASelected = view.findViewById(R.id.rv_CC_DTASelected);
        RequireUpdate.getAllObjects(Room.class).addOnSuccessListener(rooms ->{
            roomsAvailable = (ArrayList<Room>) rooms;
            ArrayAdapter<String>adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, Tool.streamToArray(rooms.stream().map(Room::getRoomTag)));
            sp_selectRoom.setAdapter(adapter);
            sp_selectRoom.setOnItemClickListener((parent, view1, position, id) -> {
                cc.room = roomsAvailable.get(position);
            });
        });

        maxMeetingStatus(false);

        btn_selectEarliest.setOnClickListener(view1 -> {
            earlyOrLatest = true;
            showTimePicker();
        });
        btn_selectLatest.setOnClickListener(view2 -> {
            earlyOrLatest = false;
            showTimePicker();
        });
        ArrayAdapter<DayOfWeek> dayAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                DayOfWeek.values()
        );
        sp_selectDay.setAdapter(dayAdapter);
        sp_selectDay.setOnItemClickListener((adapterView, view1, i, l) -> {
            Object selected = adapterView.getItemAtPosition(i);
            if (selected instanceof DayOfWeek) {
                day = (DayOfWeek) selected;
            } else {
                String selectedStr = selected.toString();
                day = DayOfWeek.valueOf(selectedStr.toUpperCase(Locale.getDefault()));
            }
        });
        cb_limit.setOnCheckedChangeListener((compoundButton, b) -> {
            limitMeetingBool = b;
            maxMeetingStatus(b);
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
                String input = editable.toString().trim();
                if (!input.isEmpty()) {
                    try {
                        meetingLimit = Integer.parseInt(input);
                    } catch (NumberFormatException e) {
                        meetingLimit = 0; // or a default/fallback value
                    }
                } else {
                    meetingLimit = 0; // or some default when empty
                }
            }
        });
        DTAAdapter adapter = new DTAAdapter(new ArrayList<>());
        adapter.setListener(new OnItemClickListener<>() {
            @Override
            public void onItemClick(Integer item) {
                adapter.removeItem(item);
                cc.dtas.remove((int) item);
            }

            @Override
            public void onItemLongClick(Integer item) {

            }
        });
        rv_DTASelected.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_DTASelected.setAdapter(adapter);
        btn_addDTA.setOnClickListener(v -> {
            if(checkCompletion()){
                DayTimeArrangement dta = new DayTimeArrangement(new Course(), day, earliest, latest, meetingLimit);
                adapter.addItem(dta);
                cc.dtas.add(dta);
            }
            //TODO: set the course to the dta
        });

        return view;
    }

    private void maxMeetingStatus(boolean isLimited) {
        if (isLimited) {
            // ENABLED STATE
            et_meetingLimit.setEnabled(true);
            et_meetingLimit.setHint("Max students per meeting");

            // Restore the Emerald input style
            et_meetingLimit.setBackgroundResource(R.drawable.bg_emerald_input);

            // Optional: Reset text color if you changed it
            et_meetingLimit.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.brand_emerald_dark));

        } else {
            // DISABLED STATE
            et_meetingLimit.setEnabled(false);
            et_meetingLimit.setText("");
            et_meetingLimit.setHint("Unlimited");

            // Set to the grayed-out style
            et_meetingLimit.setBackgroundResource(R.drawable.bg_input_disabled);

            // Optional: Make the "Unlimited" hint look more subtle
            et_meetingLimit.setHintTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        }
    }

    private boolean checkCompletion(){

        if(earliest == null){
            Toast.makeText(requireContext(), "Start of Course is not selected!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(latest == null){
            Toast.makeText(requireContext(), "End of Course is not selected!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(latest.isBefore(earliest)){
            Toast.makeText(requireContext(), "Time Constraint is not Valid!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(limitMeetingBool && meetingLimit <= 0){
            Toast.makeText(requireContext(), "Please enter a valid meeting limit!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void showTimePicker() {
        // Get current hour and minute for initial time
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (timePicker, selectedHour, selectedMinute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
            if (earlyOrLatest) {
                btn_selectEarliest.setText(time);
                earliest = LocalTime.of(selectedHour, selectedMinute);
            } else {
                btn_selectLatest.setText(time);
                latest = LocalTime.of(selectedHour, selectedMinute);
            }
        }, hour, minute, true);

        timePickerDialog.show();
    }

    @Override
    public boolean checkIfCompleted() {
        if(rv_DTASelected.getChildCount() == 0){
            Toast.makeText(requireContext(), "Please Select at least One DTA!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
