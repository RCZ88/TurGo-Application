package com.example.turgo;

import com.google.firebase.database.DatabaseReference;

public class TimeSlotRepository implements RepositoryClass<TimeSlot, TimeSlotFirebase> {
    @Override
    public DatabaseReference getDbReference() {
        return null;
    }

    @Override
    public Class<TimeSlotFirebase> getFbClass() {
        return null;
    }
}
