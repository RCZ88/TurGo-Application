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
        // Handle null/empty courses list
        if (courses == null || position >= courses.size() || courses.get(position) == null) {
            holder.tv_courseName.setText("No course");
            holder.tv_numberOfStudents.setText("0 Student(s)");
            holder.tv_nextSchedule.setText("-");
            return;
        }

        Course course = courses.get(position);
        holder.tv_courseName.setText(course.getCourseName() != null ? course.getCourseName() : "Unnamed Course");

        // Handle student count safely
        String studentCountText = "0 Student(s)";
        if (studentCountOfCourse != null && position < studentCountOfCourse.size()) {
            Integer count = studentCountOfCourse.get(position);
            studentCountText = (count != null ? count : 0) + " Student(s)";
        }
        holder.tv_numberOfStudents.setText(studentCountText);

        // Handle meetings/schedule safely
        String nextMeeting = "-";
        if (meetings != null && position < meetings.size()) {
            Meeting nextMeetingOfNextSchedule = meetings.get(position);
            if (nextMeetingOfNextSchedule != null && nextMeetingOfNextSchedule.getDateOfMeeting() != null) {
                nextMeeting = nextMeetingOfNextSchedule.getDateOfMeeting().toString();
            }
        }
        holder.tv_nextSchedule.setText(nextMeeting);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }
}
