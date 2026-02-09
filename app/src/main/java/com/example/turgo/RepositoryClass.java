package com.example.turgo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public interface RepositoryClass<R extends RequireUpdate<R, F, ?>, F extends FirebaseClass<R>> {
    default void save(R object){
        try {
            F firebaseObj = object.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(object);

            getDbReference().setValue(firebaseObj);

        } catch (IllegalAccessException | InstantiationException e) {
            Log.d("RepositorySave",
                    "Error saving " + object.getClass().getSimpleName() + ", Error: " + e);
            throw new RuntimeException(e);
        }
    }
    default Task<Void> saveAsync(R object){
        try {
            F firebaseObj = object.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(object);
            return getDbReference().setValue(firebaseObj);
        } catch (IllegalAccessException | InstantiationException e) {
            return Tasks.forException(e);
        }
    }

    default void delete(R object){
        getDbReference().child(object.getID()).removeValue();
    }

    default void removeStringFromArray(String fieldName, String itemToRemove) {
        getDbReference().child(fieldName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> currentList = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String value = child.getValue(String.class);
                        if (value != null && !value.equals(itemToRemove)) {
                            currentList.add(value);
                        }
                    }
                }

                getDbReference().child(fieldName).setValue(currentList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Repository", "Failed to remove string from array", error.toException());
            }
        });
    }

    default void addStringToArray(String fieldName, String item) {
        getDbReference().child(fieldName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> currentList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String value = child.getValue(String.class);
                        if (value != null) {
                            currentList.add(value);
                        }
                    }
                }
                currentList.add(item);
                getDbReference().child(fieldName).setValue(currentList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Repository", "Failed to add string to array", error.toException());
            }
        });
    }
    default Task<Void> addStringToArrayAsync(String fieldName, String item) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        getDbReference().child(fieldName).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                ArrayList<String> currentList = new ArrayList<>();
                Object value = currentData.getValue();
                if (value instanceof List) {
                    for (Object v : (List<?>) value) {
                        if (v instanceof String) {
                            currentList.add((String) v);
                        }
                    }
                }
                if (!currentList.contains(item)) {
                    currentList.add(item);
                }
                currentData.setValue(currentList);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    tcs.setException(error.toException());
                } else {
                    tcs.setResult(null);
                }
            }
        });
        return tcs.getTask();
    }

    default Task<Void> removeStringFromArrayAsync(String fieldName, String itemToRemove) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        getDbReference().child(fieldName).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                ArrayList<String> currentList = new ArrayList<>();
                Object value = currentData.getValue();
                if (value instanceof List) {
                    for (Object v : (List<?>) value) {
                        if (v instanceof String) {
                            String str = (String) v;
                            if (!str.equals(itemToRemove)) {
                                currentList.add(str);
                            }
                        }
                    }
                }
                currentData.setValue(currentList);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    tcs.setException(error.toException());
                } else {
                    tcs.setResult(null);
                }
            }
        });
        return tcs.getTask();
    }

    default void load(ObjectCallBack<F>callBack){
        getDbReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    callBack.onObjectRetrieved((F)snapshot.getValue());
                } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    default Task<R> loadAsNormal() {
        TaskCompletionSource<R> taskSource = new TaskCompletionSource<>();

        getDbReference().get().addOnSuccessListener(snapshot -> {
            F firebaseObject = snapshot.getValue(getFbClass());

            if (firebaseObject == null) {
                taskSource.setResult(null);
                return;
            }

            try {
                firebaseObject.convertToNormal(new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(R object) {
                        taskSource.setResult(object);
                    }

                    @Override
                    public void onError(DatabaseError error) {
                        taskSource.setException(error.toException());
                    }

                });
            } catch (Exception e) {
                taskSource.setException(e);
            }
        }).addOnFailureListener(taskSource::setException);

        return taskSource.getTask();
    }

    default void loadAll(ObjectCallBack<ArrayList<F>>callBack){
        if(getDbReference().getParent() == null){
            return;
        }
        getDbReference().getParent().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<F>firebaseObjects = new ArrayList<>();
                for(DataSnapshot ds : snapshot.getChildren()){
                    firebaseObjects.add((F)ds.getValue());
                }
                try {
                    callBack.onObjectRetrieved(firebaseObjects);
                } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    DatabaseReference getDbReference();
    Class<F> getFbClass();
}
