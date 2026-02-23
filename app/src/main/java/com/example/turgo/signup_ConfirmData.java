package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link signup_ConfirmData#newInstance} factory method to
 * create an instance of this fragment.
 */
public class signup_ConfirmData extends Fragment implements checkFragmentCompletion{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Button btn_SignUp;
    TextView tvAccountType, tvFullName, tvNickname, tvBirthDate, tvGender, tvEmail, tvPhone, tvAdditional;
    User cachedUser;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public signup_ConfirmData() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment signup_ConfirmData.
     */
    // TODO: Rename and change types and number of parameters
    public static signup_ConfirmData newInstance(String param1, String param2) {
        signup_ConfirmData fragment = new signup_ConfirmData();
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
        View view = inflater.inflate(R.layout.fragment_signup__confirm_data, container, false);
        SignUpPage sup = (SignUpPage) getActivity();
        tvAccountType = view.findViewById(R.id.tv_CD_AccountType);
        tvFullName = view.findViewById(R.id.tv_CD_FullName);
        tvNickname = view.findViewById(R.id.tv_CD_Nickname);
        tvBirthDate = view.findViewById(R.id.tv_CD_BirthDate);
        tvGender = view.findViewById(R.id.tv_CD_Gender);
        tvEmail = view.findViewById(R.id.tv_CD_Email);
        tvPhone = view.findViewById(R.id.tv_CD_Phone);
        tvAdditional = view.findViewById(R.id.tv_CD_Additional);
        assert sup != null;
        cachedUser = sup.createUser();
        bindSummary(cachedUser);
        btn_SignUp = view.findViewById(R.id.btn_SignUp);
        btn_SignUp.setOnClickListener(v ->{
            try {
                sup.signUp();
            } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException | java.lang.InstantiationException e) {
                throw new RuntimeException(e);
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public boolean checkIfCompleted() {
        return true;
    }

    private void bindSummary(User user) {
        if (user == null) {
            setTextSafe(tvAccountType, "-");
            setTextSafe(tvFullName, "-");
            setTextSafe(tvNickname, "-");
            setTextSafe(tvBirthDate, "-");
            setTextSafe(tvGender, "-");
            setTextSafe(tvEmail, "-");
            setTextSafe(tvPhone, "-");
            setTextSafe(tvAdditional, "No user data available.");
            return;
        }

        String accountType = user.getUserType() != null
                ? toTitleCase(user.getUserType().type())
                : "Unknown";

        setTextSafe(tvAccountType, accountType);
        setTextSafe(tvFullName, valueOrDash(user.getFullName()));
        setTextSafe(tvNickname, valueOrDash(user.getNickname()));
        setTextSafe(tvBirthDate, valueOrDash(user.getBirthDate()));
        setTextSafe(tvGender, valueOrDash(user.getGender()));
        setTextSafe(tvEmail, valueOrDash(user.getEmail()));
        setTextSafe(tvPhone, valueOrDash(user.getPhoneNumber()));
        setTextSafe(tvAdditional, buildAdditionalInfo(user));
    }

    private String buildAdditionalInfo(User user) {
        if (user instanceof Teacher) {
            Teacher teacher = (Teacher) user;
            ArrayList<String> courseTypes = teacher.getCourseTypeTeach();
            if (courseTypes == null || courseTypes.isEmpty()) {
                return "No subjects selected yet.";
            }
            ArrayList<String> formatted = new ArrayList<>();
            for (String type : courseTypes) {
                formatted.add(toTitleCase(type));
            }
            return "Subjects: " + String.join(", ", formatted);
        }
        if (user instanceof Student) {
            Student student = (Student) user;
            ArrayList<String> interests = student.getCourseInterested();
            if (interests == null || interests.isEmpty()) {
                return "No course interests selected yet.";
            }
            ArrayList<String> formatted = new ArrayList<>();
            for (String type : interests) {
                formatted.add(toTitleCase(type));
            }
            return "Course interests: " + String.join(", ", formatted);
        }
        if (user instanceof Parent) {
            Parent parent = (Parent) user;
            ArrayList<Student> children = parent.getChildren();
            int count = children != null ? children.size() : 0;
            return "Children linked: " + count;
        }
        if (user instanceof Admin) {
            return "Admin account setup ready.";
        }
        return "-";
    }

    private String valueOrDash(String value) {
        return Tool.boolOf(value) ? value : "-";
    }

    private void setTextSafe(TextView view, String value) {
        if (view != null) {
            view.setText(valueOrDash(value));
        }
    }

    private String toTitleCase(String raw) {
        if (!Tool.boolOf(raw)) {
            return "";
        }
        String normalized = raw.replace("_", " ").toLowerCase(Locale.US);
        String[] parts = normalized.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!Tool.boolOf(part)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }
}
