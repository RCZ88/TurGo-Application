package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DayTimeViewHolder extends RecyclerView.ViewHolder {
    TextView tv_day, tv_time;
    public DayTimeViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_day = itemView.findViewById(R.id.tv_dtvh_day);
        tv_time = itemView.findViewById(R.id.tv_dtvh_time);
    }
}
