package com.example.turgo;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SubmissionViewHolder extends RecyclerView.ViewHolder{
    TextView tv_studentName, tv_taskName, tv_ofCourse, tv_fileNames, tv_submittedDate, tv_initials;
    TextView tv_fileNameSimple, tv_uploadDateSimple;
    TextView  tv_statusSubmission;
    ImageButton btn_removeFile;
    public SubmissionViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_studentName = findTextViewAny(itemView, R.id.tv_SVH_StudentName);
        tv_taskName = findTextViewAny(itemView, R.id.tv_SVH_TaskName);
        tv_ofCourse = findTextViewAny(itemView, R.id.tv_SVH_OfCourse);
        tv_fileNames = findTextViewAny(itemView, R.id.tv_SVH_FileNames);
        tv_submittedDate = findTextViewAny(itemView, R.id.tv_SVH_SubmittedDate);
        tv_initials = itemView.findViewById(R.id.tv_SVH_Initial);

        tv_fileNameSimple = findTextViewAny(itemView, R.id.tv_sd_FileName);
        tv_uploadDateSimple = findTextViewAny(itemView, R.id.tv_sd_UploadDateTime);
        tv_statusSubmission = itemView.findViewById(R.id.tv_sd_submissionStatus);
        btn_removeFile = itemView.findViewById(R.id.btn_sd_RemoveFile);
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
}
