package com.example.turgo;

import java.io.File;
import java.time.LocalDateTime;

public class file {
    private File file;
    private User uploader;
    private LocalDateTime fileCreateDate;
    public file(File file, User user, LocalDateTime fileCreateDate){
        this.file = file;
        this.uploader = user;
        this.fileCreateDate = fileCreateDate;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public User getUploader() {
        return uploader;
    }

    public void setUploader(User uploader) {
        this.uploader = uploader;
    }

    public LocalDateTime getFileCreateDate() {
        return fileCreateDate;
    }

    public void setFileCreateDate(LocalDateTime fileCreateDate) {
        this.fileCreateDate = fileCreateDate;
    }
}
