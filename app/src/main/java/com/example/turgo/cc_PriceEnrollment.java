package com.example.turgo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link cc_PriceEnrollment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class cc_PriceEnrollment extends Fragment {
    EditText et_hourlyCost, et_baseCost, et_monthlyDiscount;
    ChipGroup cg_groupOrPrivate, cg_paymentMethods;
    Chip c_autoAcceptStudent;

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
        cg_groupOrPrivate = view.findViewById(R.id.cg_CC_GroupPrivate);
        cg_paymentMethods = view.findViewById(R.id.cg_CC_PaymentMethods);
        CreateCourse cc = (CreateCourse) requireActivity();

        et_baseCost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                cc.baseCost = Integer.parseInt(editable.toString());
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
                cc.hourlyCost = Integer.parseInt(editable.toString());
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
                cc.monthlyDiscount = Integer.parseInt(editable.toString());
            }
        });
        cg_paymentMethods.setOnCheckedStateChangeListener((group, checkedIds) -> {
            for(int i = 0; i<cg_paymentMethods.getChildCount(); i++){
                Chip chip = (Chip) cg_paymentMethods.getChildAt(i);
                cc.acceptedPaymentMethods[i] = chip.isChecked();
            }
        });
        cg_groupOrPrivate.setOnCheckedStateChangeListener((group, checkedIds) -> {
            for(int i = 0; i<cg_groupOrPrivate.getChildCount(); i++){
                Chip chip = (Chip) cg_paymentMethods.getChildAt(i);
                cc.acceptedPaymentMethods[i] = chip.isChecked();
            }
        });

        return view;

    }
}