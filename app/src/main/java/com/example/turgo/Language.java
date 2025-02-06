package com.example.turgo;

public enum Language {
    ENGLISH("English"),
    INDONESIAN("Indonesian");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
