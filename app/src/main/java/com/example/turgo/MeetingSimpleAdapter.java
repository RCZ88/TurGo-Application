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
import java.util.ArrayList;

public class MeetingSimpleAdapter extends RecyclerView.Adapter<ScheduleSimpleViewHolder>{
    ArrayList<Meeting> meetings;
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
        holder.tv_timeRange.setText(meeting.getStartTimeChange().toString() + " - "  + meeting.getEndTimeChange());
        meeting.getMeetingOfCourse().addOnSuccessListener(course -> holder.tv_CourseName.setText(course.getCourseName()));
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }
}
