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

        // Reference that tells us if this client is connected to Firebase
        connectedRef = db.getReference(".info/connected");

        // Reference to this user’s online status node
        userStatusRef = db.getReference("userStatus").child(userId);

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                if (connected != null && connected) {
                    // User came online

                    userStatusRef.setValue(new UserStatus(userId, true, LocalDateTime.now()));

                    // Set up onDisconnect — automatically runs when user leaves or loses connection
                    userStatusRef.onDisconnect()
                            .setValue(new UserStatus(userId, false, LocalDateTime.now()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    public static void stopTracking(String userId) {
        if (userStatusRef != null) {
            userStatusRef.setValue(new UserStatus(userId, false, LocalDateTime.now()));
        }
    }

    public static UserStatus getUserStatus(User user){
        DatabaseReference dbrf = FirebaseDatabase.getInstance().getReference(FirebaseNode.USER_STATUS.getPath()).child(user.getUID());
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