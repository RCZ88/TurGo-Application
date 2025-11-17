package com.example.turgo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<CourseViewHolder>{
    private final ArrayList<Course>courses;
    protected OnItemClickListener<Course> listener;
    private Context context;
    Student student;
    public CourseAdapter(ArrayList<Course> courses, Student student, OnItemClickListener<Course> listener, Context context){
        this.courses = courses;
        this.student = student;
        this.listener = listener;
        this.context = context;

    }
    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_display, parent, false);
        return new CourseViewHolder(view, listener, this);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        holder.tv_nextMeeting.setText(student.getClosestMeetingOfCourse(courses.get(position)).toString());
        holder.tv_courseTeacher.setText(courses.get(position).getTeacher().getFullName());
        holder.tv_CourseName.setText(courses.get(position).getCourseName());
        Glide.with(context).load(courses.get(position).getLogo()).into(holder.iv_courseIcon);
//        holder.iv_courseIcon.setImageBitmap(courses.get(position).getLogo());
        holder.courseAdapter = this;
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public ArrayList<Course> getCourses(){
        return courses;
    }
}
