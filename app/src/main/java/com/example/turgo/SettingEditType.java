package com.example.turgo;

public enum SettingEditType {
    EDIT_TEXT("EditText"),
    SPINNER("Spinner"),
    UPLOAD("Upload");

    private String type;
    SettingEditType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
