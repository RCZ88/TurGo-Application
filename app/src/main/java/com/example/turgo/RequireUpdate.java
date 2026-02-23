package com.example.turgo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface RequireUpdate<C extends RequireUpdate<C, FBC, RC>, FBC extends FirebaseClass<C>, RC extends RepositoryClass<C, FBC>> {
    FirebaseNode getFirebaseNode();
    Class<RC> getRepositoryClass();
    Class<FBC>getFirebaseClass();

    default Task<List<C>> getAllObjects() {
        return RequireUpdate.getAllObjects((Class<C>) this.getClass());
    }

    static <C extends RequireUpdate<C, FBC, RC>, FBC extends FirebaseClass<C>, RC extends RepositoryClass<C, FBC>>
    Task<List<C>> getAllObjects(Class<C> modelClass) {
        TaskCompletionSource<List<C>> taskSource = new TaskCompletionSource<>();

        final C tempInstance;
        try {
            tempInstance = modelClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            taskSource.setException(e);
            return taskSource.getTask();
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference(tempInstance.getFirebaseNode().getPath());
        Class<RC> repositoryClass = tempInstance.getRepositoryClass();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Task<C>> tasks = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String childId = child.getKey();
                    if (childId == null) {
                        continue;
                    }
                    try {
                        RC repository = repositoryClass.getDeclaredConstructor(String.class).newInstance(childId);
                        tasks.add(repository.loadAsNormal());
                    } catch (Exception e) {
                        if (!taskSource.getTask().isComplete()) {
                            taskSource.setException(e);
                        }
                        return;
                    }
                }

                Tasks.whenAllSuccess(tasks)
                        .addOnSuccessListener(results -> {
                            ArrayList<C> objects = new ArrayList<>();
                            for (Object result : results) {
                                if (result != null) {
                                    objects.add((C) result);
                                }
                            }
                            if (!taskSource.getTask().isComplete()) {
                                taskSource.setResult(objects);
                            }
                        })
                        .addOnFailureListener(error -> {
                            if (!taskSource.getTask().isComplete()) {
                                taskSource.setException(error);
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!taskSource.getTask().isComplete()) {
                    taskSource.setException(error.toException());
                }
            }
        });

        return taskSource.getTask();
    }

    default DatabaseReference getDBRef(String ID){
        return FirebaseDatabase.getInstance().getReference(getFirebaseNode().getPath() + "/" + ID);
    }
    static void getUserDBRef(String ID, ObjectCallBack<Pair<DatabaseReference, Class<? extends UserFirebase>>>pairObjectCallBack){
        Log.d("User Type Retrieving", "Retrieving User Type from ID" + ID + "...");
        String path = FirebaseNode.USER_ID_ROLES.getPath() + "/" + ID ;
        Log.d("User Type Path: ", path);
        DatabaseReference userRoleRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.USER_ID_ROLES.getPath()).child(ID);
        Log.d("DEBUG", "ID being used: " + ID);
        Log.d("DEBUG", "Full path: " + userRoleRef);
        userRoleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("DEBUG", "onDataChange CALLED!");
                Log.d("DEBUG", "Snapshot exists: " + snapshot.exists());
                Log.d("DEBUG", "Snapshot value: " + snapshot.getValue());
                Log.d("DEBUG", "Snapshot value class: " + (snapshot.getValue() != null ? snapshot.getValue().getClass() : "null"));
                Log.d("DEBUG", "Snapshot key: " + snapshot.getKey());

                String type = null;
                Object rawRoleNode = snapshot.getValue();
                if (rawRoleNode instanceof String) {
                    type = (String) rawRoleNode;
                }
                if (!Tool.boolOf(type) && snapshot.hasChild("role")) {
                    type = snapshot.child("role").getValue(String.class);
                }


                if (!Tool.boolOf(type)) {
                    pairObjectCallBack.onError(DatabaseError.fromException(
                            new IllegalStateException("User role not found for ID: " + ID)
                    ));
                    return;
                }

                String studentType = UserType.STUDENT.type();
                String teacherType = UserType.TEACHER.type();
                String parentType = UserType.PARENT.type();
                String adminType = UserType.ADMIN.type();

                Pair<DatabaseReference, Class<? extends UserFirebase>> returnPair = null;

                if(type.equals(studentType)){
                    returnPair = new Pair<>(FirebaseDatabase.getInstance().getReference(FirebaseNode.STUDENT.getPath()).child(ID), StudentFirebase.class);
                } else if (type.equals(teacherType)) {
                    returnPair = new Pair<>(FirebaseDatabase.getInstance().getReference(FirebaseNode.TEACHER.getPath()).child(ID), TeacherFirebase.class);
                }else if(type.equals(adminType)){
                    returnPair = new Pair<>(FirebaseDatabase.getInstance().getReference(FirebaseNode.ADMIN.getPath()).child(ID), AdminFirebase.class);
                }else if(type.equals(parentType)){
                    returnPair = new Pair<>(FirebaseDatabase.getInstance().getReference(FirebaseNode.PARENT.getPath()).child(ID), ParentFirebase.class);
                }

                if (returnPair == null) {
                    pairObjectCallBack.onError(DatabaseError.fromException(
                            new IllegalStateException("Unsupported user role '" + type + "' for ID: " + ID)
                    ));
                    return;
                }
                try {
                    pairObjectCallBack.onObjectRetrieved(returnPair);
                } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pairObjectCallBack.onError(error);
            }
        });
    }
    //can be used on any class (preferably)
    static void retrieveUser(String ID, ObjectCallBack<Object>callBack){
        final Pair<DatabaseReference, Class<? extends UserFirebase>>[] userPair = new Pair[1];
        getUserDBRef(ID, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(Pair<DatabaseReference, Class<? extends UserFirebase>> object) {
                userPair[0] = object;
                if (userPair[0] == null || userPair[0].one == null || userPair[0].two == null) {
                    callBack.onError(DatabaseError.fromException(
                            new NullPointerException("User database reference is null for ID: " + ID)
                    ));
                    return;
                }
                DatabaseReference dbRef = userPair[0].one;
                Class<? extends UserFirebase> type = userPair[0].two;
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            callBack.onObjectRetrieved(snapshot.getValue(type));
                        } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                                 IllegalAccessException | InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callBack.onError(error);
                    }
                });
            }

            @Override
            public void onError(DatabaseError error) {
                callBack.onError(error);
            }
        });


    }

    default void retrieveOnce(ObjectCallBack<FBC> ocb, String ID){

        DatabaseReference databaseRef = getDBRef(ID);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FBC fbc = snapshot.getValue(getFirebaseClass());
                if(fbc != null){
                    try {
                        ocb.onObjectRetrieved(fbc);
                    } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                             IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
                ocb.onError(DatabaseError.fromException(
                        new IllegalStateException("No data found for ID: " + ID + " at " + databaseRef)
                ));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ocb.onError(error);
            }
        });

    }
    default ArrayList<FBC> retrieveListFromUser(String userID, String listName, ObjectCallBack<ArrayList<FBC>>ocb){
        ArrayList<FBC>listOfObjects = new ArrayList<>();
        getUserDBRef(userID, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(Pair<DatabaseReference, Class<? extends UserFirebase>> object) {
                if (object == null || object.one == null) {
                    ocb.onError(DatabaseError.fromException(
                            new NullPointerException("User database reference is null for ID: " + userID)
                    ));
                    return;
                }
                DatabaseReference databaseRef = object.one.child(listName);
                databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String>listOfIDs = new ArrayList<>();
                        for(DataSnapshot child : snapshot.getChildren()){
                            String value = child.getValue(String.class);
                            if (Tool.boolOf(value)) {
                                listOfIDs.add(value);
                            }
                        }
                        if (listOfIDs.isEmpty()) {
                            try {
                                ocb.onObjectRetrieved(listOfObjects);
                            } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                                     IllegalAccessException | InstantiationException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }
                        AtomicInteger pending = new AtomicInteger(listOfIDs.size());
                        for(String id : listOfIDs){
                            retrieveOnce(new ObjectCallBack<>() {
                                @Override
                                public void onObjectRetrieved(FBC object) {
                                    listOfObjects.add(object);
                                    if (pending.decrementAndGet() == 0) {
                                        try {
                                            ocb.onObjectRetrieved(listOfObjects);
                                        } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                                                 IllegalAccessException | InstantiationException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }

                                @Override
                                public void onError(DatabaseError error) {
                                    if (pending.decrementAndGet() == 0) {
                                        try {
                                            ocb.onObjectRetrieved(listOfObjects);
                                        } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                                                 IllegalAccessException | InstantiationException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }, id);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        ocb.onError(error);
                    }
                });
            }

            @Override
            public void onError(DatabaseError error) {
                ocb.onError(error);
            }
        });
        return listOfObjects;
    }
    default void toggleRealtime(String ID,FBC[]mutableObject){
        DatabaseReference databaseRef = getDBRef(ID);


        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FBC fbc = snapshot.getValue(getFirebaseClass());
                if(fbc != null){
                    mutableObject[0] = fbc;
                    Log.d("Firebase", "Retrieved: " + fbc);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
    }
    default <CO extends RequireUpdate<?, ?, ?>, COFB extends FirebaseClass<CO>>
    void findAggregatedObject(Class<CO> containerClass, String varName, ObjectCallBack<CO> callBack)
            throws IllegalAccessException, InstantiationException {

        CO tempInstance = containerClass.newInstance();
        String path = tempInstance.getFirebaseNode().getPath();
        String searchId = getID();

        Log.d("findAggregatedObject", "🔍 Scanning: " + path + " | varName: " + varName + " | searching ID: " + searchId);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(path);

        // ⚠️ NO QUERY - Must scan ALL containers
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("findAggregatedObject", "📊 Scanning " + snapshot.getChildrenCount() + " containers...");

                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    Log.w("findAggregatedObject", "❌ No containers found at path: " + path);
                    try {
                        callBack.onObjectRetrieved(null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }

                // 🔍 Scan through all containers (teachers, courses, etc.)
                for (DataSnapshot containerSnapshot : snapshot.getChildren()) {
                    DataSnapshot arraySnapshot = containerSnapshot.child(varName);

                    if (!arraySnapshot.exists()) {
                        continue; // This container doesn't have the field
                    }

                    // Check if array contains our ID
                    boolean found = false;
                    for (DataSnapshot itemSnapshot : arraySnapshot.getChildren()) {
                        String itemId = itemSnapshot.getValue(String.class);
                        if (searchId.equals(itemId)) {
                            found = true;
                            Log.d("findAggregatedObject", "✅ FOUND in container: " + containerSnapshot.getKey());
                            break;
                        }
                    }

                    if (found) {
                        // Convert this container to object
                        try {
                            Class<COFB> fbClass = (Class<COFB>) Class.forName(
                                    containerClass.getName() + "Firebase"
                            );

                            COFB fbc = containerSnapshot.getValue(fbClass);

                            if (fbc == null) {
                                Log.e("findAggregatedObject", "❌ Failed to parse container");
                                callBack.onObjectRetrieved(null);
                                return;
                            }

                            Log.d("findAggregatedObject", "📦 Converting: " + fbc.getClass().getSimpleName());

                            // Convert Firebase → Normal
                            ((FirebaseClass<CO>) fbc).convertToNormal(new ObjectCallBack<CO>() {
                                @Override
                                public void onObjectRetrieved(CO object) {
                                    Log.d("findAggregatedObject", "🎉 SUCCESS: " + object.getClass().getSimpleName());
                                    try {
                                        callBack.onObjectRetrieved(object);
                                    } catch (ParseException | InvocationTargetException |
                                             NoSuchMethodException | IllegalAccessException |
                                             InstantiationException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                @Override
                                public void onError(DatabaseError error) {
                                    Log.e("findAggregatedObject", "💥 Conversion error: " + error.getMessage());
                                    callBack.onError(error);
                                }
                            });

                            return; // Found and processing

                        } catch (Exception e) {
                            Log.e("findAggregatedObject", "💥 Error processing container", e);
                            try {
                                callBack.onObjectRetrieved(null);
                            } catch (ParseException | InvocationTargetException |
                                     NoSuchMethodException | IllegalAccessException |
                                     InstantiationException ex) {
                                throw new RuntimeException(ex);
                            }
                            return;
                        }
                    }
                }

                // Not found in any container
                Log.w("findAggregatedObject", "❌ ID not found in any container's " + varName);
                try {
                    callBack.onObjectRetrieved(null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("findAggregatedObject", "🚫 Query cancelled: " + error.getMessage());
                callBack.onError(error);
            }
        });
    }




    // In RequireUpdate or a utils class
    default <CO extends RequireUpdate<?, ?, ?>, COFB extends FirebaseClass<CO>>
    void findAllAggregatedObjects(
            Class<CO> containerClass,
            String varName,
            ObjectCallBack<ArrayList<CO>> callBack
    ) throws IllegalAccessException, InstantiationException {

        CO tempInstance = containerClass.newInstance();
        String path = tempInstance.getFirebaseNode().getPath();
        String searchId = getID();

        Log.d("findAllAggregatedObjects", "🔍 Scanning: " + path + " | varName: " + varName + " | searching ID: " + searchId);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(path);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("findAllAggregatedObjects", "📊 Scanning " + snapshot.getChildrenCount() + " containers...");

                ArrayList<CO> result = new ArrayList<>();

                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    Log.w("findAllAggregatedObjects", "❌ No containers found");
                    try {
                        callBack.onObjectRetrieved(result); // Return empty list
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }

                // Track async conversions
                AtomicInteger pendingConversions = new AtomicInteger(0);
                List<DataSnapshot> matchingContainers = new ArrayList<>();

                // PHASE 1: Find all matching containers (synchronous scan)
                for (DataSnapshot containerSnapshot : snapshot.getChildren()) {
                    DataSnapshot arraySnapshot = containerSnapshot.child(varName);

                    if (!arraySnapshot.exists()) {
                        continue;
                    }

                    // Check if this container's array contains our ID
                    boolean found = false;
                    for (DataSnapshot itemSnapshot : arraySnapshot.getChildren()) {
                        String itemId = itemSnapshot.getValue(String.class);
                        if (itemId != null && itemId.equals(searchId)) {
                            found = true;
                            Log.d("findAllAggregatedObjects", "✅ Match in container: " + containerSnapshot.getKey());
                            break;
                        }
                    }

                    if (found) {
                        matchingContainers.add(containerSnapshot);
                    }
                }

                Log.d("findAllAggregatedObjects", "📦 Found " + matchingContainers.size() + " matching containers");

                if (matchingContainers.isEmpty()) {
                    try {
                        callBack.onObjectRetrieved(result); // Return empty list
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }

                // PHASE 2: Convert all matching containers (async)
                pendingConversions.set(matchingContainers.size());

                for (DataSnapshot containerSnapshot : matchingContainers) {
                    try {
                        // Get the FirebaseClass type
                        Class<COFB> fbClass = (Class<COFB>) Class.forName(
                                containerClass.getName() + "Firebase"
                        );

                        COFB fbcContainer = containerSnapshot.getValue(fbClass);

                        if (fbcContainer == null) {
                            Log.e("findAllAggregatedObjects", "❌ Failed to parse container: " + containerSnapshot.getKey());
                            if (pendingConversions.decrementAndGet() == 0) {
                                callBack.onObjectRetrieved(result);
                            }
                            continue;
                        }

                        Log.d("findAllAggregatedObjects", "🔄 Converting: " + containerSnapshot.getKey());

                        // Convert Firebase → Normal (async!)
                        ((FirebaseClass<CO>) fbcContainer).convertToNormal(new ObjectCallBack<CO>() {
                            @Override
                            public void onObjectRetrieved(CO object) {
                                synchronized (result) {
                                    result.add(object);
                                    Log.d("findAllAggregatedObjects", "✅ Converted: " + object.getClass().getSimpleName());
                                }

                                // Check if all conversions done
                                if (pendingConversions.decrementAndGet() == 0) {
                                    Log.d("findAllAggregatedObjects", "🎉 All conversions complete: " + result.size() + " objects");
                                    try {
                                        callBack.onObjectRetrieved(result);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }

                            @Override
                            public void onError(DatabaseError error) {
                                Log.e("findAllAggregatedObjects", "💥 Conversion error: " + error.getMessage());

                                // Still decrement and check completion
                                if (pendingConversions.decrementAndGet() == 0) {
                                    try {
                                        callBack.onObjectRetrieved(result);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e("findAllAggregatedObjects", "💥 Error processing container", e);
                        if (pendingConversions.decrementAndGet() == 0) {
                            try {
                                callBack.onObjectRetrieved(result);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("findAllAggregatedObjects", "🚫 Query cancelled: " + error.getMessage());
                callBack.onError(error);
            }
        });
    }



    default void updateDB() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Log.d("RequireUpdate(UpdateDB)", "Updating User to DB...");
        FBC firebaseObj = getFirebaseClass().getDeclaredConstructor().newInstance();
        firebaseObj.importObjectData((C)this);
        Log.d("RequireUpdate(UpdateDB)", "Successfully Converted to FirebaseClass: " +firebaseObj);

        FirebaseNode fn  = getFirebaseNode();
        String path = fn.getPath();

        RTDBManager<FBC> rtdb = new RTDBManager<>();
        // Now we know FBC implements RequireUpdate<FBC>, so no cast needed
        rtdb.storeData(path, this.getID(), firebaseObj, firebaseObj.getClass().toString(), "Successfull");



        if(this instanceof User){
            rtdb.storeUserType((User)this);
        }
    }
    String getID();
    default RC getRepositoryInstance(){
        try {
            return getRepositoryClass().getDeclaredConstructor(String.class).newInstance(getID());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
