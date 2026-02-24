package com.example.turgo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;

public class CourseScheduleListFragment extends Fragment {
    private Course course;

    public static CourseScheduleListFragment newInstance(Course course) {
        CourseScheduleListFragment fragment = new CourseScheduleListFragment();
        Bundle args = new Bundle();
        args.putSerializable(Course.SERIALIZE_KEY_CODE, course);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            course = (Course) getArguments().getSerializable(Course.SERIALIZE_KEY_CODE);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_schedule_list, container, false);

        CollapsingToolbarLayout collapsingToolbar = view.findViewById(R.id.ct_csl_Title);
        RecyclerView rvSchedules = view.findViewById(R.id.rv_csl_Schedules);
        TextView tvEmpty = view.findViewById(R.id.tv_csl_Empty);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        if (toolbar != null) {
            AppCompatActivity activity = (AppCompatActivity) requireActivity();
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v ->
                    requireActivity().getOnBackPressedDispatcher().onBackPressed()
            );
        }

        if (course != null) {
            if (collapsingToolbar != null) {
                collapsingToolbar.setTitle(course.getCourseName());
            }
            View banner = view.findViewById(R.id.iv_csl_Banner);
            if (banner instanceof android.widget.ImageView) {
                Tool.setImageCloudinary(getContext(), course.getBackgroundCloudinary(), (android.widget.ImageView) banner);
            }
        }

        ArrayList<Schedule> schedules = course == null ? new ArrayList<>() : course.getSchedules();
        if (schedules == null) {
            schedules = new ArrayList<>();
        }
        ScheduleAdapter adapter = new ScheduleAdapter(schedules, course);
        rvSchedules.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSchedules.setAdapter(adapter);

        Tool.handleEmpty(schedules.isEmpty(), rvSchedules, tvEmpty);

        return view;
    }
}
