package com.example.turgo;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class TimeSlotViewHolder extends RecyclerView.ViewHolder {
    TextView tv_timeSlot;
    TimeSlot ts;
    static int exist =  Color.parseColor("#4F4789");
    static int nonExist = Color.parseColor("#FFB17A");
    boolean scheduleExist;
    public TimeSlotViewHolder(@NonNull View itemView, OnItemClickListener<SelectedIndicatorHelper<TimeSlot>>listener){
        super(itemView);
        tv_timeSlot = itemView.findViewById(R.id.tv_SingleText);
        tv_timeSlot.setOnClickListener(view -> listener.onItemClick(new SelectedIndicatorHelper<>(this, ts)));
    }
    public void updateBackground(){
        if(scheduleExist){
            tv_timeSlot.setBackgroundColor(exist);
        }else{

            tv_timeSlot.setBackgroundColor(nonExist);
        }
    }
    public void setTS(TimeSlot ts){
        this.ts = ts;
        if(!ts.getSchedules().isEmpty()){
            scheduleExist = true;
        }else{
            scheduleExist = false;
        }
        updateBackground();
    }
}
