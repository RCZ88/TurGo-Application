package com.example.turgo;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CourseImageViewHolder extends RecyclerView.ViewHolder {
    ImageView iv_image;
    public CourseImageViewHolder(@NonNull View itemView) {
        super(itemView);
        iv_image = itemView.findViewById(R.id.iv_courseImage);
    }

}
