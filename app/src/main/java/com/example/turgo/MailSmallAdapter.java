package com.example.turgo;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MailSmallAdapter extends RecyclerView.Adapter<MailSmallViewHolder> {
    private ArrayList<Mail>mails;

    public MailSmallAdapter(ArrayList<Mail>mails){
        this.mails = mails;
    }
    @NonNull
    @Override
    public MailSmallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mail_small_display, parent, false);
        return new MailSmallViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MailSmallViewHolder holder, int position) {
        Mail mail = mails.get(position);
        if(mail instanceof MailApplyCourse){
            holder.tv_titleHeading.setText(mail.getHeader());
            holder.tv_shortPreview.setText(mail.getPreview());
            holder.tv_fromUser.setText(mail.getFrom().getFullName());
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
