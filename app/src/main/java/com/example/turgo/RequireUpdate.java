package com.example.turgo;

import static com.example.turgo.UserType.STUDENT;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

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

                String studentType = UserType.STUDENT.type();
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

}