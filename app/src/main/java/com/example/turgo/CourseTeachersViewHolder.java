package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CourseTeachersViewHolder extends RecyclerView.ViewHolder {
    TextView tv_courseName, tv_nextSchedule, tv_numberOfStudents;
    public CourseTeachersViewHolder(@NonNull View itemView, OnItemClickListener<Course> listener, Course course) {
        super(itemView);
        tv_courseName = itemView.findViewById(R.id.tv_TVH_CourseName);
        tv_nextSchedule = itemView.findViewById(R.id.tv_TVH_NextScheduleTime);
        tv_numberOfStudents = itemView.findViewById(R.id.tv_TVH_StudentCount);
        itemView.setOnClickListener(view -> listener.onItemClick(course));
    }
}
