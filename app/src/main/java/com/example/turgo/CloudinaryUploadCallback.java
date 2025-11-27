package com.example.turgo;

import android.net.Uri;

public interface CloudinaryUploadCallback {
    void onUploadComplete(String cloudinaryUrl, String fileName, Uri uri);
    void onUploadFailed(Exception error);
}
