package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DayScheduleViewHolder extends RecyclerView.ViewHolder{
    RecyclerView rv_schedulesOfDay;
    TextView tv_dayName;
    public DayScheduleViewHolder(@NonNull View itemView) {
        super(itemView);
        rv_schedulesOfDay = itemView.findViewById(R.id.rv_DSI_Schedules);
        tv_dayName = itemView.findViewById(R.id.tv_DSI_Day);
    }
}
