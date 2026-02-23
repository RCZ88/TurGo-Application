package com.example.turgo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.google.android.material.card.MaterialCardView;
import androidx.recyclerview.widget.RecyclerView;

public class STCViewHolder extends RecyclerView.ViewHolder {
    TextView tv_StudentName, tv_studentStatus;
    ImageView iv_pfp;
    View v_statusDot; // Added for the green/gray dot
    View ll_statusContainer; // Added for the pill background
    MaterialCardView mcv_container;
    Student student;

    public STCViewHolder(@NonNull View itemView, OnItemClickListener<Student> onclickListener) {
        super(itemView);
        tv_StudentName = itemView.findViewById(R.id.tv_stc_StudentName);
        tv_studentStatus = itemView.findViewById(R.id.tv_stc_StudentStatus);
        iv_pfp = itemView.findViewById(R.id.iv_stc_ProfilePicture);

        // New UI references
        v_statusDot = itemView.findViewById(R.id.v_status_dot);
        ll_statusContainer = itemView.findViewById(R.id.ll_status_container);
        mcv_container = itemView.findViewById(R.id.mcv_stc_container);

        itemView.setOnClickListener(view -> {
            if (onclickListener != null) {
                onclickListener.onItemClick(student);
            }
        });
        itemView.setOnLongClickListener(view -> {
            if (onclickListener != null) {
                onclickListener.onItemLongClick(student);
                return true;
            }
            return false;
        });
    }
}
