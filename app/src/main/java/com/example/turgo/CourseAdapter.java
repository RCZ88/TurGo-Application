package com.example.turgo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<CourseViewHolder>{
    private final ArrayList<Course>courses;
    private static final DateTimeFormatter MEETING_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy");
    protected OnItemClickListener<Course> listener;
    private ArrayList<Teacher> teachers;
    private Context context;
    Student student;
    public CourseAdapter(ArrayList<Course> courses, Student student, OnItemClickListener<Course> listener, ArrayList<Teacher> teacher, Context context){
        this.courses = courses;
        this.student = student;
        this.listener = listener;
        this.context = context;
        this.teachers = teacher;

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
        LocalDate closestMeeting = student.getClosestMeetingOfCourse(courses.get(position));
        if(closestMeeting != null){
            holder.tv_nextMeeting.setText(closestMeeting.format(MEETING_DATE_FORMAT));
        }else{
            holder.tv_nextMeeting.setText("No Meeting Found!");
        }

        holder.tv_courseTeacher.setText(teachers.get(position).getFullName());
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
