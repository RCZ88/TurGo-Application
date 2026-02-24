package com.example.turgo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileSettingsAdapter extends RecyclerView.Adapter<ProfileSettingsAdapter.SettingGroupViewHolder> {

    private final Context context;
    private final ArrayList<Setting> settings;
    private final ProfileSubSettingActionListener actionListener;
    private int expandedGroup = RecyclerView.NO_POSITION;

    public ProfileSettingsAdapter(Context context, ArrayList<Setting> settings, ProfileSubSettingActionListener actionListener) {
        this.context = context;
        this.settings = settings;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public SettingGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_setting_group, parent, false);
        return new SettingGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingGroupViewHolder holder, int position) {
        Setting setting = settings.get(position);
        holder.tvTitle.setText(setting.getSetting());

        boolean isExpanded = expandedGroup == position;
        holder.subSettingsRecycler.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.chevron.setImageResource(isExpanded ? R.drawable.caret : R.drawable.caret_down);

        holder.subSettingsRecycler.setLayoutManager(new LinearLayoutManager(context));
        holder.subSettingsRecycler.setAdapter(new ProfileSubSettingsAdapter(context, setting, actionListener));

        holder.headerContainer.setOnClickListener(v -> {
            int previous = expandedGroup;
            expandedGroup = (expandedGroup == position) ? RecyclerView.NO_POSITION : position;
            if (previous != RecyclerView.NO_POSITION) {
                notifyItemChanged(previous);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return settings.size();
    }

    static class SettingGroupViewHolder extends RecyclerView.ViewHolder {
        LinearLayout headerContainer;
        TextView tvTitle;
        ImageView chevron;
        RecyclerView subSettingsRecycler;

        SettingGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            headerContainer = itemView.findViewById(R.id.ll_psg_header);
            tvTitle = itemView.findViewById(R.id.tv_psg_title);
            chevron = itemView.findViewById(R.id.iv_psg_chevron);
            subSettingsRecycler = itemView.findViewById(R.id.rv_psg_subsettings);
        }
    }
}
