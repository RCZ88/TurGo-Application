package com.example.turgo;

import static com.example.turgo.UserType.STUDENT;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RequireUpdate<C, FBC extends FirebaseClass<C>> {
    FirebaseNode getFirebaseNode();
    Class<FBC>getFirebaseClass();



    default DatabaseReference getDBRef(String ID){
        return FirebaseDatabase.getInstance().getReference(getFirebaseNode().getPath() + "/" + ID);
    }
    static void getUserDBRef(String ID, ObjectCallBack<Pair<DatabaseReference, Class<? extends UserFirebase>>>pairObjectCallBack){
        final String[] type = {""};
        Log.d("User Type Retrieving", "Retrieving User Type from ID" + ID + "...");
        String path = FirebaseNode.USERIDROLES.getPath() + "/" + ID + "/role";
        Log.d("User Type Path: ", path);
        DatabaseReference userRoleRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.USERIDROLES.getPath()).child(ID).child("role");
        userRoleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("DEBUG", "onDataChange CALLED!");
                Log.d("DEBUG", "Snapshot exists: " + snapshot.exists());
                Log.d("DEBUG", "Snapshot value: " + snapshot.getValue());
                Log.d("DEBUG", "Snapshot key: " + snapshot.getKey());

                type[0] = snapshot.getValue(String.class);
                Log.d("User Type Retrieved: ", type[0]);

                String studentType = STUDENT.type();
                String teacherType = UserType.TEACHER.type();
                String parentType = UserType.PARENT.type();
                String adminType = UserType.ADMIN.type();

                Pair<DatabaseReference, Class<? extends UserFirebase>> returnPair = null;

                if(type[0].equals(studentType)){
                    returnPair = new Pair<>(FirebaseDatabase.getInstance().getReference(FirebaseNode.STUDENT.getPath()).child(ID), StudentFirebase.class);
                } else if (type[0].equals(teacherType)) {
                    returnPair = new Pair<>(FirebaseDatabase.getInstance().getReference(FirebaseNode.TEACHER.getPath()).child(ID), TeacherFirebase.class);
                }else if(type[0].equals(adminType)){
                    returnPair = new Pair<>(FirebaseDatabase.getInstance().getReference(FirebaseNode.ADMIN.getPath()).child(ID), AdminFirebase.class);
                }else if(type[0].equals(parentType)){
                    returnPair = new Pair<>(FirebaseDatabase.getInstance().getReference(FirebaseNode.PARENT.getPath()).child(ID), ParentFirebase.class);
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

            }
        });
    }
    //can be used on any class (preferably)
    static void retrieveUser(String ID, ObjectCallBack<Object>user){
        final Pair<DatabaseReference, Class<? extends UserFirebase>>[] userPair = new Pair[1];
        getUserDBRef(ID, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(Pair<DatabaseReference, Class<? extends UserFirebase>> object) {
                userPair[0] = object;
                DatabaseReference dbRef = userPair[0].one;
                Class<? extends UserFirebase> type = userPair[0].two;
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            user.onObjectRetrieved(snapshot.getValue(type));
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

            @Override
            public void onError(DatabaseError error) {

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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    default ArrayList<FBC> retrieveListFromUser(String userID, String listName, ObjectCallBack<ArrayList<FBC>>ocb){
        final Pair<DatabaseReference, Class<? extends UserFirebase>>[] pair = new Pair[1];
        getUserDBRef(userID, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(Pair<DatabaseReference, Class<? extends UserFirebase>> object) {
                pair[0] = object;
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
        DatabaseReference databaseRef = pair[0].one.child(listName);
        ArrayList<FBC>listOfObjects = new ArrayList<>();
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String>listOfIDs = new ArrayList<>();
                for(DataSnapshot child : snapshot.getChildren()){
                    listOfIDs.add(child.getValue(String.class));
                }
                for(String id : listOfIDs){
                    retrieveOnce(new ObjectCallBack<>() {
                        @Override
                        public void onObjectRetrieved(FBC object) {
                            listOfObjects.add(object);
                        }

                        @Override
                        public void onError(DatabaseError error) {

                        }
                    }, id);
                }
                try {
                    ocb.onObjectRetrieved(listOfObjects);
                } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
    default <CO extends RequireUpdate<?, ?>, COFB extends FirebaseClass<CO>> void findAggregatedObject( Class<CO> containerClass, String varName, ObjectCallBack<CO> callBack) throws IllegalAccessException, InstantiationException {

        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference(containerClass.newInstance().getFirebaseNode().getPath());

        // 🔥 Apply indexed query, then cast back to DatabaseReference
        Query indexedQuery = dbRef.orderByChild(varName + "/id").equalTo(getID());
        indexedQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    try {
                        callBack.onObjectRetrieved(null);
                    } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                             IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }

                // ✅ ONLY 1 RESULT due to indexed query
                DataSnapshot container = snapshot.getChildren().iterator().next();
                DataSnapshot part = container.child(varName);

                if (!part.exists()) {
                    try {
                        callBack.onObjectRetrieved(null);
                    } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                             IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }

                Object raw = part.getValue();
                COFB fbc = null;

                // Handle both List and single object (your original logic)
                if (raw instanceof List) {
                    for (Object obj : (List<?>) raw) {
                        if (obj instanceof FirebaseClass && ((COFB) obj).getID().equals(getID())) {
                            fbc = (COFB) obj;
                            break;
                        }
                    }
                } else {
                    fbc = (COFB) raw;  // Single object case
                }

                if (fbc == null) {
                    try {
                        callBack.onObjectRetrieved(null);
                    } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                             IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }

                // ✅ Convert FirebaseClass → RequireUpdate (your original logic)
                try {
                    fbc.convertToNormal(new ObjectCallBack<CO>() {
                        @Override
                        public void onObjectRetrieved(CO object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                            callBack.onObjectRetrieved(object);
                        }

                        @Override
                        public void onError(DatabaseError error) {
                            callBack.onError(error);
                        }
                    });
                } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | InstantiationException e) {
                    Log.e("findAggregatedObject", "Conversion failed", e);
                    try {
                        callBack.onObjectRetrieved(null);
                    } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                             IllegalAccessException | InstantiationException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("findAggregatedObject", "Query cancelled: " + error.getMessage());
                callBack.onError(error);
            }
        });
    }


    // In RequireUpdate or a utils class
    default <CO extends RequireUpdate<?, ?>, COFB extends FirebaseClass<CO>>
    void findAllAggregatedObjects(
            Class<CO> containerClass,
            String varName,                    // e.g. "schedules"
            ObjectCallBack<ArrayList<CO>> callBack
    ) throws IllegalAccessException, InstantiationException {

        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference(containerClass.newInstance().getFirebaseNode().getPath());

        // Optional: index on varName + "/id" if each element under varName has its own id
        // Otherwise this will be a scan, but only once per containerClass.
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<CO> result = new ArrayList<>();

                for (DataSnapshot container : snapshot.getChildren()) {
                    DataSnapshot part = container.child(varName);
                    if (!part.exists()) continue;

                    Object raw = part.getValue();
                    if (raw == null) continue;

                    // Case 1: list of FirebaseClass elements
                    if (raw instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> list = (List<Object>) raw;

                        boolean found = false;
                        for (Object obj : list) {
                            if (obj instanceof FirebaseClass) {
                                FirebaseClass<?> fbc = (FirebaseClass<?>) obj;
                                if (getID().equals(fbc.getID())) {
                                    found = true;
                                    break;
                                }
                            }
                        }

                        if (found) {
                            COFB fbcContainer = null;
                            try {
                                fbcContainer = container.getValue((Class<COFB>) containerClass.newInstance().getFirebaseClass());
                            } catch (IllegalAccessException | InstantiationException e) {
                                throw new RuntimeException(e);
                            }
                            if (fbcContainer != null) {
                                try {
                                    fbcContainer.convertToNormal(new ObjectCallBack<CO>() {
                                        @Override
                                        public void onObjectRetrieved(CO object) {
                                            result.add(object);
                                        }

                                        @Override
                                        public void onError(DatabaseError error) {
                                            // ignore single errors, we’re aggregating
                                        }
                                    });
                                } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                                         IllegalAccessException | InstantiationException e) {
                                    // ignore this container, continue
                                }
                            }
                        }

                        // Case 2: map of id → FirebaseClass
                    } else if (raw instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) raw;
                        if (map.containsKey(getID())) {
                            COFB fbcContainer = null;
                            try {
                                fbcContainer = container.getValue((Class<COFB>) containerClass.newInstance().getFirebaseClass());
                            } catch (IllegalAccessException | InstantiationException e) {
                                throw new RuntimeException(e);
                            }
                            if (fbcContainer != null) {
                                try {
                                    fbcContainer.convertToNormal(new ObjectCallBack<CO>() {
                                        @Override
                                        public void onObjectRetrieved(CO object) {
                                            result.add(object);
                                        }

                                        @Override
                                        public void onError(DatabaseError error) {
                                            // ignore
                                        }
                                    });
                                } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                                         IllegalAccessException | InstantiationException e) {
                                    // ignore
                                }
                            }
                        }
                    }
                }

                try {
                    callBack.onObjectRetrieved(result);
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

//        ArrayList<Field>objectVariables = new ArrayList<>(Arrays.asList(this.getClass().getDeclaredFields()));
//        for(Field field : objectVariables){
//            if(!Tool.isPrimitive(field)){
//                if(field.getType() == RequireUpdate.class){
//                    RequireUpdate<? , ?> obj = (RequireUpdate<?, ?>) field.get(this);
//                    if (obj != null) {
//                        obj.updateDB();
//                    }
//                }else if(field.getType() == ArrayList.class && Tool.getArrayListElementType(field) == RequireUpdate.class){
//                    ArrayList<Object> objects = (ArrayList<Object>) field.get(this);
//                    for(Object object : objects){
//                        RequireUpdate<? , ?> obj = (RequireUpdate<?, ?>) object;
//                        if (obj != null) {
//                            obj.updateDB();
//                        }
//                    }
//
//                }
//            }
//        }


        if(this instanceof User){
            rtdb.storeUserType((User)this);
        }
    }
    String getID();

}