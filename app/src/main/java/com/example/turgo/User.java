package com.example.turgo;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public abstract class User implements Serializable{
    protected static final String FIREBASE_DB_REFERENCE = "users";
    public static String SERIALIZE_KEY_CODE = "userObj";
    private String uid;
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
    private ArrayList<Mail> outbox;
    private ArrayList<Mail> draftMails;
    private ArrayList<Notification<?>> notifications;
    private String pfpCloudinary;
    public User(UserType userType, String gender, String fullName, String birthDate,  String nickname, String email, String phoneNumber) throws ParseException {
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
        this.inbox = new ArrayList<>();
        this.outbox = new ArrayList<>();
        this.draftMails = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }
    public abstract FirebaseNode getFirebaseNode();
    public abstract void updateUserDB() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException;
    public static String getSerializeKeyCode() {
        return SERIALIZE_KEY_CODE;
    }

    public static void setSerializeKeyCode(String serializeKeyCode) {
        SERIALIZE_KEY_CODE = serializeKeyCode;
    }

    public User(){
        this.inbox = new ArrayList<>();
        this.outbox = new ArrayList<>();
        this.draftMails = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }
    public UserStatus getUserStatus(){
        return UserPresenceManager.getUserStatus(this);
    }

    public ArrayList<Mail> getOutbox() {
        return outbox;
    }

    public ArrayList<Mail> getDraftMails() {
        return draftMails;
    }
    public void addDraftMail(Mail draftMail) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        draftMails.add(draftMail);
        updateUserDB();
    }
    public void setDraftMails(ArrayList<Mail> draftMails) {
        this.draftMails = draftMails;
    }

    public void setOutbox(ArrayList<Mail> outbox) {
        this.outbox = outbox;
    }

    public ArrayList<Notification<?>> getNotifications() {
        return notifications;
    }

    public void setNotifications(ArrayList<Notification<?>> notifications) {
        this.notifications = notifications;
    }
    public abstract String getSerializeCode();


    private void addStatusToDB(){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.USER_STATUS.getPath()).child(uid);
        userRef.setValue(new UserStatus(uid, true, LocalDateTime.now().toString()));
    }

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

    public String getUid(){
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
                "UID='" + uid + '\'' +
                ", userType=" + userType +
                ", fullName='" + fullName + '\'' +
                ", age=" + age +
                ", birthDate='" + birthDate + '\'' +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", language=" + language +
                ", theme=" + theme +
                ", inbox=" + inbox +
                ", outbox=" + outbox +
                ", draftMails=" + draftMails +
                ", notifications=" + notifications +
                ", pfpCloudinary='" + pfpCloudinary + '\'' +
                '}';
    }

    public static void sendMail(Mail mail) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        User to = mail.getTo();
        to.inbox.add(mail);
        ((UserRepositoryClass)(((RequireUpdate)to).getRepositoryClass().newInstance())).recieveMail(mail);
        User from = mail.getFrom();
        from.outbox.add(mail);
        ((UserRepositoryClass)(((RequireUpdate)from).getRepositoryClass().newInstance())).sendMail(mail);
    }


     public static void getUserDataFromDB(String uid, ObjectCallBack<User> callback) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(FIREBASE_DB_REFERENCE).child(uid);

         userRef.addValueEventListener(new ValueEventListener() {
             @SuppressLint("RestrictedApi")
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) throws RuntimeException {
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
                         try {
                             callback.onObjectRetrieved(user);
                         } catch (ParseException | InvocationTargetException |
                                  NoSuchMethodException | IllegalAccessException |
                                  InstantiationException e) {
                             throw new RuntimeException(e);
                         }
                     } else {
                         callback.onError(DatabaseError.fromCode(DatabaseError.DATA_STALE));
                     }
                 } else {
                     callback.onError(DatabaseError.fromCode(DatabaseError.DATA_STALE));
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {
                 callback.onError(error);
             }
         });
    }

    public void recieveNotification(Notification<?>notification){
        this.notifications.add(notification);
    }

    public String getPfpCloudinary() {
        return pfpCloudinary;
    }

    public void setPfpCloudinary(String pfpCloudinary) {
        this.pfpCloudinary = pfpCloudinary;
    }
}
