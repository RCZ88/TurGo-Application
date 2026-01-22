package com.example.turgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CourseImageAdapter extends RecyclerView.Adapter<CourseImageViewHolder> {

    ArrayList<String> imageUrls;

    CourseImageAdapter(ArrayList<String>imageUrls){
        this.imageUrls = imageUrls;
    }
    @NonNull
    @Override
    public CourseImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_image_adapter, parent, false);
        return new CourseImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseImageViewHolder holder, int position) {
        String url = imageUrls.get(position);
        Tool.setImageCloudinary(holder.iv_image.getContext(), url, holder.iv_image);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }
}
