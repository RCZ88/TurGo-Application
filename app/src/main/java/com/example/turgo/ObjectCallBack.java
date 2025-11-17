package com.example.turgo;

import com.google.firebase.database.DatabaseError;

public interface ObjectCallBack<T> {
    void onObjectRetrieved(T object);
    void onError(DatabaseError error);
}
