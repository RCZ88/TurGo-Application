package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Student_Dashboard extends Fragment {

    FragmentContainerView fcv_upcomingSchedule;
    Student user;
    ArrayList<Task> studentTasks = new ArrayList<>();
    ViewPager2 vp2_listOfTasks;

    CircularProgressIndicator pb_progressThisWeek;
    ImageButton btn_details, ib_prevTask, ib_nextTask, btn_scanQR;
    TaskAdapter adapter;
    TextView tv_noScheduleFound, tv_noTaskFound, tv_noMeetingCompleted, tv_meetingsCompletedCount, tv_weeklyProgressPercent;
    LinearLayout ll_Task;
    boolean expanded;

    Schedule scheduleOfNextMeeting;
    Course course;
    String roomTag;
    ArrayList<Schedule> schedulesCompleted = new ArrayList<>();
    Meeting nextMeeting;
    Teacher teacher;

    private Meeting currentMeeting;

    public Student_Dashboard() {}

    @SuppressLint({"MissingInflatedId", "CommitTransaction", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_student_dashboard, container, false);

        // Initialize UI components
        tv_noScheduleFound = view.findViewById(R.id.tv_SD_ScheduleEmpty);
        tv_noMeetingCompleted = view.findViewById(R.id.tv_SD_NoMeetingsCompleted);
        tv_meetingsCompletedCount = view.findViewById(R.id.tv_SD_MeetingsCompletedCount);
        tv_weeklyProgressPercent = view.findViewById(R.id.tv_SD_WeekProgressPercent);
        tv_noTaskFound = view.findViewById(R.id.tv_SD_NoTask);
        ib_nextTask = view.findViewById(R.id.ib_NextTask);
        ib_prevTask = view.findViewById(R.id.ib_PrevTask);
        btn_scanQR = view.findViewById(R.id.btn_ScanQRAttendance);
        btn_details = view.findViewById(R.id.ib_SD_ShowDetails);
        ll_Task = view.findViewById(R.id.ll_SD_TaskToDo);
        fcv_upcomingSchedule = view.findViewById(R.id.fcv_UpcomingClass);
        pb_progressThisWeek = view.findViewById(R.id.pb_WeeksProgress);
        vp2_listOfTasks = view.findViewById(R.id.vp2_TasksContainer);
        MeetingProcessorWorker.enqueueCleanupNow(requireContext());

        // Get student instance
        StudentScreen activity = (StudentScreen) getActivity();
        if (activity == null) throw new IllegalStateException("Activity is null");
        user = activity.getStudent();
        Log.d("StudentDashboard", "Student Retrieved: " + user);

        // Optimized selective loading
        new StudentRepository(user.getID()).loadDashboardData()
                .addOnSuccessListener(data -> {
                    if (!isAdded()) return;
                    renderDashboard(data);
                })
                .addOnFailureListener(e -> {
                    Log.e("StudentDashboard", "Failed to load dashboard data", e);
                });

        setupTaskNavigation();
        btn_scanQR.setOnClickListener(this::attendMeetingScan);

        return view;
    }

    private void renderDashboard(StudentDashboardData data) {
        // 1. Progress
        int progressInt = (int) Math.ceil(data.percentageCompleted);
        pb_progressThisWeek.setMax(100);
        pb_progressThisWeek.setProgressCompat(progressInt, true);
        tv_weeklyProgressPercent.setText(progressInt + "%");

        // 2. Completed Schedules
        int completedCount = data.scheduleCompletedThisWeekIds != null ? data.scheduleCompletedThisWeekIds.size() : 0;
        tv_meetingsCompletedCount.setText(String.valueOf(completedCount));
        tv_noMeetingCompleted.setText(completedCount == 1 ? "meeting completed this week" : "meetings completed this week");

        // 3. Tasks
        studentTasks = data.uncompletedTasks != null ? data.uncompletedTasks : new ArrayList<>();
        Tool.handleEmpty(studentTasks.isEmpty(), ll_Task, tv_noTaskFound);
        if (!studentTasks.isEmpty()) {
            adapter = new TaskAdapter(studentTasks, user, new OnItemClickListener<>() {
                @Override public void onItemClick(Task item) {
                    Bundle b = new Bundle();
                    b.putSerializable(Task.SERIALIZE_KEY_CODE, item);
                    TaskFullPage tfp = new TaskFullPage();
                    tfp.setArguments(b);
                    Tool.loadFragment(requireActivity(), R.id.nhf_ss_FragContainer, tfp);
                }
                @Override public void onItemLongClick(Task item) {}
            }, TaskItemMode.VIEW_PAGER);
            vp2_listOfTasks.setAdapter(adapter);
        }

        // 4. Upcoming Meeting
        if (Tool.boolOf(data.nextMeetingId)) {
            Tool.handleEmpty(true, fcv_upcomingSchedule, tv_noScheduleFound);
            setupUpcomingMeetingFragments(data);
        } else {
            Tool.handleEmpty(false, fcv_upcomingSchedule, tv_noScheduleFound);
        }
    }

    private void setupUpcomingMeetingFragments(StudentDashboardData data) {
        if (!isAdded() || getView() == null || fcv_upcomingSchedule == null) return;

        MeetingDisplay meetingDisplay = new MeetingDisplay();
        Bundle bundleFirst = new Bundle();
        bundleFirst.putSerializable(Student.SERIALIZE_KEY_CODE, user);
        
        String mTitle = Tool.boolOf(data.nextCourseName) ? data.nextCourseName : "Course";
        String mTime  = Tool.boolOf(data.nextMeetingTime) ? data.nextMeetingTime : "-";
        String mDate  = Tool.boolOf(data.nextMeetingDate) ? data.nextMeetingDate : "-";
        String mLogo  = Tool.boolOf(data.nextCourseLogo) ? data.nextCourseLogo : "";
        
        bundleFirst.putAll(MeetingDisplay.createArgs(mTitle, mTime, mDate, mLogo));
        meetingDisplay.setArguments(bundleFirst);

        FragmentManager fm = getChildFragmentManager();
        if (!fm.isStateSaved()) {
            fm.beginTransaction().replace(R.id.fcv_UpcomingClass, meetingDisplay).commit();
        }

        CourseDetails courseDetails = new CourseDetails();
        courseDetails.setArguments(CourseDetails.createArgs(
                mTitle,
                Tool.boolOf(data.nextTeacherName) ? data.nextTeacherName : "Unknown",
                mDate,
                Tool.boolOf(data.nextCourseDays) ? data.nextCourseDays : "-",
                String.valueOf(data.nextCourseDuration),
                Tool.boolOf(data.nextRoomTag) ? data.nextRoomTag : "No Room",
                mLogo
        ));

        expanded = false;
        btn_details.setOnClickListener(v -> {
            if (!isAdded() || getView() == null) return;
            Fragment toLoad = expanded ? meetingDisplay : courseDetails;
            expanded = !expanded;
            FragmentManager cfm = getChildFragmentManager();
            if (!cfm.isStateSaved()) {
                cfm.beginTransaction().replace(R.id.fcv_UpcomingClass, toLoad).commit();
            }
            btn_details.setImageResource(expanded ? R.drawable.caret : R.drawable.caret_down);
        });
    }

    private void setupTaskNavigation() {
        ib_nextTask.setOnClickListener(v -> {
            int currentItem = vp2_listOfTasks.getCurrentItem();
            if (adapter != null && currentItem < adapter.getItemCount() - 1) {
                vp2_listOfTasks.setCurrentItem(currentItem + 1, true);
            }
        });

        ib_prevTask.setOnClickListener(v -> {
            int currentItem = vp2_listOfTasks.getCurrentItem();
            if (currentItem > 0) {
                vp2_listOfTasks.setCurrentItem(currentItem - 1, true);
            }
        });
    }

    public void attendMeetingScan(View view) {
        QRCmanager.scanCode(barLauncher);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    AtomicReference<Schedule> schedule = new AtomicReference<>();

                    currentMeeting = new Meeting();
                    currentMeeting.getMeetingOfSchedule().addOnSuccessListener(mos->{
                        schedule.set(mos);
                        user.attendMeeting(currentMeeting);
                        int hour = schedule.get().getMeetingEnd().getHour();
                        int minute = schedule.get().getMeetingEnd().getMinute();
                        TimeChecker.addTimer(hour, minute, currentMeeting);
                        Toast.makeText(getContext(), "Successful!", Toast.LENGTH_SHORT).show();
                    });
                }
            }
    );
}
