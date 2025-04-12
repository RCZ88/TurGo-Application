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
    private OnItemClickListener<TimeSlot>listener;
    private DayOfWeek day;
    private int selectedPosition = RecyclerView.NO_POSITION;
    public TimeSlotAdapter(ArrayList<TimeSlot>ts, OnItemClickListener<TimeSlot>listener, DayOfWeek day){
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

        if (position == selectedPosition) {
            holder.itemView.setAlpha(0.5f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v ->{
            int previousPosition = selectedPosition;
            selectedPosition = position;

            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }


}
