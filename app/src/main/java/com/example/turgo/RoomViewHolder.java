package com.example.turgo;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RoomViewHolder extends RecyclerView.ViewHolder {
    TextView tv_roomTag, tv_roomCapacity, tv_courseCompat;
    ImageButton btn_removeRoom;
    public RoomViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_roomTag = itemView.findViewById(R.id.tv_RIV_roomTag);
        tv_roomCapacity = itemView.findViewById(R.id.tv_RIV_RoomCapacity);
        tv_courseCompat = itemView.findViewById(R.id.tv_RIV_courseCompat);
        btn_removeRoom = itemView.findViewById(R.id.ib_RIV_RemoveRoom);
    }
}
