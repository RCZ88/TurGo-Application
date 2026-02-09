package com.example.turgo;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class rcScheduleViewHolder extends RecyclerView.ViewHolder {
    TextView tv_day, tv_time, tv_amtPeople;
    ImageButton btn_remove;
    int id;

    public rcScheduleViewHolder(@NonNull View itemView, OnItemClickListener<Integer>clickListener, boolean removeButton) {
        super(itemView);
        btn_remove = itemView.findViewById(R.id.ib_rcsi_removeSchedule);
        tv_day =itemView.findViewById(R.id.tv_rcsi_day);
        tv_time = itemView.findViewById(R.id.tv_rcsi_time);
        tv_amtPeople = itemView.findViewById(R.id.tv_rcsi_amtOfPerson);
        if(removeButton){
            btn_remove.setVisibility(View.VISIBLE);
            btn_remove.setOnClickListener(view->{
                clickListener.onItemClick(id);
            });
        }else{
            btn_remove.setVisibility(View.GONE);
        }
    }
    public void setId(int id){
        this.id = id;
    }
}
