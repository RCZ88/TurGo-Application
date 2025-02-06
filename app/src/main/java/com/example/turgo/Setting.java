package com.example.turgo;

import java.util.ArrayList;

public class Setting {
    private String setting;
    private ArrayList<SubSetting>subSettings;
    public Setting(String setting){
        this.setting = setting;
        subSettings = new ArrayList<>();
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public ArrayList<SubSetting> getSubSettings(){
        return subSettings;
    }
    public void addSubSettings(SubSetting subSetting){
        this.subSettings.add(subSetting);
    }
    public void addSubSettings(String subSetting, String preference, SettingEditType type){
        SubSetting ss = new SubSetting(subSetting, preference, type);
        addSubSettings(ss);
    }
}
