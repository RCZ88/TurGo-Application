package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link signup_PasswordSetup#newInstance} factory method to
 * create an instance of this fragment.
 */
public class signup_PasswordSetup extends Fragment implements checkFragmentCompletion{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    TextInputEditText pi_password;
    TextInputEditText pi_confirmPassword;
    LinearLayout checkboxContainer;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private boolean[]checklists;
    public signup_PasswordSetup() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment signup_PasswordSetup.
     */
    // TODO: Rename and change types and number of parameters
    public static signup_PasswordSetup newInstance(String param1, String param2) {
        signup_PasswordSetup fragment = new signup_PasswordSetup();
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
        View view = inflater.inflate(R.layout.fragment_signup__password_setup, container, false);
        pi_password = view.findViewById(R.id.tie_Password);
        pi_confirmPassword = view.findViewById(R.id.tie_ConfirmPassword);
        checkboxContainer = view.findViewById(R.id.ll_PasswordChecklist);

        pi_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /*Log.d("Password Input", "Text Change detected (ON TEXT CHANGE)");
                updateChecklist(Objects.requireNonNull(pi_password.getText()).toString());*/

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("Password Input", "Text Change detected (AFTER TEXT CHANGE)");
                updateChecklist(Objects.requireNonNull(pi_password.getText()).toString());
            }
        });
        // Inflate the layout for this fragment
        return view;
    }
    public void updateChecklist(String password){
        checklists = checkPasswordStrength(password);
        for(int i = 0; i<checklists.length; i++){
            CheckBox cb = (CheckBox) checkboxContainer.getChildAt(i);

            // Assign a listener to handle changes
//            int index = i; // Capture the current index
            cb.setChecked(checklists[i]);
            /*cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // Update the boolean array whenever a CheckBox state changes
                    checklists[index] = isChecked;
                }
            });*/
        }
    }
    public boolean[] checkPasswordStrength(String password){
        /*
        Lowercase character required

Uppercase character required

Numeric character required

Non-alphanumeric character required

The following characters satisfy the non-alphanumeric character requirement: ^ $ * . [ ] { } ( ) ? " ! @ # % & / \ , > < ' : ; | _ ~

Minimum password length (ranges from 6 to 30 characters; defaults to 6)

Maximum password length (maximum length of 4096 characters)
        */
        boolean[]conditionsMet = new boolean[5];
        for(int i = 0; i<password.length(); i++){
            char c = password.charAt(i);
            if(Character.isLowerCase(c)){
                conditionsMet[0]= true;
            }
            if(Character.isUpperCase(c)){
                conditionsMet[1] = true;
            }
            if(Character.isDigit(c)){
                conditionsMet[2] = true;
            }
            if(!Character.isAlphabetic(c) && !Character.isDigit(c)){
                conditionsMet[3] = true;
            }
        }
        if(password.length() >= 6 && password.length() <= 30){
            conditionsMet[4] = true;
        }
        return conditionsMet;
    }
    public boolean conditionMet(){
        for(boolean conditions : checklists){
            if(!conditions){
                return false;
            }
        }
        return true;
    }
    public boolean checkConfirmPassword() {
        if(Objects.requireNonNull(pi_password.getText()).toString()
                .equals(Objects.requireNonNull(pi_confirmPassword.getText()).toString())){
            return true;
        }
        Toast.makeText(requireContext(), "Confirm Password Does NOT match!", Toast.LENGTH_SHORT).show();
        return false;
    }


    @Override
    public boolean checkIfCompleted() {
        boolean conditionsMet = conditionMet();
        boolean passwordsMatch = checkConfirmPassword();
        boolean passwordNotEmpty = Objects.requireNonNull(pi_password.getText()).length() != 0;

        Log.d("CheckIfCompleted", "Conditions Met: " + conditionsMet);
        Log.d("CheckIfCompleted", "Passwords Match: " + passwordsMatch);
        Log.d("CheckIfCompleted", "Password Not Empty: " + passwordNotEmpty);

        return conditionsMet && passwordsMatch && passwordNotEmpty;
    }
}