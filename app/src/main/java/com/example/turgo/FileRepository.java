package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public class FileRepository implements RepositoryClass<file, fileFirebase>{
    private DatabaseReference fileRef;

    public FileRepository(String fileId) {
        fileRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.FILE.getPath())
                .child(fileId);
    }

    @Override
    public DatabaseReference getDbReference() {
        return fileRef;
    }

    @Override
    public Class<fileFirebase> getFbClass() {
        return fileFirebase.class;
    }

    @Override
    public com.google.android.gms.tasks.Task<file> loadAsNormal() {
        TaskCompletionSource<file> taskSource = new TaskCompletionSource<>();
        String dbKey = fileRef.getKey();
        if (!Tool.boolOf(dbKey)) {
            taskSource.setResult(null);
            return taskSource.getTask();
        }

        fileRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                taskSource.setResult(null);
                return;
            }

            fileFirebase firebaseObject = snapshot.getValue(fileFirebase.class);
            if (firebaseObject == null) {
                taskSource.setResult(null);
                return;
            }

            String resolvedId = Tool.boolOf(firebaseObject.getID()) ? firebaseObject.getID() : dbKey;
            try {
                firebaseObject.constructClass(file.class, resolvedId, new ConstructClassCallback() {
                    @Override
                    public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                        file loaded = (file) object;
                        loaded.setFileID(resolvedId);
                        taskSource.setResult(loaded);
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

    public void delete() {
        fileRef.removeValue();
    }
}
