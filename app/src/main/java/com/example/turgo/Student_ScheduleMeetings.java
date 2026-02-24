package com.example.turgo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Student_ScheduleMeetings extends Fragment {

    private enum Section {
        SCHEDULES,
        UPCOMING,
        HISTORY
    }

    private static final String KEY_SECTION = "ssm_section";

    private Student student;
    private RecyclerView rvContent;
    private LinearLayout llEmpty;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;
    private TextView tvStatsSchedules;
    private TextView tvStatsUpcoming;
    private TextView tvStatsHistory;
    private MaterialButtonToggleGroup tgSections;

    private final ArrayList<Schedule> schedules = new ArrayList<>();
    private final ArrayList<Meeting> upcomingMeetings = new ArrayList<>();
    private final ArrayList<Meeting> meetingHistory = new ArrayList<>();

    private ScheduleAdapter schedulesAdapter;
    private MeetingAdapter upcomingAdapter;
    private MeetingAdapter historyAdapter;

    private Section selectedSection = Section.UPCOMING;
    private boolean isSchedulesLoading = true;

    public Student_ScheduleMeetings() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            String sectionName = savedInstanceState.getString(KEY_SECTION, Section.UPCOMING.name());
            try {
                selectedSection = Section.valueOf(sectionName);
            } catch (IllegalArgumentException ignored) {
                selectedSection = Section.UPCOMING;
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_schedule_meetings, container, false);

        rvContent = view.findViewById(R.id.rv_SSM_Content);
        llEmpty = view.findViewById(R.id.ll_SSM_Empty);
        tvEmptyTitle = view.findViewById(R.id.tv_SSM_EmptyTitle);
        tvEmptySubtitle = view.findViewById(R.id.tv_SSM_EmptySubtitle);
        tvStatsSchedules = view.findViewById(R.id.tv_SSM_StatSchedules);
        tvStatsUpcoming = view.findViewById(R.id.tv_SSM_StatUpcoming);
        tvStatsHistory = view.findViewById(R.id.tv_SSM_StatHistory);
        tgSections = view.findViewById(R.id.tg_SSM_Sections);

        rvContent.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvContent.setAdapter(null);

        tgSections.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.btn_SSM_Schedules) {
                selectedSection = Section.SCHEDULES;
            } else if (checkedId == R.id.btn_SSM_History) {
                selectedSection = Section.HISTORY;
            } else {
                selectedSection = Section.UPCOMING;
            }
            renderSelectedSection();
        });

        if (selectedSection == Section.SCHEDULES) {
            tgSections.check(R.id.btn_SSM_Schedules);
        } else if (selectedSection == Section.HISTORY) {
            tgSections.check(R.id.btn_SSM_History);
        } else {
            tgSections.check(R.id.btn_SSM_Upcoming);
        }

        StudentScreen activity = (StudentScreen) getActivity();
        if (activity == null) {
            return view;
        }
        student = activity.getStudent();
        if (student == null) {
            showEmpty("Student data unavailable", "Reload this page to fetch meeting data.");
            return view;
        }

        loadScheduleSection();
        loadMeetingSections();
        updateStats();
        renderSelectedSection();
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SECTION, selectedSection.name());
    }

    private void loadScheduleSection() {
        schedules.clear();
        if (student.getAllSchedules() != null) {
            schedules.addAll(student.getAllSchedules());
        }

        schedules.sort(
                Comparator.comparing(Schedule::getDay, Comparator.nullsLast(DayOfWeek::compareTo))
                        .thenComparing(Schedule::getMeetingStart, Comparator.nullsLast(LocalTime::compareTo))
        );

        if (schedules.isEmpty()) {
            schedulesAdapter = null;
            isSchedulesLoading = false;
            updateStats();
            renderSelectedSection();
            return;
        }

        List<Task<Course>> courseTasks = new ArrayList<>();
        for (Schedule schedule : schedules) {
            courseTasks.add(schedule.getScheduleOfCourse());
        }

        Tasks.whenAllSuccess(courseTasks)
                .addOnSuccessListener(results -> {
                    ArrayList<Course> courses = new ArrayList<>();
                    for (Object item : results) {
                        courses.add((Course) item);
                    }
                    schedulesAdapter = new ScheduleAdapter(schedules, courses);
                    isSchedulesLoading = false;
                    updateStats();
                    renderSelectedSection();
                })
                .addOnFailureListener(e -> {
                    schedulesAdapter = null;
                    isSchedulesLoading = false;
                    renderSelectedSection();
                });
    }

    private void loadMeetingSections() {
        upcomingMeetings.clear();
        if (student.getPreScheduledMeetings() != null) {
            upcomingMeetings.addAll(student.getPreScheduledMeetings());
        }
        Log.d("Student_ScheduleMeetings", "Before Remove if (line 181): " + upcomingMeetings);
        upcomingMeetings.removeIf(m -> m == null || !isUpcoming(m));
        Log.d("Student_ScheduleMeetings", "After Remove if (line 183): " + upcomingMeetings);
        upcomingMeetings.sort(Comparator
                .comparing(Meeting::getDateOfMeeting)
                .thenComparing(Meeting::getStartTimeChange, Comparator.nullsLast(LocalTime::compareTo)));
        upcomingAdapter = upcomingMeetings.isEmpty() ? null : new MeetingAdapter(upcomingMeetings, new ArrayList<>());

        meetingHistory.clear();
        if (student.getMeetingHistory() != null) {
            meetingHistory.addAll(student.getMeetingHistory());
        }
        meetingHistory.removeIf(m -> m == null);
        meetingHistory.sort((a, b) -> {
            LocalDate da = a.getDateOfMeeting();
            LocalDate db = b.getDateOfMeeting();
            if (da == null && db == null) {
                return 0;
            }
            if (da == null) {
                return 1;
            }
            if (db == null) {
                return -1;
            }
            return db.compareTo(da);
        });
        historyAdapter = meetingHistory.isEmpty() ? null : new MeetingAdapter(meetingHistory, new ArrayList<>());
        updateStats();
        renderSelectedSection();
    }

    private boolean isUpcoming(Meeting meeting) {
        if (meeting.getDateOfMeeting() == null || meeting.isCompleted()) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (meeting.getDateOfMeeting().isAfter(today)) {
            return true;
        }
        if (meeting.getDateOfMeeting().isBefore(today)) {
            return false;
        }
        LocalTime end = meeting.getEndTimeChange();
        return end == null || end.isAfter(LocalTime.now());
    }

    private void renderSelectedSection() {
        if (rvContent == null || llEmpty == null) {
            return;
        }
        if (selectedSection == Section.SCHEDULES) {
            if (isSchedulesLoading) {
                showEmpty("Loading schedules", "Fetching your weekly schedule blocks.");
                return;
            }
            if (schedulesAdapter == null) {
                showEmpty("No schedules found", "Join a course to see schedule blocks here.");
                return;
            }
            showList(schedulesAdapter);
            return;
        }

        if (selectedSection == Section.UPCOMING) {
            if (upcomingAdapter == null) {
                showEmpty("No upcoming meetings", "Your future sessions will appear here.");
                return;
            }
            showList(upcomingAdapter);
            return;
        }

        if (historyAdapter == null) {
            showEmpty("No meeting history", "Completed meetings will appear here.");
            return;
        }
        showList(historyAdapter);
    }

    private void showList(RecyclerView.Adapter<?> adapter) {
        rvContent.setAdapter(adapter);
        Tool.handleEmpty(false, rvContent, llEmpty);
    }

    private void showEmpty(String title, String subtitle) {
        tvEmptyTitle.setText(title);
        tvEmptySubtitle.setText(subtitle);
        Tool.handleEmpty(true, rvContent, llEmpty);
    }

    private void updateStats() {
        if (tvStatsSchedules == null || tvStatsUpcoming == null || tvStatsHistory == null) {
            return;
        }
        tvStatsSchedules.setText(String.valueOf(schedules.size()));
        tvStatsUpcoming.setText(String.valueOf(upcomingMeetings.size()));
        tvStatsHistory.setText(String.valueOf(meetingHistory.size()));
    }
}
