package com.example.turgo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskViewHolder extends RecyclerView.ViewHolder{
    TextView tv_TaskTitle, tv_TaskMonth, tv_TaskDate, tv_TaskTime;
    ImageView iv_submissionStatus;
    public TaskViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_TaskTitle = itemView.findViewById(R.id.tv_TaskTitle);
        tv_TaskMonth = itemView.findViewById(R.id.tv_monthSubmission);
        tv_TaskDate = itemView.findViewById(R.id.tv_dateSubmission);
        tv_TaskTime = itemView.findViewById(R.id.tv_TimeSubmission);
        iv_submissionStatus = itemView.findViewById(R.id.iv_SubmissionStatus);
    }
}
