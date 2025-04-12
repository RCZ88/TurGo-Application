package com.example.turgo;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.util.ArrayList;

public class DayAdapter extends RecyclerView.Adapter<DayViewHolder> {
    private ArrayList<DayOfWeek> days;
    protected OnItemClickListener<DayOfWeek> listener;
    private int selectedPosition =  RecyclerView.NO_POSITION;

    public DayAdapter(ArrayList<DayOfWeek>days, OnItemClickListener<DayOfWeek>listener){
        this.days = days;
        this.listener = listener;
    }
    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_text_display, parent, false);
        return new DayViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.tv_Day.setText(days.get(position).toString());
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
        return days.size();
    }
}
