package com.example.turgo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;

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
    private boolean showMeeting;
    Student student;
    public CourseAdapter(ArrayList<Course> courses, Student student, OnItemClickListener<Course> listener, ArrayList<Teacher> teacher, Context context, boolean showMeeting){
        this.courses = courses;
        this.student = student;
        this.listener = listener;
        this.context = context;
        this.teachers = teacher;
        this.showMeeting = showMeeting;

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
        Course course = courses.get(position);
        Log.d("CourseAdapter", "onBindViewHolder position=" + position
                + ", courseId=" + course.getID()
                + ", showMeeting=" + showMeeting);

        if(showMeeting){
            holder.tv_nextMeeting.setText("Loading...");
            Task<LocalDate> closestMeetingOfCourse = student.getClosestMeetingOfCourse(course);
            if (closestMeetingOfCourse == null) {
                Log.w("CourseAdapter", "closestMeeting task is null for courseId=" + course.getID());
                holder.tv_nextMeeting.setText("No Meeting Found!");
            } else {
                final String boundCourseId = course.getID();
                closestMeetingOfCourse
                        .addOnSuccessListener(closestMeeting -> {
                            int currentPos = holder.getBindingAdapterPosition();
                            if (currentPos == RecyclerView.NO_POSITION) {
                                Log.d("CourseAdapter", "Holder detached before result for courseId=" + boundCourseId);
                                return;
                            }
                            String currentCourseId = courses.get(currentPos).getID();
                            if (!boundCourseId.equals(currentCourseId)) {
                                Log.d("CourseAdapter", "Skipping stale async bind. expectedCourseId="
                                        + boundCourseId + ", currentCourseId=" + currentCourseId);
                                return;
                            }

                            if (closestMeeting != null) {
                                Log.d("CourseAdapter", "Closest meeting for courseId=" + boundCourseId
                                        + " is " + closestMeeting);
                                holder.tv_nextMeeting.setText(closestMeeting.format(MEETING_DATE_FORMAT));
                            } else {
                                Log.d("CourseAdapter", "No upcoming meeting for courseId=" + boundCourseId);
                                holder.tv_nextMeeting.setText("No Meeting Found!");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("CourseAdapter", "Failed to load closest meeting for courseId=" + boundCourseId, e);
                            holder.tv_nextMeeting.setText("No Meeting Found!");
                        });
            }

            holder.iv_meetingIcon.setVisibility(View.VISIBLE);
            holder.tv_nextMeeting.setVisibility(View.VISIBLE);
        }else{
            holder.tv_nextMeeting.setVisibility(View.GONE);
            holder.iv_meetingIcon.setVisibility(View.GONE);
        }


        Teacher teacher = (teachers != null && position < teachers.size()) ? teachers.get(position) : null;
        holder.tv_courseTeacher.setText(teacher != null ? teacher.getFullName() : "-");
        holder.tv_CourseName.setText(course.getCourseName());
        Glide.with(context).load(course.getLogo()).into(holder.iv_courseIcon);
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
