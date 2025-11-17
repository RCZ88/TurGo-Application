package com.example.turgo;

public interface CloudinaryUploadCallback {
    void onUploadComplete(String cloudinaryUrl, String fileName);
    void onUploadFailed(Exception error);
}
