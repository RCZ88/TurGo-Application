package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SubmissionViewHolder extends RecyclerView.ViewHolder{
    TextView tv_studentName, tv_taskName, tv_ofCourse, tv_fileNames, tv_submittedDate;
    public SubmissionViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_studentName = itemView.findViewById(R.id.tv_SVH_StudentName);
        tv_taskName = itemView.findViewById(R.id.tv_SVH_TaskName);
        tv_ofCourse = itemView.findViewById(R.id.tv_SVH_OfCourse);
        tv_fileNames = itemView.findViewById(R.id.tv_SVH_FileNames);
        tv_submittedDate = itemView.findViewById(R.id.tv_SVH_SubmittedDate);
    }
}
