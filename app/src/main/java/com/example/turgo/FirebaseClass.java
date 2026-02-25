package com.example.turgo;

import android.content.Context;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public interface FirebaseClass<F>{
    ThreadLocal<Set<String>> ACTIVE_CONSTRUCT_PATH = ThreadLocal.withInitial(LinkedHashSet::new);
    ThreadLocal<Map<String, Object>> CONSTRUCTED_OBJECT_CACHE = ThreadLocal.withInitial(HashMap::new);
    ThreadLocal<ArrayList<String>> ACTIVE_FIELD_EDGES = ThreadLocal.withInitial(ArrayList::new);
    /// 1. Create an empty firebaseObject (ex. StudentFirebase sf = new StudentFirebase())
    /// 2. Then use the method to import all the normalObject to the firebaseObject (sf.importObjectDataToFirebase(this))
    void importObjectData(F from);
    default <O extends RequireUpdate<?, ?, ?>>ArrayList<String> convertToIdList(ArrayList<O> objectList){
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
        boolean log = false;
        String nodeKey = clazz.getName() + "#" + id;
        Set<String> activePath = ACTIVE_CONSTRUCT_PATH.get();
        Map<String, Object> objectCache = CONSTRUCTED_OBJECT_CACHE.get();
        ArrayList<String> edgeStack = ACTIVE_FIELD_EDGES.get();
        if (activePath.contains(nodeKey)) {
            Object cachedObject = objectCache.get(nodeKey);
            String fieldTrace = String.join(" | ", edgeStack);
            if (cachedObject != null) {
                if (log) {
                    Log.w("constructClass", "Cycle detected, reusing in-progress object for " + nodeKey);
                }
                if (log) {
                    Log.w("constructClass", "Edge trace: " + fieldTrace);
                }
                try {
                    callback.onSuccess(cachedObject);
                } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | InstantiationException e) {
                    callback.onError(DatabaseError.fromException(e));
                }
                return;
            }
            ArrayList<String> activeNodes = new ArrayList<>(activePath);
            int cycleStart = activeNodes.indexOf(nodeKey);
            String cyclePath;
            if (cycleStart >= 0) {
                ArrayList<String> cycleNodes = new ArrayList<>(activeNodes.subList(cycleStart, activeNodes.size()));
                cycleNodes.add(nodeKey);
                cyclePath = String.join(" -> ", cycleNodes);
            } else {
                activeNodes.add(nodeKey);
                cyclePath = String.join(" -> ", activeNodes);
            }
            if (log) {
                Log.w("constructClass", "Cycle detected, skipping recursive construct for " + nodeKey);
            }
            if (log) {
                Log.w("constructClass", "Cycle path: " + cyclePath);
            }
            if (log) {
                Log.w("constructClass", "Edge trace: " + fieldTrace);
            }
            if (log) {
                Log.w("constructClass", "Active chain before cycle: " + String.join(" -> ", activeNodes));
            }
            try {
                callback.onError(DatabaseError.fromException(
                        new Exception("Recursive reference detected. Path: " + cyclePath + " | Edges: " + fieldTrace)
                ));
            } catch (Exception ignored) {
                if (log) {
                    Log.w("constructClass", "Cycle callback error ignored for " + nodeKey);
                }
            }
            return;
        }
        activePath.add(nodeKey);
        Object object = objectCache.get(nodeKey);
        if (object == null) {
            object = clazz.getDeclaredConstructor().newInstance();
            objectCache.put(nodeKey, object);
        }
        final Object rootObject = object;
        AtomicBoolean cleanedUp = new AtomicBoolean(false);
        Runnable cleanupPath = () -> {
            if (cleanedUp.compareAndSet(false, true)) {
                activePath.remove(nodeKey);
                objectCache.remove(nodeKey);
                if (activePath.isEmpty()) {
                    ACTIVE_CONSTRUCT_PATH.remove();
                    CONSTRUCTED_OBJECT_CACHE.remove();
                    ACTIVE_FIELD_EDGES.remove();
                }
            }
        };

        if (log) {

            Log.d("constructClass", "========== START ==========");

        }
        if (log) {
            Log.d("constructClass", "Class: " + clazz.getSimpleName() + ", ID: " + id);
        }

        // Get Firebase path
        RequireUpdate<?, ?, ?> tempInstance = (RequireUpdate<?, ?, ?>) clazz.getDeclaredConstructor().newInstance();
        String path = tempInstance.getFirebaseNode().getPath();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(path).child(id);
        if (log) {
            Log.d("constructClass", "Firebase path: " + dbRef);
        }

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Object object = rootObject;
                    if (log) {
                        Log.d("constructClass", "onDataChange called for " + clazz.getSimpleName());
                    }
                    if (log) {
                        Log.d("constructClass", "Snapshot exists: " + snapshot.exists());
                    }

                    if (!snapshot.exists()) {
                        String errorMsg = "No data found at: " + path + "/" + id;
                        if (log) {
                            Log.e("constructClass", errorMsg);
                        }
                        cleanupPath.run();
                        callback.onError(DatabaseError.fromException(new Exception(errorMsg)));
                        return;
                    }

                    // Create object instance
                    if (log) {
                        Log.d("constructClass", "Using instance of " + clazz.getSimpleName() + " from cache");
                    }

                    // Get all fields
                    ArrayList<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
                    if(hasParent(clazz)){
                        if (log) {
                            Log.d("constructClass", "Class has parent, adding parent fields");
                        }
                        fields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
                    }
                    if (log) {
                        Log.d("constructClass", "Total fields to process: " + fields.size());
                    }

                    // Track async operations
                    AtomicInteger pendingFields = new AtomicInteger(0);
                    AtomicBoolean hasError = new AtomicBoolean(false);
                    AtomicBoolean callbackCompleted = new AtomicBoolean(false);

                    Runnable finalizeIfDone = () -> {
                        if (pendingFields.get() == 0
                                && !hasError.get()
                                && callbackCompleted.compareAndSet(false, true)) {
                            try {
                                if (log) {
                                    Log.d("constructClass", "✓✓ SUCCESS: " + clazz.getSimpleName() + " constructed completely");
                                }
                                cleanupPath.run();
                                callback.onSuccess(rootObject);
                            } catch (ParseException | InvocationTargetException |
                                     NoSuchMethodException | IllegalAccessException |
                                     InstantiationException e) {
                                if (!hasError.getAndSet(true)) {
                                    cleanupPath.run();
                                    callback.onError(DatabaseError.fromException(e));
                                }
                            }
                        }
                    };

                    // Process each field
                    for(Field field : fields){
                        field.setAccessible(true);
                        String fieldName = field.getName();

                        if (log) {

                            Log.d("constructClass", "Processing field: " + fieldName + " (Type: " + field.getType().getSimpleName() + ")");

                        }

                        // Skip static final fields
                        if(Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())){
                            if (log) {
                                Log.d("constructClass", "  → Skipping static final field");
                            }
                            continue;
                        }

                        // Skip transient runtime/cache fields (not persisted in Firebase)
                        if (Modifier.isTransient(field.getModifiers())) {
                            if (log) {
                                Log.d("constructClass", "  → Skipping transient field");
                            }
                            continue;
                        }

                        if (isRuntimeOnlyField(field)) {
                            if (log) {
                                Log.d("constructClass", "  -> Skipping runtime-only field");
                            }
                            continue;
                        }

                        // Check if field exists in Firebase
                        if(!snapshot.hasChild(fieldName)) {
                            if (log) {
                                Log.d("constructClass", "  ⚠️ Field '" + fieldName + "' not found in Firebase, skipping");
                            }
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
                                if (log) {
                                    Log.d("constructClass", "  → Boolean field '" + fieldName + "' = " + boolValue + " (from " + value + ")");
                                }
                            } else {
                                field.set(object, false);
                                if (log) {
                                    Log.d("constructClass", "  → Boolean field '" + fieldName + "' is null, defaulting to false");
                                }
                            }
                        }
                        // Handle primitives and basic types
                        else if(Tool.isPrimitive(field)) {
                            Object value = snapshot.child(fieldName).getValue(field.getType());
                            if (log) {
                                Log.d("constructClass", "  → Primitive/Basic field '" + fieldName + "' = " + value);
                            }
                            field.set(object, value);
                        }
                        else if(field.getType() == LocalTime.class){
                            String timeString = snapshot.child(fieldName).getValue(String.class);
                            LocalTime time = LocalTime.parse(timeString);
                            if (log) {
                                Log.d("constructClass", "  → LocalTime field '" + fieldName + "' = " + time);
                            }
                            field.set(object, time);
                        }
                        else if(field.getType() == LocalDateTime.class){
                            String dateTimeString = snapshot.child(fieldName).getValue(String.class);
                            if(Tool.boolOf(dateTimeString)){
                                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString);
                                if (log) {
                                    Log.d("constructClass", "  → LocalDateTime field '" + fieldName + "' = " + dateTime);
                                }
                                field.set(object, dateTime);
                            }else{
                                field.set(object, null);
                            }


                        }
                        // Handle ArrayList
                        else if(field.getType() == ArrayList.class){
                            if (log) {
                                Log.d("constructClass", "  → ArrayList field: " + fieldName);
                            }
                            Type listType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];

                            if (listType instanceof Class<?>) {
                                Class<?> elementClass = (Class<?>) listType;
                                if (log) {
                                    Log.d("constructClass", "  → Element type: " + elementClass.getSimpleName());
                                }

                                // Check if it's a list of RequireUpdate objects
                                if (RequireUpdate.class.isAssignableFrom(elementClass)) {
                                    if (log) {
                                        Log.d("constructClass", "  → Complex object list (RequireUpdate)");
                                    }
                                    ArrayList<Object> list = new ArrayList<>();

                                    // Get all child IDs
                                    DataSnapshot listSnapshot = snapshot.child(fieldName);
                                    ArrayList<String> ids = extractIdListFromSnapshot(listSnapshot);
                                    for (String objectID : ids) {
                                        if (log) {
                                            Log.d("constructClass", "    -> Found ID: " + objectID);
                                        }
                                    }

                                    if (log) {

                                        Log.d("constructClass", "  → Total IDs found: " + ids.size());

                                    }

                                    Field relationIdsField = findRelationIdField(clazz, fieldName, true);
                                    if (relationIdsField != null) {
                                        setRelationIdsFieldValue(object, relationIdsField, ids);
                                        field.set(object, list);
                                        if (log) {
                                            Log.d("constructClass", "  → ID-only list field '" + fieldName + "', skipped nested construct");
                                        }
                                        continue;
                                    }

                                    if (!ids.isEmpty()) {
                                        pendingFields.addAndGet(ids.size());
                                        if (log) {
                                            Log.d("constructClass", "  → Pending async fields: " + pendingFields.get());
                                        }
                                        AtomicInteger pendingThisField = new AtomicInteger(ids.size());

                                        // Recursively construct each object
                                        for(String objectID : ids) {
                                            if (log) {
                                                Log.d("constructClass", "  → Fetching nested object with ID: " + objectID);
                                            }

                                            final String edgeTraceEntry = clazz.getSimpleName() + "#" + id + "." + fieldName + " -> " + elementClass.getSimpleName() + "#" + objectID;
                                            edgeStack.add(edgeTraceEntry);
                                            constructClass(elementClass, objectID,  new ConstructClassCallback() {
                                                @Override
                                                public void onSuccess(Object childObject) {
                                                    edgeStack.remove(edgeTraceEntry);
                                                    if (log) {
                                                        Log.d("constructClass", "  ✓ Nested object retrieved: " + elementClass.getSimpleName() + " (ID: " + objectID + ")");
                                                    }
                                                    synchronized (list) {
                                                        list.add(childObject);
                                                    }

                                                    int remainingThisField = pendingThisField.decrementAndGet();
                                                    if (remainingThisField == 0) {
                                                        try {
                                                            field.set(object, list);
                                                            if (log) {
                                                                Log.d("constructClass", "✓ Set async list field '" + fieldName + "' size=" + list.size());
                                                            }
                                                        } catch (IllegalAccessException e) {
                                                            if (log) {
                                                                Log.e("constructClass", "✗ Error setting field " + fieldName + ": " + e.getMessage(), e);
                                                            }
                                                            if (!hasError.getAndSet(true) && callbackCompleted.compareAndSet(false, true)) {
                                                                cleanupPath.run();
                                                                callback.onError(DatabaseError.fromException(e));
                                                            }
                                                            return;
                                                        }
                                                    }

                                                    int remaining = decrementPendingSafely(pendingFields, "async");
                                                    if (log) {
                                                        Log.d("constructClass", "  → Remaining pending fields: " + remaining);
                                                    }
                                                    finalizeIfDone.run();
                                                }

                                                @Override
                                                public void onError(DatabaseError error) {
                                                    edgeStack.remove(edgeTraceEntry);
                                                    if (log) {
                                                        Log.w("constructClass", "⚠️ Skipping nested object (ID: " + objectID + "): " + error.getMessage());
                                                    }

                                                    // Don't fail entire construction - just skip this object
                                                    int remainingThisField = pendingThisField.decrementAndGet();
                                                    if (remainingThisField == 0) {
                                                        try {
                                                            field.set(object, list);
                                                            if (log) {
                                                                Log.d("constructClass", "✓ Set async list field '" + fieldName + "' (partial) size=" + list.size());
                                                            }
                                                        } catch (IllegalAccessException e) {
                                                            if (log) {
                                                                Log.e("constructClass", "✗ Error setting field: " + e.getMessage(), e);
                                                            }
                                                            if (!hasError.getAndSet(true) && callbackCompleted.compareAndSet(false, true)) {
                                                                cleanupPath.run();
                                                                callback.onError(DatabaseError.fromException(e));
                                                            }
                                                            return;
                                                        }
                                                    }

                                                    int remaining = decrementPendingSafely(pendingFields, "async");
                                                    if (log) {
                                                        Log.d("constructClass", "  → Skipped, remaining: " + remaining);
                                                    }
                                                    finalizeIfDone.run();
                                                }
                                            });
                                        }
                                    } else {
                                        // Empty list
                                        if (log) {
                                            Log.d("constructClass", "  → Empty list for field: " + fieldName);
                                        }
                                        field.set(object, list);
                                    }
                                }
                                // Handle ArrayList<Boolean>
                                else if(elementClass == Boolean.class){
                                    if (log) {
                                        Log.d("constructClass", "  → Boolean List (Converting from Long/Integer to Boolean)");
                                    }
                                    ArrayList<Boolean> boolList = new ArrayList<>();
                                    DataSnapshot listSnapshot = snapshot.child(fieldName);

                                    for(DataSnapshot childSnapshot : listSnapshot.getChildren()){
                                        Object value = childSnapshot.getValue();
                                        if (value != null) {
                                            boolean boolValue;
                                            if (value instanceof Long) {
                                                boolValue = ((Long) value) == 1L;
                                                if (log) {
                                                    Log.d("constructClass", "    → Converted Long " + value + " to " + boolValue);
                                                }
                                            } else if (value instanceof Integer) {
                                                boolValue = ((Integer) value) == 1;
                                                if (log) {
                                                    Log.d("constructClass", "    → Converted Integer " + value + " to " + boolValue);
                                                }
                                            } else if (value instanceof Boolean) {
                                                boolValue = (Boolean) value;
                                                if (log) {
                                                    Log.d("constructClass", "    → Already Boolean: " + boolValue);
                                                }
                                            } else {
                                                // Fallback: try parsing as string "1"/"0" or "true"/"false"
                                                String strValue = value.toString();
                                                boolValue = "1".equals(strValue) || "true".equalsIgnoreCase(strValue);
                                                if (log) {
                                                    Log.d("constructClass", "    → Converted String " + value + " to " + boolValue);
                                                }
                                            }
                                            boolList.add(boolValue);
                                        }
                                    }
                                    if (log) {
                                        Log.d("constructClass", "  → Boolean list size: " + boolList.size());
                                    }
                                    field.set(object, boolList);
                                }
                                // Handle other simple lists
                                else {
                                    if (log) {
                                        Log.d("constructClass", "  → Simple list (String/Integer/etc.)");
                                    }
                                    ArrayList<Object> simpleList = new ArrayList<>();
                                    DataSnapshot listSnapshot = snapshot.child(fieldName);
                                    if (elementClass == String.class) {
                                        ArrayList<String> ids = extractIdListFromSnapshot(listSnapshot);
                                        simpleList.addAll(ids);
                                        for (String id : ids) {
                                            if (log) {
                                                Log.d("constructClass", "    -> Added simple value: " + id);
                                            }
                                        }
                                    } else {
                                        for(DataSnapshot childSnapshot : listSnapshot.getChildren()){
                                            Object value = childSnapshot.getValue(elementClass);
                                            if (value != null) {
                                                simpleList.add(value);
                                                if (log) {
                                                    Log.d("constructClass", "    -> Added simple value: " + value);
                                                }
                                            }
                                        }
                                    }
                                    if (log) {
                                        Log.d("constructClass", "  → Simple list size: " + simpleList.size());
                                    }
                                    field.set(object, simpleList);
                                }
                            }
                        }
                        else if (Map.class.isAssignableFrom(field.getType()) || field.getType() == HashMap.class) {
                            if (log) {
                                Log.d("constructClass", "  → Map field: " + fieldName);
                            }
                            DataSnapshot mapSnapshot = snapshot.child(fieldName);
                            HashMap<Object, Object> resolvedMap = new HashMap<>();

                            Type genericType = field.getGenericType();
                            Class<?> keyClass = String.class;
                            Class<?> valueClass = Object.class;
                            if (genericType instanceof ParameterizedType) {
                                Type[] args = ((ParameterizedType) genericType).getActualTypeArguments();
                                if (args.length == 2) {
                                    if (args[0] instanceof Class<?>) {
                                        keyClass = (Class<?>) args[0];
                                    }
                                    if (args[1] instanceof Class<?>) {
                                        valueClass = (Class<?>) args[1];
                                    }
                                }
                            }

                            ArrayList<DataSnapshot> mapEntries = new ArrayList<>();
                            for (DataSnapshot child : mapSnapshot.getChildren()) {
                                mapEntries.add(child);
                            }

                            if (mapEntries.isEmpty()) {
                                field.set(object, resolvedMap);
                                continue;
                            }

                            boolean keyAsync = RequireUpdate.class.isAssignableFrom(keyClass);
                            boolean valueAsync = RequireUpdate.class.isAssignableFrom(valueClass);
                            boolean hasAsync = keyAsync || valueAsync;

                            if (!hasAsync) {
                                for (DataSnapshot entry : mapEntries) {
                                    Object parsedKey = convertMapKey(entry.getKey(), keyClass);
                                    Object parsedValue = convertMapValueSnapshot(entry, valueClass);
                                    if (parsedKey != null) {
                                        resolvedMap.put(parsedKey, parsedValue);
                                    }
                                }
                                field.set(object, resolvedMap);
                                continue;
                            }

                            pendingFields.incrementAndGet();
                            AtomicInteger pendingEntries = new AtomicInteger(mapEntries.size());

                            Runnable completeMapField = () -> {
                                int remainingEntries = pendingEntries.decrementAndGet();
                                if (remainingEntries != 0) {
                                    return;
                                }
                                try {
                                    field.set(object, resolvedMap);
                                    if (log) {
                                        Log.d("constructClass", "✓ Set async map field '" + fieldName + "' size=" + resolvedMap.size());
                                    }
                                } catch (IllegalAccessException e) {
                                    if (log) {
                                        Log.e("constructClass", "✗ Error setting async map field '" + fieldName + "': " + e.getMessage(), e);
                                    }
                                    if (!hasError.getAndSet(true) && callbackCompleted.compareAndSet(false, true)) {
                                        cleanupPath.run();
                                        callback.onError(DatabaseError.fromException(e));
                                    }
                                    return;
                                }
                                int remainingPending = decrementPendingSafely(pendingFields, "async");
                                if (log) {
                                    Log.d("constructClass", "  → Remaining pending fields: " + remainingPending);
                                }
                                finalizeIfDone.run();
                            };

                            for (DataSnapshot entry : mapEntries) {
                                Object[] resolvedKeyHolder = new Object[1];
                                Object[] resolvedValueHolder = new Object[1];
                                AtomicBoolean skipEntry = new AtomicBoolean(false);
                                AtomicInteger entryParts = new AtomicInteger((keyAsync ? 1 : 0) + (valueAsync ? 1 : 0));
                                AtomicBoolean entryCompleted = new AtomicBoolean(false);

                                Runnable finalizeEntry = () -> {
                                    if (skipEntry.get()) {
                                        completeMapField.run();
                                        return;
                                    }
                                    Object keyObj = resolvedKeyHolder[0];
                                    if (keyObj != null) {
                                        synchronized (resolvedMap) {
                                            resolvedMap.put(keyObj, resolvedValueHolder[0]);
                                        }
                                    }
                                    completeMapField.run();
                                };
                                Runnable tryFinalizeEntry = () -> {
                                    if (entryParts.get() == 0 && entryCompleted.compareAndSet(false, true)) {
                                        finalizeEntry.run();
                                    }
                                };

                                if (!keyAsync) {
                                    resolvedKeyHolder[0] = convertMapKey(entry.getKey(), keyClass);
                                } else {
                                    String keyId = entry.getKey();
                                    if (!Tool.boolOf(keyId)) {
                                        skipEntry.set(true);
                                        entryParts.decrementAndGet();
                                        tryFinalizeEntry.run();
                                    } else {
                                        constructClass(keyClass, keyId, new ConstructClassCallback() {
                                            @Override
                                            public void onSuccess(Object keyObject) {
                                                resolvedKeyHolder[0] = keyObject;
                                                entryParts.decrementAndGet();
                                                tryFinalizeEntry.run();
                                            }

                                            @Override
                                            public void onError(DatabaseError error) {
                                                if (log) {
                                                    Log.w("constructClass", "⚠️ Skipping map entry key for field '" + fieldName + "' id=" + keyId + ": " + error.getMessage());
                                                }
                                                skipEntry.set(true);
                                                entryParts.decrementAndGet();
                                                tryFinalizeEntry.run();
                                            }
                                        });
                                    }
                                }

                                if (!valueAsync) {
                                    resolvedValueHolder[0] = convertMapValueSnapshot(entry, valueClass);
                                } else {
                                    String valueId = entry.getValue(String.class);
                                    if (!Tool.boolOf(valueId)) {
                                        skipEntry.set(true);
                                        entryParts.decrementAndGet();
                                        tryFinalizeEntry.run();
                                    } else {
                                        constructClass(valueClass, valueId, new ConstructClassCallback() {
                                            @Override
                                            public void onSuccess(Object valueObject) {
                                                resolvedValueHolder[0] = valueObject;
                                                entryParts.decrementAndGet();
                                                tryFinalizeEntry.run();
                                            }

                                            @Override
                                            public void onError(DatabaseError error) {
                                                if (log) {
                                                    Log.w("constructClass", "⚠️ Skipping map entry value for field '" + fieldName + "' id=" + valueId + ": " + error.getMessage());
                                                }
                                                skipEntry.set(true);
                                                entryParts.decrementAndGet();
                                                tryFinalizeEntry.run();
                                            }
                                        });
                                    }
                                }

                                tryFinalizeEntry.run();
                            }
                        }
                        // Handle LocalDateTime
                        else {
                            field.getType();
                            if(field.getType() == LocalDate.class){
                                String fbLD = snapshot.child(fieldName).getValue(String.class);
                                if (fbLD != null && !fbLD.isEmpty()) {
                                    // Special case for Student lastScheduled
                                    if(clazz == Student.class && fieldName.equals("lastScheduled")){
                                        if (log) {
                                            Log.d("ConstructClass", "Special Student.lastScheduled case");
                                        }
                                        if(snapshot.hasChild("hasScheduled")) {
                                            boolean hasScheduled = Boolean.TRUE.equals(snapshot.child("hasScheduled").getValue(Boolean.class));
                                            if (log) {
                                                Log.d("constructClass", "hasScheduled: " + hasScheduled);
                                            }
                                            if(!hasScheduled){
                                                field.set(object, Schedule.NEVER_SCHEDULED);
                                                continue;
                                            }
                                        }
                                    }
                                    LocalDate date = LocalDate.parse(fbLD);
                                    field.set(object, date);
                                    if (log) {
                                        Log.d("constructClass", "  → LocalDate field '" + fieldName + "' = " + date);
                                    }
                                } else {
                                    if (log) {
                                        Log.d("constructClass", "  → LocalDate field '" + fieldName + "' is null");
                                    }
                                }
                            }
                            // Handle Enum
                            else if(field.getType().isEnum()){
                                String enumValue = snapshot.child(fieldName).getValue(String.class);
                                if (log) {
                                    Log.d("constructClass", "  → Enum field: " + fieldName + " = " + enumValue);
                                }

                                if (enumValue != null && !enumValue.isEmpty()) {
                                    try {
                                        Class<? extends Enum> enumClass = (Class<? extends Enum>) field.getType();
                                        Enum enumConstant = Enum.valueOf(enumClass, enumValue.toUpperCase());
                                        field.set(object, enumConstant);
                                        if (log) {
                                            Log.d("constructClass", "    ✓ Enum set successfully");
                                        }
                                    } catch (IllegalArgumentException e) {
                                        if (log) {
                                            Log.e("constructClass", "    ✗ Invalid enum value: " + enumValue, e);
                                        }
                                    }
                                } else {
                                    if (log) {
                                        Log.d("constructClass", "    ⚠ Enum value is null, skipping");
                                    }
                                }
                            }
                            // Handle Duration
                            else if(field.getType() == Duration.class){
                                Object durationValue = snapshot.child(fieldName).getValue();
                                if (durationValue != null) {
                                    int durationInMinutes = ((Number) durationValue).intValue();
                                    Duration duration = Duration.ofMinutes(durationInMinutes);
                                    field.set(object, duration);
                                    if (log) {
                                        Log.d("constructClass", "  → Duration field '" + fieldName + "' = " + duration);
                                    }
                                } else {
                                    if (log) {
                                        Log.d("constructClass", "  → Duration field '" + fieldName + "' is null");
                                    }
                                }
                            }
                            // Handle single User object with explicit type hint (ex: from + fromType)
                            else if (User.class.isAssignableFrom(field.getType())) {
                                String nestedUserId = snapshot.child(fieldName).getValue(String.class);
                                if (!Tool.boolOf(nestedUserId)) {
                                    if (log) {
                                        Log.d("constructClass", "→ User field " + fieldName + " has null/empty ID, skipping");
                                    }
                                } else {
                                    Field relationIdField = findRelationIdField(clazz, fieldName, false);
                                    if (relationIdField != null) {
                                        setRelationIdFieldValue(object, relationIdField, nestedUserId);
                                        if (log) {
                                            Log.d("constructClass", "  → ID-only user field '" + fieldName + "', skipped nested construct");
                                        }
                                        continue;
                                    }
                                    String typeFieldName = fieldName + "Type";
                                    String typeHint = snapshot.hasChild(typeFieldName)
                                            ? snapshot.child(typeFieldName).getValue(String.class)
                                            : null;

                                    Class<? extends User> targetUserClass = resolveUserClassFromTypeHint(typeHint);

                                    if (targetUserClass != null) {
                                        String nestedUserNodeKey = targetUserClass.getName() + "#" + nestedUserId;
                                        if (activePath.contains(nestedUserNodeKey)) {
                                            if (log) {
                                                Log.w("constructClass", "Reusing active user node for field '" + fieldName + "': " + nestedUserNodeKey);
                                            }
                                        }
                                        pendingFields.incrementAndGet();
                                        Class<?> userClassForConstruct = targetUserClass;
                                        constructClass(userClassForConstruct, nestedUserId, new ConstructClassCallback() {
                                            @Override
                                            public void onSuccess(Object nestedObject) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                                                try {
                                                    field.set(object, nestedObject);
                                                } catch (IllegalAccessException e) {
                                                    if (log) {
                                                        Log.e("constructClass", "✗ Error setting user field " + fieldName + ": " + e.getMessage(), e);
                                                    }
                                                    if (!hasError.getAndSet(true)) {
                                                        cleanupPath.run();
                                                        callback.onError(DatabaseError.fromException(e));
                                                    }
                                                    return;
                                                }
                                                int remaining = decrementPendingSafely(pendingFields, "async");
                                                if (log) {
                                                    Log.d("constructClass", "  → Remaining pending fields: " + remaining);
                                                }
                                                finalizeIfDone.run();
                                            }

                                            @Override
                                            public void onError(DatabaseError error) {
                                                if (log) {
                                                    Log.w("constructClass", "⚠️ Skipping user field '" + fieldName + "' (ID: " + nestedUserId + "): " + error.getMessage());
                                                }
                                                int remaining = decrementPendingSafely(pendingFields, "async");
                                                if (log) {
                                                    Log.d("constructClass", "  → Skipped user, remaining: " + remaining);
                                                }
                                                finalizeIfDone.run();
                                            }
                                        });
                                    } else {
                                        if (log) {
                                            Log.w("constructClass", "⚠️ User type hint missing/invalid for field '" + fieldName + "'. Hint=" + typeHint + " (ID: " + nestedUserId + ")");
                                        }
                                        pendingFields.incrementAndGet();
                                        FirebaseDatabase.getInstance()
                                                .getReference(FirebaseNode.USER_ID_ROLES.getPath())
                                                .child(nestedUserId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot roleSnapshot) {
                                                        String fallbackType = null;
                                                        Object rawRoleNode = roleSnapshot.getValue();
                                                        if (rawRoleNode instanceof String) {
                                                            fallbackType = (String) rawRoleNode;
                                                        }
                                                        if (!Tool.boolOf(fallbackType) && roleSnapshot.hasChild("role")) {
                                                            fallbackType = roleSnapshot.child("role").getValue(String.class);
                                                        }
                                                        Class<? extends User> fallbackClass = resolveUserClassFromTypeHint(fallbackType);
                                                        if (fallbackClass == null) {
                                                            if (log) {
                                                                Log.w("constructClass", "⚠️ Unable to resolve user type from users/roles for ID: " + nestedUserId);
                                                            }
                                                            int remaining = decrementPendingSafely(pendingFields, "async");
                                                            if (log) {
                                                                Log.d("constructClass", "  → Remaining pending fields: " + remaining);
                                                            }
                                                            finalizeIfDone.run();
                                                            return;
                                                        }
                                                        String nestedUserNodeKey = fallbackClass.getName() + "#" + nestedUserId;
                                                        if (activePath.contains(nestedUserNodeKey)) {
                                                            if (log) {
                                                                Log.w("constructClass", "Reusing active fallback user node for field '" + fieldName + "': " + nestedUserNodeKey);
                                                            }
                                                        }
                                                        try {
                                                            constructClass(fallbackClass, nestedUserId, new ConstructClassCallback() {
                                                                @Override
                                                                public void onSuccess(Object nestedObject) {
                                                                    try {
                                                                        field.set(object, nestedObject);
                                                                    } catch (IllegalAccessException e) {
                                                                        if (!hasError.getAndSet(true)) {
                                                                            cleanupPath.run();
                                                                            callback.onError(DatabaseError.fromException(e));
                                                                        }
                                                                        return;
                                                                    }
                                                                    int remaining = decrementPendingSafely(pendingFields, "async");
                                                                    if (log) {
                                                                        Log.d("constructClass", "  → Remaining pending fields: " + remaining);
                                                                    }
                                                                    finalizeIfDone.run();
                                                                }

                                                                @Override
                                                                public void onError(DatabaseError error) {
                                                                    if (log) {
                                                                        Log.w("constructClass", "⚠️ Fallback user fetch failed for ID " + nestedUserId + ": " + error.getMessage());
                                                                    }
                                                                    int remaining = decrementPendingSafely(pendingFields, "async");
                                                                    if (log) {
                                                                        Log.d("constructClass", "  → Remaining pending fields: " + remaining);
                                                                    }
                                                                    finalizeIfDone.run();
                                                                }
                                                            });
                                                        } catch (NoSuchMethodException |
                                                                 InvocationTargetException |
                                                                 IllegalAccessException |
                                                                 InstantiationException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        if (log) {
                                                            Log.w("constructClass", "⚠️ users/roles lookup cancelled for " + nestedUserId + ": " + error.getMessage());
                                                        }
                                                        int remaining = decrementPendingSafely(pendingFields, "async");
                                                        if (log) {
                                                            Log.d("constructClass", "  → Remaining pending fields: " + remaining);
                                                        }
                                                        finalizeIfDone.run();
                                                    }
                                                });
                                    }
                                }
                            }
                            // Handle single RequireUpdate object
                            else if(RequireUpdate.class.isAssignableFrom(field.getType())){
                                String nestedObjectId = snapshot.child(fieldName).getValue(String.class);

                                if (nestedObjectId != null && !nestedObjectId.isEmpty()) {
                                    if (log) {
                                        Log.d("constructClass", "→ Found nested RequireUpdate field: " + fieldName + " with ID: " + nestedObjectId);
                                    }
                                    Field relationIdField = findRelationIdField(clazz, fieldName, false);
                                    if (relationIdField != null) {
                                        setRelationIdFieldValue(object, relationIdField, nestedObjectId);
                                        if (log) {
                                            Log.d("constructClass", "  → ID-only nested field '" + fieldName + "', skipped nested construct");
                                        }
                                        continue;
                                    }
                                    String nestedNodeKey = field.getType().getName() + "#" + nestedObjectId;
                                    if (activePath.contains(nestedNodeKey)) {
                                        if (log) {
                                            Log.w("constructClass", "Reusing active nested field '" + fieldName + "': " + nestedNodeKey);
                                        }
                                    }

                                    pendingFields.incrementAndGet();

                                    final String edgeTraceEntry = clazz.getSimpleName() + "#" + id + "." + fieldName + " -> " + field.getType().getSimpleName() + "#" + nestedObjectId;
                                    edgeStack.add(edgeTraceEntry);
                                    constructClass(field.getType(), nestedObjectId,  new ConstructClassCallback() {
                                        @Override
                                        public void onSuccess(Object nestedObject) throws NoSuchMethodException, IllegalAccessException, InstantiationException {
                                            edgeStack.remove(edgeTraceEntry);
                                            if (log) {
                                                Log.d("constructClass", "  ✓ Nested object retrieved: " + field.getType().getSimpleName() + " (ID: " + nestedObjectId + ")");
                                            }

                                            try {
                                                field.set(object, nestedObject);
                                                if (log) {
                                                    Log.d("constructClass", "  ✓ Set field " + fieldName + " with nested object");
                                                }
                                            } catch (IllegalAccessException e) {
                                                if (log) {
                                                    Log.e("constructClass", "✗ Error setting field " + fieldName + ": " + e.getMessage(), e);
                                                }
                                                if (!hasError.getAndSet(true)) {
                                                    cleanupPath.run();
                                                    callback.onError(DatabaseError.fromException(e));
                                                }
                                                return;
                                            }

                                            int remaining = decrementPendingSafely(pendingFields, "async");
                                            if (log) {
                                                Log.d("constructClass", "  → Remaining pending fields: " + remaining);
                                            }
                                            finalizeIfDone.run();
                                        }

                                        @Override
                                        public void onError(DatabaseError error) {
                                            edgeStack.remove(edgeTraceEntry);
                                            if (log) {
                                                Log.w("constructClass", "⚠️ Skipping nested object field '" + fieldName + "' (ID: " + nestedObjectId + "): " + error.getMessage());
                                            }

                                            // Don't fail - just leave field as null
                                            int remaining = decrementPendingSafely(pendingFields, "async");
                                            if (log) {
                                                Log.d("constructClass", "  → Skipped, remaining: " + remaining);
                                            }
                                            finalizeIfDone.run();
                                        }
                                    });
                                } else {
                                    if (log) {
                                        Log.d("constructClass", "→ Field " + fieldName + " has null/empty ID, skipping");
                                    }
                                }
                            }
                            else {
                                if (log) {
                                    Log.d("constructClass", "  → Skipping unsupported field type: " + field.getType().getSimpleName());
                                }
                            }
                        }
                    }

                    if (log) {

                        Log.d("constructClass", "Field processing complete. Pending fields: " + pendingFields.get());

                    }

                    // If no async fields, return immediately
                    if (pendingFields.get() == 0) {
                        finalizeIfDone.run();
                    }

                } catch (Exception e) {
                    if (log) {
                        Log.e("constructClass", "✗✗ EXCEPTION in constructClass for " + clazz.getSimpleName() + ": " + e.getMessage(), e);
                    }
                    cleanupPath.run();
                    callback.onError(DatabaseError.fromException(e));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (log) {
                    Log.e("constructClass", "✗✗ DATABASE CANCELLED for " + clazz.getSimpleName() + ": " + error.getMessage());
                }
                if (log) {
                    Log.e("constructClass", "Error code: " + error.getCode());
                }
                if (log) {
                    Log.e("constructClass", "Error details: " + error.getDetails());
                }
                cleanupPath.run();
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

    default int decrementPendingSafely(AtomicInteger pendingFields, String contextTag) {
        while (true) {
            int current = pendingFields.get();
            if (current <= 0) {
                return 0;
            }
            if (pendingFields.compareAndSet(current, current - 1)) {
                return current - 1;
            }
        }
    }

    default boolean isRuntimeOnlyField(Field field) {
        if (field == null) {
            return false;
        }
        String fieldName = field.getName();
        Class<?> type = field.getType();
        if ("fbc".equals(fieldName) || "fbn".equals(fieldName) || "context".equals(fieldName)) {
            return true;
        }
        if (type == Class.class || type == FirebaseNode.class) {
            return true;
        }
        if (Context.class.isAssignableFrom(type)) {
            return true;
        }
        String typeName = type.getSimpleName();
        return typeName.endsWith("Repository") || typeName.endsWith("RTDBManager");
    }

    default Field findRelationIdField(Class<?> clazz, String relationFieldName, boolean isList) {
        if (clazz == null || !Tool.boolOf(relationFieldName)) {
            return null;
        }
        String idFieldName = relationFieldName + (isList ? "Ids" : "Id");
        Field idField = findFieldIncludingParents(clazz, idFieldName);
        if (idField == null) {
            return null;
        }
        idField.setAccessible(true);
        if (isList) {
            if (!ArrayList.class.isAssignableFrom(idField.getType())) {
                return null;
            }
            Type genericType = idField.getGenericType();
            if (!(genericType instanceof ParameterizedType)) {
                return null;
            }
            Type[] args = ((ParameterizedType) genericType).getActualTypeArguments();
            if (args.length != 1 || args[0] != String.class) {
                return null;
            }
            return idField;
        }
        if (idField.getType() != String.class) {
            return null;
        }
        return idField;
    }

    default Field findFieldIncludingParents(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    default void setRelationIdsFieldValue(Object object, Field idsField, ArrayList<String> ids) throws IllegalAccessException {
        if (idsField == null) {
            return;
        }
        idsField.set(object, ids == null ? new ArrayList<>() : new ArrayList<>(ids));
    }

    default void setRelationIdFieldValue(Object object, Field idField, String id) throws IllegalAccessException {
        if (idField == null) {
            return;
        }
        idField.set(object, Tool.boolOf(id) ? id : null);
    }

    default Class<? extends User> resolveUserClassFromTypeHint(String typeHint) {
        if (!Tool.boolOf(typeHint)) return null;
        String normalized = typeHint.trim();
        for (UserType userType : UserType.values()) {
            if (userType.name().equalsIgnoreCase(normalized) || userType.type().equalsIgnoreCase(normalized)) {
                return userType.getUserClass();
            }
        }
        return null;
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

    default Object convertMapKey(String rawKey, Class<?> keyClass) {
        if (!Tool.boolOf(rawKey)) {
            return null;
        }
        if (keyClass == String.class || keyClass == Object.class) {
            return rawKey;
        }
        try {
            if (keyClass == Integer.class || keyClass == int.class) {
                return Integer.parseInt(rawKey);
            }
            if (keyClass == Long.class || keyClass == long.class) {
                return Long.parseLong(rawKey);
            }
            if (keyClass == Boolean.class || keyClass == boolean.class) {
                return "1".equals(rawKey) || "true".equalsIgnoreCase(rawKey);
            }
            if (keyClass.isEnum()) {
                Class<? extends Enum> enumClass = (Class<? extends Enum>) keyClass;
                return Enum.valueOf(enumClass, rawKey.toUpperCase());
            }
        } catch (Exception e) {
            Log.w("constructClass", "convertMapKey failed for key=" + rawKey + ", target=" + keyClass.getSimpleName());
            return null;
        }
        return rawKey;
    }

    default Object convertMapValueSnapshot(DataSnapshot snapshot, Class<?> valueClass) {
        if (snapshot == null) {
            return null;
        }
        Object raw = snapshot.getValue();
        if (valueClass == Object.class) {
            return raw;
        }
        if (raw == null) {
            return null;
        }
        try {
            if (valueClass == Boolean.class || valueClass == boolean.class) {
                if (raw instanceof Boolean) {
                    return raw;
                }
                if (raw instanceof Number) {
                    return ((Number) raw).intValue() == 1;
                }
                return "1".equals(raw.toString()) || "true".equalsIgnoreCase(raw.toString());
            }
            if (valueClass == Integer.class || valueClass == int.class) {
                if (raw instanceof Number) {
                    return ((Number) raw).intValue();
                }
                return Integer.parseInt(raw.toString());
            }
            if (valueClass == Long.class || valueClass == long.class) {
                if (raw instanceof Number) {
                    return ((Number) raw).longValue();
                }
                return Long.parseLong(raw.toString());
            }
            if (valueClass == Double.class || valueClass == double.class) {
                if (raw instanceof Number) {
                    return ((Number) raw).doubleValue();
                }
                return Double.parseDouble(raw.toString());
            }
            if (valueClass == Float.class || valueClass == float.class) {
                if (raw instanceof Number) {
                    return ((Number) raw).floatValue();
                }
                return Float.parseFloat(raw.toString());
            }
            if (valueClass == String.class) {
                return raw.toString();
            }
            if (valueClass == LocalTime.class) {
                return LocalTime.parse(raw.toString());
            }
            if (valueClass == LocalDateTime.class) {
                return LocalDateTime.parse(raw.toString());
            }
            if (valueClass == LocalDate.class) {
                return LocalDate.parse(raw.toString());
            }
            if (valueClass == Duration.class) {
                if (raw instanceof Number) {
                    return Duration.ofMinutes(((Number) raw).longValue());
                }
                String rawString = raw.toString();
                if (rawString.startsWith("PT")) {
                    return Duration.parse(rawString);
                }
                return Duration.ofMinutes(Long.parseLong(rawString));
            }
            if (valueClass.isEnum()) {
                Class<? extends Enum> enumClass = (Class<? extends Enum>) valueClass;
                return Enum.valueOf(enumClass, raw.toString().toUpperCase());
            }
            return snapshot.getValue(valueClass);
        } catch (Exception e) {
            Log.w("constructClass", "convertMapValueSnapshot failed for type=" + valueClass.getSimpleName() + ", raw=" + raw, e);
            return null;
        }
    }

    default ArrayList<String> extractIdListFromSnapshot(DataSnapshot listSnapshot) {
        LinkedHashSet<String> deduped = new LinkedHashSet<>();
        if (listSnapshot == null || !listSnapshot.exists()) {
            return new ArrayList<>();
        }

        for (DataSnapshot childSnapshot : listSnapshot.getChildren()) {
            Object rawValue = childSnapshot.getValue();
            String key = childSnapshot.getKey();

            if (rawValue instanceof String) {
                String valueId = (String) rawValue;
                if (Tool.boolOf(valueId)) {
                    deduped.add(valueId);
                    continue;
                }
            }

            if ((rawValue instanceof Boolean || rawValue instanceof Number || rawValue == null)
                    && Tool.boolOf(key) && !isNumericArrayKey(key)) {
                if (isTruthyMembershipFlag(rawValue)) {
                    deduped.add(key);
                }
            }
        }

        Object rawContainer = listSnapshot.getValue();
        if (deduped.isEmpty() && rawContainer instanceof String && Tool.boolOf((String) rawContainer)) {
            deduped.add((String) rawContainer);
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
}

