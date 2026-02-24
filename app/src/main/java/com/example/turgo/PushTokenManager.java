package com.example.turgo;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class PushTokenManager {
    private static final String TAG = "PushTokenManager";

    private PushTokenManager() {
    }

    public static void syncCurrentUserTokenByRole(String uid) {
        if (!Tool.boolOf(uid)) {
            return;
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (!Tool.boolOf(token)) {
                        return;
                    }
                    FirebaseDatabase.getInstance()
                            .getReference(FirebaseNode.USER_ID_ROLES.getPath())
                            .child(uid)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                String role = null;
                                if (snapshot.hasChild("role")) {
                                    role = snapshot.child("role").getValue(String.class);
                                }
                                if (!Tool.boolOf(role)) {
                                    Object raw = snapshot.getValue();
                                    if (raw instanceof String) {
                                        role = (String) raw;
                                    }
                                }
                                syncKnownRole(uid, role, token);
                            })
                            .addOnFailureListener(e -> Log.w(TAG, "Failed loading user role for token sync", e));
                })
                .addOnFailureListener(e -> Log.w(TAG, "Failed getting FCM token", e));
    }

    public static void syncKnownRole(String uid, String role, String token) {
        if (!Tool.boolOf(uid) || !Tool.boolOf(role) || !Tool.boolOf(token)) {
            return;
        }
        DatabaseReference userRef = resolveUserRef(uid, role);
        if (userRef == null) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", token);
        updates.put("fcmTokenUpdatedAt", ServerValue.TIMESTAMP);
        userRef.updateChildren(updates)
                .addOnFailureListener(e -> Log.w(TAG, "Failed syncing token for role=" + role, e));
    }

    public static void syncKnownRoleWithLatestToken(String uid, String role) {
        if (!Tool.boolOf(uid) || !Tool.boolOf(role)) {
            return;
        }
        Task<String> tokenTask = FirebaseMessaging.getInstance().getToken();
        tokenTask.addOnSuccessListener(token -> syncKnownRole(uid, role, token))
                .addOnFailureListener(e -> Log.w(TAG, "Failed getting latest token for role sync", e));
    }

    private static DatabaseReference resolveUserRef(String uid, String roleRaw) {
        String role = roleRaw.trim().toUpperCase(Locale.US);
        if ("STUDENT".equals(role)) {
            return FirebaseDatabase.getInstance().getReference(FirebaseNode.STUDENT.getPath()).child(uid);
        }
        if ("TEACHER".equals(role)) {
            return FirebaseDatabase.getInstance().getReference(FirebaseNode.TEACHER.getPath()).child(uid);
        }
        if ("PARENT".equals(role)) {
            return FirebaseDatabase.getInstance().getReference(FirebaseNode.PARENT.getPath()).child(uid);
        }
        if ("ADMIN".equals(role)) {
            return FirebaseDatabase.getInstance().getReference(FirebaseNode.ADMIN.getPath()).child(uid);
        }
        return null;
    }
}
