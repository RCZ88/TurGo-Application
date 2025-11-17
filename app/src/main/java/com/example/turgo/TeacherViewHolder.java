package com.example.turgo;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TeacherViewHolder extends RecyclerView.ViewHolder {
    TextView teacherName, subjectTeaching;
    ImageView teacherPFP;
    TeacherMini tm;
    public TeacherViewHolder(@NonNull View itemView, OnItemClickListener<TeacherMini>listener) {
        super(itemView);
        teacherName = itemView.findViewById(R.id.tv_TDVH_teacherName);
        subjectTeaching = itemView.findViewById(R.id.tv_TDVH_teacherSubjectTeached);
        teacherPFP = itemView.findViewById(R.id.iv_TDVH_teacherPFP);

        itemView.setOnClickListener(v-> listener.onItemClick(tm));
    }
    public TeacherViewHolder(@NonNull View itemView){
        super(itemView);
        teacherName = itemView.findViewById(R.id.tv_TDVH_teacherName);
        subjectTeaching = itemView.findViewById(R.id.tv_TDVH_teacherSubjectTeached);
        teacherPFP = itemView.findViewById(R.id.iv_TDVH_teacherPFP);

    }
}
