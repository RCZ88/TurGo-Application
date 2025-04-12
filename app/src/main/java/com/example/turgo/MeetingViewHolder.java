package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MeetingViewHolder extends RecyclerView.ViewHolder{
    TextView tv_date, tv_day, tv_timeFromUntil, tv_CourseName;
    public MeetingViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_date = itemView.findViewById(R.id.tv_md_MeetingDate);
        tv_day = itemView.findViewById(R.id.tv_md_MeetingDay);
        tv_timeFromUntil = itemView.findViewById(R.id.tv_md_MeetingFromUntil);
        tv_CourseName = itemView.findViewById(R.id.tv_md_CourseName);
    }
}
