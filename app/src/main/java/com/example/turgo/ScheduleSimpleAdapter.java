package com.example.turgo;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;

public class ScheduleSimpleAdapter extends RecyclerView.Adapter<ScheduleSimpleViewHolder>{

    private ArrayList<Schedule> schedules;

    public ScheduleSimpleAdapter(ArrayList<Schedule>schedules){
        this.schedules = schedules;
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
        Schedule schedule = schedules.get(position);
        holder.tv_CourseName.setText(schedule.getScheduleOfCourse().getCourseName());
        holder.tv_timeRange.setText(schedule.getMeetingStart().toString() + "-" + schedule.getMeetingEnd().toString());
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

}
