package com.example.turgo;

import static com.example.turgo.Tool.hasParent;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public interface FirebaseClass<F>{
    /// 1. Create an empty firebaseObject (ex. StudentFirebase sf = new StudentFirebase())
    /// 2. Then use the method to import all the normalObject to the firebaseObject (sf.importObjectDataToFirebase(this))
    public abstract void importObjectData(F from);
    default <O extends RequireUpdate<?, ?>>ArrayList<String> convertToIdList(ArrayList<O> objectList){
        ArrayList<String> idList = new ArrayList<>();
        for (O obj : objectList) {
            idList.add(obj.getID());
        }
        return idList;
    }
    String getID();
    void convertToNormal(ObjectCallBack<F>objectCallBack)throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException ;

    //class -> ? implements requireupdate

    default void constructClass(Class<?> clazz, String id, ConstructClassCallback callback)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {

        Log.d("constructClass", "========== START ==========");
        Log.d("constructClass", "Class: " + clazz.getSimpleName() + ", ID: " + id);

        // Get Firebase path
        RequireUpdate<?, ?> tempInstance = (RequireUpdate<?, ?>) clazz.getDeclaredConstructor().newInstance();
        String path = tempInstance.getFirebaseNode().getPath();


        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(path).child(id);
        Log.d("constructClass", "Firebase path: " + dbRef);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("constructClass", "onDataChange called for " + clazz.getSimpleName());
                Log.d("constructClass", "Snapshot exists: " + snapshot.exists());

                try {
                    if (!snapshot.exists()) {
                        String errorMsg = "No data found at: " + path;
                        Log.e("constructClass", errorMsg);
                        callback.onError(DatabaseError.fromException(
                                new Exception(errorMsg)
                        ));
                        return;
                    }

                    // Create object instance
                    Log.d("constructClass", "Creating instance of " + clazz.getSimpleName());
                    Object object = clazz.getDeclaredConstructor().newInstance();
                    Log.d("constructClass", "Instance created successfully");

                    // Get all fields
                    ArrayList<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
                    if(hasParent(clazz)){
                        Log.d("constructClass", "Class has parent, adding parent fields");
                        fields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
                    }
                    Log.d("constructClass", "Total fields to process: " + fields.size());

                    // Track async operations for ArrayLists
                    AtomicInteger pendingFields = new AtomicInteger(0);
                    AtomicBoolean hasError = new AtomicBoolean(false);

                    // Process each field
                    for(Field field : fields){
                        field.setAccessible(true);
                        Log.d("constructClass", "Processing field: " + field.getName() + " (Type: " + field.getType().getSimpleName() + ")");
                        if(Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())){
                            continue;
                        }

                        // Handle primitives and basic types
                        if(field.getType().isPrimitive() ||
                                field.getType() == String.class ||
                                field.getType() == Integer.class ||
                                field.getType() == Long.class ||
                                field.getType() == Boolean.class ||
                                field.getType() == Double.class ||
                                field.getType() == Float.class) {

                            Object value = snapshot.child(field.getName()).getValue(field.getType());
                            Log.d("constructClass", "  → Primitive/Basic field '" + field.getName() + "' = " + value);
                            field.set(object, value);
                        }
                        // Handle ArrayList
                        else if(field.getType() == ArrayList.class){
                            Log.d("constructClass", "  → ArrayList field: " + field.getName());
                            Type listType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];

                            if (listType instanceof Class<?>) {
                                Class<?> elementClass = (Class<?>) listType;
                                Log.d("constructCslass", "  → Element type: " + elementClass.getSimpleName());

                                // Check if it's a list of IDs or objects
                                if (RequireUpdate.class.isAssignableFrom(elementClass)) {
                                    Log.d("constructClass", "  → Complex object list (RequireUpdate)");
                                    // It's a list of complex objects
                                    ArrayList<Object> list = new ArrayList<>();

                                    // Get all child IDs
                                    DataSnapshot listSnapshot = snapshot.child(field.getName());
                                    ArrayList<String> ids = new ArrayList<>();

                                    for(DataSnapshot childSnapshot : listSnapshot.getChildren()){
                                        // FIX: Get string value from snapshot
                                        String objectID = childSnapshot.getValue(String.class);
                                        if (objectID != null) {
                                            ids.add(objectID);
                                            Log.d("constructClass", "    → Found ID: " + objectID);
                                        }
                                    }

                                    Log.d("constructClass", "  → Total IDs found: " + ids.size());

                                    if (!ids.isEmpty()) {
                                        pendingFields.addAndGet(ids.size());
                                        Log.d("constructClass", "  → Pending async fields: " + pendingFields.get());

                                        // Recursively construct each object
                                        for(String objectID : ids) {
                                            Log.d("constructClass", "  → Fetching nested object with ID: " + objectID);
                                            constructClass(elementClass, objectID, new ConstructClassCallback() {
                                                @Override
                                                public void onSuccess(Object childObject) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                                                    Log.d("constructClass", "  ✓ Nested object retrieved: " + elementClass.getSimpleName() + " (ID: " + objectID + ")");
                                                    synchronized (list) {
                                                        list.add(childObject);
                                                    }

                                                    int remaining = pendingFields.decrementAndGet();
                                                    Log.d("constructClass", "  → Remaining pending fields: " + remaining);

                                                    // Check if all done
                                                    if (remaining == 0 && !hasError.get()) {
                                                        try {
                                                            field.set(object, list);
                                                            Log.d("constructClass", "✓ All nested objects loaded, setting field: " + field.getName());
                                                            Log.d("constructClass", "✓✓ SUCCESS: " + clazz.getSimpleName() + " constructed completely");
                                                            callback.onSuccess(object);
                                                        } catch (IllegalAccessException e) {
                                                            Log.e("constructClass", "✗ Error setting field " + field.getName() + ": " + e.getMessage(), e);
                                                            callback.onError(DatabaseError.fromException(e));
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onError(DatabaseError error) {
                                                    Log.e("constructClass", "✗ Error fetching nested object (ID: " + objectID + "): " + error.getMessage());
                                                    if (!hasError.getAndSet(true)) {
                                                        callback.onError(error);
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        // Empty list
                                        Log.d("constructClass", "  → Empty list for field: " + field.getName());
                                        field.set(object, list);
                                    }
                                } else {
                                    Log.d("constructClass", "  → Simple list (String/Integer/etc.)");
                                    // It's a simple list (Strings, Integers, etc.)
                                    ArrayList<Object> simpleList = new ArrayList<>();
                                    for(DataSnapshot childSnapshot : snapshot.child(field.getName()).getChildren()){
                                        Object value = childSnapshot.getValue(elementClass);
                                        if (value != null) {
                                            simpleList.add(value);
                                            Log.d("constructClass", "    → Added simple value: " + value);
                                        }
                                    }
                                    Log.d("constructClass", "  → Simple list size: " + simpleList.size());
                                    field.set(object, simpleList);
                                }
                            }
                        }
                        // Handle LocalDateTime
                        else if(field.getType() == LocalDateTime.class){
                            String fbLDT = snapshot.child(field.getName()).getValue(String.class);
                            if (fbLDT != null) {
                                LocalDateTime dateTime = LocalDateTime.parse(fbLDT);
                                field.set(object, dateTime);
                                Log.d("constructClass", "  → LocalDateTime field '" + field.getName() + "' = " + dateTime);
                            } else {
                                Log.d("constructClass", "  → LocalDateTime field '" + field.getName() + "' is null");
                            }
                        }
                        // Handle LocalDate
                        else if(field.getType() == LocalDate.class){
                            String fbLD = snapshot.child(field.getName()).getValue(String.class);
                            if (fbLD != null) {
                                LocalDate date = LocalDate.parse(fbLD);
                                field.set(object, date);
                                Log.d("constructClass", "  → LocalDate field '" + field.getName() + "' = " + date);
                            } else {
                                Log.d("constructClass", "  → LocalDate field '" + field.getName() + "' is null");
                            }
                        }else if(field.getType().isEnum()){
                            String enumValue = snapshot.child(field.getName()).getValue(String.class);
                            Log.d("constructClass", "  → Enum field: " + field.getName() + " = " + enumValue);

                            if (enumValue != null && !enumValue.isEmpty()) {
                                try {
                                    Class<? extends Enum> enumClass = (Class<? extends Enum>) field.getType();
                                    Enum enumConstant = Enum.valueOf(enumClass, enumValue.toUpperCase());
                                    field.set(object, enumConstant);
                                    Log.d("constructClass", "    ✓ Enum set successfully");
                                } catch (IllegalArgumentException e) {
                                    Log.e("constructClass", "    ✗ Invalid enum value: " + enumValue, e);
                                }
                            } else {
                                Log.d("constructClass", "    ⚠ Enum value is null, skipping");
                            }
                        }
                        else {
                            Log.d("constructClass", "  → Skipping unsupported field type: " + field.getType().getSimpleName());
                        }
                    }

                    Log.d("constructClass", "Field processing complete. Pending fields: " + pendingFields.get());

                    // If no async fields, return immediately
                    if (pendingFields.get() == 0) {
                        Log.d("constructClass", "✓✓ SUCCESS: " + clazz.getSimpleName() + " constructed (no async fields)");
                        callback.onSuccess(object);
                    }

                } catch (Exception e) {
                    Log.e("constructClass", "✗✗ EXCEPTION in constructClass for " + clazz.getSimpleName() + ": " + e.getMessage(), e);
                    callback.onError(DatabaseError.fromException(e));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("constructClass", "✗✗ DATABASE CANCELLED for " + clazz.getSimpleName() + ": " + error.getMessage());
                Log.e("constructClass", "Error code: " + error.getCode());
                Log.e("constructClass", "Error details: " + error.getDetails());
                callback.onError(error);
            }
        });
    }

    // Helper method
    default boolean hasParent(Class<?> clazz) {
        boolean hasParent = clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class;
        if (hasParent) {
            Log.d("constructClass", "Parent class: " + clazz.getSuperclass().getSimpleName());
        }
        return hasParent;
    }
    default <FBC extends FirebaseClass<?>> void getAllObject(FirebaseNode fbn, Class<? extends FirebaseClass>firebaseClass, ObjectCallBack<ArrayList<FBC>>callBack){
        DatabaseReference dbf = FirebaseDatabase.getInstance().getReference(fbn.getPath());
        Log.d("FirebaseClass(getAllObject)", dbf.toString());
        ArrayList<FBC> firebaseClasses = new ArrayList<>();
        dbf.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Log.d("FirebaseClass(getAllObject)", "Snapshot Exist, Processing Children:");
                }
                for(DataSnapshot ds : snapshot.getChildren()){
                    Log.d("FirebaseClass(getAllObject)", "FB Object:" +ds.toString());

//                    try {
                        //firebaseClasses.add((FBC)ds.getValue(((RequireUpdate<?, ?>)(fbn.getClazz()).newInstance()).getFirebaseClass()));
                        firebaseClasses.add((FBC)ds.getValue(firebaseClass));
//                    } catch (IllegalAccessException | InstantiationException e) {
//                        throw new RuntimeException(e);
//                    }
                }
                try {
                    callBack.onObjectRetrieved(firebaseClasses);
                } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseClass(getAllObject)", "Error Retrieving Object: " + error);
            }
        });
    }
//    User getNormalClass();
}
