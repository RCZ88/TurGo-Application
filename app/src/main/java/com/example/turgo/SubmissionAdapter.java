package com.example.turgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionViewHolder>{
    ArrayList<SubmissionDisplay> submissions;

    public SubmissionAdapter(ArrayList<SubmissionDisplay> submissions) {
        this.submissions = submissions;
    }
    @NonNull
    @Override
    public SubmissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.latest_submission_view_holder, parent, false);
        return new SubmissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionViewHolder holder, int position) {
        SubmissionDisplay submission = submissions.get(position);
        holder.tv_taskName.setText(submission.getTask());
        holder.tv_studentName.setText(submission.getName());
        holder.tv_ofCourse.setText(submission.getCourse());
        holder.tv_submittedDate.setText(submission.getSubmittedTimeDate());
    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }
}
