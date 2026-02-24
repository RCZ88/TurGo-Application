package com.example.turgo;

import java.util.ArrayList;

public class TeacherTaskSubmissionItem {
    private final String studentName;
    private final String status;
    private final String submittedAt;
    private final ArrayList<String> fileNames;
    private final String primaryFileUrl;

    public TeacherTaskSubmissionItem(String studentName, String status, String submittedAt, ArrayList<String> fileNames, String primaryFileUrl) {
        this.studentName = studentName;
        this.status = status;
        this.submittedAt = submittedAt;
        this.fileNames = fileNames == null ? new ArrayList<>() : fileNames;
        this.primaryFileUrl = primaryFileUrl;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStatus() {
        return status;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public ArrayList<String> getFileNames() {
        return fileNames;
    }

    public String getPrimaryFileUrl() {
        return primaryFileUrl;
    }
}

