package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Student_Dashboard extends Fragment {

    RecyclerView rv_coursesCompletedThisWeek;
    FragmentContainerView fcv_upcomingSchedule;
    Student user;
    ArrayList<Task> studentTasks = new ArrayList<>();
    ViewPager2 vp2_listOfTasks;

    ProgressBar pb_progressThisWeek;
    ImageButton btn_details, ib_prevTask, ib_nextTask, btn_scanQR;
    TaskPagerAdapter adapter;
    TextView tv_noScheduleFound, tv_noTaskFound, tv_noMeetingCompleted;
    LinearLayout ll_Task;
    boolean expanded;

    Schedule scheduleOfNextMeeting;
    Course course;
    String roomId;
    ArrayList<Course> coursesForScheduleAdapter = new ArrayList<>();
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
        rv_coursesCompletedThisWeek = view.findViewById(R.id.rv_CourseCompleted);
        tv_noScheduleFound = view.findViewById(R.id.tv_SD_ScheduleEmpty);
        tv_noMeetingCompleted = view.findViewById(R.id.tv_SD_NoMeetingsCompleted);
        tv_noTaskFound = view.findViewById(R.id.tv_SD_NoTask);
        ib_nextTask = view.findViewById(R.id.ib_NextTask);
        ib_prevTask = view.findViewById(R.id.ib_PrevTask);
        btn_scanQR = view.findViewById(R.id.btn_ScanQRAttendance);
        btn_details = view.findViewById(R.id.ib_SD_ShowDetails);
        ll_Task = view.findViewById(R.id.ll_SD_TaskToDo);
        fcv_upcomingSchedule = view.findViewById(R.id.fcv_UpcomingClass);
        pb_progressThisWeek = view.findViewById(R.id.pb_WeeksProgress);
        vp2_listOfTasks = view.findViewById(R.id.vp2_TasksContainer);

        // Get student instance
        StudentScreen activity = (StudentScreen) getActivity();
        if (activity == null) throw new IllegalStateException("Activity is null");
        user = activity.getStudent();
        Log.d("StudentDashboard", "Student Retrieved: " + user);

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
        coursesForScheduleAdapter = new ArrayList<>();
        for(Schedule schedule : schedulesCompleted){
            schedule.getScheduleOfCourse().addOnSuccessListener(course ->{
                coursesForScheduleAdapter.add(course);
            });
        }

        rv_coursesCompletedThisWeek.setLayoutManager(new LinearLayoutManager(getContext()));
        ScheduleAdapter sa = new ScheduleAdapter(schedulesCompleted, coursesForScheduleAdapter);
        rv_coursesCompletedThisWeek.setAdapter(sa);

        boolean empty = schedulesCompleted == null || schedulesCompleted.isEmpty();
        Tool.handleEmpty(empty, rv_coursesCompletedThisWeek, tv_noMeetingCompleted);
    }

    private void loadProgress() {
        double progress = user.getPercentageCompleted();
        pb_progressThisWeek.setProgress((int) Math.ceil(progress));
    }

    private void loadTasks() {
        studentTasks = user.getUncompletedTask();
        Tool.handleEmpty(studentTasks == null || studentTasks.isEmpty(), ll_Task, tv_noTaskFound);

        if (studentTasks != null && !studentTasks.isEmpty()) {
            adapter = new TaskPagerAdapter(getActivity(), studentTasks);
            vp2_listOfTasks.setAdapter(adapter);
        }
    }

    private void loadUpcomingMeeting() {
        nextMeeting = user.getNextMeeting();
        Tool.handleEmpty(nextMeeting == null, fcv_upcomingSchedule, tv_noScheduleFound);

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
                    roomId = result.getID();
                });
        // synchronous

        // Setup fragments
        MeetingDisplay meetingDisplay = new MeetingDisplay();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Student.SERIALIZE_KEY_CODE, user);
        meetingDisplay.setArguments(bundle);

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fcv_UpcomingClass, meetingDisplay);
        fragmentTransaction.commit();

        CourseDetails courseDetails = new CourseDetails();
        courseDetails.tv_room.setText(roomId);
        courseDetails.tv_duration.setText(scheduleOfNextMeeting.getDuration());
        courseDetails.tv_teacherName.setText(teacher.getFullName());
        course.getDaysOfSchedule(user).addOnSuccessListener(days->{
            courseDetails.tv_dayOfWeek.setText(days);
        });


        LocalDate closestMeeting = user.getClosestMeetingOfCourse(course);
        courseDetails.tv_nextMeetingDate.setText(
                closestMeeting != null ? closestMeeting.toString() : "No Meeting Found!"
        );

        Glide.with(requireContext())
                .load(course.getLogo())
                .into(courseDetails.iv_courseLogo);

        expanded = false;

        btn_details.setOnClickListener(v -> {
            Fragment fragmentToLoad = expanded ? meetingDisplay : courseDetails;
            expanded = !expanded;
            Tool.loadFragment(requireActivity(), fcv_upcomingSchedule.getId(), fragmentToLoad);
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
