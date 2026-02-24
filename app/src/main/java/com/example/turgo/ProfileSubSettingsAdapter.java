package com.example.turgo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileSubSettingsAdapter extends RecyclerView.Adapter<ProfileSubSettingsAdapter.SubSettingViewHolder> {

    private final Context context;
    private final Setting group;
    private final ArrayList<SubSetting> subSettings;
    private final ProfileSubSettingActionListener actionListener;
    private int expandedChild = RecyclerView.NO_POSITION;

    public ProfileSubSettingsAdapter(Context context, Setting group, ProfileSubSettingActionListener actionListener) {
        this.context = context;
        this.group = group;
        this.subSettings = group.getSubSettings();
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public SubSettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_subsetting, parent, false);
        return new SubSettingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubSettingViewHolder holder, int position) {
        SubSetting subSetting = subSettings.get(position);
        holder.tvLabel.setText(subSetting.getSubSetting());
        holder.tvValue.setText(Tool.boolOf(subSetting.getPreference()) ? subSetting.getPreference() : "-");

        boolean isExpanded = expandedChild == position;
        holder.editorContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        bindEditor(holder, subSetting, isExpanded);

        holder.summaryContainer.setOnClickListener(v -> {
            if (subSetting.getType() == SettingEditType.UPLOAD) {
                actionListener.onUploadRequested(group, subSetting);
                return;
            }
            int previous = expandedChild;
            expandedChild = (expandedChild == position) ? RecyclerView.NO_POSITION : position;
            if (previous != RecyclerView.NO_POSITION) {
                notifyItemChanged(previous);
            }
            notifyItemChanged(position);
        });

        holder.btnCancel.setOnClickListener(v -> {
            expandedChild = RecyclerView.NO_POSITION;
            notifyItemChanged(position);
        });

        holder.btnSave.setOnClickListener(v -> {
            String newValue;
            if (subSetting.getType() == SettingEditType.SPINNER) {
                Object selected = holder.spinnerValue.getSelectedItem();
                newValue = selected == null ? "" : selected.toString();
            } else {
                newValue = holder.etValue.getText().toString().trim();
            }
            if (TextUtils.isEmpty(newValue)) {
                Toast.makeText(context, "Please enter a value.", Toast.LENGTH_SHORT).show();
                return;
            }
            subSetting.setPreference(newValue);
            holder.tvValue.setText(newValue);
            actionListener.onSaveSubSetting(group, subSetting, newValue);
            expandedChild = RecyclerView.NO_POSITION;
            notifyItemChanged(position);
        });
    }

    private void bindEditor(SubSettingViewHolder holder, SubSetting subSetting, boolean isExpanded) {
        if (!isExpanded) {
            return;
        }

        if (subSetting.getType() == SettingEditType.SPINNER) {
            holder.spinnerValue.setVisibility(View.VISIBLE);
            holder.etValue.setVisibility(View.GONE);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    context,
                    android.R.layout.simple_spinner_item,
                    optionsFor(subSetting)
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spinnerValue.setAdapter(adapter);
            int selected = adapter.getPosition(subSetting.getPreference());
            if (selected >= 0) {
                holder.spinnerValue.setSelection(selected);
            }
        } else {
            holder.spinnerValue.setVisibility(View.GONE);
            holder.etValue.setVisibility(View.VISIBLE);
            holder.etValue.setText(subSetting.getPreference());
            holder.etValue.setSelection(holder.etValue.getText().length());
        }
    }

    private ArrayList<String> optionsFor(SubSetting subSetting) {
        ArrayList<String> options = new ArrayList<>();
        String key = subSetting.getSubSetting();
        if ("Gender".equalsIgnoreCase(key)) {
            options.add("Male");
            options.add("Female");
            options.add("Other");
            return options;
        }
        if ("Theme".equalsIgnoreCase(key)) {
            options.add(Theme.SYSTEM.getTheme());
            options.add(Theme.LIGHT.getTheme());
            options.add(Theme.DARK.getTheme());
            return options;
        }
        if ("Language".equalsIgnoreCase(key)) {
            options.add(Language.ENGLISH.getDisplayName());
            options.add(Language.INDONESIAN.getDisplayName());
            return options;
        }
        if ("Notifications".equalsIgnoreCase(key)) {
            options.add("Enabled");
            options.add("Disabled");
            return options;
        }
        if ("Auto Schedule Meeting".equalsIgnoreCase(key)) {
            options.add("1");
            options.add("2");
            options.add("3");
            options.add("4");
            return options;
        }
        options.add(Tool.boolOf(subSetting.getPreference()) ? subSetting.getPreference() : "Default");
        return options;
    }

    @Override
    public int getItemCount() {
        return subSettings.size();
    }

    static class SubSettingViewHolder extends RecyclerView.ViewHolder {
        LinearLayout summaryContainer;
        LinearLayout editorContainer;
        TextView tvLabel;
        TextView tvValue;
        EditText etValue;
        Spinner spinnerValue;
        Button btnCancel;
        Button btnSave;

        SubSettingViewHolder(@NonNull View itemView) {
            super(itemView);
            summaryContainer = itemView.findViewById(R.id.ll_pss_summary);
            editorContainer = itemView.findViewById(R.id.ll_pss_editor);
            tvLabel = itemView.findViewById(R.id.tv_pss_label);
            tvValue = itemView.findViewById(R.id.tv_pss_value);
            etValue = itemView.findViewById(R.id.et_pss_value);
            spinnerValue = itemView.findViewById(R.id.sp_pss_value);
            btnCancel = itemView.findViewById(R.id.btn_pss_cancel);
            btnSave = itemView.findViewById(R.id.btn_pss_save);
        }
    }
}
