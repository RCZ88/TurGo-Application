package com.example.turgo;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

public class TeacherViewHolder extends RecyclerView.ViewHolder {
    TextView teacherName, subjectTeaching;
    ImageView teacherPFP;
    MaterialButton addTeacher;
    TeacherMini tm;
    int position;
    Pair<Integer, TeacherMini> pair;
    public TeacherViewHolder(@NonNull View itemView, OnItemClickListener<Pair<Integer, TeacherMini>>listener) {
        super(itemView);
        bindViews(itemView);
        addTeacher.setOnClickListener(v-> {
            Log.d("TeacherViewHolder", "Teacher Mini Object: " + pair.two);
            listener.onItemClick(pair);
        });
    }

    public TeacherViewHolder(@NonNull View itemView){
        super(itemView);
        bindViews(itemView);
    }

    private void bindViews(@NonNull View itemView) {
        teacherName = itemView.findViewById(R.id.tv_TDVH_teacherName);
        subjectTeaching = itemView.findViewById(R.id.tv_TDVH_teacherSubjectTeached);
        teacherPFP = itemView.findViewById(R.id.iv_TDVH_teacherPFP);
        addTeacher = itemView.findViewById(R.id.btn_TDVH_AddTeacher);
    }
}
