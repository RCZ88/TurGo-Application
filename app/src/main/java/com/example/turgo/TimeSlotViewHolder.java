package com.example.turgo;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class TimeSlotViewHolder extends RecyclerView.ViewHolder {
    TextView tv_timeSlot;

    MaterialCardView mcv_bg;
    TimeSlot ts;
    static int exist =  Color.parseColor("#4F4789");
    static int nonExist = Color.parseColor("#FFB17A");
    boolean scheduleExist;
    public TimeSlotViewHolder(@NonNull View itemView, OnItemClickListener<SelectedIndicatorHelper<TimeSlot>>listener){
        super(itemView);
        tv_timeSlot = itemView.findViewById(R.id.tv_SingleText);
        mcv_bg = itemView.findViewById(R.id.cv_timeSlotCard);
        tv_timeSlot.setOnClickListener(view -> listener.onItemClick(new SelectedIndicatorHelper<>(this, ts)));
    }
    public void updateBackground(){

        Context context = itemView.getContext();
        int colorEmerald = ContextCompat.getColor(context, R.color.brand_emerald);
        int colorMint = ContextCompat.getColor(context, R.color.brand_emerald_pale);
        int colorWhite = ContextCompat.getColor(context, R.color.white_soft);
        int colorDarkText = ContextCompat.getColor(context, R.color.brand_emerald_dark);
        if(scheduleExist){
            mcv_bg.setCardBackgroundColor(colorMint);
            mcv_bg.setStrokeWidth(0);
            mcv_bg.setCardElevation(4f);
            tv_timeSlot.setTextColor(colorDarkText);
        }else{
            mcv_bg.setCardBackgroundColor(colorWhite);
            mcv_bg.setStrokeColor(colorEmerald);
            mcv_bg.setStrokeWidth(3); // 1.5dp approx in pixels (or use conversion)
            mcv_bg.setCardElevation(2f); // Lower elevation
            tv_timeSlot.setTextColor(colorEmerald);
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
