package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.time.DayOfWeek;

public class DayViewHolder extends RecyclerView.ViewHolder {
    TextView tv_Day;
    public MaterialCardView cardView;
    public DayViewHolder(@NonNull View itemView, OnItemClickListener<SelectedIndicatorHelper<DayOfWeek>>listener) {
        super(itemView);
        tv_Day = itemView.findViewById(R.id.tv_SingleText);
        cardView = itemView.findViewById(R.id.cv_timeSlotCard);
        tv_Day.setOnClickListener(view -> {
            listener.onItemClick(new SelectedIndicatorHelper<>(this, DayOfWeek.valueOf((String) tv_Day.getText())));
        });
    }
}
