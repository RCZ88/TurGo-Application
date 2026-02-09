package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeetingDisplay#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeetingDisplay extends Fragment{

    ImageView iv_courseLogo;
    TextView tv_courseTitle, tv_courseTime, tv_courseDateDay;
    Schedule schedule;
    Course course;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MeetingDisplay() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CourseDisplay.
     */
    // TODO: Rename and change types and number of parameters
    public static MeetingDisplay newInstance(String param1, String param2) {
        MeetingDisplay fragment = new MeetingDisplay();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meeting_display, container, false);

        iv_courseLogo = view.findViewById(R.id.iv_cdf_CourseIcon);
        tv_courseDateDay = view.findViewById(R.id.tv_mdf_MeetingDates);
        tv_courseTime = view.findViewById(R.id.tv_StartTime);
        tv_courseTitle = view.findViewById(R.id.tv_mdf_CourseTitle);

        Student student = (Student) getArguments().getSerializable(Student.SERIALIZE_KEY_CODE);

        if(student == null){
            Log.e("MeetingDisplay", "Student is Null!");
            return view;
        }
        student.getNextMeeting().getMeetingOfSchedule()
                .continueWithTask(task -> task.getResult().getScheduleOfCourse())
                .addOnSuccessListener(c ->{
                    course = c;
                    if(course != null){
                        Glide.with(requireContext()).load(course.getLogo()).into(iv_courseLogo);
                        tv_courseTitle.setText(course.getCourseName());
                        tv_courseTime.setText(student.getNextMeeting().getStartTimeChange().toString());
                        tv_courseDateDay.setText(student.getNextMeeting().getDateOfMeeting().toString());
                    }
                });

        return view;
    }

}