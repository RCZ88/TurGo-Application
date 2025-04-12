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
    public TimeSlotViewHolder(@NonNull View itemView, OnItemClickListener<TimeSlot>listener){
        super(itemView);
        itemView.setOnClickListener(view -> listener.onItemClick(ts));
        if(ts.getExistingSchedule() != null){
            scheduleExist = true;
        }else{
            scheduleExist = false;
        }
        updateBackground();
    }
    public void updateBackground(){
        if(scheduleExist){
            itemView.setBackgroundColor(exist);
        }else{

            itemView.setBackgroundColor(nonExist);
        }
    }
    public void setTS(TimeSlot ts){
        this.ts = ts;
    }
}
