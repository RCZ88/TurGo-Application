package com.example.turgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingViewHolder>{
    ArrayList<Meeting> meetings;
    ArrayList<Course> courses;
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("EEE", Locale.getDefault());
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault());

    public MeetingAdapter(ArrayList<Meeting> meetings, ArrayList<Course> courses){
        this.meetings = meetings;
        this.courses = courses;
    }

    @NonNull
    @Override
    public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meeting_display, parent, false);
        return new MeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        Meeting meeting = meetings.get(position);
        if (meeting == null) {
            holder.tv_timeFromUntil.setText("--:--");
            holder.tv_date.setText("--");
            holder.tv_day.setText("---");
            holder.tv_CourseName.setText("Course unavailable");
            holder.tv_roomAddress.setText("Room TBD");
            return;
        }

        String boundMeetingId = meeting.getID();
        holder.itemView.setTag(boundMeetingId);

        String start = formatTime(meeting.getStartTimeChange());
        String end = formatTime(meeting.getEndTimeChange());
        holder.tv_timeFromUntil.setText(start + " - " + end);

        bindDate(holder, meeting.getDateOfMeeting());
        holder.tv_CourseName.setText("Loading course...");
        holder.tv_roomAddress.setText("Room TBD");

        meeting.getMeetingOfSchedule()
                .addOnSuccessListener(schedule -> {
                    if (!isSameBoundMeeting(holder, boundMeetingId) || schedule == null) {
                        return;
                    }

                    if (Tool.boolOf(schedule.getOfCourse())) {
                        schedule.getScheduleOfCourse()
                                .addOnSuccessListener(course -> {
                                    if (!isSameBoundMeeting(holder, boundMeetingId)) {
                                        return;
                                    }
                                    if (course != null && Tool.boolOf(course.getCourseName())) {
                                        holder.tv_CourseName.setText(course.getCourseName());
                                    } else {
                                        holder.tv_CourseName.setText("Course");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (isSameBoundMeeting(holder, boundMeetingId)) {
                                        holder.tv_CourseName.setText("Course");
                                    }
                                });
                    } else {
                        holder.tv_CourseName.setText("Course");
                    }

                    if (Tool.boolOf(schedule.getRoomId())) {
                        schedule.getRoom()
                                .addOnSuccessListener(room -> {
                                    if (!isSameBoundMeeting(holder, boundMeetingId)) {
                                        return;
                                    }
                                    if (room != null && Tool.boolOf(room.getRoomTag())) {
                                        holder.tv_roomAddress.setText(room.getRoomTag());
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (isSameBoundMeeting(holder, boundMeetingId)) {
                                        holder.tv_roomAddress.setText("Room TBD");
                                    }
                                });
                    } else {
                        holder.tv_roomAddress.setText("Room TBD");
                    }
                })
                .addOnFailureListener(e -> {
                    if (isSameBoundMeeting(holder, boundMeetingId)) {
                        holder.tv_CourseName.setText("Course");
                        holder.tv_roomAddress.setText("Room TBD");
                    }
                });

    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }

    private static String formatTime(LocalTime time) {
        if (time == null) {
            return "--:--";
        }
        return TIME_FORMAT.format(time).toUpperCase(Locale.getDefault());
    }

    private static void bindDate(MeetingViewHolder holder, LocalDate date) {
        if (date == null) {
            holder.tv_date.setText("--");
            holder.tv_day.setText("---");
            return;
        }
        holder.tv_date.setText(String.valueOf(date.getDayOfMonth()));
        holder.tv_day.setText(DAY_FORMAT.format(date).toUpperCase(Locale.getDefault()));
    }

    private static boolean isSameBoundMeeting(MeetingViewHolder holder, String expectedMeetingId) {
        Object tag = holder.itemView.getTag();
        return expectedMeetingId != null && expectedMeetingId.equals(tag);
    }
}
