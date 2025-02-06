package com.example.turgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder>{
    private ArrayList<Schedule> schedules;

    public ScheduleAdapter(ArrayList<Schedule> schedules){
        this.schedules = schedules;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.schedule_display_layout, parent, false);

        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        Schedule schedule = schedules.get(position);
        holder.tv_duration.setText(schedule.getDuration());
        holder.tv_subject.setText(schedule.getScheduleOfCourse().getCourseName());
        holder.tv_day.setText(schedule.getDay().toString());
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }
}
