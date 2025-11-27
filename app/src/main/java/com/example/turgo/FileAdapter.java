package com.example.turgo;

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
        holder.tv_fileName.setText(submissions.get(position).getFileName());
        holder.tv_timeDateUpload.setText(submissions.get(position).getFileCreateDate().toString());
    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }
}
