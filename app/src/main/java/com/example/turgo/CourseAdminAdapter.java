package com.example.turgo;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class CourseAdminAdapter extends RecyclerView.Adapter<CourseAdminViewHolder> {

    private ArrayList<Course> courseList;
    private OnCourseClickListener clickListener;

    // Interface to handle clicks on a course card
    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    // Constructor
    public CourseAdminAdapter(ArrayList<Course> courseList, OnCourseClickListener clickListener) {
        this.courseList = courseList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CourseAdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item_course.xml layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_admin, parent, false);
        return new CourseAdminViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull CourseAdminViewHolder holder, int position) {
        Course currentCourse = courseList.get(position);

        // 1. Set Course Name & Teacher
        holder.tv_courseName.setText(currentCourse.getCourseName());
        TeacherRepository tr = new TeacherRepository(currentCourse.getTeacherId());
        tr.load(new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(TeacherFirebase object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                holder.tv_teacher.setText(object.getFullName());
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });


        // 2. Set Course Type (Convert enum/object to string)
        if (currentCourse.getCourseType() != null) {
            holder.tv_courseType.setText(currentCourse.getCourseType().toString());
        } else {
            holder.tv_courseType.setText("UNKNOWN");
        }

        // 3. Set Base Cost Formatting (e.g. "$120.00")
        holder.tv_baseCost.setText(String.format("$%.2f", currentCourse.getBaseCost()));

        // 4. Set Capacity (Current enrolled / Max capacity)
        int currentEnrolled = 0;
        if (currentCourse.getStudentsId() != null) {
            currentEnrolled = currentCourse.getStudentsId().size();
        }
        holder.tv_studentCount.setText(currentEnrolled + " Students");

        // 5. Handle clicks to open the Admin Full Page View
        holder.cv_container.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCourseClick(currentCourse);
            }
        });
    }
    @SuppressLint("NotifyDataSetChanged")
    public void setCourseList(ArrayList<Course>courses){
        courseList = courses;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (courseList == null) {
            return 0;
        }
        return courseList.size();
    }
}