package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class MailSmallViewHolder extends RecyclerView.ViewHolder  {
    public TextView tv_fromUser, tv_titleHeading, tv_shortPreview;
    public View ll_mailPreview;
    public Mail mail;
    public MailSmallViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_fromUser = itemView.findViewById(R.id.tv_UserSent);
        tv_shortPreview = itemView.findViewById(R.id.tv_ShortMailContent);
        tv_titleHeading = itemView.findViewById(R.id.tv_TitleHeading);
        ll_mailPreview = itemView.findViewById(R.id.ll_MailSmallDisplay);
    }

    public void setSelectedVisual(boolean selected){
        int colorRes = selected ? R.color.brand_emerald_pale : android.R.color.white;
        ll_mailPreview.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), colorRes));
    }
}
