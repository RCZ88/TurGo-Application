package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;

public class DayViewHolder extends RecyclerView.ViewHolder {
    TextView tv_Day;
    public DayViewHolder(@NonNull View itemView, OnItemClickListener<DayOfWeek>listener) {
        super(itemView);
        tv_Day = itemView.findViewById(R.id.tv_Day);
        itemView.setOnClickListener(view -> {
            listener.onItemClick(DayOfWeek.valueOf((String) tv_Day.getText()));
        });
    }
}
