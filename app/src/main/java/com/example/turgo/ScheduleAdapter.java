package com.example.turgo;

import static com.example.turgo.Tool.boolOf;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.util.ArrayList;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder>{
    private ArrayList<Schedule> schedules;
    private ArrayList<Course> courses;

    public ScheduleAdapter(ArrayList<Schedule> schedules, ArrayList<Course> courses){
        this.schedules = schedules;
        this.courses = courses;
    }

    public ScheduleAdapter(ArrayList<Schedule> schedules, Course course){
        this.schedules = schedules;
        this.courses = new ArrayList<>();
        if (schedules != null && course != null) {
            for (int i = 0; i < schedules.size(); i++) {
                this.courses.add(course);
            }
        }
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
        if (schedules == null || courses == null || position >= schedules.size() || position >= courses.size()) {
            return;
        }
        Schedule schedule = schedules.get(position);
        Course course = courses.get(position);

        holder.tv_subject.setText(course.getCourseName());
        holder.tv_day.setText(formatDay(schedule.getDay()));
        holder.tv_time.setText(Tool.stringifyStartEndTime(schedule.getMeetingStart(), schedule.getMeetingEnd()));
        holder.tv_duration.setText(formatDuration(schedule.getDuration()));
    }

    @Override
    public int getItemCount() {
        return schedules == null ? 0 : schedules.size();
    }

    public ArrayList<Course> getCourses(){
        return courses;
    }

    private static String formatDuration(int minutes) {
        if (minutes <= 0) {
            return "0m";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours > 0 && mins > 0) {
            return hours + "h " + mins + "m";
        }
        if (hours > 0) {
            return hours + "h";
        }
        return mins + "m";
    }

    private static String formatDay(DayOfWeek day) {
        if (day == null) {
            return "";
        }
        String name = day.toString().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
