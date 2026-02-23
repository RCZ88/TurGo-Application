package com.example.turgo;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
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
    private ArrayList<Mail> drafts;
    private ArrayList<Notification<?>> notifications;
    private ArrayList<String> inboxIds;
    private ArrayList<String> outboxIds;
    private ArrayList<String> draftsIds;
    private ArrayList<String> notificationsIds;
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
        this.drafts = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.inboxIds = new ArrayList<>();
        this.outboxIds = new ArrayList<>();
        this.draftsIds = new ArrayList<>();
        this.notificationsIds = new ArrayList<>();
    }
    public abstract FirebaseNode getFirebaseNode();
    public static String getSerializeKeyCode() {
        return SERIALIZE_KEY_CODE;
    }

    public static void setSerializeKeyCode(String serializeKeyCode) {
        SERIALIZE_KEY_CODE = serializeKeyCode;
    }

    public User(){
        this.inbox = new ArrayList<>();
        this.outbox = new ArrayList<>();
        this.drafts = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.inboxIds = new ArrayList<>();
        this.outboxIds = new ArrayList<>();
        this.draftsIds = new ArrayList<>();
        this.notificationsIds = new ArrayList<>();
    }
    public UserStatus getUserStatus(){
        return UserPresenceManager.getUserStatus(this);
    }

    private <T extends RequireUpdate<?, ?, ?>> ArrayList<String> syncIdsFromObjects(ArrayList<String> ids, ArrayList<T> objects) {
        if (ids == null) {
            ids = new ArrayList<>();
        }
        if (objects != null) {
            for (T object : objects) {
                if (object == null || !Tool.boolOf(object.getID())) {
                    continue;
                }
                if (!ids.contains(object.getID())) {
                    ids.add(object.getID());
                }
            }
        }
        return ids;
    }

    private <T> Task<ArrayList<T>> loadByIds(ArrayList<String> ids, Loader<T> loader) {
        TaskCompletionSource<ArrayList<T>> tcs = new TaskCompletionSource<>();
        if (!Tool.boolOf(ids)) {
            tcs.setResult(new ArrayList<>());
            return tcs.getTask();
        }
        ArrayList<Task<T>> tasks = new ArrayList<>();
        for (String id : ids) {
            if (!Tool.boolOf(id)) {
                continue;
            }
            tasks.add(loader.load(id).continueWith(task -> task.isSuccessful() ? task.getResult() : null));
        }
        if (tasks.isEmpty()) {
            tcs.setResult(new ArrayList<>());
            return tcs.getTask();
        }
        Tasks.whenAllComplete(tasks).addOnCompleteListener(done -> {
            ArrayList<T> result = new ArrayList<>();
            for (Task<T> task : tasks) {
                if (task.isSuccessful() && task.getResult() != null) {
                    result.add(task.getResult());
                }
            }
            tcs.setResult(result);
        });
        return tcs.getTask();
    }

    private interface Loader<T> {
        Task<T> load(String id);
    }

    public ArrayList<Mail> getOutbox() {
        return outbox;
    }

    public ArrayList<String> getOutboxIds() {
        outboxIds = syncIdsFromObjects(outboxIds, outbox);
        return outboxIds;
    }

    public Task<ArrayList<Mail>> getOutboxTask() {
        return loadByIds(getOutboxIds(), id -> new MailRepository(id).loadAsNormal());
    }

    public ArrayList<Mail> getDrafts() {
        return drafts;
    }

    public ArrayList<String> getDraftMailsIds() {
        draftsIds = syncIdsFromObjects(draftsIds, drafts);
        return draftsIds;
    }

    public Task<ArrayList<Mail>> getDraftsTask() {
        return loadByIds(getDraftMailsIds(), id -> new MailRepository(id).loadAsNormal());
    }
    public void addDraftMail(Mail draftMail) throws IllegalAccessException, InstantiationException {
        drafts.add(draftMail);
        if (draftMail != null && Tool.boolOf(draftMail.getID()) && !getDraftMailsIds().contains(draftMail.getID())) {
            getDraftMailsIds().add(draftMail.getID());
        }
        ((UserRepositoryClass) ((RequireUpdate<?, ?, ?>) this).getRepositoryInstance()).draftMail(draftMail);

    }
    public void setDrafts(ArrayList<Mail> drafts) {
        this.drafts = drafts;
    }

    public void setDraftMailsIds(ArrayList<String> draftMailsIds) {
        this.draftsIds = draftMailsIds;
    }

    public void setOutbox(ArrayList<Mail> outbox) {
        this.outbox = outbox;
    }

    public void setOutboxIds(ArrayList<String> outboxIds) {
        this.outboxIds = outboxIds;
    }

    public ArrayList<Notification<?>> getNotifications() {
        return notifications;
    }

    public ArrayList<String> getNotificationsIds() {
        notificationsIds = syncIdsFromObjects(notificationsIds, notifications);
        return notificationsIds;
    }

    public Task<ArrayList<Notification<?>>> getNotificationsTask() {
        return this.<Notification<?>>loadByIds(getNotificationsIds(), id -> new NotificationRepository(id).loadAsNormal());
    }

    public void setNotifications(ArrayList<Notification<?>> notifications) {
        this.notifications = notifications;
    }

    public void setNotificationsIds(ArrayList<String> notificationsIds) {
        this.notificationsIds = notificationsIds;
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

    public ArrayList<String> getInboxIds() {
        inboxIds = syncIdsFromObjects(inboxIds, inbox);
        return inboxIds;
    }

    public Task<ArrayList<Mail>> getInboxTask() {
        return loadByIds(getInboxIds(), id -> new MailRepository(id).loadAsNormal());
    }

    public void setInbox(ArrayList<Mail> inbox) {
        this.inbox = inbox;
    }

    public void setInboxIds(ArrayList<String> inboxIds) {
        this.inboxIds = inboxIds;
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
                ", draftMails=" + drafts +
                ", notifications=" + notifications +
                ", pfpCloudinary='" + pfpCloudinary + '\'' +
                '}';
    }

    public static void sendMail(Mail mail, User from, User to) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        if (mail == null || from == null || to == null) return;
        mail.setFrom(from);
        mail.setTo(to);

        to.inbox.add(mail);
        if (Tool.boolOf(mail.getID()) && !to.getInboxIds().contains(mail.getID())) {
            to.getInboxIds().add(mail.getID());
        }
        ((UserRepositoryClass) ((RequireUpdate<?, ?, ?>) to).getRepositoryInstance()).recieveMail(mail);

        from.outbox.add(mail);
        if (Tool.boolOf(mail.getID()) && !from.getOutboxIds().contains(mail.getID())) {
            from.getOutboxIds().add(mail.getID());
        }
        ((UserRepositoryClass) ((RequireUpdate<?, ?, ?>) from).getRepositoryInstance()).sendMail(mail);
    }

    public static void sendMail(Mail mail) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        User from = mail != null ? mail.getFromCached() : null;
        User to = mail != null ? mail.getToCached() : null;
        if (from == null || to == null) {
            throw new IllegalStateException("Mail sender/recipient must be resolved before sendMail(mail). Use sendMail(mail, from, to).");
        }
        sendMail(mail, from, to);
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
        if (notification != null && Tool.boolOf(notification.getID()) && !getNotificationsIds().contains(notification.getID())) {
            getNotificationsIds().add(notification.getID());
        }
    }

    public String getPfpCloudinary() {
        return pfpCloudinary;
    }

    public void setPfpCloudinary(String pfpCloudinary) {
        this.pfpCloudinary = pfpCloudinary;
    }
}
