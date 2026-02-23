package com.example.turgo;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CourseTeachersAdapter extends RecyclerView.Adapter<CourseTeachersViewHolder>{
    private static final DateTimeFormatter MEETING_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    ArrayList<Course>courses;
    ArrayList<Meeting>meetings;
    OnItemClickListener<Course>listener;
    ArrayList<Integer>studentCountOfCourse;
    private final CourseTeacherItemMode mode;

    public CourseTeachersAdapter(ArrayList<Course> courses, ArrayList<Meeting>meetings, ArrayList<Integer>studentCountOfCourse, OnItemClickListener<Course>listener) {
        this(courses, meetings, studentCountOfCourse, listener, CourseTeacherItemMode.RECYCLER);
    }

    public CourseTeachersAdapter(ArrayList<Course> courses, ArrayList<Meeting>meetings, ArrayList<Integer>studentCountOfCourse, OnItemClickListener<Course>listener, CourseTeacherItemMode mode) {
        this.courses = courses;
        this.meetings = meetings;
        this.studentCountOfCourse = studentCountOfCourse;
        this.listener = listener;
        this.mode = mode != null ? mode : CourseTeacherItemMode.RECYCLER;
    }

    @NonNull
    @Override
    public CourseTeachersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = mode == CourseTeacherItemMode.VIEW_PAGER
                ? R.layout.course_teacher_viewpager_viewholder
                : R.layout.course_teacher_viewholder;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return new CourseTeachersViewHolder(view, listener);
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
        holder.course = course;
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
                nextMeeting = formatMeetingDate(nextMeetingOfNextSchedule.getDateOfMeeting());
            }
        }
        holder.tv_nextSchedule.setText(nextMeeting);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    private static String formatMeetingDate(LocalDate date) {
        if (date == null) {
            return "-";
        }
        int year = date.getYear();
        if (year < 0 || year > 9999) {
            return "-";
        }
        return date.format(MEETING_DATE_FORMAT);
    }
}
