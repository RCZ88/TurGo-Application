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
//        Submission s1 = new Submission(user, "5f4afa7e-f844-44d8-bcf2-b74cfa056178");
//        Submission s2 = new Submission(user, "d3b3a67f-f130-44aa-9c9d-e8a9b9da6266");
//        Submission s3 = new Submission(user, "6c8c377e-e11e-40ea-b904-2c32a0d7f23e");
//        ArrayList<Submission>submissions = new ArrayList<>();
//        submissions.add(s1);
//        submissions.add(s2);
//        submissions.add(s3);
//
//        for (Submission submission : submissions) {
//            SubmissionRepository submissionRepository = new SubmissionRepository(submission.getID());
//            submissionRepository.save(submission);
//        }

        // Load all data synchronously
        loadCompletedSchedules();
        loadProgress();
        loadTasks();
        loadUpcomingMeeting();
        setupTaskNavigation();
        btn_scanQR.setOnClickListener(this::attendMeetingScan);

        return view;
    }

    private void loadCompletedSchedules() {
        schedulesCompleted = user.getScheduleCompletedThisWeek();
        int completedThisWeek = user.getCompletedScheduleCountThisWeek();
        Log.d("StudentDashboard", "Schedule Completed This Week Count: " + completedThisWeek);
        tv_meetingsCompletedCount.setText(String.valueOf(completedThisWeek));
        String label = completedThisWeek == 1
                ? "meeting completed this week"
                : "meetings completed this week";
        tv_noMeetingCompleted.setText(label);
    }

    private void loadProgress() {
        double progress = user.getPercentageCompleted();
        int progressInt = (int) Math.ceil(progress);
        pb_progressThisWeek.setMax(100);
        pb_progressThisWeek.setProgressCompat(progressInt, true);
        tv_weeklyProgressPercent.setText(progressInt + "%");
    }

    private void loadTasks() {
        studentTasks = user.getUncompletedTask();
        Log.d("StudentDashboard", "Student Tasks: " + studentTasks);
        Tool.handleEmpty(studentTasks == null || studentTasks.isEmpty(), ll_Task, tv_noTaskFound);

        if (studentTasks != null && !studentTasks.isEmpty()) {
            adapter = new TaskAdapter(studentTasks, user, new OnItemClickListener<>() {
                @Override
                public void onItemClick(Task item) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Task.SERIALIZE_KEY_CODE, item);

                    TaskFullPage tfp = new TaskFullPage();
                    tfp.setArguments(bundle);
                    Tool.loadFragment(requireActivity(), R.id.nhf_ss_FragContainer, tfp);
                }

                @Override
                public void onItemLongClick(Task item) {

                }
            }, TaskItemMode.VIEW_PAGER);
            vp2_listOfTasks.setAdapter(adapter);
        }
    }

    private void loadUpcomingMeeting() {
        nextMeeting = user.getNextMeeting();
        Tool.handleEmpty(Tool.boolOf(nextMeeting), fcv_upcomingSchedule, tv_noScheduleFound);

        if (nextMeeting == null) return;

        nextMeeting.getMeetingOfSchedule()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw Objects.requireNonNull(task.getException());

                    scheduleOfNextMeeting = task.getResult();
                    return scheduleOfNextMeeting.getScheduleOfCourse();
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw Objects.requireNonNull(task.getException());

                    course = task.getResult();
                    return course.getTeacher(); // must return Task<Teacher>
                })
                .continueWithTask(task -> {
                    teacher = task.getResult();
                    return scheduleOfNextMeeting.getRoom();
                }).addOnSuccessListener(result->{
                    roomTag = result.getRoomTag();
                    continueAfterObjectsLoaded();
                });
        // synchronous
        // Setup fragments
    }

    private void continueAfterObjectsLoaded(){
        if (!isAdded() || getView() == null || fcv_upcomingSchedule == null) {
            return;
        }

        MeetingDisplay meetingDisplay = new MeetingDisplay();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Student.SERIALIZE_KEY_CODE, user);
        String meetingTitle = course != null ? course.getCourseName() : "Course";
        String meetingTime = scheduleOfNextMeeting != null && scheduleOfNextMeeting.getMeetingStart() != null
                ? scheduleOfNextMeeting.getMeetingStart().toString()
                : "-";

        String meetingDate = nextMeeting != null && nextMeeting.getDateOfMeeting() != null
                ? nextMeeting.getDateOfMeeting().toString()
                : "-";

        String meetingLogo = course != null ? course.getLogo() : "";
        bundle.putAll(MeetingDisplay.createArgs(meetingTitle, meetingTime, meetingDate, meetingLogo));
        meetingDisplay.setArguments(bundle);

        FragmentManager fragmentManager = getChildFragmentManager();
        if (!fragmentManager.isStateSaved()) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fcv_UpcomingClass, meetingDisplay);
            fragmentTransaction.commit();
        }

        CourseDetails courseDetails = new CourseDetails();
        String durationText = String.valueOf(scheduleOfNextMeeting.getDuration());
        String teacherName = teacher != null ? teacher.getFullName() : "Unknown";
        String daysOfSchedule = course != null ? course.getDaysOfSchedule(user) : "-";
        String roomText = Tool.boolOf(roomTag) ? roomTag : "No Room";
        String nextMeetingDate = nextMeeting != null && nextMeeting.getDateOfMeeting() != null
                ? nextMeeting.getDateOfMeeting().toString()
                : "No Meeting Found!";
        String courseName = course != null ? course.getCourseName() : "Course";
        String courseLogo = course != null ? course.getLogo() : "";

        courseDetails.setArguments(CourseDetails.createArgs(
                courseName,
                teacherName,
                nextMeetingDate,
                daysOfSchedule,
                durationText,
                roomText,
                courseLogo
        ));

        expanded = false;

        btn_details.setOnClickListener(v -> {
            if (!isAdded() || getView() == null || fcv_upcomingSchedule == null) {
                return;
            }
            Fragment fragmentToLoad = expanded ? meetingDisplay : courseDetails;
            expanded = !expanded;
            FragmentManager childFm = getChildFragmentManager();
            if (!childFm.isStateSaved()) {
                childFm.beginTransaction()
                        .replace(R.id.fcv_UpcomingClass, fragmentToLoad)
                        .commit();
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
