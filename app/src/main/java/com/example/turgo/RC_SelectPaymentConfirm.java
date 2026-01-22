package com.example.turgo;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RC_SelectPaymentConfirm#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RC_SelectPaymentConfirm extends Fragment implements checkFragmentCompletion{

    LinearLayout  VL_pwSlot, VL_pmSlot;
    RecyclerView rv_reviewSchedules;
    HashMap<TimeSlot, Integer> lists;
    static final String viewName = "Confirmation & Payment Selection";
    TextView tv_ppw, tv_ppm, tv_savePercentage, tv_requestWarning;
    Button btn_sw, btn_sm;

    boolean pwm; //false = week, true = month
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RC_SelectPaymentConfirm() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RC_SelectPaymentConfirm.
     */
    // TODO: Rename and change types and number of parameters
    public static RC_SelectPaymentConfirm newInstance(String param1, String param2) {
        RC_SelectPaymentConfirm fragment = new RC_SelectPaymentConfirm();
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
        RegisterCourse rc = (RegisterCourse) getActivity();
        assert rc != null;
        rc.tv_title.setText(viewName);
        View view = inflater.inflate(R.layout.fragment_register__select_payment_confirm, container, false);
        ArrayList<TimeSlot>timeSlots = rc.getSelectedTS();
        rv_reviewSchedules = view.findViewById(R.id.rv_rspc_Selections);

        Course course = rc.getCourse();
        ArrayList<Double>prices = new ArrayList<>();

        RcScheduleAdapter rcScheduleAdapter = new RcScheduleAdapter(timeSlots);
        rv_reviewSchedules.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv_reviewSchedules.setAdapter(rcScheduleAdapter);
        lists = rc.slotAmount;
        if(lists == null){
            lists = new HashMap<>();
        }
        for(int i = 0; i<lists.size(); i++){
            prices.add(course.calcPrice(rc.getSq() == ScheduleQuality.PRIVATE_ONLY, timeSlots.get(i).getPersonCount().one, (double) timeSlots.get(i).getTime().getSeconds() /60));
//            LinearLayout ll = new LinearLayout(getContext());
//            ll.setOrientation(LinearLayout.VERTICAL);
//            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT, // width
//                    ViewGroup.LayoutParams.MATCH_PARENT); //height
//            containerParams.setMargins(8, 0, 8, 0);
//            ll.setLayoutParams(containerParams);
//
//            LinearLayout.LayoutParams textContentParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            textContentParams.setMargins(0, 0, 0, 8);
//            TextView tv_day = new TextView(getContext()), tv_timeSlot = new TextView(getContext()), tv_peopleAmount = new TextView(getContext());
//            tv_day.setText(timeSlots.get(i).getDay().toString());
//            tv_day.setTextSize(18);
//            tv_day.setTypeface(null, Typeface.BOLD);
//            tv_day.setLayoutParams(textContentParams);
//
//            ll.addView(tv_day);
//
//            tv_timeSlot.setText(timeSlots.get(i).toStr());
//            tv_timeSlot.setTextSize(14);
//            tv_timeSlot.setLayoutParams(textContentParams);
//
//            ll.addView(tv_timeSlot);
//
//            LinearLayout pplAmountContainer = new LinearLayout(getContext());
//
//            ImageView iv_peopleLogo = new ImageView(getContext());
//            iv_peopleLogo.setImageResource(R.drawable.user);
//
//            tv_peopleAmount.setText(amtPpl.get(i));
//            tv_peopleAmount.setTextSize(14);
//
//            pplAmountContainer.addView(iv_peopleLogo);
//            pplAmountContainer.addView(tv_peopleAmount);
//
//            ll.addView(pplAmountContainer);
//
//            LL_tsSlot.addView(ll);


        }

        tv_ppw = view.findViewById(R.id.tv_pricePerWeek);
        tv_ppm = view.findViewById(R.id.tv_PricePerMonth);
        tv_savePercentage = view.findViewById(R.id.tv_DiscountAmount);

        double costPW = prices.stream().mapToDouble(price -> price).sum();
        String cpw = "$" + (int)costPW;

        double costPM = costPW * 4 * (100 -course.getMonthlyDiscountPercentage())/100;
        String cpm = "$" + (int)costPM;

        String saveDisplay = "Save " + course.getMonthlyDiscountPercentage() + "%";
        tv_ppw.setText(cpw);
        tv_ppm.setText(cpm);
        tv_savePercentage.setText(saveDisplay);

        btn_sm = view.findViewById(R.id.btn_SelectMonth);
        btn_sw = view.findViewById(R.id.btn_SelectWeek);
        VL_pwSlot = view.findViewById(R.id.vl_pwContainer);
        VL_pmSlot = view.findViewById(R.id.vl_pmContainer);
        btn_sm.setOnClickListener(view1 -> {
            pwm = false;
            rc.setPaymentPreferences(false);
            rc.setSelectedPrice((int)costPW);
            VL_pwSlot.setBackgroundColor(Color.parseColor("#FFC107"));
            VL_pmSlot.setBackgroundColor(Color.parseColor("#63A375"));
        });
        btn_sw.setOnClickListener(view12 -> {
            pwm = true;
            rc.setPaymentPreferences(true);
            rc.setSelectedPrice((int)costPM);
            VL_pmSlot.setBackgroundColor(Color.parseColor("#FFC107"));
            VL_pwSlot.setBackgroundColor(Color.parseColor("#63A375"));
        });

        tv_requestWarning = view.findViewById(R.id.tv_requestWarning);
        if(course.isAutoAcceptStudent()){
            tv_requestWarning.setVisibility(View.GONE);
        }else{
            tv_requestWarning.setVisibility(View.VISIBLE);
            tv_requestWarning.setText("This will send a request to the teacher to accept the meeting.");
        }
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public boolean checkIfCompleted() {
        RegisterCourse rc = (RegisterCourse) getActivity();
        assert rc != null;
        return rc.getSelectedPrice() != 0;
    }
}