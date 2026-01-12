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

public class MeetingAdapter extends RecyclerView.Adapter<MeetingViewHolder>{
    ArrayList<Meeting> meetings;
    ArrayList<Course>courses;

    public MeetingAdapter(ArrayList<Meeting>meetings, ArrayList<Course>courses){
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
        String startEnd = meeting.getStartTimeChange().toString() + " - " + meeting.getEndTimeChange().toString();
        holder.tv_timeFromUntil.setText(startEnd);
        holder.tv_date.setText(meeting.getDateOfMeeting().getDayOfYear());
        holder.tv_day.setText(meeting.getDateOfMeeting().getDayOfWeek().toString());
        Schedule schedule = Await.get(meeting::getMeetingOfSchedule);
        Course course = Await.get(schedule::getScheduleOfCourse);
        holder.tv_CourseName.setText(course.getCourseName());
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }
}
