package com.example.turgo;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TeacherCourseTasksFragment extends Fragment {

    private TextView tvCourseName;
    private MaterialButton btnActive;
    private MaterialButton btnPast;
    private RecyclerView rvTasks;
    private TextView tvEmpty;

    private Course course;
    private Teacher teacher;
    private TaskAdapter taskAdapter;

    private final ArrayList<Task> allCourseTasks = new ArrayList<>();
    private final ArrayList<Task> activeTasks = new ArrayList<>();
    private final ArrayList<Task> pastTasks = new ArrayList<>();

    private boolean showActive = true;

    public static TeacherCourseTasksFragment newInstance(Course course) {
        TeacherCourseTasksFragment fragment = new TeacherCourseTasksFragment();
        Bundle args = new Bundle();
        args.putSerializable(Course.SERIALIZE_KEY_CODE, course);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_course_tasks, container, false);

        tvCourseName = view.findViewById(R.id.tv_TCTS_CourseName);
        btnActive = view.findViewById(R.id.btn_TCTS_Active);
        btnPast = view.findViewById(R.id.btn_TCTS_Past);
        rvTasks = view.findViewById(R.id.rv_TCTS_TaskList);
        tvEmpty = view.findViewById(R.id.tv_TCTS_Empty);

        Bundle args = getArguments();
        if (args != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                course = args.getSerializable(Course.SERIALIZE_KEY_CODE, Course.class);
            } else {
                course = (Course) args.getSerializable(Course.SERIALIZE_KEY_CODE);
            }
        }

        if (course == null || !Tool.boolOf(course.getID())) {
            Toast.makeText(requireContext(), "Course data unavailable.", Toast.LENGTH_SHORT).show();
            tvEmpty.setText("Unable to load tasks for this course.");
            Tool.handleEmpty(true, rvTasks, tvEmpty);
            return view;
        }

        TeacherScreen teacherScreen = (TeacherScreen) requireActivity();
        teacher = teacherScreen.getTeacher();

        tvCourseName.setText(Tool.boolOf(course.getCourseName()) ? course.getCourseName() : "Course Tasks");

        taskAdapter = new TaskAdapter(new ArrayList<>(), teacher, new OnItemClickListener<>() {
            @Override
            public void onItemClick(Task item) {
                if (item == null) {
                    return;
                }
                TaskFullPage taskFullPage = new TaskFullPage();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Task.SERIALIZE_KEY_CODE, item);
                bundle.putString(TaskFullPage.ARG_VIEWER_ROLE, TaskFullPage.VIEWER_ROLE_TEACHER);
                taskFullPage.setArguments(bundle);
                Tool.loadFragment(requireActivity(), TeacherScreen.getContainerId(), taskFullPage);
            }

            @Override
            public void onItemLongClick(Task item) {
            }
        }, TaskItemMode.RECYCLER);

        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(taskAdapter);

        btnActive.setOnClickListener(v -> {
            showActive = true;
            updateTabVisuals();
            renderCurrentTab();
        });

        btnPast.setOnClickListener(v -> {
            showActive = false;
            updateTabVisuals();
            renderCurrentTab();
        });

        updateTabVisuals();
        loadTasksOfCourse();

        return view;
    }

    private void loadTasksOfCourse() {
        RequireUpdate.getAllObjects(Task.class)
                .addOnSuccessListener(tasks -> {
                    allCourseTasks.clear();
                    if (tasks != null) {
                        for (Task task : tasks) {
                            if (task == null || !Tool.boolOf(task.getTaskOfCourse())) {
                                continue;
                            }
                            if (task.getTaskOfCourse().equals(course.getID())) {
                                allCourseTasks.add(task);
                            }
                        }
                    }
                    partitionTasks();
                    renderCurrentTab();
                })
                .addOnFailureListener(error -> {
                    tvEmpty.setText("Failed to load tasks.");
                    Tool.handleEmpty(true, rvTasks, tvEmpty);
                });
    }

    private void partitionTasks() {
        activeTasks.clear();
        pastTasks.clear();

        LocalDateTime now = LocalDateTime.now();
        for (Task task : allCourseTasks) {
            if (task == null || task.getDueDate() == null) {
                pastTasks.add(task);
                continue;
            }
            if (task.getDueDate().isBefore(now)) {
                pastTasks.add(task);
            } else {
                activeTasks.add(task);
            }
        }

        activeTasks.sort(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
        pastTasks.sort(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.reverseOrder())));
    }

    private void renderCurrentTab() {
        List<Task> source = showActive ? activeTasks : pastTasks;
        taskAdapter.replaceTasks(new ArrayList<>(source));
        if (showActive) {
            tvEmpty.setText("No active tasks yet.");
        } else {
            tvEmpty.setText("No past tasks yet.");
        }
        Tool.handleEmpty(source.isEmpty(), rvTasks, tvEmpty);
    }

    private void updateTabVisuals() {
        int activeBg = ContextCompat.getColor(requireContext(), R.color.brand_emerald);
        int inactiveBg = ContextCompat.getColor(requireContext(), R.color.white_soft);
        int activeText = ContextCompat.getColor(requireContext(), R.color.white_soft);
        int inactiveText = ContextCompat.getColor(requireContext(), R.color.brand_emerald_dark);

        if (showActive) {
            btnActive.setBackgroundColor(activeBg);
            btnActive.setTextColor(activeText);
            btnPast.setBackgroundColor(inactiveBg);
            btnPast.setTextColor(inactiveText);
        } else {
            btnPast.setBackgroundColor(activeBg);
            btnPast.setTextColor(activeText);
            btnActive.setBackgroundColor(inactiveBg);
            btnActive.setTextColor(inactiveText);
        }
    }
}
