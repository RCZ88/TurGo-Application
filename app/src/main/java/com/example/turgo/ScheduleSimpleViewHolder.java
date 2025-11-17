package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ScheduleSimpleViewHolder extends RecyclerView.ViewHolder {
    TextView tv_timeRange, tv_CourseName;
    public ScheduleSimpleViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_timeRange = itemView.findViewById(R.id.tv_SSD_TimeRange);
        tv_CourseName = itemView.findViewById(R.id.tv_SSD_CourseName);
    }
}
