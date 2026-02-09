package com.example.turgo;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class RcScheduleAdapter extends RecyclerView.Adapter<rcScheduleViewHolder>{
    ArrayList<TimeSlot>slots;
    OnItemClickListener<Integer>clickListener;
    boolean deleteButton;
    public RcScheduleAdapter(ArrayList<TimeSlot>slots, boolean deleteButton){
        this.slots = slots;
        this.deleteButton = deleteButton;
    }
    @NonNull
    @Override
    public rcScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rc_schedule_item, parent, false);
        return new rcScheduleViewHolder(view, clickListener, deleteButton);
    }

    public void setClickListener(OnItemClickListener<Integer> clickListener) {
        this.clickListener = clickListener;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull rcScheduleViewHolder holder, int position) {
        TimeSlot ts = slots.get(position);
        String pplCountText;
        Pair<Integer, Integer> maxMin = ts.getPersonCount();
        if(maxMin.one ==1 && maxMin.one.equals(maxMin.two)){
            pplCountText ="Private";
        }else{
            pplCountText = maxMin.two + "-" + maxMin.one + " People";
        }
        holder.tv_amtPeople.setText(pplCountText);
        holder.tv_day.setText(ts.getDay().toString());
        holder.tv_time.setText(Tool.formatTime24h(ts.getStart()) + "-" + Tool.formatTime24h(ts.getEnd()));
        holder.setId(position);
    }
    @SuppressLint("NotifyDataSetChanged")
    public void addSlot(TimeSlot ts){
        slots.add(ts);
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void removeSlot(int index){
        slots.remove(index);
        notifyDataSetChanged();
    }

    public ArrayList<TimeSlot> getSlots() {
        return slots;
    }

    public void setSlots(ArrayList<TimeSlot> slots) {
        this.slots = slots;
    }

    public OnItemClickListener<Integer> getClickListener() {
        return clickListener;
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }
}
