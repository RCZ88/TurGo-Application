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
import java.util.ArrayList;
import java.util.Collection;

public interface RequireUpdate<C, FBC extends FirebaseClass<C>> {
    FirebaseNode getFirebaseNode();
    Class<FBC>getFirebaseClass();


//    public default void storeToDB() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
//        FBC fbc = getFirebaseClass().getDeclaredConstructor().newInstance();
//        fbc.importObjectData((C)this);
//        DatabaseReference drf = FirebaseDatabase.getInstance().getReference(getFirebaseNode().getPath()).child(getID());
//        drf.setValue(fbc);
//    }

    default DatabaseReference getDBRef(String ID){
        return FirebaseDatabase.getInstance().getReference(getFirebaseNode().getPath() + "/" + ID);
    }
    static Pair<DatabaseReference, Class<? extends UserFirebase>> getUserDBRef(String ID){
        final String[] type = {""};
        DatabaseReference userRoleRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.USERIDROLES.getPath()  + "/" + ID+ "/role");
        userRoleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                type[0] = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        User u = null;

        String studentType = STUDENT.type();
        String teacherType = UserType.TEACHER.type();
        String parentType = UserType.PARENT.type();
        String adminType = UserType.ADMIN.type();

        Pair<DatabaseReference, Class<? extends UserFirebase>> returnPair = null;

        if(type[0].equals(studentType)){
            returnPair = new Pair<>(FirebaseDatabase.getInstance().getReference(studentType + "/" + ID), StudentFirebase.class);
        } else if (type[0].equals(teacherType)) {
            returnPair = new Pair<>(FirebaseDatabase.getInstance().getReference(teacherType + "/" + ID), TeacherFirebase.class);
        } /*else if(type[0].equals(parentType)){
            returnPair = new Pair<>(FirebaseDatabase.getInstance().getReference(parentType + "/" + ID), ParentFirebase.class);
        }else if (type[0].equals(adminType)){
            returnPair = new Pair<>(FirebaseDatabase.getInstance().getReference(adminType + "/" + ID), AdminFirebase.class);
        }*/
        return returnPair;
    }
    //can be used on any class (preferably)
    static void retrieveUser(String ID, UserFirebase[] mutatableUser){
        Pair<DatabaseReference, Class<? extends UserFirebase>> userPair= getUserDBRef(ID);
        DatabaseReference dbRef = userPair.one;
        Class<? extends UserFirebase> type = userPair.two;
        final UserFirebase[] u = {null};
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                 u[0] = snapshot.getValue(type);
//                 ocb.onObjectRetrieved(u[0]);
                 mutatableUser[0] = snapshot.getValue(type);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
//    static <T extends FirebaseClass<?>> ArrayList<T> retrieveListOf(T emptyObject) throws NoSuchMethodException {
//        DatabaseReference dr = FirebaseDatabase.getInstance().getReference(emptyObject.);
//        ArrayList<T> listOfObject = new ArrayList<>();
//        dr.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(Object firebaseObject : snapshot.getChildren()){
//                    try {
//                        listOfObject.add((T) ((FirebaseClass<?>)firebaseObject).constructClass(this.getClass(), ));
//                    } catch (NoSuchMethodException | InstantiationException |
//                             IllegalAccessException | InvocationTargetException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        return listOfObject;
//    }
    default void retrieveOnce(ObjectCallBack<FBC> ocb, String ID){

        DatabaseReference databaseRef = getDBRef(ID);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FBC fbc = snapshot.getValue(getFirebaseClass());
                if(fbc != null){
                    ocb.onObjectRetrieved(fbc);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    default ArrayList<FBC> retrieveListFromUser(String userID, String listName, ObjectCallBack<ArrayList<FBC>>ocb){
        Pair<DatabaseReference, Class<? extends UserFirebase>> pair = getUserDBRef(userID);
        DatabaseReference databaseRef = pair.one.child(listName);
        ArrayList<FBC>listOfObjects = new ArrayList<>();
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String>listOfIDs = new ArrayList<>();
                for(DataSnapshot child : snapshot.getChildren()){
                    listOfIDs.add(child.getValue(String.class));
                }
                for(String id : listOfIDs){
                    retrieveOnce(new ObjectCallBack<FBC>() {
                        @Override
                        public void onObjectRetrieved(FBC object) {
                            listOfObjects.add(object);
                        }

                        @Override
                        public void onError(DatabaseError error) {

                        }
                    }, id);
                }
                ocb.onObjectRetrieved(listOfObjects);

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
        FBC firebaseObj = getFirebaseClass().getDeclaredConstructor().newInstance();
        firebaseObj.importObjectData((C)this);

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