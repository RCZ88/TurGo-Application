package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ComposeRecipientViewHolder extends RecyclerView.ViewHolder {
    TextView tvName;
    TextView tvRole;
    TextView tvEmail;

    public ComposeRecipientViewHolder(@NonNull View itemView) {
        super(itemView);
        tvName = itemView.findViewById(R.id.tv_compose_recipient_name);
        tvRole = itemView.findViewById(R.id.tv_compose_recipient_role);
        tvEmail = itemView.findViewById(R.id.tv_compose_recipient_email);
    }
}
