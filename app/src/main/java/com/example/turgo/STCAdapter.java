package com.example.turgo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class STCAdapter extends RecyclerView.Adapter<STCViewHolder>{
    private ArrayList<Student> students;
    private Context context;
    private OnItemClickListener<Student> onclickListener;

    public STCAdapter(ArrayList<Student> students, Context context, OnItemClickListener<Student> onclickListener) {
        this.students = students;
        this.context = context;
        this.onclickListener = onclickListener;
    }

    @NonNull
    @Override
    public STCViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_teachercourse_viewholder, parent, false);
        return new STCViewHolder(view, onclickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull STCViewHolder holder, int position) {
        holder.tv_StudentName.setText(students.get(position).getFullName());
        holder.student = students.get(position);
        Tool.setImageCloudinary(context, students.get(position).getPfpCloudinary(), holder.iv_pfp);
        holder.tv_studentStatus.setText(students.get(position).getUserStatus().toString());
        if(students.get(position).getUserStatus().isOnline()){
            holder.tv_studentStatus.setTextColor(context.getColor(R.color.onlineGreen));
        }else{
            holder.tv_studentStatus.setTextColor(context.getColor(R.color.offlineRed));
        }
    }

    @Override
    public int getItemCount() {
        return students.size();
    }
}
