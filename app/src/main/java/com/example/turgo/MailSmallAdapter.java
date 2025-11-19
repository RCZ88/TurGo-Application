package com.example.turgo;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class MailSmallAdapter extends RecyclerView.Adapter<MailSmallViewHolder> {
    private String userID;
    private ArrayList<Mail>mailSelected;
    private ArrayList<Mail>mails;
    private boolean selectMode;
    private boolean editable;
    private MailPageFull activity;

    public MailSmallAdapter(String userID, ArrayList<Mail>mails, boolean editable,  MailPageFull activity){
        this.userID = userID;
        this.mails = mails;
        this.mailSelected = new ArrayList<>();
        this.editable = editable;
        this.activity = activity;
    }
    public MailSmallAdapter(String userID, ArrayList<Mail>mails, boolean editable) {
        this.userID = userID;
        this.mails = mails;
        this.mailSelected = new ArrayList<>();
        this.editable = editable;
    }
    @NonNull
    @Override
    public MailSmallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mail_small_display, parent, false);
        return new MailSmallViewHolder(view, this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MailSmallViewHolder holder, int position) {
        Mail mail = mails.get(position);
        if(mail instanceof MailApplyCourse){
            holder.tv_titleHeading.setText(mail.getHeader());
            holder.tv_shortPreview.setText(mail.getPreview());
            holder.mail = mails.get(position);
            final UserFirebase[] user = {null};
            RequireUpdate.retrieveUser(userID, user);
            holder.tv_fromUser.setText(user[0].getFullName());
        }
    }


    @Override
    public int getItemCount() {
        return mails.size();
    }

    public MailPageFull getActivity() {
        return activity;
    }

    public void setActivity(MailPageFull activity) {
        this.activity = activity;
    }

    public ArrayList<Mail> getMailSelected() {
        return mailSelected;
    }

    public void setMailSelected(ArrayList<Mail> mailSelected) {
        this.mailSelected = mailSelected;
    }

    public boolean isSelectMode() {
        return selectMode;
    }

    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void exitSelectMode(){
        selectMode = false;
        mailSelected.clear();
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void deleteSelectedMails(){
        for(Mail mail: mailSelected){
            mails.remove(mail);
        }
        mailSelected.clear();
        notifyDataSetChanged();
    }

    public String getUserID() {
        return userID;
    }


    public void setUserID(String userID) {
        this.userID = userID;
    }

    public ArrayList<Mail> getMails() {
        return mails;
    }

    public void setMails(ArrayList<Mail> mails) {
        this.mails = mails;
    }


    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void addMail(Mail mail) {
        mails.add(mail);
        notifyItemInserted(mails.size() - 1);
    }

}
