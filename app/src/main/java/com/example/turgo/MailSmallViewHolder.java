package com.example.turgo;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MailSmallViewHolder extends RecyclerView.ViewHolder {
    public TextView tv_fromUser, tv_titleHeading, tv_shortPreview;
    public LinearLayout ll_mailPreview;
    public MailSmallViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_fromUser = itemView.findViewById(R.id.tv_UserSent);
        tv_shortPreview = itemView.findViewById(R.id.tv_ShortMailContent);
        tv_titleHeading = itemView.findViewById(R.id.tv_TitleHeading);
        ll_mailPreview.setOnClickListener(view -> {
            Intent intent = new Intent(itemView.getContext(), MailExpandFull.class);
            itemView.getContext().startActivity(intent);
        });
    }
}
