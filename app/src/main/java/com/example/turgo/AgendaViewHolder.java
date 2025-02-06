package com.example.turgo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AgendaViewHolder extends RecyclerView.ViewHolder{
    TextView tv_dateDisplay, tv_contents, tv_teacherName;
    public AgendaViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_dateDisplay = itemView.findViewById(R.id.tv_AgendaDate);
        tv_contents = itemView.findViewById(R.id.tv_AgendaContent);
        tv_teacherName = itemView.findViewById(R.id.tv_AgendaTeacher);
    }
}
