package com.example.turgo;

import com.google.firebase.database.DatabaseError;

public interface UserCallback {
    void onUserRetrieved(User user);
    void onError(DatabaseError error);
}
