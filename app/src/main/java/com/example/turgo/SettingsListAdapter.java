package com.example.turgo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class SettingsListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<Setting>settings;

    public SettingsListAdapter(Context context, ArrayList<Setting>settings) {
        this.context = context;
        this.settings = settings;
    }

    @Override
    public int getGroupCount() {
        return this.settings.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.settings.get(groupPosition).getSubSettings().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.settings.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.settings.get(groupPosition).getSubSettings().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Setting setting = (Setting) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
        }
        TextView listTitleTextView = convertView.findViewById(android.R.id.text1);
        listTitleTextView.setText(setting.getSetting());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        SubSetting subSetting = (SubSetting) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.settingsubcategoryview, null);
        }

        TextView subSettingsCategory = convertView.findViewById(R.id.tv_SettingsSubCategory);
        TextView settingsSelected = convertView.findViewById(R.id.tv_SelectedSettingOption);

        subSettingsCategory.setText(subSetting.getSubSetting());
        settingsSelected.setText(subSetting.getPreference());

        settingsSelected.setOnClickListener(view -> {

        });


        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
