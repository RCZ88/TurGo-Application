package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FileViewHolder extends RecyclerView.ViewHolder {
    TextView tv_fileName, tv_timeDateUpload, tv_statusSubmission;


    public FileViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_fileName = itemView.findViewById(R.id.tv_sd_FileName);
        tv_timeDateUpload = itemView.findViewById(R.id.tv_sd_UploadDateTime);
        tv_statusSubmission = itemView.findViewById(R.id.tv_sd_submissionStatus);
    }
}
