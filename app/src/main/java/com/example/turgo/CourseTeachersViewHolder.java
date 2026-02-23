package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CourseTeachersViewHolder extends RecyclerView.ViewHolder {
    TextView tv_courseName, tv_nextSchedule, tv_numberOfStudents;
    Course course;
    public CourseTeachersViewHolder(@NonNull View itemView, OnItemClickListener<Course> listener) {
        super(itemView);
        tv_courseName = itemView.findViewById(R.id.tv_TVH_CourseName);
        tv_nextSchedule = itemView.findViewById(R.id.tv_TVH_NextScheduleTime);
        tv_numberOfStudents = itemView.findViewById(R.id.tv_TVH_StudentCount);
        itemView.setOnClickListener(view -> {
            if (listener != null && course != null) {
                listener.onItemClick(course);
            }
        });
    }
}
