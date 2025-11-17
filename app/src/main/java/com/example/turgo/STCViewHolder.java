package com.example.turgo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class STCViewHolder extends RecyclerView.ViewHolder {
    TextView tv_StudentName, tv_studentStatus;
    ImageView iv_pfp;
    Student student;
    public STCViewHolder(@NonNull View itemView, OnItemClickListener<Student>onclickListener) {
        super(itemView);
        tv_StudentName = itemView.findViewById(R.id.tv_stc_StudentName);
        tv_studentStatus = itemView.findViewById(R.id.tv_stc_StudentStatus);
        iv_pfp = itemView.findViewById(R.id.iv_stc_ProfilePicture);
        itemView.setOnClickListener(view -> {
            onclickListener.onItemClick(student);
        });
    }
}
