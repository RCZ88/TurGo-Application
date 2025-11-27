package com.example.turgo;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TeacherViewHolder extends RecyclerView.ViewHolder {
    TextView teacherName, subjectTeaching;
    ImageView teacherPFP;
    TeacherMini tm;
    int position;
    Pair<Integer, TeacherMini> pair;
    public TeacherViewHolder(@NonNull View itemView, OnItemClickListener<Pair<Integer, TeacherMini>>listener) {
        super(itemView);
        teacherName = itemView.findViewById(R.id.tv_TDVH_teacherName);
        subjectTeaching = itemView.findViewById(R.id.tv_TDVH_teacherSubjectTeached);
        teacherPFP = itemView.findViewById(R.id.iv_TDVH_teacherPFP);
        itemView.setOnClickListener(v-> {
            Log.d("TeacherViewHolder", "Teacher Mini Object: " + pair.two);
            listener.onItemClick(pair);
        });
    }
    public TeacherViewHolder(@NonNull View itemView){
        super(itemView);
        teacherName = itemView.findViewById(R.id.tv_TDVH_teacherName);
        subjectTeaching = itemView.findViewById(R.id.tv_TDVH_teacherSubjectTeached);
        teacherPFP = itemView.findViewById(R.id.iv_TDVH_teacherPFP);

    }
}
