package com.example.turgo;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;

public class UserPresenceManager {
    private static DatabaseReference connectedRef;
    private static DatabaseReference userStatusRef;

    public static void startTracking(String userId) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        connectedRef = db.getReference(".info/connected");
        userStatusRef = db.getReference("user-status").child(userId);

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                if (connected != null && connected) {
                    // Create UserStatus with String timestamp
                    UserStatus onlineStatus = new UserStatus(
                            userId,
                            true,
                            java.time.LocalDateTime.now().toString()
                    );

                    userStatusRef.setValue(onlineStatus);  // ✅ Works now!

                    // Set up onDisconnect
                    UserStatus offlineStatus = new UserStatus(
                            userId,
                            false,
                            java.time.LocalDateTime.now().toString()
                    );

                    userStatusRef.onDisconnect().setValue(offlineStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("UserPresenceManager", "Error: " + error.getMessage());
            }
        });
    }

    public static void stopTracking(String userId) {
        if (userStatusRef != null) {
            UserStatus us=new UserStatus(userId, false, LocalDateTime.now().toString());
            userStatusRef.setValue(us);
        }
    }

    public static UserStatus getUserStatus(User user){
        DatabaseReference dbrf = FirebaseDatabase.getInstance().getReference(FirebaseNode.USER_STATUS.getPath()).child(user.getUid());
        final UserStatus[] us = new UserStatus[1];
        dbrf.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                us[0] = snapshot.getValue(UserStatus.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return us[0];
    }
}