package com.example.turgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TeacherScheduleAdapter extends RecyclerView.Adapter<TeacherScheduleViewHolder>{

     ArrayList<Schedule> scheduleList;

    public TeacherScheduleAdapter(ArrayList<Schedule> scheduleList) {
        this.scheduleList = scheduleList != null ? scheduleList : new ArrayList<>();
    }

    @NonNull
    @Override
    public TeacherScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scheduleteacher_viewholder, parent, false);
        return new TeacherScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherScheduleViewHolder holder, int position) {
        Schedule schedule = scheduleList.get(position);

        // Date: Use DayOfWeek + next occurrence (simplified — use your Tools class if available)
        holder.tvScheduleDate.setText(schedule.getNextMeetingDate().toString());

        // Time range
        String timeRange = schedule.getMeetingStart().format(DateTimeFormatter.ofPattern("HH:mm")) +
                " - " +
                schedule.getMeetingEnd().format(DateTimeFormatter.ofPattern("HH:mm"));
        holder.tvTimeConstraint.setText(timeRange);

        // Duration
        holder.tvDuration.setText(schedule.getDuration() + " minutes");

        // Private or Group
        if (schedule.isPrivate()) {
            holder.tvGroupPrivate.setText("Private");
            String studentName = (schedule.getStudents() != null && !schedule.getStudents().isEmpty())
                    ? schedule.getStudents().get(0).getFullName()
                    : "—";
            holder.tvStudentNumberORName.setText(studentName);
        } else {
            holder.tvGroupPrivate.setText("Group");
            int enrolled = schedule.getStudents() != null ? schedule.getStudents().size() : 0;
            int max = schedule.getScheduleOfCourse().getMaxStudentPerMeeting();
            holder.tvStudentNumberORName.setText(enrolled + "/" + max + " Students");
        }

        // Status
        String status = schedule.isHasScheduled() ? "Completed" : "Upcoming";
        holder.tvStatus.setText(status);

    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }
}
