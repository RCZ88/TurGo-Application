package com.example.turgo;

import com.google.firebase.database.DatabaseError;

public interface ObjectCallBack<T> {
    void onObjectRetrieved(T Object);
    void onError(DatabaseError error);
}
