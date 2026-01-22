package com.example.turgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DayTimeAdapter extends RecyclerView.Adapter<DayTimeViewHolder> {
    ArrayList<String>day, time;
    public DayTimeAdapter(ArrayList<String> day, ArrayList<String>time){
        this.day = day;
        this.time = time;
    }
    @NonNull
    @Override
    public DayTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_time_viewholder, parent, false);
        return new DayTimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayTimeViewHolder holder, int position) {
        holder.tv_day.setText(day.get(position));
        holder.tv_time.setText(time.get(position));
    }

    @Override
    public int getItemCount() {
        return day.size();
    }
}
