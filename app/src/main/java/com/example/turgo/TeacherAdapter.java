package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherViewHolder> {

    ArrayList<TeacherMini> teacherFullList;
    ArrayList<TeacherMini> teacherSearchResult;
    Context context;
    OnItemClickListener<TeacherMini>listener;
    public TeacherAdapter(ArrayList<TeacherMini>teachers, Context context, OnItemClickListener<TeacherMini>listener){
        this.teacherFullList = teachers;
        teacherSearchResult = new ArrayList<>();
        this.context = context;
        this.listener = listener;
    }
    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.teacher_display, parent, false);
        if(listener != null){
            return new TeacherViewHolder(view, listener);
        }else{
            return new TeacherViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        Tool.setImageCloudinary(context, teacherSearchResult.get(position).getTeacherPfp(), holder.teacherPFP);
        holder.subjectTeaching.setText(teacherSearchResult.get(position).getSubjectTeaching());
        holder.teacherName.setText(teacherSearchResult.get(position).getTeacherName());
        holder.tm = teacherSearchResult.get(position);
    }

    @Override
    public int getItemCount() {
        return teacherFullList.size();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void filter(String text){
        teacherSearchResult.clear();
        if(text.isEmpty()){
            teacherSearchResult.addAll(teacherFullList);
        }else{
            text = text.toLowerCase();
            for(TeacherMini teacherMini : teacherFullList){
                if(teacherMini.getTeacherName().contains(text)){
                    teacherSearchResult.add(teacherMini);
                }
            }
        }
        notifyDataSetChanged();
    }
}
