package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TeacherScheduleViewHolder extends RecyclerView.ViewHolder{
    public TextView tvScheduleDate;
    public TextView tvTimeConstraint;
    public TextView tvDuration;
    public TextView tvGroupPrivate;
    public TextView tvStudentNumberORName;
    public TextView tvStatus;

    public TeacherScheduleViewHolder(@NonNull View itemView) {
        super(itemView);
        tvScheduleDate = itemView.findViewById(R.id.tv_STVH_ScheduleDate);
        tvTimeConstraint = itemView.findViewById(R.id.tv_SVTH_TimeConstraint);
        tvDuration = itemView.findViewById(R.id.tv_SVTH_Duration);
        tvGroupPrivate = itemView.findViewById(R.id.tv_SVTH_GroupPrivate);
        tvStudentNumberORName = itemView.findViewById(R.id.tv_SVTH_StudentNumberORName);
        tvStatus = itemView.findViewById(R.id.tv_SVTH_Status);
    }
}
