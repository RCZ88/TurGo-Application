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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    default void remove(){
        getDbReference().removeValue();
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
                ArrayList<String> currentList = extractStringList(currentData.getValue());
                if (Tool.boolOf(item) && !currentList.contains(item)) {
                    currentList.add(item);
                }
                currentData.setValue(currentList);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (isTooManyRetries(error)) {
                    fallbackMergeSet(fieldName, item, tcs);
                    return;
                }
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
                ArrayList<String> currentList = extractStringList(currentData.getValue());
                currentList.removeIf(str -> str.equals(itemToRemove));
                currentData.setValue(currentList);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (isTooManyRetries(error)) {
                    fallbackRemoveSet(fieldName, itemToRemove, tcs);
                    return;
                }
                if (error != null) {
                    tcs.setException(error.toException());
                } else {
                    tcs.setResult(null);
                }
            }
        });
        return tcs.getTask();
    }

    default void fallbackMergeSet(String fieldName, String item, TaskCompletionSource<Void> tcs) {
        getDbReference().child(fieldName).get().addOnSuccessListener(snapshot -> {
            ArrayList<String> merged = extractStringList(snapshot.getValue());
            if (Tool.boolOf(item) && !merged.contains(item)) {
                merged.add(item);
            }
            getDbReference().child(fieldName).setValue(merged)
                    .addOnSuccessListener(unused -> tcs.setResult(null))
                    .addOnFailureListener(tcs::setException);
        }).addOnFailureListener(tcs::setException);
    }

    default void fallbackRemoveSet(String fieldName, String itemToRemove, TaskCompletionSource<Void> tcs) {
        getDbReference().child(fieldName).get().addOnSuccessListener(snapshot -> {
            ArrayList<String> merged = extractStringList(snapshot.getValue());
            merged.removeIf(str -> str.equals(itemToRemove));
            getDbReference().child(fieldName).setValue(merged)
                    .addOnSuccessListener(unused -> tcs.setResult(null))
                    .addOnFailureListener(tcs::setException);
        }).addOnFailureListener(tcs::setException);
    }

    default boolean isTooManyRetries(DatabaseError error) {
        if (error == null) {
            return false;
        }
        int code = error.getCode();
        if (code == DatabaseError.MAX_RETRIES
                || code == DatabaseError.OVERRIDDEN_BY_SET
                || code == DatabaseError.DATA_STALE) {
            return true;
        }
        String message = error.getMessage();
        if (!Tool.boolOf(message)) {
            return false;
        }
        String lowered = message.toLowerCase(Locale.US);
        return lowered.contains("too many retries")
                || lowered.contains("overridden by a subsequent set")
                || lowered.contains("data is stale");
    }

    default ArrayList<String> extractStringList(Object value) {
        LinkedHashSet<String> deduped = new LinkedHashSet<>();
        if (value instanceof List<?>) {
            for (Object v : (List<?>) value) {
                if (v instanceof String && Tool.boolOf((String) v)) {
                    deduped.add((String) v);
                }
            }
        } else if (value instanceof Map<?, ?>) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                Object rawKey = entry.getKey();
                Object rawVal = entry.getValue();
                String key = rawKey == null ? null : rawKey.toString();
                if (rawVal instanceof String && Tool.boolOf((String) rawVal)) {
                    deduped.add((String) rawVal);
                    continue;
                }
                if (Tool.boolOf(key) && !isNumericArrayKey(key) && isTruthyMembershipFlag(rawVal)) {
                    deduped.add(key);
                }
            }
        } else if (value instanceof String && Tool.boolOf((String) value)) {
            deduped.add((String) value);
        }
        return new ArrayList<>(deduped);
    }

    default boolean isNumericArrayKey(String key) {
        if (!Tool.boolOf(key)) {
            return false;
        }
        for (int i = 0; i < key.length(); i++) {
            if (!Character.isDigit(key.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    default boolean isTruthyMembershipFlag(Object rawValue) {
        if (rawValue == null) {
            return true;
        }
        if (rawValue instanceof Boolean) {
            return (Boolean) rawValue;
        }
        if (rawValue instanceof Number) {
            return ((Number) rawValue).intValue() != 0;
        }
        String value = rawValue.toString();
        return "1".equals(value) || "true".equalsIgnoreCase(value);
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
    // -------------------------------------------------------------------------
    // Selective / partial field loading
    // -------------------------------------------------------------------------

    /** Load a single child field as a raw DataSnapshot. */
    default Task<DataSnapshot> loadField(String field) {
        return getDbReference().child(field).get();
    }

    /** Load a single String-valued child field. */
    default Task<String> loadStringField(String field) {
        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();
        getDbReference().child(field).get()
                .addOnSuccessListener(snap -> tcs.setResult(snap.getValue(String.class)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /** Load a child field that holds a list of Strings (e.g. ID arrays). */
    default Task<ArrayList<String>> loadStringListField(String field) {
        TaskCompletionSource<ArrayList<String>> tcs = new TaskCompletionSource<>();
        getDbReference().child(field).get()
                .addOnSuccessListener(snap -> tcs.setResult(extractStringList(snap.getValue())))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Load multiple named child fields in parallel and return them as a plain
     * Map keyed by field name. Values are raw Firebase objects.
     * Use extractStringList() or cast as needed.
     */
    default Task<Map<String, Object>> loadFields(String... fields) {
        TaskCompletionSource<Map<String, Object>> tcs = new TaskCompletionSource<>();
        if (fields == null || fields.length == 0) {
            tcs.setResult(new java.util.HashMap<>());
            return tcs.getTask();
        }
        List<Task<DataSnapshot>> tasks = new ArrayList<>();
        for (String field : fields) {
            tasks.add(getDbReference().child(field).get());
        }
        Tasks.whenAllComplete(tasks).addOnCompleteListener(done -> {
            Map<String, Object> result = new java.util.HashMap<>();
            for (int i = 0; i < fields.length; i++) {
                Task<DataSnapshot> t = tasks.get(i);
                if (t.isSuccessful() && t.getResult() != null) {
                    result.put(fields[i], t.getResult().getValue());
                }
            }
            tcs.setResult(result);
        });
        return tcs.getTask();
    }

    // -------------------------------------------------------------------------

    DatabaseReference getDbReference();
    Class<F> getFbClass();
}
