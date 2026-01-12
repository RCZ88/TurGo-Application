package com.example.turgo;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DayScheduleAdapter extends RecyclerView.Adapter<DayScheduleViewHolder> implements ModeUpdatable{
    ArrayList<ScheduleToDisplay>daySchedules;

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
//        dayFilled =0;
//        for(DayOfWeek day : DayOfWeek.values()){
//            ArrayList<Schedule>scheduleOfDay = Schedule.getScheduleOfDay(day, daySchedules);
//            if(!scheduleOfDay.isEmpty()){
//                dayFilled ++;
//            }
//            holder.tv_dayName.setText(day.toString());
//
//            ScheduleSimpleAdapter scheduleSimpleAdapter = new ScheduleSimpleAdapter(scheduleOfDay);
//            holder.rv_schedulesOfDay.setAdapter(scheduleSimpleAdapter);
//        }
        ScheduleToDisplay scheduleToDisplay = daySchedules.get(position);
        holder.tv_dayName.setText(scheduleToDisplay.day);
        holder.rv_schedulesOfDay.setAdapter(new ScheduleSimpleAdapter(scheduleToDisplay.schedules));
    }

    @Override
    public int getItemCount() {
        return daySchedules.size();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void getSchedulesOfMode(Teacher teacher, boolean timeFrame){
        Calendar calendar = Calendar.getInstance();
        DayOfWeek today = DayOfWeek.of(calendar.get(Calendar.DAY_OF_WEEK));

        if(timeFrame){
            for(DayTimeArrangement dta: teacher.getTimeArrangements()){
                if(dta.getDay() == today){
                    this.daySchedules.add(new ScheduleToDisplay(dta.toString(), dta.getOccupied()));
                    notifyDataSetChanged();
                    break;
                }
            }
        }else{
            ArrayList<ScheduleToDisplay> schedules = new ArrayList<>();
            for(DayTimeArrangement dta: teacher.getTimeArrangements()){
                schedules.add(new ScheduleToDisplay(dta.getDay().toString(), dta.getOccupied()));
            }
            this.daySchedules = schedules;
            notifyDataSetChanged();
        }
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
