package com.example.turgo;

import java.time.LocalDateTime;
import java.util.UUID;

public class file implements RequireUpdate<file, fileFirebase, FileRepository>{
    private final FirebaseNode fbn = FirebaseNode.FILE;
    private final Class<fileFirebase> fbc = fileFirebase.class;
    private String fileID;
    private String secureURL; //Cloudinary
    private String fileName;
    private static RTDBManager<String> linkRTDB;
    private User uploader;
    private LocalDateTime fileCreateDate;
    private LocalDateTime submitTime;
    private Task ofTask;

    public file(String fileName, String cloudinaryURL, User user, LocalDateTime fileCreateDate){
        this(fileName, cloudinaryURL, user, fileCreateDate, null);
    }

    public file(String fileName, String cloudinaryURL, User user, LocalDateTime fileCreateDate, Task ofTask){
        this.fileID = UUID.randomUUID().toString();
        this.secureURL = cloudinaryURL;
        this.uploader = user;
        this.fileName = fileName;
        this.fileCreateDate = fileCreateDate;
        this.ofTask = ofTask;
    }
    public file(){this.fileID = UUID.randomUUID().toString();;}



    public FirebaseNode getFbn() {
        return fbn;
    }

    public Class<fileFirebase> getFbc() {
        return fbc;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public static RTDBManager<String> getLinkRTDB() {
        return linkRTDB;
    }

    public static void setLinkRTDB(RTDBManager<String> linkRTDB) {
        file.linkRTDB = linkRTDB;
    }

    public void setOfTask(Task task){
        ofTask = task;
    }
    public Task getOfTask(){
        return ofTask;
    }
    public void setSubmitTime(LocalDateTime submitTime){
        this.submitTime = submitTime;
    }
    public LocalDateTime getSubmitTime(){
        return submitTime;
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

    public String getSecureURL() {
        return secureURL;
    }

    public void setSecureURL(String secureURL) {
        this.secureURL = secureURL;
        if (linkRTDB != null && uploader != null && Tool.boolOf(uploader.getUid()) && Tool.boolOf(secureURL)) {
            linkRTDB.storeData("URL Cloudinary", uploader.getUid(), secureURL, "Submission url", "Submission url");
        }
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<fileFirebase> getFirebaseClass() {
        return fbc;
    }

    @Override
    public Class<FileRepository> getRepositoryClass() {
        return FileRepository.class;
    }


    @Override
    public String getID() {
        return fileID;
    }


}
