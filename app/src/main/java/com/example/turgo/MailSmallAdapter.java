package com.example.turgo;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class MailSmallAdapter extends RecyclerView.Adapter<MailSmallViewHolder> {
    private String userID;
    private ArrayList<MailFirebase>mails;
    Class<? extends User>userType;

    public MailSmallAdapter(String userID, ArrayList<MailFirebase>mails, Class<? extends User>userType){
        this.userID = userID;
        this.mails = mails;
        this.userType = userType;
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
        MailFirebase<?, ?> mail = mails.get(position);
        if(mail instanceof MACFirebase){
            holder.tv_titleHeading.setText(mail.getHeader());
            holder.tv_shortPreview.setText(mail.getPreview());
            final UserFirebase[] user = {null};
            RequireUpdate.retrieveUser(userID, user);
            holder.tv_fromUser.setText(user[0].getFullName());
        }
    }

    @Override
    public int getItemCount() {
        return mails.size();
    }

    public void addMail(MailFirebase mail) {
        mails.add(mail);
        notifyItemInserted(mails.size() - 1);
    }

}
