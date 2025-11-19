package com.example.turgo;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public class MailSmallViewHolder extends RecyclerView.ViewHolder  {
    public TextView tv_fromUser, tv_titleHeading, tv_shortPreview;
    public LinearLayout ll_mailPreview;
    public boolean editable;
    public User user;
    public Mail mail;
    public MailSmallViewHolder(@NonNull View itemView, RecyclerView.Adapter<?> adapter) {
        super(itemView);
        tv_fromUser = itemView.findViewById(R.id.tv_UserSent);
        tv_shortPreview = itemView.findViewById(R.id.tv_ShortMailContent);
        tv_titleHeading = itemView.findViewById(R.id.tv_TitleHeading);
        ll_mailPreview = itemView.findViewById(R.id.ll_MailSmallDisplay);
        MailSmallAdapter mailAdapter = (MailSmallAdapter) adapter;
        editable = mailAdapter.isEditable();
        updateMailState(mailAdapter.isSelectMode());
        if(mailAdapter.isSelectMode()){
            ll_mailPreview.setOnClickListener(view -> {
                mailAdapter.getMailSelected().add(mail);
                updateMailState(true);
            });
        }else{
            if(editable){
                ll_mailPreview.setOnClickListener(view -> {
                    Intent intent = new Intent(itemView.getContext(), ComposeMail.class);
                    try {
                        user = Tool.getUserOfId(mailAdapter.getUserID());
                    } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                             IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                    intent.putExtra(User.SERIALIZE_KEY_CODE, user);
                    intent.putExtra(Mail.SERIALIZE_KEY_CODE, mail);
                    itemView.getContext().startActivity(intent);
                });
            }else{
                ll_mailPreview.setOnClickListener(view -> {
                    Intent intent = new Intent(itemView.getContext(), MailExpandFull.class);
                    itemView.getContext().startActivity(intent);
                });
            }
            ll_mailPreview.setOnLongClickListener(v -> {
                mailAdapter.getMailSelected().add(mail);
                mailAdapter.setSelectMode(true);
                if(mailAdapter.getActivity() !=null){
                    mailAdapter.getActivity().setSelectionMode(true);
                }
                return true;
            });
        }
    }
    public void updateMailState(boolean selectMode){
        if(selectMode){
            ll_mailPreview.setBackgroundColor(Color.parseColor("#B0C4DE"));
        }else{
            ll_mailPreview.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
    }
}
