package com.example.turgo;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class MeetingSimpleAdapter extends RecyclerView.Adapter<ScheduleSimpleViewHolder>{
    ArrayList<Meeting> meetings;
    private static final DateTimeFormatter DATE_MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault());
    public MeetingSimpleAdapter(ArrayList<Meeting> meetings) {
        this.meetings = meetings;
    }
    @NonNull
    @Override
    public ScheduleSimpleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.schedule_simple_display, parent, false);
        return new ScheduleSimpleViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ScheduleSimpleViewHolder holder, int position) {
        Meeting meeting = meetings.get(position);
        String start = meeting.getStartTimeChange() != null ? meeting.getStartTimeChange().toString() : "--:--";
        String end = meeting.getEndTimeChange() != null ? meeting.getEndTimeChange().toString() : "--:--";
        holder.tv_timeRange.setText(start + " - " + end);
        holder.tv_dateMonth.setVisibility(View.VISIBLE);
        if (meeting.getDateOfMeeting() != null) {
            holder.tv_dateMonth.setText(meeting.getDateOfMeeting().format(DATE_MONTH_FORMATTER));
        } else {
            holder.tv_dateMonth.setText("No Date");
        }
        meeting.getMeetingOfCourse().addOnSuccessListener(course -> holder.tv_CourseName.setText(course.getCourseName()));
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }
}
