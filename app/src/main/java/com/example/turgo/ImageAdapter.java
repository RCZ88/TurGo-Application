package com.example.turgo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {

    private final Context context;
    private final List<String> imageUrls;
    private OnItemClickListener<String> onItemClickListener; // Added listener field
    private int selectedPosition = -1;

    public ImageAdapter(@NonNull Context context, @NonNull List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    public void setSelectedPosition(int position){
        int previousPosition = selectedPosition;
        selectedPosition = position;

        if (previousPosition >= 0) {
            notifyItemChanged(previousPosition);
        }
        if (selectedPosition >= 0) {
            notifyItemChanged(selectedPosition);
        }
    }
    // Method to set the click listener
    public void setOnItemClickListener(OnItemClickListener<String> listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_itemview, parent, false);
        ImageViewHolder holder = new ImageViewHolder(v);
        // Pass listener to ViewHolder
        holder.setOnItemClickListener(onItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageUrls.get(position);
        Tool.setImageCloudinary(context, url, holder.iv_image);
        // Bind the current item URL to the holder
        holder.bind(url, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }
}
