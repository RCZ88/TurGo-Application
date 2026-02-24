package com.example.turgo;

public interface ProfileSubSettingActionListener {
    void onSaveSubSetting(Setting group, SubSetting subSetting, String newValue);
    void onUploadRequested(Setting group, SubSetting subSetting);
}
