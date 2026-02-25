package com.example.turgo;

/**
 * Lightweight data holder for the User Profile screen (read-only view).
 * Populated by {@link StudentRepository#loadProfileData()} or
 * {@link TeacherRepository#loadProfileData()}.
 */
public class UserProfileData {
    public String uid;
    public String userType;
    public String fullName;
    public String nickname;
    public String email;
    public String phoneNumber;
    public String birthDate;
    public String gender;
    public String language;
    public String theme;
    public String pfpCloudinary;

    public UserProfileData() {}
}
