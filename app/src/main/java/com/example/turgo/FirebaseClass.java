package com.example.turgo;

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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public interface FirebaseClass<F>{
    /// 1. Create an empty firebaseObject (ex. StudentFirebase sf = new StudentFirebase())
    /// 2. Then use the method to import all the normalObject to the firebaseObject (sf.importObjectDataToFirebase(this))
    public abstract void importObjectData(F from);
    default <O extends RequireUpdate<?, ?>>ArrayList<String> convertToIdList(ArrayList<O> objectList){
        ArrayList<String> idList = new ArrayList<>();
        if(objectList != null){
            for (O obj : objectList) {
                idList.add(obj.getID());
            }
        }
        return idList;
    }
    default ArrayList<Integer> convertBooleanToInt(ArrayList<Boolean> arrayBool){
        ArrayList<Integer> intBooleans = new ArrayList<>();
        for(boolean bool : arrayBool){
            intBooleans.add(bool ? 1 : 0);
        }
        return intBooleans;
    }
    String getID();
    void convertToNormal(ObjectCallBack<F>objectCallBack)throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException ;

    //class -> ? implements requireupdate

    // Add this at class level to track objects being constructed
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
                try {
                    Log.d("constructClass", "onDataChange called for " + clazz.getSimpleName());
                    Log.d("constructClass", "Snapshot exists: " + snapshot.exists());

                    if (!snapshot.exists()) {
                        String errorMsg = "No data found at: " + path + "/" + id;
                        Log.e("constructClass", errorMsg);
                        callback.onError(DatabaseError.fromException(new Exception(errorMsg)));
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

                    // Track async operations
                    AtomicInteger pendingFields = new AtomicInteger(0);
                    AtomicBoolean hasError = new AtomicBoolean(false);

                    // Process each field
                    for(Field field : fields){
                        field.setAccessible(true);
                        String fieldName = field.getName();

                        Log.d("constructClass", "Processing field: " + fieldName + " (Type: " + field.getType().getSimpleName() + ")");

                        // Skip static final fields
                        if(Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())){
                            Log.d("constructClass", "  → Skipping static final field");
                            continue;
                        }

                        // Check if field exists in Firebase
                        if(!snapshot.hasChild(fieldName)) {
                            Log.d("constructClass", "  ⚠️ Field '" + fieldName + "' not found in Firebase, skipping");
                            continue;
                        }

                        // Handle single Boolean field (Firebase stores as Long 1/0)
                        if(field.getType() == Boolean.class || field.getType() == boolean.class) {
                            Object value = snapshot.child(fieldName).getValue();
                            if (value != null) {
                                boolean boolValue;
                                if (value instanceof Long) {
                                    boolValue = ((Long) value) == 1L;
                                } else if (value instanceof Integer) {
                                    boolValue = ((Integer) value) == 1;
                                } else if (value instanceof Boolean) {
                                    boolValue = (Boolean) value;
                                } else {
                                    boolValue = "1".equals(value.toString()) || "true".equalsIgnoreCase(value.toString());
                                }
                                field.set(object, boolValue);
                                Log.d("constructClass", "  → Boolean field '" + fieldName + "' = " + boolValue + " (from " + value + ")");
                            } else {
                                field.set(object, false);
                                Log.d("constructClass", "  → Boolean field '" + fieldName + "' is null, defaulting to false");
                            }
                        }
                        // Handle primitives and basic types
                        else if(Tool.isPrimitive(field)) {
                            Object value = snapshot.child(fieldName).getValue(field.getType());
                            Log.d("constructClass", "  → Primitive/Basic field '" + fieldName + "' = " + value);
                            field.set(object, value);
                        }
                        else if(field.getType() == LocalTime.class){
                            String timeString = snapshot.child(fieldName).getValue(String.class);
                            LocalTime time = LocalTime.parse(timeString);
                            Log.d("constructClass", "  → LocalTime field '" + fieldName + "' = " + time);
                            field.set(object, time);
                        }
                        else if(field.getType() == LocalDateTime.class){
                            String dateTimeString = snapshot.child(fieldName).getValue(String.class);
                            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString);
                            Log.d("constructClass", "  → LocalDateTime field '" + fieldName + "' = " + dateTime);
                            field.set(object, dateTime);
                        }
                        // Handle ArrayList
                        else if(field.getType() == ArrayList.class){
                            Log.d("constructClass", "  → ArrayList field: " + fieldName);
                            Type listType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];

                            if (listType instanceof Class<?>) {
                                Class<?> elementClass = (Class<?>) listType;
                                Log.d("constructClass", "  → Element type: " + elementClass.getSimpleName());

                                // Check if it's a list of RequireUpdate objects
                                if (RequireUpdate.class.isAssignableFrom(elementClass)) {
                                    Log.d("constructClass", "  → Complex object list (RequireUpdate)");
                                    ArrayList<Object> list = new ArrayList<>();

                                    // Get all child IDs
                                    DataSnapshot listSnapshot = snapshot.child(fieldName);
                                    ArrayList<String> ids = new ArrayList<>();

                                    for(DataSnapshot childSnapshot : listSnapshot.getChildren()){
                                        String objectID = childSnapshot.getValue(String.class);
                                        if (objectID != null && !objectID.isEmpty()) {
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
                                                public void onSuccess(Object childObject) {
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
                                                            Log.d("constructClass", "✓ All nested objects loaded, setting field: " + fieldName);
                                                            Log.d("constructClass", "✓✓ SUCCESS: " + clazz.getSimpleName() + " constructed completely");
                                                            callback.onSuccess(object);
                                                        } catch (IllegalAccessException e) {
                                                            Log.e("constructClass", "✗ Error setting field " + fieldName + ": " + e.getMessage(), e);
                                                            if (!hasError.getAndSet(true)) {
                                                                callback.onError(DatabaseError.fromException(e));
                                                            }
                                                        } catch (ParseException |
                                                                 InvocationTargetException |
                                                                 NoSuchMethodException |
                                                                 InstantiationException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onError(DatabaseError error) {
                                                    Log.w("constructClass", "⚠️ Skipping nested object (ID: " + objectID + "): " + error.getMessage());

                                                    // Don't fail entire construction - just skip this object
                                                    int remaining = pendingFields.decrementAndGet();
                                                    Log.d("constructClass", "  → Skipped, remaining: " + remaining);

                                                    if (remaining == 0 && !hasError.get()) {
                                                        try {
                                                            field.set(object, list);
                                                            Log.d("constructClass", "✓ All nested objects processed (some skipped), setting field: " + fieldName);
                                                            callback.onSuccess(object);
                                                        } catch (IllegalAccessException e) {
                                                            Log.e("constructClass", "✗ Error setting field: " + e.getMessage(), e);
                                                            if (!hasError.getAndSet(true)) {
                                                                callback.onError(DatabaseError.fromException(e));
                                                            }
                                                        } catch (ParseException |
                                                                 InvocationTargetException |
                                                                 NoSuchMethodException |
                                                                 InstantiationException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        // Empty list
                                        Log.d("constructClass", "  → Empty list for field: " + fieldName);
                                        field.set(object, list);
                                    }
                                }
                                // Handle ArrayList<Boolean>
                                else if(elementClass == Boolean.class){
                                    Log.d("constructClass", "  → Boolean List (Converting from Long/Integer to Boolean)");
                                    ArrayList<Boolean> boolList = new ArrayList<>();
                                    DataSnapshot listSnapshot = snapshot.child(fieldName);

                                    for(DataSnapshot childSnapshot : listSnapshot.getChildren()){
                                        Object value = childSnapshot.getValue();
                                        if (value != null) {
                                            boolean boolValue;
                                            if (value instanceof Long) {
                                                boolValue = ((Long) value) == 1L;
                                                Log.d("constructClass", "    → Converted Long " + value + " to " + boolValue);
                                            } else if (value instanceof Integer) {
                                                boolValue = ((Integer) value) == 1;
                                                Log.d("constructClass", "    → Converted Integer " + value + " to " + boolValue);
                                            } else if (value instanceof Boolean) {
                                                boolValue = (Boolean) value;
                                                Log.d("constructClass", "    → Already Boolean: " + boolValue);
                                            } else {
                                                // Fallback: try parsing as string "1"/"0" or "true"/"false"
                                                String strValue = value.toString();
                                                boolValue = "1".equals(strValue) || "true".equalsIgnoreCase(strValue);
                                                Log.d("constructClass", "    → Converted String " + value + " to " + boolValue);
                                            }
                                            boolList.add(boolValue);
                                        }
                                    }
                                    Log.d("constructClass", "  → Boolean list size: " + boolList.size());
                                    field.set(object, boolList);
                                }
                                // Handle other simple lists
                                else {
                                    Log.d("constructClass", "  → Simple list (String/Integer/etc.)");
                                    ArrayList<Object> simpleList = new ArrayList<>();
                                    for(DataSnapshot childSnapshot : snapshot.child(fieldName).getChildren()){
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
                            String fbLDT = snapshot.child(fieldName).getValue(String.class);
                            if (fbLDT != null && !fbLDT.isEmpty()) {
                                LocalDateTime dateTime = LocalDateTime.parse(fbLDT);
                                field.set(object, dateTime);
                                Log.d("constructClass", "  → LocalDateTime field '" + fieldName + "' = " + dateTime);
                            } else {
                                Log.d("constructClass", "  → LocalDateTime field '" + fieldName + "' is null");
                            }
                        }
                        // Handle LocalDate
                        else if(field.getType() == LocalDate.class){
                            String fbLD = snapshot.child(fieldName).getValue(String.class);
                            if (fbLD != null && !fbLD.isEmpty()) {
                                // Special case for Student lastScheduled
                                if(clazz == Student.class && fieldName.equals("lastScheduled")){
                                    Log.d("ConstructClass", "Special Student.lastScheduled case");
                                    if(snapshot.hasChild("hasScheduled")) {
                                        boolean hasScheduled = Boolean.TRUE.equals(snapshot.child("hasScheduled").getValue(Boolean.class));
                                        Log.d("constructClass", "hasScheduled: " + hasScheduled);
                                        if(!hasScheduled){
                                            field.set(object, Schedule.NEVER_SCHEDULED);
                                            continue;
                                        }
                                    }
                                }
                                LocalDate date = LocalDate.parse(fbLD);
                                field.set(object, date);
                                Log.d("constructClass", "  → LocalDate field '" + fieldName + "' = " + date);
                            } else {
                                Log.d("constructClass", "  → LocalDate field '" + fieldName + "' is null");
                            }
                        }
                        // Handle Enum
                        else if(field.getType().isEnum()){
                            String enumValue = snapshot.child(fieldName).getValue(String.class);
                            Log.d("constructClass", "  → Enum field: " + fieldName + " = " + enumValue);

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
                        // Handle Duration
                        else if(field.getType() == Duration.class){
                            Object durationValue = snapshot.child(fieldName).getValue();
                            if (durationValue != null) {
                                int durationInMinutes = ((Number) durationValue).intValue();
                                Duration duration = Duration.ofMinutes(durationInMinutes);
                                field.set(object, duration);
                                Log.d("constructClass", "  → Duration field '" + fieldName + "' = " + duration);
                            } else {
                                Log.d("constructClass", "  → Duration field '" + fieldName + "' is null");
                            }
                        }
                        // Handle single RequireUpdate object
                        else if(RequireUpdate.class.isAssignableFrom(field.getType())){
                            String nestedObjectId = snapshot.child(fieldName).getValue(String.class);

                            if (nestedObjectId != null && !nestedObjectId.isEmpty()) {
                                Log.d("constructClass", "→ Found nested RequireUpdate field: " + fieldName + " with ID: " + nestedObjectId);

                                pendingFields.incrementAndGet();

                                constructClass(field.getType(), nestedObjectId, new ConstructClassCallback() {
                                    @Override
                                    public void onSuccess(Object nestedObject) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                                        Log.d("constructClass", "  ✓ Nested object retrieved: " + field.getType().getSimpleName() + " (ID: " + nestedObjectId + ")");

                                        try {
                                            field.set(object, nestedObject);
                                            Log.d("constructClass", "  ✓ Set field " + fieldName + " with nested object");
                                        } catch (IllegalAccessException e) {
                                            Log.e("constructClass", "✗ Error setting field " + fieldName + ": " + e.getMessage(), e);
                                            if (!hasError.getAndSet(true)) {
                                                callback.onError(DatabaseError.fromException(e));
                                            }
                                            return;
                                        }

                                        int remaining = pendingFields.decrementAndGet();
                                        Log.d("constructClass", "  → Remaining pending fields: " + remaining);

                                        if (remaining == 0 && !hasError.get()) {
                                            Log.d("constructClass", "✓✓ SUCCESS: " + clazz.getSimpleName() + " constructed completely");
                                            callback.onSuccess(object);
                                        }
                                    }

                                    @Override
                                    public void onError(DatabaseError error) {
                                        Log.w("constructClass", "⚠️ Skipping nested object field '" + fieldName + "' (ID: " + nestedObjectId + "): " + error.getMessage());

                                        // Don't fail - just leave field as null
                                        int remaining = pendingFields.decrementAndGet();
                                        Log.d("constructClass", "  → Skipped, remaining: " + remaining);

                                        if (remaining == 0 && !hasError.get()) {
                                            try {
                                                callback.onSuccess(object);
                                            } catch (ParseException | InvocationTargetException |
                                                     NoSuchMethodException |
                                                     IllegalAccessException |
                                                     InstantiationException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }
                                });
                            } else {
                                Log.d("constructClass", "→ Field " + fieldName + " has null/empty ID, skipping");
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
