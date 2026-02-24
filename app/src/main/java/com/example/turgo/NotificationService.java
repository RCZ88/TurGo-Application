package com.example.turgo;

import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class NotificationService {
    private static final String TAG = "NotificationService";
    private NotificationService() {
    }

    public static Task<Void> notifyUser(User to, RequireUpdate<?, ?, ?> from, String title, String content) {
        if (to == null || from == null || !Tool.boolOf(title) || !Tool.boolOf(content)) {
            Log.w(TAG, "Skipping notifyUser due to invalid args");
            return Tasks.forResult(null);
        }
        String recipientId = resolveUserId(to);
        if (!Tool.boolOf(recipientId)) {
            Log.w(TAG, "Skipping notifyUser: recipientId is empty");
            return Tasks.forResult(null);
        }

        Notification<RequireUpdate<?, ?, ?>> notification = new Notification<>();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTimeSent(LocalDateTime.now());
        notification.setFrom(from);
        notification.setTo(to);

        NotificationFirebase firebaseObject = new NotificationFirebase();
        firebaseObject.importObjectData((Notification<?>) notification);
        // Ensure recipient id is always populated for downstream push routing.
        firebaseObject.setTo(recipientId);

        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.NOTIFICATION.getPath())
                .child(notification.getID());

        Task<Void> saveTask = notifRef.setValue(firebaseObject);
        Task<Void> indexTask = addNotificationIndex(to, notification.getID());

        Log.d(TAG, "notifyUser queued: notifId=" + notification.getID() + ", to=" + recipientId);
        return Tasks.whenAll(saveTask, indexTask);
    }

    public static Task<Void> notifyUsers(List<? extends User> recipients,
                                         RequireUpdate<?, ?, ?> from,
                                         String title,
                                         String content) {
        if (recipients == null || recipients.isEmpty()) {
            return Tasks.forResult(null);
        }
        List<Task<Void>> tasks = new ArrayList<>();
        for (User recipient : recipients) {
            tasks.add(notifyUser(recipient, from, title, content));
        }
        return Tasks.whenAll(tasks);
    }

    private static Task<Void> addNotificationIndex(User user, String notificationId) {
        if (user == null || !Tool.boolOf(notificationId)) {
            return Tasks.forResult(null);
        }

        String userId = resolveUserId(user);
        if (!Tool.boolOf(userId)) {
            return Tasks.forResult(null);
        }

        if (user instanceof Student) {
            return new StudentRepository(userId).addNotificationId(notificationId);
        }
        if (user instanceof Teacher) {
            return new TeacherRepository(userId).addNotificationId(notificationId);
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference(user.getFirebaseNode().getPath())
                .child(userId);
        return Tasks.whenAll(
                appendUnique(userRef.child("notitficationIDs"), notificationId),
                appendUnique(userRef.child("notificationIDs"), notificationId),
                appendUnique(userRef.child("notifications"), notificationId)
        );
    }

    private static Task<Void> appendUnique(DatabaseReference ref, String item) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        ref.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                ArrayList<String> values = new ArrayList<>();
                Object current = currentData.getValue();
                if (current instanceof List) {
                    for (Object object : (List<?>) current) {
                        if (object instanceof String) {
                            values.add((String) object);
                        }
                    }
                }
                if (!values.contains(item)) {
                    values.add(item);
                }
                currentData.setValue(values);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    tcs.setException(error.toException());
                    return;
                }
                tcs.setResult(null);
            }
        });
        return tcs.getTask();
    }

    private static String resolveUserId(User user) {
        if (user == null) {
            return "";
        }
        if (Tool.boolOf(user.getUid())) {
            return user.getUid();
        }
        if (user instanceof RequireUpdate<?, ?, ?>) {
            return ((RequireUpdate<?, ?, ?>) user).getID();
        }
        return "";
    }
}
