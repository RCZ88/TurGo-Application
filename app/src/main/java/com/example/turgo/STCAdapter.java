package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Set;

public class STCAdapter extends RecyclerView.Adapter<STCViewHolder>{
    private ArrayList<Student> students;
    private Context context;
    private OnItemClickListener<Student> onclickListener;
    private final Set<String> selectedStudentIds;

    public STCAdapter(ArrayList<Student> students, Context context, OnItemClickListener<Student> onclickListener, Set<String> selectedStudentIds) {
        this.students = students;
        this.context = context;
        this.onclickListener = onclickListener;
        this.selectedStudentIds = selectedStudentIds;
    }

    @NonNull
    @Override
    public STCViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_teachercourse_viewholder, parent, false);
        return new STCViewHolder(view, onclickListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull STCViewHolder holder, int position) {
        Student currentStudent = students.get(position);
        holder.student = currentStudent;

        // Set basic info
        holder.tv_StudentName.setText(currentStudent.getFullName());
        Tool.setImageCloudinary(context, currentStudent.getPfpCloudinary(), holder.iv_pfp);
        String studentId = resolveStudentId(currentStudent);
        boolean isSelected = Tool.boolOf(studentId) && selectedStudentIds.contains(studentId);
        if (holder.mcv_container != null) {
            holder.mcv_container.setStrokeWidth(isSelected ? 2 : 1);
            holder.mcv_container.setStrokeColor(context.getColor(isSelected ? R.color.brand_emerald : R.color.brand_emerald_pale));
            holder.mcv_container.setCardBackgroundColor(context.getColor(isSelected ? R.color.surface_inner_panel : R.color.white));
        }
        holder.itemView.setActivated(isSelected);
        holder.itemView.setAlpha(isSelected ? 1f : 0.96f);

        // Modern Status Logic
        boolean isOnline = currentStudent.getUserStatus() != null && currentStudent.getUserStatus().isOnline();

        if (isOnline) {
            holder.tv_studentStatus.setText("ONLINE");
            // Use the emerald green dot
            holder.v_statusDot.setBackgroundResource(R.drawable.shape_status_dot_online);
            // Set text color to a dark green for readability
            holder.tv_studentStatus.setTextColor(context.getColor(R.color.brand_emerald_dark));
            // Use the light green pill background
            holder.ll_statusContainer.setBackgroundResource(R.drawable.bg_status_pill_light);
            holder.ll_statusContainer.setAlpha(1f);
        } else {
            holder.tv_studentStatus.setText("OFFLINE");
            // Use a gray dot for offline
            holder.v_statusDot.setBackgroundResource(R.drawable.shape_status_dot_offline);
            // Set text color to a muted gray
            holder.tv_studentStatus.setTextColor(context.getColor(R.color.brand_emerald)); // Or a gray color
            // Use a light gray pill background (Optional: create bg_status_pill_offline)
            holder.ll_statusContainer.setAlpha(0.6f); // Ghost out the pill slightly if offline
        }
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    private String resolveStudentId(Student student) {
        if (student == null) {
            return "";
        }
        if (Tool.boolOf(student.getUid())) {
            return student.getUid();
        }
        return Tool.boolOf(student.getID()) ? student.getID() : "";
    }
}
