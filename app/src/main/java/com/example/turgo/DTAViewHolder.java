package com.example.turgo;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DTAViewHolder extends RecyclerView.ViewHolder {
    TextView tvDayOfWeek, tvTimeRange, tvMaxMeeting;
    ImageButton btn_remove;
    int position;

    @SuppressLint("WrongViewCast")
    public DTAViewHolder(@NonNull View itemView, OnItemClickListener<Integer>listener) {
        super(itemView);
        tvDayOfWeek = itemView.findViewById(R.id.tv_DVH_DayOfWeek);
        tvTimeRange = itemView.findViewById(R.id.tv_DVH_TimeRange);
        tvMaxMeeting = itemView.findViewById(R.id.tv_DVH_MaxMeeting);
        btn_remove = itemView.findViewById(R.id.btn_DVH_RemoveDTA);
        itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(position);
            }
        });
        btn_remove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }
}
