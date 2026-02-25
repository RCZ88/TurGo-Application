package com.example.turgo;

public interface UserRepositoryClass {
    void sendMail(Mail mail);
    void recieveMail(Mail mail);
    void draftMail(Mail mail);
    com.google.android.gms.tasks.Task<UserProfileData> loadProfileData();
}
