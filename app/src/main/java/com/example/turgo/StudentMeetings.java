package com.example.turgo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;

public class StudentMeetings extends Fragment {
    private static final String TAG = "StudentMeetings";

    private RecyclerView rvFutureMeeting;
    private RecyclerView rvPastMeetings;
    private TextView tvMeetingOfCourse;

    private Course course;
    private Student student;

    private final ArrayList<Meeting> futureMeetings = new ArrayList<>();
    private final ArrayList<Meeting> pastMeetings = new ArrayList<>();

    private MeetingAdapter futureMeetingAdapter;
    private MeetingAdapter pastMeetingAdapter;

    public StudentMeetings() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            course = (Course) getArguments().getSerializable(Course.SERIALIZE_KEY_CODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_meetings, container, false);

        rvFutureMeeting = view.findViewById(R.id.rv_SM_futureMeetings);
        rvPastMeetings = view.findViewById(R.id.rv_SM_pastMeetings);
        tvMeetingOfCourse = view.findViewById(R.id.tv_MeetingOfCourse);

        pastMeetingAdapter = new MeetingAdapter(pastMeetings, new ArrayList<>());
        rvPastMeetings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPastMeetings.setAdapter(pastMeetingAdapter);

        futureMeetingAdapter = new MeetingAdapter(futureMeetings, new ArrayList<>());
        rvFutureMeeting.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFutureMeeting.setAdapter(futureMeetingAdapter);

        loadItemsAsync();
        return view;
    }

    private void loadItemsAsync() {
        if (!isAdded()) {
            return;
        }
        if (course == null && getArguments() != null) {
            course = (Course) getArguments().getSerializable(Course.SERIALIZE_KEY_CODE);
        }

        StudentScreen studentScreen = (StudentScreen) getActivity();
        if (studentScreen == null) {
            Log.w(TAG, "StudentScreen activity is null");
            return;
        }
        student = studentScreen.getStudent();
        if (student == null) {
            Log.w(TAG, "Student is null, cannot load meetings");
            return;
        }
        if (course == null) {
            Log.w(TAG, "Course is null, cannot load meetings");
            return;
        }

        if (tvMeetingOfCourse != null) {
            tvMeetingOfCourse.setText(Tool.boolOf(course.getCourseName()) ? course.getCourseName() : "Meetings");
        }

        student.getAllMeetingOfCourse(course, 10)
                .addOnSuccessListener(loadedMeetings -> {
                    if (!isUiReady()) {
                        return;
                    }
                    applyMeetings(loadedMeetings != null ? loadedMeetings : new ArrayList<>());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed loading meetings for course " + course.getID(), e));
    }

    private void applyMeetings(ArrayList<Meeting> meetings) {
        ArrayList<Meeting> nextFuture = new ArrayList<>();
        ArrayList<Meeting> nextPast = new ArrayList<>();

        for (Meeting meeting : meetings) {
            if (meeting == null) {
                continue;
            }
            if (isUpcoming(meeting)) {
                nextFuture.add(meeting);
            } else {
                nextPast.add(meeting);
            }
        }

        nextFuture.sort(Comparator
                .comparing(Meeting::getDateOfMeeting, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(Meeting::getStartTimeChange, Comparator.nullsLast(LocalTime::compareTo)));

        nextPast.sort((a, b) -> {
            LocalDate aDate = a.getDateOfMeeting();
            LocalDate bDate = b.getDateOfMeeting();
            if (aDate == null && bDate == null) {
                return 0;
            }
            if (aDate == null) {
                return 1;
            }
            if (bDate == null) {
                return -1;
            }
            int dateCompare = bDate.compareTo(aDate);
            if (dateCompare != 0) {
                return dateCompare;
            }
            LocalTime aStart = a.getStartTimeChange();
            LocalTime bStart = b.getStartTimeChange();
            if (aStart == null && bStart == null) {
                return 0;
            }
            if (aStart == null) {
                return 1;
            }
            if (bStart == null) {
                return -1;
            }
            return bStart.compareTo(aStart);
        });

        futureMeetings.clear();
        futureMeetings.addAll(nextFuture);
        pastMeetings.clear();
        pastMeetings.addAll(nextPast);

        if (futureMeetingAdapter != null) {
            futureMeetingAdapter.notifyDataSetChanged();
        }
        if (pastMeetingAdapter != null) {
            pastMeetingAdapter.notifyDataSetChanged();
        }
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

    private boolean isUiReady() {
        return isAdded() && rvFutureMeeting != null && rvPastMeetings != null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rvFutureMeeting = null;
        rvPastMeetings = null;
        tvMeetingOfCourse = null;
        futureMeetingAdapter = null;
        pastMeetingAdapter = null;
    }
}
