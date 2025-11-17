package com.example.turgo;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
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

    public TaskAdapter(ArrayList<Task> tasks, User user, OnItemClickListener<Task>listener) {
        this.tasks = tasks;
        this.user = user;
        this.listener = listener;

    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_viewlayout, parent, false);
        d_Submitted = Tool.getDrawableFromId(view.getContext(), R.drawable.checkbox);
        d_nSubmitted = Tool.getDrawableFromId(view.getContext(), R.drawable.pending);

        return new TaskViewHolder(view, listener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        LocalDateTime submissionDate = task.getDueDate();
        holder.tv_TaskMonth.setText(submissionDate.getMonth().toString());
        holder.tv_TaskDate.setText(submissionDate.getDayOfMonth());
        holder.tv_TaskTime.setText(submissionDate.getHour() + ":" + submissionDate.getMinute());
        holder.task = task;
        if(task.getDropbox().getSubmissionSlot((Student)user).isCompleted()){
            holder.iv_submissionStatus.setImageBitmap(Tool.drawableToBitmap(d_Submitted));
        }else{
            holder.iv_submissionStatus.setImageBitmap(Tool.drawableToBitmap(d_nSubmitted));
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }
}
