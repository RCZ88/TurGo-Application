package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class TeacherDashboard extends Fragment {

    LinearLayout ll_viewTodayScheduleAction, ll_createTaskAction, ll_addAgendaAction, ll_addDTAAction, ll_createCourseAction;
    TextView tv_courseName, tv_scheduleTime, tv_meetingRoom, tv_noActiveCourses, tv_noRecentStudentSubmit;
    RecyclerView rv_recentStudentSubmit;
    ViewPager2 vp2_activeCourses;
    CourseTeachersAdapter courseTeachersAdapter;
    SubmissionAdapter submissionAdapter;

    ArrayList<Course> teacherCoursesTeach = new ArrayList<>();
    ArrayList<Integer> studentCountOfCourses = new ArrayList<>();
    ArrayList<Meeting> nextMeetingOfCourses = new ArrayList<>();

    public TeacherDashboard() {
        // Required empty public constructor
    }

    public static TeacherDashboard newInstance(String param1, String param2) {
        TeacherDashboard fragment = new TeacherDashboard();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("TeacherDashboard", "========== onCreateView START ==========");

        View view = inflater.inflate(R.layout.fragment_teacher_dashboard, container, false);

        // Find all views
        ll_viewTodayScheduleAction = view.findViewById(R.id.ll_TD_ViewSchedulesAction);
        ll_createTaskAction = view.findViewById(R.id.ll_TD_CreateTaskAction);
        ll_addAgendaAction = view.findViewById(R.id.ll_TD_AddAgendaAction);
        ll_addDTAAction = view.findViewById(R.id.ll_TD_AddDTAAction);
        ll_createCourseAction = view.findViewById(R.id.ll_TD_CreateCourseAction);
        tv_courseName = view.findViewById(R.id.tv_TD_courseName);
        tv_scheduleTime = view.findViewById(R.id.tv_TD_ScheduleTime);
        tv_meetingRoom = view.findViewById(R.id.tv_TD_MeetingRoom);
        tv_noActiveCourses = view.findViewById(R.id.tv_TD_NoActiveCourses);
        tv_noRecentStudentSubmit = view.findViewById(R.id.tv_TD_NoRecentSubmission);
        vp2_activeCourses = view.findViewById(R.id.vp_TD_activeCourses);
        rv_recentStudentSubmit = view.findViewById(R.id.rv_TD_RecentStudentSubmit);

        TeacherScreen activity = (TeacherScreen) getActivity();
        if (activity == null) return view;
        String tid = activity.getTeacherId();

        if (!Tool.boolOf(tid)) {
            Log.e("TeacherDashboard", "Teacher ID is null!");
            return view;
        }

        // Progressives/Selective loading
        new TeacherRepository(tid).loadDashboardData()
            .addOnSuccessListener(data -> {
                if (!isAdded()) return;
                renderDashboard(data);
            })
            .addOnFailureListener(e -> Log.e("TeacherDashboard", "Failed lite load", e));

        setupStaticListeners();

        return view;
    }

    private void renderDashboard(TeacherDashboardData data) {
        // 1. Next Schedule
        tv_courseName.setText(Tool.boolOf(data.nextCourseName) ? data.nextCourseName : "No schedule");
        tv_meetingRoom.setText(Tool.boolOf(data.nextRoomTag) ? data.nextRoomTag : "No room");
        tv_scheduleTime.setText(Tool.boolOf(data.nextScheduleTime) ? data.nextScheduleTime : "No time");

        // 2. Active Courses
        teacherCoursesTeach = data.coursesTeach;
        nextMeetingOfCourses = data.nextMeetingOfCourses;
        studentCountOfCourses = data.studentCountOfCourses;

        Tool.handleEmpty(teacherCoursesTeach.isEmpty(), vp2_activeCourses, tv_noActiveCourses);
        courseTeachersAdapter = new CourseTeachersAdapter(teacherCoursesTeach, nextMeetingOfCourses, studentCountOfCourses, new OnItemClickListener<>() {
            @Override public void onItemClick(Course item) {
                TeacherCourseScreen tcs = new TeacherCourseScreen();
                Bundle b = new Bundle();
                b.putSerializable(Course.SERIALIZE_KEY_CODE, item);
                tcs.setArguments(b);
                Tool.loadFragment(requireActivity(), R.id.nhf_ts_FragmentContainer, tcs);
            }
            @Override public void onItemLongClick(Course item) {}
        }, CourseTeacherItemMode.VIEW_PAGER);
        vp2_activeCourses.setAdapter(courseTeachersAdapter);

        // 3. Recent Submissions
        ArrayList<SubmissionDisplay> latest = data.latestSubmissions;
        Tool.handleEmpty(latest.isEmpty(), rv_recentStudentSubmit, tv_noRecentStudentSubmit);
        submissionAdapter = new SubmissionAdapter(latest, SubmissionItemMode.DASHBOARD);
        rv_recentStudentSubmit.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_recentStudentSubmit.setAdapter(submissionAdapter);
    }

    private void setupStaticListeners() {
        ll_viewTodayScheduleAction.setOnClickListener(v -> {
             Tool.loadFragment(requireActivity(), R.id.nhf_ts_FragmentContainer, new TeacherScheduleList());
        });
        ll_createTaskAction.setOnClickListener(v -> {
             startActivity(new Intent(requireContext(), TeacherCreateTask.class));
        });
        ll_addAgendaAction.setOnClickListener(v -> {
             startActivity(new Intent(requireContext(), TeacherAddAgenda.class));
        });
        ll_addDTAAction.setOnClickListener(v -> {
             startActivity(new Intent(requireContext(), TeacherAddDTA.class));
        });
        ll_createCourseAction.setOnClickListener(v -> {
             startActivity(new Intent(requireContext(), CreateCourse.class));
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
