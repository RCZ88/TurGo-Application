package com.example.turgo;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DayMeetingAdapter extends RecyclerView.Adapter<DayScheduleViewHolder> implements ModeUpdatable{
    ArrayList<MeetingToDisplay>meetings;
    private final Set<Integer> expandedPositions = new HashSet<>();

    public DayMeetingAdapter(Teacher teacher, boolean timeFrame) {
        meetings = new ArrayList<>();
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
        if (holder.rv_schedulesOfDay.getLayoutManager() == null) {
            holder.rv_schedulesOfDay.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        }
        holder.rv_schedulesOfDay.setAdapter(new MeetingSimpleAdapter(meetingToDisplay.schedules));
        boolean expanded = expandedPositions.contains(position);
        holder.rv_schedulesOfDay.setVisibility(expanded ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> toggleExpanded(holder));
        holder.tv_dayName.setOnClickListener(v -> toggleExpanded(holder));
    }

    private void toggleExpanded(DayScheduleViewHolder holder) {
        int adapterPosition = holder.getBindingAdapterPosition();
        if (adapterPosition == RecyclerView.NO_POSITION) {
            return;
        }
        if (expandedPositions.contains(adapterPosition)) {
            expandedPositions.remove(adapterPosition);
        } else {
            expandedPositions.add(adapterPosition);
        }
        notifyItemChanged(adapterPosition);
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void getMeetingsOfMode(Teacher teacher, boolean timeFrame){
        meetings.clear();
        expandedPositions.clear();
        if (teacher == null || teacher.getScheduledMeetings() == null) {
            notifyDataSetChanged();
            return;
        }
        Log.d("DayMeetingAdapter", "Teacher Prescheduled Meetings: " + teacher.getScheduledMeetings().size());
        Log.d("DayMeetingAdapter", "Teacher Completed Meetings: " + (teacher.getCompletedMeetings() == null ? 0 : teacher.getCompletedMeetings().size()));

        LocalDate today = LocalDate.now();
        if(timeFrame){
            ArrayList<Meeting> meetingsToday = new ArrayList<>();
            for (Meeting meeting : teacher.getScheduledMeetings()) {
                if (meeting == null || meeting.getDateOfMeeting() == null) {
                    continue;
                }
                if (!meeting.isCompleted() && meeting.getDateOfMeeting().isEqual(today)) {
                    meetingsToday.add(meeting);
                }
            }
            if (!meetingsToday.isEmpty()) {
                meetings.add(new MeetingToDisplay(today.getDayOfWeek().toString(), meetingsToday));
            }
        }else{
            ArrayList<MeetingToDisplay>meetingsOfDay = new ArrayList<>();
            for(DayOfWeek day : DayOfWeek.values()){
                ArrayList<Meeting> meetingOfDay = new ArrayList<>();
                for(Meeting meeting : teacher.getScheduledMeetings()){
                    if(meeting == null || meeting.getDateOfMeeting() == null){
                        continue;
                    }
                    LocalDate meetingDate = meeting.getDateOfMeeting();
                    boolean isUpcomingOrToday = !meetingDate.isBefore(today);
                    if(isUpcomingOrToday && meetingDate.getDayOfWeek() == day && !meeting.isCompleted()){
                        meetingOfDay.add(meeting);
                    }
                }
                if (!meetingOfDay.isEmpty()) {
                    meetingsOfDay.add(new MeetingToDisplay(day.toString(),meetingOfDay));
                }
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
