package com.example.turgo;

public enum Theme {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("System");
    private String theme;
    Theme(String theme){
        this.theme = theme;
    }

    public String getTheme() {
        return theme;
    }
}
