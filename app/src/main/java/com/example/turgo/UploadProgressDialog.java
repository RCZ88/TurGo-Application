package com.example.turgo;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UploadProgressDialog {
    private final AlertDialog dialog;
    private final ProgressBar progressBar;
    private final TextView tvPercentage;
    private final TextView tvStatus;
    private final TextView tvTitle;

    public UploadProgressDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_upload_progress, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        dialog = builder.create();

        // Get views
        progressBar = dialogView.findViewById(R.id.pb_upload);
        tvPercentage = dialogView.findViewById(R.id.tv_upload_percentage);
        tvStatus = dialogView.findViewById(R.id.tv_upload_status);
        tvTitle = dialogView.findViewById(R.id.tv_upload_title);
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void setTitle(String title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    public void setMessage(String message) {
        if (tvStatus != null) {
            tvStatus.setText(message);
        }
    }

    public void setProgress(int progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
        if (tvPercentage != null) {
            tvPercentage.setText(progress + "%");
        }
    }

    public void setMaxProgress(int max) {
        if (progressBar != null) {
            progressBar.setMax(max);
        }
    }
}
