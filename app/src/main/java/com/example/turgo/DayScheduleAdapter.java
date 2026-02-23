package com.example.turgo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DayScheduleAdapter extends RecyclerView.Adapter<DayScheduleViewHolder> implements ModeUpdatable{
    ArrayList<ScheduleToDisplay>daySchedules;
    private final Set<Integer> expandedPositions = new HashSet<>();

    public DayScheduleAdapter(Teacher teacher, boolean timeFrame) {
        daySchedules = new ArrayList<>();
        getSchedulesOfMode(teacher, timeFrame);
    }

    @NonNull
    @Override
    public DayScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_schedule_item, parent, false);
        return new DayScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayScheduleViewHolder holder, int position) {
        ScheduleToDisplay scheduleToDisplay = daySchedules.get(position);
        holder.tv_dayName.setText(scheduleToDisplay.day);
        if (holder.rv_schedulesOfDay.getLayoutManager() == null) {
            holder.rv_schedulesOfDay.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        }
        holder.rv_schedulesOfDay.setAdapter(new ScheduleSimpleAdapter(scheduleToDisplay.schedules));
        boolean expanded = expandedPositions.contains(position);
        holder.rv_schedulesOfDay.setVisibility(expanded ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> toggleExpanded(holder));
        holder.tv_dayName.setOnClickListener(v -> toggleExpanded(holder));
    }

    private void toggleExpanded(DayScheduleViewHolder holder) {
        int adapterPosition = holder.getBindingAdapterPosition();
        if (adapterPosition == RecyclerView.NO_POSITION) {
            return;
        }
        if (expandedPositions.contains(adapterPosition)) {
            expandedPositions.remove(adapterPosition);
        } else {
            expandedPositions.add(adapterPosition);
        }
        notifyItemChanged(adapterPosition);
    }

    @Override
    public int getItemCount() {
        return daySchedules.size();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void getSchedulesOfMode(Teacher teacher, boolean timeFrame){
        daySchedules.clear();
        expandedPositions.clear();
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        if (teacher == null || teacher.getTimeArrangements() == null) {
            notifyDataSetChanged();
            return;
        }

        if(timeFrame){
            for(DayTimeArrangement dta: teacher.getTimeArrangements()){
                if(dta.getDay() == today){
                    daySchedules.add(new ScheduleToDisplay(dta.getDay().toString(), dta.getOccupied()));
                    break;
                }
            }
        }else{
            for(DayTimeArrangement dta: teacher.getTimeArrangements()){
                daySchedules.add(new ScheduleToDisplay(dta.getDay().toString(), dta.getOccupied()));
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void updateMode(Teacher teacher, boolean timeFrame) {
        getSchedulesOfMode(teacher, timeFrame);
    }

    @Override
    public boolean isEmpty() {
        return daySchedules.isEmpty();
    }
}
