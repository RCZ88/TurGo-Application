package com.example.turgo;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionViewHolder>{
    private static final String TAG = "SubmissionAdapterDebug";
    ArrayList<SubmissionDisplay> submissions;
    private final SubmissionItemMode mode;
    private final OnPreviewRemoveListener removeListener;

    public interface OnPreviewRemoveListener {
        void onRemove(int position);
    }

    public SubmissionAdapter(ArrayList<SubmissionDisplay> submissions) {
        this.submissions = submissions;
        this.mode = SubmissionItemMode.DASHBOARD;
        this.removeListener = null;
    }

    public SubmissionAdapter(ArrayList<SubmissionDisplay> submissions, SubmissionItemMode mode) {
        this.submissions = submissions;
        this.mode = mode;
        this.removeListener = null;
    }

    public SubmissionAdapter(ArrayList<SubmissionDisplay> submissions, SubmissionItemMode mode, OnPreviewRemoveListener removeListener) {
        this.submissions = submissions;
        this.mode = mode;
        this.removeListener = removeListener;
    }
    @NonNull
    @Override
    public SubmissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = mode == SubmissionItemMode.FILE_PICKER
                ? R.layout.submission_display
                : R.layout.latest_submission_view_holder;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return new SubmissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionViewHolder holder, int position) {
        SubmissionDisplay submission = submissions.get(position);
        Log.d(TAG, "onBind position=" + position + ", mode=" + mode + ", hasSubmission=" + (submission != null));
        if (mode == SubmissionItemMode.FILE_PICKER) {
            String fileName = "-";
            if (submission.getFilesSubmitted() != null && !submission.getFilesSubmitted().isEmpty()) {
                fileName = submission.getFilesSubmitted().get(0);
            } else if (Tool.boolOf(submission.getTask())) {
                fileName = submission.getTask();
            }
            if (holder.tv_fileNameSimple != null) {
                holder.tv_fileNameSimple.setText(fileName);
            } else {
                Log.e(TAG, "tv_fileNameSimple is null for FILE_PICKER at position=" + position);
            }
            if (holder.tv_uploadDateSimple != null) {
                holder.tv_uploadDateSimple.setText(submission.getSubmittedTimeDate());
            } else {
                Log.e(TAG, "tv_uploadDateSimple is null for FILE_PICKER at position=" + position);
            }
            if(holder.tv_statusSubmission != null){
                if(submission.isHasStatus()){
                    if(submission.isLate()){
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
                }else{
                    holder.tv_statusSubmission.setVisibility(View.GONE);
                }
            }
            if (holder.btn_removeFile != null) {
                holder.btn_removeFile.setVisibility(removeListener != null ? View.VISIBLE : View.GONE);
                holder.btn_removeFile.setOnClickListener(v -> {
                    if (removeListener != null) {
                        int currentPos = holder.getBindingAdapterPosition();
                        if (currentPos != RecyclerView.NO_POSITION) {
                            removeListener.onRemove(currentPos);
                        }
                    }
                });
            }
            if (holder.tv_initials != null && Tool.boolOf(submission.getName())) {
                holder.tv_initials.setText(Character.toString(submission.getName().charAt(0)).toUpperCase());
            }

            return;
        }

        if (holder.tv_taskName != null) {
            holder.tv_taskName.setText(submission.getTask());
        }
        if (holder.tv_studentName != null) {
            holder.tv_studentName.setText(submission.getName());
        }
        if (holder.tv_ofCourse != null) {
            holder.tv_ofCourse.setText(submission.getCourse());
        }
        if (holder.tv_submittedDate != null) {
            holder.tv_submittedDate.setText(submission.getSubmittedTimeDate());
        }
        if (holder.tv_fileNames != null && submission.getFilesSubmitted() != null) {
            holder.tv_fileNames.setText(android.text.TextUtils.join(", ", submission.getFilesSubmitted()));
        }
    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }
}
