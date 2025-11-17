package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

public class DayMeetingAdapter extends RecyclerView.Adapter<DayScheduleViewHolder> implements ModeUpdatable{
    ArrayList<MeetingToDisplay>meetings;

    public DayMeetingAdapter(Teacher teacher, boolean timeFrame) {
        getMeetingsOfMode(teacher, timeFrame);
    }

    @NonNull
    @Override
    public DayScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_schedule_item, parent, false);
        return new DayScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayScheduleViewHolder holder, int position) {
        MeetingToDisplay meetingToDisplay = meetings.get(position);
        holder.tv_dayName.setText(meetingToDisplay.day);
        holder.rv_schedulesOfDay.setAdapter(new MeetingSimpleAdapter(meetingToDisplay.schedules));
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void getMeetingsOfMode(Teacher teacher, boolean timeFrame){
        LocalDate today = LocalDate.now();
        if(timeFrame){
            ArrayList<Meeting> meetings = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                meetings = (ArrayList<Meeting>) teacher.getScheduledMeetings().stream().filter(meeting -> !meeting.isCompleted() && meeting.getDateOfMeeting() == today).toList();
            }
            this.meetings.add(new MeetingToDisplay(today.toString(),meetings));
            notifyDataSetChanged();
        }else{
            ArrayList<MeetingToDisplay>meetingsOfDay = new ArrayList<>();
            for(DayOfWeek day : DayOfWeek.values()){
                ArrayList<Meeting> meetingOfDay = new ArrayList<>();
                for(Meeting meeting : teacher.getScheduledMeetings()){
                    if(meeting.getDateOfMeeting().getDayOfWeek() == day && !meeting.isCompleted()){
                        meetingOfDay.add(meeting);
                    }
                }
                meetingsOfDay.add(new MeetingToDisplay(day.toString(),meetingOfDay));
            }
            this.meetings = meetingsOfDay;

        }
        notifyDataSetChanged();
    }

    @Override
    public void updateMode(Teacher teacher, boolean timeFrame) {
        getMeetingsOfMode(teacher, timeFrame);
    }

    @Override
    public boolean isEmpty() {
        return meetings.isEmpty();
    }
}
