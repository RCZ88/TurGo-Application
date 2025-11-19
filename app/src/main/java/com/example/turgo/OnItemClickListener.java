package com.example.turgo;

public interface OnItemClickListener<T> {
    void onItemClick(T item);
    void onItemLongClick(T item);
}
