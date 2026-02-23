package com.example.turgo;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {
    private ArrayList<file> submissions;
    public FileAdapter(ArrayList<file>files){
        submissions = files;
    }
    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.submission_display, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        file file = submissions.get(position);
        holder.tv_fileName.setText(file.getFileName());
        holder.tv_timeDateUpload.setText(file.getFileCreateDate().toString());
        file.getOfTask().getSubmissionOfStudent((Student)file.getUploader()).addOnSuccessListener(submission ->{
            boolean late = Boolean.TRUE.equals(submission.getFiles().get(file));
            if(late){
                holder.tv_statusSubmission.setVisibility(View.VISIBLE);
                holder.tv_statusSubmission.setText("LATE");
                holder.tv_statusSubmission.setTextColor(Color.parseColor("#C0392B")); // Deep Red
                holder.tv_statusSubmission.setBackgroundResource(R.drawable.bg_status_badge_late);
            }else{
                holder.tv_statusSubmission.setVisibility(View.VISIBLE);
                holder.tv_statusSubmission.setText("EARLY");
                holder.tv_statusSubmission.setTextColor(Color.parseColor("#00A86B")); // Emerald Green
                holder.tv_statusSubmission.setBackgroundResource(R.drawable.bg_status_badge_early);
            }
        });
    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }
}
