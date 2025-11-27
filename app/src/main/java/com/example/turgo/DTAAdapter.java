package com.example.turgo;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DTAAdapter extends RecyclerView.Adapter<DTAViewHolder> {
    private final ArrayList<DayTimeArrangement> items;
    private OnItemClickListener<Integer> listener;

    public DTAAdapter(ArrayList<DayTimeArrangement> items) {
        this.items = items;

    }

    public void setListener(OnItemClickListener<Integer> listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DTAViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dta_view_holder, parent, false);
        return new DTAViewHolder(view, listener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DTAViewHolder holder, @SuppressLint("RecyclerView") int position) {
        DayTimeArrangement item = items.get(position);
        holder.tvDayOfWeek.setText(item.getDay().toString());
        holder.tvTimeRange.setText(item.getStart().toString() + " - " + item.getEnd().toString());
        holder.position = position;

        if (item.getMaxMeeting() > 0) {
            holder.tvMaxMeeting.setText(item.getMaxMeeting() + " meeting(s) per day");
        } else {
            holder.tvMaxMeeting.setText("Unlimited meetings per day");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void addItem(DayTimeArrangement dta){
        Log.d("DTAAdapter", "Added Item Successfully!" +dta.toString());
        items.add(dta);
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void removeItem(int index){
        items.remove(index);
        notifyDataSetChanged();
    }
}
