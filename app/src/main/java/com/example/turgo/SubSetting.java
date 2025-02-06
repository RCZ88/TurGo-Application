package com.example.turgo;

public class SubSetting {
    private String subSetting;
    private String preference;
    private SettingEditType type;

    public SubSetting(String subSetting, String preference, SettingEditType type) {
        this.subSetting = subSetting;
        this.preference = preference;
        this.type = type;
    }

    public String getSubSetting() {
        return subSetting;
    }

    public void setSubSetting(String subSetting) {
        this.subSetting = subSetting;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public SettingEditType getType() {
        return type;
    }

    public void setType(SettingEditType type) {
        this.type = type;
    }
}
