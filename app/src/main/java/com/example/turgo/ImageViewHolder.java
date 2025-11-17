package com.example.turgo;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ImageViewHolder extends RecyclerView.ViewHolder {
    ImageView iv_image;
    private OnItemClickListener<String> onItemClickListener;
    private String currentImageUrl;

    public ImageViewHolder(@NonNull View itemView) {
        super(itemView);
        iv_image = itemView.findViewById(R.id.iv_IIV_image);

        // Set click listener on the entire item view
        itemView.setOnClickListener(v -> {
            if (onItemClickListener != null && currentImageUrl != null) {
                onItemClickListener.onItemClick(currentImageUrl);
            }
        });
    }

    // Method to set the click listener
    public void setOnItemClickListener(OnItemClickListener<String> listener) {
        this.onItemClickListener = listener;
    }

    // Method to bind the current image URL
    public void bind(String imageUrl, boolean isSelected) {
        this.currentImageUrl = imageUrl;
        updateSelectedState(isSelected);
    }
    private void updateSelectedState(boolean isSelected){
        if (isSelected){
            iv_image.setBackgroundColor(0xFFE0E0E0);
        }else{
            iv_image.setBackgroundColor(0xFFFFFFFF);
        }
    }
}
