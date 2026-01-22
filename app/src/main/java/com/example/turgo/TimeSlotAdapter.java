package com.example.turgo;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.util.ArrayList;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotViewHolder> {
    private ArrayList<TimeSlot>timeSlots;
    private OnItemClickListener<SelectedIndicatorHelper<TimeSlot>>listener;
    private TimeSlotViewHolder currentHolder;
    private DayOfWeek day;
    private int selectedPosition = RecyclerView.NO_POSITION;
    public TimeSlotAdapter(ArrayList<TimeSlot>ts, OnItemClickListener<SelectedIndicatorHelper<TimeSlot>>listener, DayOfWeek day){
        timeSlots = ts;
        this.listener = listener;
        this.day = day;
    }
    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_text_display, parent, false);
        return new TimeSlotViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.tv_timeSlot.setText(timeSlots.get(position).toStr());
        holder.setTS(timeSlots.get(position));

    }

    public ArrayList<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public TimeSlotViewHolder getCurrentHolder() {
        return currentHolder;
    }

    public void setCurrentHolder(TimeSlotViewHolder currentHolder) {
        this.currentHolder = currentHolder;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTimeSlots(ArrayList<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }
    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }


}
