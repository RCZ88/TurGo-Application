package com.example.turgo;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CourseViewHolder extends RecyclerView.ViewHolder{
    ImageView iv_courseIcon;
    TextView tv_CourseName, tv_courseTeacher, tv_nextMeeting;
    CourseAdapter courseAdapter;
    public CourseViewHolder(@NonNull View itemView, OnItemClickListener<Course> listener, CourseAdapter adapter) {
        super(itemView);
        iv_courseIcon = itemView.findViewById(R.id.iv_cdnf_CourseIcon);
        tv_CourseName = itemView.findViewById(R.id.tv_cdnf_CourseTitle);
        tv_courseTeacher = itemView.findViewById(R.id.tv_cdnf_TeacherName);
        tv_nextMeeting = itemView.findViewById(R.id.tv_cdnf_MeetingDates);

        itemView.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Course course = adapter.getCourses().get(position);
                listener.onItemClick(course);
            }
        });

    }

    /*@Override
    public void onClick(View view) {
        Course course = courseAdapter.getCourses().get(getAdapterPosition());

    }*/
}
