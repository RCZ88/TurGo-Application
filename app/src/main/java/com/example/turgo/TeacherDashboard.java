package com.example.turgo;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeacherDashboard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeacherDashboard extends Fragment implements RequiresDataLoading{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    Button btn_viewTodaySchedule, btn_CreateTask, btn_addAgenda, btn_addDTA;
    TextView tv_courseName, tv_scheduleTime, tv_meetingRoom
            ,tv_noActiveCourses, tv_noRecentStudentSubmit;
    RecyclerView rv_activeCourses, rv_recentStudentSubmit;

    ArrayList<Course> teacherCoursesTeach;
    ArrayList<Meeting>nextMeetingOfCourses;
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
        Log.d("TeacherDashboard", "========== onCreateView START ==========");
        Log.d("TeacherDashboard", "inflater: " + inflater);
        Log.d("TeacherDashboard", "container: " + container);
        Log.d("TeacherDashboard", "container width: " + (container != null ? container.getWidth() : "null"));
        Log.d("TeacherDashboard", "container height: " + (container != null ? container.getHeight() : "null"));

        View view = inflater.inflate(R.layout.fragment_teacher_dashboard, container, false);
        Log.d("TeacherDashboard", "✓ View inflated: " + view);
        Log.d("TeacherDashboard", "View class: " + view.getClass().getName());
        Log.d("TeacherDashboard", "View LayoutParams: " + view.getLayoutParams());

        // Find all views
        Log.d("TeacherDashboard", "Finding views...");
        btn_viewTodaySchedule = view.findViewById(R.id.btn_TD_TodaysSchedule);
        Log.d("TeacherDashboard", "btn_viewTodaySchedule: " + (btn_viewTodaySchedule != null ? "FOUND" : "NULL"));

        btn_CreateTask = view.findViewById(R.id.btn_TD_CreateTask);
        Log.d("TeacherDashboard", "btn_CreateTask: " + (btn_CreateTask != null ? "FOUND" : "NULL"));

        btn_addAgenda = view.findViewById(R.id.btn_TD_AddAgenda);
        Log.d("TeacherDashboard", "btn_addAgenda: " + (btn_addAgenda != null ? "FOUND" : "NULL"));

        btn_addDTA = view.findViewById(R.id.btn_TD_AddDTA);
        Log.d("TeacherDashboard", "btn_addDTA: " + (btn_addDTA != null ? "FOUND" : "NULL"));


        tv_courseName = view.findViewById(R.id.tv_TD_courseName);
        Log.d("TeacherDashboard", "tv_courseName: " + (tv_courseName != null ? "FOUND" : "NULL"));
        if (tv_courseName != null) {
            Log.d("TeacherDashboard", "tv_courseName text: " + tv_courseName.getText());
            Log.d("TeacherDashboard", "tv_courseName visibility: " + tv_courseName.getVisibility());
        }

        tv_scheduleTime = view.findViewById(R.id.tv_TD_ScheduleTime);
        Log.d("TeacherDashboard", "tv_scheduleTime: " + (tv_scheduleTime != null ? "FOUND" : "NULL"));

        tv_meetingRoom = view.findViewById(R.id.tv_TD_MeetingRoom);
        Log.d("TeacherDashboard", "tv_meetingRoom: " + (tv_meetingRoom != null ? "FOUND" : "NULL"));

        tv_noActiveCourses = view.findViewById(R.id.tv_TD_NoActiveCourses);
        Log.d("TeacherDashboard", "tv_noActiveCourses: " + (tv_noActiveCourses != null ? "FOUND" : "NULL"));
        if (tv_noActiveCourses != null) {
            Log.d("TeacherDashboard", "tv_noActiveCourses visibility: " + tv_noActiveCourses.getVisibility());
        }

        tv_noRecentStudentSubmit = view.findViewById(R.id.tv_TD_NoRecentSubmission);
        Log.d("TeacherDashboard", "tv_noRecentStudentSubmit: " + (tv_noRecentStudentSubmit != null ? "FOUND" : "NULL"));

        rv_activeCourses = view.findViewById(R.id.rv_TD_ActiveCourses);
        Log.d("TeacherDashboard", "rv_activeCourses: " + (rv_activeCourses != null ? "FOUND" : "NULL"));

        rv_recentStudentSubmit = view.findViewById(R.id.rv_TD_RecentStudentSubmit);
        Log.d("TeacherDashboard", "rv_recentStudentSubmit: " + (rv_recentStudentSubmit != null ? "FOUND" : "NULL"));

        Log.d("TeacherDashboard", "Getting teacher from activity...");
        Teacher teacher = ((TeacherScreen)requireActivity()).getTeacher();

        if(teacher != null){
            Log.d("TeacherDashboard", "✓ Teacher obtained: " + teacher.getFullName());
        } else {
            Log.e("TeacherDashboard", "❌ Teacher is NULL!");
            return view;
        }

        Log.d("TeacherDashboard", "Setting up button listeners...");

        btn_viewTodaySchedule.setOnClickListener(view1 -> {
            Log.d("TeacherDashboard", "View Today's Schedule clicked");
            TeacherScheduleList tesl = new TeacherScheduleList();
            Bundle bundle = new Bundle();
            bundle.putBoolean("isMeetingMode", false);
            bundle.putBoolean("isDaily", true);
            tesl.setArguments(bundle);
            Tool.loadFragment(requireActivity(), R.id.nhf_ts_FragmentContainer, tesl);
        });

        btn_CreateTask.setOnClickListener(view1 -> {
            Log.d("TeacherDashboard", "Create Task clicked");
            TeacherCreateTask tct = new TeacherCreateTask();
            Tool.loadFragment(requireActivity(), R.id.nhf_ts_FragmentContainer, tct);
        });

        btn_addAgenda.setOnClickListener(view1 -> {
            Log.d("TeacherDashboard", "Add Agenda clicked");
            TeacherAddAgenda taa = new TeacherAddAgenda();
            Tool.loadFragment(requireActivity(), R.id.nhf_ts_FragmentContainer, taa);
        });

        btn_addDTA.setOnClickListener(view1 -> {
            Log.d("TeacherDashboard", "Add DTA clicked");
            TeacherAddDTA tad = new TeacherAddDTA();
            Tool.loadFragment(requireActivity(), R.id.nhf_ts_FragmentContainer, tad);
        });
        

        Log.d("TeacherDashboard", "Processing courses...");
        ArrayList<Course> coursesTeach = new ArrayList<>();

        if(teacherCoursesTeach != null){
            coursesTeach = teacherCoursesTeach;
            Log.d("TeacherDashboard", "Courses count: " + coursesTeach.size());
        } else {
            Log.d("TeacherDashboard", "No courses (null)");
        }

        Log.d("TeacherDashboard", "Calling Tool.handleEmpty for courses...");
        Tool.handleEmpty(teacherCoursesTeach == null || teacherCoursesTeach.isEmpty(),
                rv_activeCourses, tv_noActiveCourses);
        Log.d("TeacherDashboard", "After handleEmpty - rv_activeCourses visibility: " + rv_activeCourses.getVisibility());
        Log.d("TeacherDashboard", "After handleEmpty - tv_noActiveCourses visibility: " + tv_noActiveCourses.getVisibility());

        Log.d("TeacherDashboard", "Creating CourseTeachersAdapter...");
        //async - completed
        CourseTeachersAdapter cta = new CourseTeachersAdapter(coursesTeach, nextMeetingOfCourses, new OnItemClickListener<>() {
            @Override
            public void onItemClick(Course item) {
                Log.d("TeacherDashboard", "Course clicked: " + item);
                TeacherCourseScreen tcs = new TeacherCourseScreen();
                Bundle bundle = new Bundle();
                bundle.putSerializable("course", item);
                tcs.setArguments(bundle);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.nhf_ts_FragmentContainer, tcs)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onItemLongClick(Course item) {
            }
        });

        Log.d("TeacherDashboard", "Setting adapter to rv_activeCourses...");
        rv_activeCourses.setAdapter(cta);
        Log.d("TeacherDashboard", "✓ Adapter set");

        Log.d("TeacherDashboard", "Processing submissions...");
        ArrayList<SubmissionDisplay> latestSubmission = new ArrayList<>();
        ArrayList<SubmissionDisplay> teacherLatestSubmission = teacher.getLatestSubmission();

        if(teacherLatestSubmission != null){
            latestSubmission = teacherLatestSubmission;
            Log.d("TeacherDashboard", "Submissions count: " + latestSubmission.size());
        } else {
            Log.d("TeacherDashboard", "No submissions (null)");
        }

        Log.d("TeacherDashboard", "Calling Tool.handleEmpty for submissions...");
        Tool.handleEmpty(teacherLatestSubmission == null || teacherLatestSubmission.isEmpty(),
                rv_recentStudentSubmit, tv_noRecentStudentSubmit);
        Log.d("TeacherDashboard", "After handleEmpty - rv_recentStudentSubmit visibility: " + rv_recentStudentSubmit.getVisibility());
        Log.d("TeacherDashboard", "After handleEmpty - tv_noRecentStudentSubmit visibility: " + tv_noRecentStudentSubmit.getVisibility());

        Log.d("TeacherDashboard", "Creating SubmissionAdapter...");
        SubmissionAdapter sa = new SubmissionAdapter(latestSubmission);
        Log.d("TeacherDashboard", "Setting adapter to rv_recentStudentSubmit...");
        rv_recentStudentSubmit.setAdapter(sa);
        Log.d("TeacherDashboard", "✓ Adapter set");

        Log.d("TeacherDashboard", "========== onCreateView END - returning view ==========");
        Log.d("TeacherDashboard", "Returned view parent: " + view.getParent());
        Log.d("TeacherDashboard", "Returned view visibility: " + view.getVisibility());

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("TeacherDashboard", "========== onViewCreated ==========");
        Log.d("TeacherDashboard", "View: " + view);
        Log.d("TeacherDashboard", "View parent: " + view.getParent());
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("TeacherDashboard", "========== onStart ==========");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("TeacherDashboard", "========== onResume ==========");
    }

    @Override
    public Bundle loadDataInBackground(Bundle input, TextView logLoading) {
        Teacher teacher = (Teacher) input.getSerializable(Teacher.SERIALIZE_KEY_CODE);
        ArrayList<Course> teacherCoursesTeach = teacher.getCoursesTeach();
        ArrayList<Meeting>nextMeetingOfCourses = teacherCoursesTeach.stream().map(course-> course.getNextMeetingOfNextSchedule()).collect(Collectors.toCollection(ArrayList::new));

        Bundle output = new Bundle();
        output.putSerializable("teacherCoursesTeach", teacherCoursesTeach);
        output.putSerializable("nextMeetingOfCourses", nextMeetingOfCourses);
        return output;
    }

    @Override
    public void onDataLoaded(Bundle preloadedData) {
        teacherCoursesTeach = (ArrayList<Course>) preloadedData.getSerializable("teacherCoursesTeacher");
        nextMeetingOfCourses = (ArrayList<Meeting>) preloadedData.getSerializable("nextMeetingOfCourses");
    }

    @Override
    public void onLoadingError(Exception error) {

    }
}