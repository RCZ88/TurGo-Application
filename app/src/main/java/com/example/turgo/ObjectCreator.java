package com.example.turgo;

import com.google.firebase.database.DatabaseError;

public class ObjectCreator {
    public static void main(String[] args) {
        final Teacher[] ta= new Teacher[1];
        User.getUserDataFromDB("os40TM8yvSRuKHJiTNcgNVGYmgh2", new ObjectCallBack<User>() {
            @Override
            public void onObjectRetrieved(User object) {
                ta[0] = (Teacher) object;
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
        Teacher teacher = ta[0];

    }
}
