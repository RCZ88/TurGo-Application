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
    protected OnItemClickListener<SelectedIndicatorHelper<DayOfWeek>> listener;
    private int selectedPosition =  RecyclerView.NO_POSITION;

    public DayAdapter(ArrayList<DayOfWeek>days, OnItemClickListener<SelectedIndicatorHelper<DayOfWeek>>listener){
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

    }

    public ArrayList<DayOfWeek> getDays() {
        return days;
    }

    public void setDays(ArrayList<DayOfWeek> days) {
        this.days = days;
    }

    public OnItemClickListener<SelectedIndicatorHelper<DayOfWeek>> getListener() {
        return listener;
    }

    public void setListener(OnItemClickListener<SelectedIndicatorHelper<DayOfWeek>> listener) {
        this.listener = listener;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addDay(DayOfWeek day){
        if (days.contains(day)) {
            return;
        }
        this.days.add(day);
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return days.size();
    }
}
