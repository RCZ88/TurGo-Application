package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TeacherTaskSubmissionViewHolder extends RecyclerView.ViewHolder {
    TextView tvStudentName;
    TextView tvStatus;
    TextView tvSubmittedAt;
    TextView tvFiles;

    public TeacherTaskSubmissionViewHolder(@NonNull View itemView) {
        super(itemView);
        tvStudentName = itemView.findViewById(R.id.tv_TTSI_StudentName);
        tvStatus = itemView.findViewById(R.id.tv_TTSI_Status);
        tvSubmittedAt = itemView.findViewById(R.id.tv_TTSI_SubmittedAt);
        tvFiles = itemView.findViewById(R.id.tv_TTSI_Files);
    }
}

