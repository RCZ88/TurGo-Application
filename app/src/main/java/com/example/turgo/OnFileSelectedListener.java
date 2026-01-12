package com.example.turgo;

import android.net.Uri;

public interface OnFileSelectedListener {
    void onFileSelected(String fileName, Uri uri);
//    void onUploadComplete(String cloudinaryUrl, String fileName, Uri uri);
//    void onUploadFailed(Exception error);
}
