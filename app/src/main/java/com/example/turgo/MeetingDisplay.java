package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.content.res.ColorStateList;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeetingDisplay#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeetingDisplay extends Fragment{

    ImageView iv_courseLogo;
    TextView tv_courseTitle, tv_courseTime, tv_courseDateDay;
    private static final String ARG_COURSE_TITLE = "course_title";
    private static final String ARG_COURSE_TIME = "course_time";
    private static final String ARG_COURSE_DATE = "course_date";
    private static final String ARG_LOGO_URL = "logo_url";
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

        Bundle args = getArguments();
        Student student = args == null ? null : (Student) args.getSerializable(Student.SERIALIZE_KEY_CODE);
        if (args != null && args.containsKey(ARG_COURSE_TITLE)) {
            tv_courseTitle.setText(args.getString(ARG_COURSE_TITLE, "Course"));
            tv_courseTime.setText(args.getString(ARG_COURSE_TIME, "-"));
            tv_courseDateDay.setText(args.getString(ARG_COURSE_DATE, "-"));

            String logoUrl = args.getString(ARG_LOGO_URL, "");
            if (Tool.boolOf(logoUrl)) {
                iv_courseLogo.setImageTintList(null);
                Tool.setImageCloudinary(requireContext(), logoUrl, iv_courseLogo);
            } else {
                iv_courseLogo.setImageResource(R.drawable.piano);
                iv_courseLogo.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.brand_emerald)));
            }
            return view;
        }

        if(student == null){
            Log.e("MeetingDisplay", "Student is Null!");
            return view;
        }

        student.getNextMeeting().getMeetingOfSchedule()
                .continueWithTask(task -> task.getResult().getScheduleOfCourse())
                .addOnSuccessListener(c ->{
                    course = c;
                    if(course != null){
                        iv_courseLogo.setImageTintList(null);
                        Tool.setImageCloudinary(requireContext(), course.getLogo(), iv_courseLogo);
                        tv_courseTitle.setText(course.getCourseName());
                        tv_courseTime.setText(student.getNextMeeting().getStartTimeChange().toString());
                        tv_courseDateDay.setText(student.getNextMeeting().getDateOfMeeting().toString());
                    }
                })
                .addOnFailureListener(e -> Log.e("MeetingDisplay", "Failed to load meeting display data", e));

        return view;
    }

    public static Bundle createArgs(String title, String time, String date, String logoUrl) {
        Bundle args = new Bundle();
        args.putString(ARG_COURSE_TITLE, title);
        args.putString(ARG_COURSE_TIME, time);
        args.putString(ARG_COURSE_DATE, date);
        args.putString(ARG_LOGO_URL, logoUrl);
        return args;
    }
}
