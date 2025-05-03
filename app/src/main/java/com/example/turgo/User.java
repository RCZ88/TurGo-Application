package com.example.turgo;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class User implements Serializable {
    private static final String FIREBASE_DB_REFERENCE = "Users";
    public static String SERIALIZE_KEY_CODE;
    private String UID;
    private UserType userType;
    private String fullName;
    private int age;
    private String birthDate;
    private String nickname;
    private String email;
    private String gender;
    private String phoneNumber;
    private Language language;
    private Theme theme;
    private ArrayList<Mail> inbox;

    public User(UserType userType, String gender, String fullName, String birthDate,  String nickname, String email, String phoneNumber, String SERIALIZE_KEY_CODE) throws ParseException {
        this.userType = userType;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        calculateAge();
        this.nickname = nickname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.language = Language.ENGLISH;
        this.theme = Theme.SYSTEM;
        User.SERIALIZE_KEY_CODE = SERIALIZE_KEY_CODE;
    }

    public User(){}


    public void calculateAge() throws ParseException {
        Date dateOfBirth;
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        dateOfBirth = dateFormat.parse(birthDate);
        LocalDate today = LocalDate.now();
        LocalDate birthDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        age = Period.between(birthDate, today).getYears();
    }
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge(){
        return age;
    }

    public void setAge(int age){
        this.age = age;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUID(){
        return UID;
    }

    public void setUID(String uid) {
        UID = uid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getBirthDate(){return birthDate;}

    public void setBirthDate(String birthDate){this.birthDate = birthDate;}

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public ArrayList<Mail> getInbox() {
        return inbox;
    }

    public void setInbox(ArrayList<Mail> inbox) {
        this.inbox = inbox;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Language getLanguage() {
        return language;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "User{" +
                ", fullName='" + fullName + '\'' +
                ", age=" + age +
                ", birthDate=" + birthDate +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
    public static void sendMail(Mail mail){
        mail.getTo().inbox.add(mail);
    }

     public static void getUserDataFromDB(String uid, ObjectCallBack<User> callback) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(FIREBASE_DB_REFERENCE).child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String type = snapshot.child("type").getValue(String.class);
                    User user = null;

                    switch (Objects.requireNonNull(type)) {
                        case "STUDENT":
                            user = snapshot.getValue(Student.class);
                            break;
                        case "TEACHER":
                            user = snapshot.getValue(Teacher.class);
                            break;
                        case "PARENT":
                            user = snapshot.getValue(Parent.class);
                            break;
                        case "ADMIN":
                            user = snapshot.getValue(Admin.class);
                            break;
                    }

                    if (user != null) {
                        callback.onObjectRetrieved(user); // Pass the user back to the callback
                    } else {
                        callback.onError(DatabaseError.fromCode(DatabaseError.DATA_STALE)); // Handle null user
                    }
                } else {
                    callback.onError(DatabaseError.fromCode(DatabaseError.DATA_STALE)); // Handle no data found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error); // Pass the error back to the callback
            }
        });
    }



}
