package com.example.turgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ComposeRecipientListAdapter extends RecyclerView.Adapter<ComposeRecipientViewHolder> {

    public interface RecipientActionListener {
        void onAction(User user);
    }

    private final ArrayList<User> users = new ArrayList<>();
    private final RecipientActionListener actionListener;
    private final boolean selectedListMode;

    public ComposeRecipientListAdapter(boolean selectedListMode, RecipientActionListener actionListener) {
        this.selectedListMode = selectedListMode;
        this.actionListener = actionListener;
    }

    public void submitList(List<User> updated) {
        users.clear();
        if (updated != null) {
            users.addAll(updated);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComposeRecipientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_compose_recipient, parent, false);
        return new ComposeRecipientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComposeRecipientViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvName.setText(Tool.boolOf(user.getFullName()) ? user.getFullName() : "-");
        holder.tvRole.setText(resolveRole(user));
        holder.tvEmail.setText(Tool.boolOf(user.getEmail()) ? user.getEmail() : "-");
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onAction(user);
            }
        });
        holder.itemView.setAlpha(selectedListMode ? 0.95f : 1.0f);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private String resolveRole(User user) {
        if (user instanceof Student) {
            return "Student";
        }
        if (user instanceof Teacher) {
            return "Teacher";
        }
        if (user instanceof Parent) {
            return "Parent";
        }
        if (user instanceof Admin) {
            return "Admin";
        }
        return "User";
    }
}
