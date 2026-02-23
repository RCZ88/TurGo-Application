package com.example.turgo;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder>{
    private final ArrayList<Task> tasks;
    private User user;
    private Drawable d_Submitted, d_nSubmitted;
    private OnItemClickListener<Task>listener;
    private TaskItemMode mode;

    public TaskAdapter(ArrayList<Task> tasks, User user, OnItemClickListener<Task>listener) {
        this(tasks, user, listener, TaskItemMode.RECYCLER);
    }

    public TaskAdapter(ArrayList<Task> tasks, User user, OnItemClickListener<Task>listener, TaskItemMode taskItemMode) {
        this.tasks = tasks != null ? new ArrayList<>(tasks) : new ArrayList<>();
        this.user = user;
        this.listener = listener;
        mode = taskItemMode;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = mode == TaskItemMode.RECYCLER? R.layout.task_display_card_content : R.layout.task_viewlayout;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        d_Submitted = Tool.getDrawableFromId(view.getContext(), R.drawable.checkbox);
        d_nSubmitted = Tool.getDrawableFromId(view.getContext(), R.drawable.pending);

        return new TaskViewHolder(view, listener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        if (holder.tv_TaskMonth == null || holder.tv_TaskDate == null || holder.tv_TaskTime == null
                || holder.tv_TaskTitle == null || holder.iv_submissionStatus == null) {
            Log.e("TaskAdapter", "TaskViewHolder is missing required views for mode=" + mode);
            return;
        }
        if (task == null) {
            holder.tv_TaskMonth.setText("-");
            holder.tv_TaskDate.setText("-");
            holder.tv_TaskTime.setText("-");
            return;
        }
        LocalDateTime submissionDate = task.getDueDate();
        if (submissionDate != null) {
            holder.tv_TaskMonth.setText(submissionDate.getMonth().toString().substring(0, 3));
            holder.tv_TaskDate.setText(String.valueOf(submissionDate.getDayOfMonth()));
            String taskTime = submissionDate.getHour() + ":" + submissionDate.getMinute();
            Log.d("TaskAdapter", "Task Time: " + taskTime );
            holder.tv_TaskTime.setText(taskTime);
        } else {
            holder.tv_TaskMonth.setText("-");
            holder.tv_TaskDate.setText("-");
            holder.tv_TaskTime.setText("-");
        }
        holder.task = task;
        holder.tv_TaskTitle.setText(task.getTitle());
        holder.iv_submissionStatus.setImageBitmap(Tool.drawableToBitmap(d_nSubmitted));
        if (user instanceof Student) {
            Student studentUser = (Student) user;
            TaskStatus status = studentUser.resolveTaskStatus(task, LocalDateTime.now());
            boolean completedByStatus = status == TaskStatus.COMPLETED;
            holder.iv_submissionStatus.setImageBitmap(Tool.drawableToBitmap(completedByStatus ? d_Submitted : d_nSubmitted));
        }
        if (user instanceof Student && Tool.boolOf(task.getDropbox())) {
            String boundTaskId = task.getTaskID();
            task.getDropboxObject().addOnSuccessListener(dropbox -> {
                if (dropbox == null) {
                    return;
                }
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION || currentPos >= tasks.size()) {
                    return;
                }
                Task rebound = tasks.get(currentPos);
                if (rebound == null
                        || !Tool.boolOf(rebound.getTaskID())
                        || !rebound.getTaskID().equals(boundTaskId)) {
                    return;
                }
                Submission slot = dropbox.getSubmissionSlot((Student) user);
                boolean isCompleted = slot != null && slot.isCompleted();
                holder.iv_submissionStatus.setImageBitmap(Tool.drawableToBitmap(isCompleted ? d_Submitted : d_nSubmitted));
            });
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void replaceTasks(ArrayList<Task> updatedTasks) {
        tasks.clear();
        if (updatedTasks != null) {
            tasks.addAll(updatedTasks);
        }
        notifyDataSetChanged();
    }
}
