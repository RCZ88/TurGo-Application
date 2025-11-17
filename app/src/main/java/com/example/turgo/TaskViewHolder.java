package com.example.turgo;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

public class TaskViewHolder extends RecyclerView.ViewHolder{
    TextView tv_TaskTitle, tv_TaskMonth, tv_TaskDate, tv_TaskTime;
    ImageView iv_submissionStatus;
    Task task;
    public TaskViewHolder(@NonNull View itemView, OnItemClickListener<Task>listener) {
        super(itemView);
        tv_TaskTitle = itemView.findViewById(R.id.tv_TaskTitle);
        tv_TaskMonth = itemView.findViewById(R.id.tv_monthSubmission);
        tv_TaskDate = itemView.findViewById(R.id.tv_dateSubmission);
        tv_TaskTime = itemView.findViewById(R.id.tv_TimeSubmission);
        iv_submissionStatus = itemView.findViewById(R.id.iv_SubmissionStatus);
        itemView.setOnClickListener(view -> {
            listener.onItemClick(task);

        });
    }
}
