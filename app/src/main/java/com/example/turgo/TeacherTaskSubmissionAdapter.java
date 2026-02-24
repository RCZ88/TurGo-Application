package com.example.turgo;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TeacherTaskSubmissionAdapter extends RecyclerView.Adapter<TeacherTaskSubmissionViewHolder> {

    private final ArrayList<TeacherTaskSubmissionItem> items = new ArrayList<>();

    public void submit(ArrayList<TeacherTaskSubmissionItem> updated) {
        items.clear();
        if (updated != null) {
            items.addAll(updated);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeacherTaskSubmissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher_task_submission, parent, false);
        return new TeacherTaskSubmissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherTaskSubmissionViewHolder holder, int position) {
        TeacherTaskSubmissionItem item = items.get(position);
        holder.tvStudentName.setText(Tool.boolOf(item.getStudentName()) ? item.getStudentName() : "-");
        holder.tvStatus.setText(Tool.boolOf(item.getStatus()) ? item.getStatus() : "Pending");
        holder.tvSubmittedAt.setText(Tool.boolOf(item.getSubmittedAt()) ? item.getSubmittedAt() : "Not submitted");
        holder.tvFiles.setText(item.getFileNames().isEmpty() ? "No files" : android.text.TextUtils.join(", ", item.getFileNames()));

        holder.itemView.setOnClickListener(v -> {
            if (!Tool.boolOf(item.getPrimaryFileUrl())) {
                return;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getPrimaryFileUrl()));
            v.getContext().startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

