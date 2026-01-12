package com.example.turgo;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ImageViewHolder extends RecyclerView.ViewHolder {
    ImageView iv_image;
    private OnItemClickListener<Integer> onItemClickListener;
    private int currentImageId;

    public ImageViewHolder(@NonNull View itemView) {
        super(itemView);
        iv_image = itemView.findViewById(R.id.iv_IIV_image);

        // Set click listener on the entire item view
        itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(currentImageId);
            }
        });
        itemView.setOnLongClickListener(v -> {
            if(currentImageId!= RecyclerView.NO_POSITION && onItemClickListener != null){
                onItemClickListener.onItemLongClick(currentImageId);
                return true;
            }
            return false;
        });
    }

    // Method to set the click listener
    public void setOnItemClickListener(OnItemClickListener<Integer> listener) {
        this.onItemClickListener = listener;
    }

    // Method to bind the current image URL
    public void bind(int imageId, boolean isSelected) {
        this.currentImageId = imageId;
        updateSelectedState(isSelected);
    }
    public void bind(int imageIndex){
        this.currentImageId = imageIndex;
    }
    private void updateSelectedState(boolean isSelected){
        if (isSelected){
            iv_image.setBackgroundResource(R.drawable.edittext_border);
        }else{
            iv_image.setBackgroundResource(R.drawable.image_item_background);
        }
    }
}
