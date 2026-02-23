package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

public class CourseAdminViewHolder extends RecyclerView.ViewHolder {

    // Declare the views from item_course.xml
    MaterialCardView cv_container;
    TextView tv_courseName, tv_courseType, tv_teacher, tv_baseCost, tv_studentCount;

    public CourseAdminViewHolder(@NonNull View itemView) {
        super(itemView);

        // Map the variables to the XML IDs
        cv_container = itemView.findViewById(R.id.cv_ICA_container);
        tv_courseName = itemView.findViewById(R.id.tv_ICA_courseName);
        tv_courseType = itemView.findViewById(R.id.tv_ICA_courseType);
        tv_teacher = itemView.findViewById(R.id.tv_ICA_teacher);
        tv_baseCost = itemView.findViewById(R.id.tv_ICA_baseCost);
        tv_studentCount = itemView.findViewById(R.id.tv_ICA_studentCount);
    }
}