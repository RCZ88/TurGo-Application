package com.example.turgo;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CourseTeachersAdapter extends RecyclerView.Adapter<CourseTeachersViewHolder>{
    ArrayList<Course>courses;
    OnItemClickListener<Course>listener;

    public CourseTeachersAdapter(ArrayList<Course> courses, OnItemClickListener<Course>listener) {
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseTeachersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_teacher_viewholder, parent, false);
        return new CourseTeachersViewHolder(view, listener, courses.get(viewType));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CourseTeachersViewHolder holder, int position) {
        holder.tv_nextSchedule.setText(courses.get(position).getNextMeetingOfNextSchedule().getDateOfMeeting().toString());
        holder.tv_courseName.setText(courses.get(position).getCourseName());
        holder.tv_numberOfStudents.setText(courses.get(position).getStudents().size() + "Student(s)");
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }
}
