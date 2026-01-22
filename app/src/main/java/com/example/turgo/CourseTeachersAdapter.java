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
    ArrayList<Meeting>meetings;
    OnItemClickListener<Course>listener;
    ArrayList<Integer>studentCountOfCourse;

    public CourseTeachersAdapter(ArrayList<Course> courses, ArrayList<Meeting>meetings, ArrayList<Integer>studentCountOfCourse, OnItemClickListener<Course>listener) {
        this.courses = courses;
        this.meetings = meetings;
        this.studentCountOfCourse = studentCountOfCourse;
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
        //async - completed
        Meeting nextMeetingOfNextSchedule = meetings.get(position);
        String nextMeeting = "-";
        if(nextMeetingOfNextSchedule != null){
            nextMeeting = nextMeetingOfNextSchedule.getDateOfMeeting().toString();
        }
        holder.tv_nextSchedule.setText(nextMeeting);
        Course course = courses.get(position);
        holder.tv_courseName.setText(course.getCourseName());
        holder.tv_numberOfStudents.setText(studentCountOfCourse.get(position) + " Student(s)");
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }
}
