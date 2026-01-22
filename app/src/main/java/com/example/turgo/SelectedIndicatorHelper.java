package com.example.turgo;

import androidx.recyclerview.widget.RecyclerView;

public class SelectedIndicatorHelper<C> {
    RecyclerView.ViewHolder currentViewHolder;
    C object;

    public SelectedIndicatorHelper(RecyclerView.ViewHolder currentViewHolder, C object) {
        this.currentViewHolder = currentViewHolder;
        this.object = object;
    }

}
