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
        tv_TaskTitle = findTextViewAny(itemView, R.id.tv_TaskTitle, R.id.tv_ftd_TaskTitle);
        tv_TaskMonth = findTextViewAny(itemView, R.id.tv_monthSubmission, R.id.tv_ftd_monthSubmission);
        tv_TaskDate = findTextViewAny(itemView, R.id.tv_dateSubmission, R.id.tv_ftd_dateSubmission);
        tv_TaskTime = findTextViewAny(itemView, R.id.tv_TimeSubmission, R.id.tv_ftd_TimeSubmission);
        iv_submissionStatus = findImageViewAny(itemView, R.id.iv_SubmissionStatus, R.id.iv_ftd_SubmissionStatus);
        itemView.setOnClickListener(view -> {
            if (listener != null && task != null) {
                listener.onItemClick(task);
            }

        });
    }

    private TextView findTextViewAny(View root, int... ids) {
        for (int id : ids) {
            View view = root.findViewById(id);
            if (view instanceof TextView) {
                return (TextView) view;
            }
        }
        return null;
    }

    private ImageView findImageViewAny(View root, int... ids) {
        for (int id : ids) {
            View view = root.findViewById(id);
            if (view instanceof ImageView) {
                return (ImageView) view;
            }
        }
        return null;
    }
}
