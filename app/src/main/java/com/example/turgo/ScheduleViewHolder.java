package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ScheduleViewHolder extends RecyclerView.ViewHolder {
    TextView tv_subject;
    TextView tv_day;
    TextView tv_time;
    TextView tv_duration;
    public ScheduleViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_subject = itemView.findViewById(R.id.tv_sdl_subjectToDisplay);
        tv_day = itemView.findViewById(R.id.tv_sdl_Day);
        tv_time = itemView.findViewById(R.id.tv_sdl_Time);
        tv_duration = itemView.findViewById(R.id.tv_sdl_Duration);
    }

}
