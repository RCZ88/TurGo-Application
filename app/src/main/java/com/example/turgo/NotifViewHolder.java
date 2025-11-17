package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotifViewHolder extends RecyclerView.ViewHolder {
    public TextView tv_header, tv_content;
    public NotifViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_header = itemView.findViewById(R.id.tv_notif_TitleHeader);
        tv_content = itemView.findViewById(R.id.tv_notif_Content);
    }
}
