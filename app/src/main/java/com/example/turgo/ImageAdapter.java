package com.example.turgo;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {

    private ArrayList<Uri>uris;
    private ArrayList<Integer> imageIds;
    private OnItemClickListener<Integer> onItemClickListener; // Added listener field
    private int selectedPosition = -1;

    public ImageAdapter(ArrayList<Integer> imageUrls, int dummy) {
        this.imageIds = imageUrls;
    }
    public ImageAdapter(ArrayList<Uri>imageUris){
        this.uris = imageUris;
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
    public void setOnItemClickListener(OnItemClickListener<Integer> listener) {
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
        if(imageIds != null){
            holder.iv_image.setImageResource(imageIds.get(position));
            holder.bind(imageIds.get(position), position == selectedPosition);
        }else{
            holder.iv_image.setImageURI(uris.get(position));
            holder.bind(position);
        }
        // Bind the current item URL to the holder

    }

    @Override
    public int getItemCount() {
        if(imageIds != null){
            return imageIds.size();
        }else{
            return uris.size();
        }

    }

    public ArrayList<Integer> getImageIds() {
        return imageIds;
    }

    public void setImageIds(ArrayList<Integer> imageIds) {
        this.imageIds = imageIds;
    }

    public ArrayList<Uri> getUris() {
        return uris;
    }

    public void setUris(ArrayList<Uri> uris) {
        this.uris = uris;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}
