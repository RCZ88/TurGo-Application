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
    boolean showButton;
    OnItemClickListener<Pair<Integer, TeacherMini>>listener;
    public TeacherAdapter(ArrayList<TeacherMini>teachers, Context context, OnItemClickListener<Pair<Integer, TeacherMini>>listener, boolean showButton){
        this.teacherFullList = teachers;
        teacherSearchResult = new ArrayList<>(teachers);
        this.context = context;
        this.listener = listener;
        this.showButton = showButton;
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
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Tool.setImageCloudinary(context, teacherSearchResult.get(position).getTeacherPfp(), holder.teacherPFP);
        holder.subjectTeaching.setText(teacherSearchResult.get(position).getSubjectTeaching());
        holder.teacherName.setText(teacherSearchResult.get(position).getTeacherName());
        holder.tm = teacherSearchResult.get(position);
        boolean canShowAction = this.showButton && listener != null;
        holder.addTeacher.setVisibility(canShowAction ? View.VISIBLE : View.GONE);
        holder.position = position;
        holder.pair =  new Pair<>(position, holder.tm);
    }

    @Override
    public int getItemCount() {
        return teacherSearchResult.size();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void filter(String text){
        teacherSearchResult.clear();
        if(text.isEmpty()){
            teacherSearchResult.addAll(teacherFullList);
        }else{
            text = text.toLowerCase();
            for(TeacherMini teacherMini : teacherFullList){
                if(teacherMini.getTeacherName().toLowerCase().contains(text)){
                    teacherSearchResult.add(teacherMini);
                }
            }
        }
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void changeTeacherSelected(TeacherMini tm){
        teacherSearchResult.clear();
        teacherSearchResult.add(tm);
        notifyDataSetChanged();
    }
}
