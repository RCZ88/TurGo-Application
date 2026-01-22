package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;

public class DayViewHolder extends RecyclerView.ViewHolder {
    TextView tv_Day;
    public DayViewHolder(@NonNull View itemView, OnItemClickListener<SelectedIndicatorHelper<DayOfWeek>>listener) {
        super(itemView);
        tv_Day = itemView.findViewById(R.id.tv_SingleText);
        tv_Day.setOnClickListener(view -> {
            listener.onItemClick(new SelectedIndicatorHelper<>(this, DayOfWeek.valueOf((String) tv_Day.getText())));
        });
    }
}
