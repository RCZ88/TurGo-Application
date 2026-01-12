package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link cc_PriceEnrollment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class cc_PriceEnrollment extends Fragment implements checkFragmentCompletion{
    EditText et_hourlyCost, et_baseCost, et_monthlyDiscount;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch sw_group, sw_private, sw_month, sw_meeting;
    CheckBox cb_autoAcceptStudent;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public cc_PriceEnrollment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment cc_set_price_enrollment.
     */
    // TODO: Rename and change types and number of parameters
    public static cc_PriceEnrollment newInstance(String param1, String param2) {
        cc_PriceEnrollment fragment = new cc_PriceEnrollment();
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
        View view = inflater.inflate(R.layout.fragment_cc_set_price_enrollment, container, false);
        et_baseCost = view.findViewById(R.id.etn_CC_BaseCost);
        et_hourlyCost = view.findViewById(R.id.etn_CC_HourlyCost);
        et_monthlyDiscount = view.findViewById(R.id.etn_MonthlyDiscount);
        sw_group = view.findViewById(R.id.sw_CC_GroupType);
        sw_meeting = view.findViewById(R.id.sw_CC_PerMeeting);
        sw_private = view.findViewById(R.id.sw_CC_PrivateType);
        sw_month = view.findViewById(R.id.sw_CC_PerMonth);
        cb_autoAcceptStudent = view.findViewById(R.id.cb_CC_AutoAccept);

        CreateCourse cc = (CreateCourse) requireActivity();
        if(cc.groupPrivate != null && cc.acceptedPaymentMethods != null){
            Log.d("cc_PriceEnrollment", "Array not Null");
        }else{
            Log.d("cc_PriceEnrollment", "Array is Null!");
            cc.groupPrivate = new boolean[2];
            cc.acceptedPaymentMethods = new boolean[2];
        }
        sw_group.setOnCheckedChangeListener((buttonView, isChecked) -> cc.groupPrivate[Course.GROUP_INDEX] = isChecked);
        sw_private.setOnCheckedChangeListener((buttonView, isChecked) -> cc.groupPrivate[Course.PRIVATE_INDEX] = isChecked);
        sw_month.setOnCheckedChangeListener((buttonView, isChecked) -> cc.acceptedPaymentMethods[Course.PER_MONTH_INDEX] = isChecked);
        sw_meeting.setOnCheckedChangeListener((buttonView, isChecked) -> cc.acceptedPaymentMethods[Course.PER_MEETING_INDEX] = isChecked);
        cb_autoAcceptStudent.setOnCheckedChangeListener((buttonView, isChecked) -> cc.autoAcceptStudent = isChecked);

        et_baseCost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable != null){
                    try{
                        cc.baseCost = Integer.parseInt(editable.toString());
                    }catch (Exception e){
                        Log.d("cc_PriceEnrollment", "String is empty, Cannot convert to int");
                    }
                }
            }
        });
        et_hourlyCost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable != null){
                    try{
                        cc.hourlyCost = Integer.parseInt(editable.toString());
                    }catch (Exception e){
                        Log.d("cc_PriceEnrollment", "String is empty, Cannot convert to int");
                    }
                }

            }
        });
        et_monthlyDiscount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable != null){
                    try{
                        cc.monthlyDiscount = Integer.parseInt(editable.toString());
                    }catch (Exception e){
                        Log.d("cc_PriceEnrollment", "String is empty, Cannot convert to int");
                    }
                }
            }
        });


        return view;

    }

    @Override
    public boolean checkIfCompleted() {
        CreateCourse cc = (CreateCourse) requireActivity();

        // Check base cost
        String baseCostStr = et_baseCost.getText().toString().trim();
        if (baseCostStr.isEmpty()) {
            et_baseCost.requestFocus();
            et_baseCost.setError("Base cost is required");
            Toast.makeText(requireContext(), "Please enter base cost", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check hourly cost
        String hourlyCostStr = et_hourlyCost.getText().toString().trim();
        if (hourlyCostStr.isEmpty()) {
            et_hourlyCost.requestFocus();
            et_hourlyCost.setError("Hourly cost is required");
            Toast.makeText(requireContext(), "Please enter hourly cost", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Update values before validation
        try {
            cc.baseCost = Integer.parseInt(baseCostStr);
            cc.hourlyCost = Integer.parseInt(hourlyCostStr);

            String monthlyDiscountStr = et_monthlyDiscount.getText().toString().trim();
            if (!monthlyDiscountStr.isEmpty()) {
                cc.monthlyDiscount = Integer.parseInt(monthlyDiscountStr);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check at least one group/private option selected
        boolean hasGroupOption = cc.groupPrivate[Course.GROUP_INDEX] || cc.groupPrivate[Course.PRIVATE_INDEX];
        if (!hasGroupOption) {
            Toast.makeText(requireContext(), "Please select group or private", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check at least one payment method selected
        boolean hasPaymentMethod = cc.acceptedPaymentMethods[Course.PER_MONTH_INDEX] ||
                cc.acceptedPaymentMethods[Course.PER_MEETING_INDEX];
        if (!hasPaymentMethod) {
            Toast.makeText(requireContext(), "Please select payment method", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

}