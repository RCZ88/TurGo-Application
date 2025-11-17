package com.example.turgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotifAdapter extends RecyclerView.Adapter<NotifViewHolder> {
    private ArrayList<NotificationFirebase> notifications;

    public NotifAdapter(ArrayList<NotificationFirebase>notifications){
        this.notifications = notifications;
    }
    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notif_small_display, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        holder.tv_header.setText(notifications.get(position).getTitle());
        holder.tv_content.setText(notifications.get(position).getContent());
    }

    public void addNotification(NotificationFirebase notificationFirebase){
        notifications.add(notificationFirebase);
        notifyItemInserted(notifications.size() - 1);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }
}
