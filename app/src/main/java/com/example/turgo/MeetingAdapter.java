package com.example.turgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingViewHolder>{
    ArrayList<Meeting> meetings;

    public MeetingAdapter(ArrayList<Meeting>meetings){
        this.meetings = meetings;
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
        holder.tv_CourseName.setText(meeting.getMeetingOfSchedule().getScheduleOfCourse().getCourseName());
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }
}
