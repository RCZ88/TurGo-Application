package com.example.turgo;

public enum BannerTheme {
    RED("red"),
    BLUE("blue"),
    GREEN("blue");
    private final String bannerColor;

    BannerTheme(String bannerColor) {
        this.bannerColor = bannerColor;
    }

    public String getBannerColor() {
        return bannerColor;
    }
}
