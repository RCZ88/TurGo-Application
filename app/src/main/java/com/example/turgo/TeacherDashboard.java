package com.example.turgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeacherDashboard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeacherDashboard extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    Button btn_viewTodaySchedule, btn_CreateTask, btn_addAgenda, btn_addDTA;
    TextView tv_courseName, tv_scheduleTime, tv_meetingRoom;
    RecyclerView rv_activeCourses, rv_recentStudentSubmit;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TeacherDashboard() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TeacherDashboard.
     */
    // TODO: Rename and change types and number of parameters
    public static TeacherDashboard newInstance(String param1, String param2) {
        TeacherDashboard fragment = new TeacherDashboard();
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
        View view = inflater.inflate(R.layout.fragment_teacher_dashboard, container, false);
        btn_viewTodaySchedule = view.findViewById(R.id.btn_TD_TodaysSchedule);
        btn_CreateTask = view.findViewById(R.id.btn_TD_CreateTask);
        btn_addAgenda = view.findViewById(R.id.btn_TD_AddAgenda);
        btn_addDTA = view.findViewById(R.id.btn_TD_AddDTA);
        tv_courseName = view.findViewById(R.id.tv_TD_courseName);
        tv_scheduleTime = view.findViewById(R.id.tv_TD_ScheduleTime);
        tv_meetingRoom = view.findViewById(R.id.tv_TD_MeetingRoom);
        rv_activeCourses = view.findViewById(R.id.rv_TD_ActiveCourses);
        rv_recentStudentSubmit = view.findViewById(R.id.rv_TD_RecentStudentSubmit);
        Teacher teacher = ((TeacherScreen)requireActivity()).getTeacher();


        btn_viewTodaySchedule.setOnClickListener(view1 -> {
            TeacherScheduleList tesl = new TeacherScheduleList();
            Bundle bundle = new Bundle();
            bundle.putBoolean("isMeetingMode", false);
            bundle.putBoolean("isDaily", true);
            tesl.setArguments(bundle);

            getParentFragmentManager().beginTransaction().replace(R.id.nhf_ts_FragmentContainer, tesl).commit();
        });

        btn_CreateTask.setOnClickListener(view1 -> {
            TeacherCreateTask tct = new TeacherCreateTask();
            getParentFragmentManager().beginTransaction().replace(R.id.nhf_ts_FragmentContainer, tct).commit();

        });
        btn_addAgenda.setOnClickListener(view1 -> {
            TeacherAddAgenda taa = new TeacherAddAgenda();
            getParentFragmentManager().beginTransaction().replace(R.id.nhf_ts_FragmentContainer, taa).commit();
        });
        btn_addDTA.setOnClickListener(view1 -> {
            TeacherAddDTA tad = new TeacherAddDTA();
            getParentFragmentManager().beginTransaction().replace(R.id.nhf_ts_FragmentContainer, tad).commit();
        });
        CourseTeachersAdapter cta = new CourseTeachersAdapter(teacher.getCoursesTeach(), item -> {
            //TODO: change to course teacher page
            TeacherCourseScreen tcs = new TeacherCourseScreen();
            Bundle bundle = new Bundle();
            bundle.putSerializable("course", item);
            tcs.setArguments(bundle);
            getParentFragmentManager().beginTransaction().replace(R.id.nhf_ts_FragmentContainer, tcs).commit();
        });

        rv_activeCourses.setAdapter(cta);
        SubmissionAdapter sa = new SubmissionAdapter(teacher.getLatestSubmission());
        rv_recentStudentSubmit.setAdapter(sa);

        // Inflate the layout for this fragment
        return view;
    }
}