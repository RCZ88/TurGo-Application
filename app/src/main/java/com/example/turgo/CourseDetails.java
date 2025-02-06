package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourseDetails#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CourseDetails extends Fragment {
    TextView tv_courseTitle, tv_teacherName, tv_nextMeetingDate, tv_dayOfWeek, tv_duration, tv_room;
    ImageView iv_courseLogo;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CourseDetails.
     */
    // TODO: Rename and change types and number of parameters
    public static CourseDetails newInstance(String param1, String param2) {
        CourseDetails fragment = new CourseDetails();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public CourseDetails() {
        // Required empty public constructor
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_course_details, container, false);
        tv_courseTitle = view.findViewById(R.id.tv_cd_CourseTitle);
        tv_dayOfWeek = view.findViewById(R.id.tv_cd_DayOfWeek);
        tv_duration = view.findViewById(R.id.tv_cd_Duration);
        tv_room = view.findViewById(R.id.tv_cd_RoomID);
        tv_teacherName = view.findViewById(R.id.tv_cd_TeacherName);
        iv_courseLogo = view.findViewById(R.id.iv_cdnf_CourseIcon);

        return view;
    }
}