package com.example.turgo;

public enum BuildInBanner {
    BLUE("https://res.cloudinary.com/daccry0jr/image/upload/v1760587817/hyncdevsv92vkf4xl5te.png"),
    GREEN("https://res.cloudinary.com/daccry0jr/image/upload/v1760587835/tldbom7l9jfnrsf8zzu2.png"),
    RED("https://res.cloudinary.com/daccry0jr/image/upload/v1760587847/fekyq44pgqymklluoaoi.png");
    private final String link;
    BuildInBanner(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }
}
