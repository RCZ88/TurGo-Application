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
        holder.tv_dateMonth.setVisibility(View.GONE);
        schedule.getScheduleOfCourse().addOnSuccessListener(course ->{
            holder.tv_CourseName.setText(course.getCourseName());
            holder.tv_timeRange.setText(schedule.getMeetingStart().toString() + "-" + schedule.getMeetingEnd().toString());
        });
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

}
