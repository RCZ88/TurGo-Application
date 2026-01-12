package com.example.turgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder>{
    private ArrayList<Schedule> schedules;
    private ArrayList<Course>courses;

    public ScheduleAdapter(ArrayList<Schedule> schedules, ArrayList<Course>courses){
        this.schedules = schedules;
        this.courses = courses;
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
        //Course course = Await.get(schedule::getScheduleOfCourse);
        Course course = courses.get(position);
        //async - completed
        holder.tv_duration.setText(schedule.getDuration());
        holder.tv_subject.setText(course.getCourseName());
        holder.tv_day.setText(schedule.getDay().toString());
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }
}
