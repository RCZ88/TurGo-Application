package com.example.turgo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Tool {
    public static ArrayList<Integer> getListIdFromXmlArray(Context context, int arrId){
        TypedArray objects = context.getResources().obtainTypedArray(arrId);
        ArrayList<Integer> objectIds = new ArrayList<>();
        for(int i =0; i< objects.length(); i++){
            int objId = objects.getResourceId(i, 0);
            if(objId != 0){
                objectIds.add(objId);
            }
        }
        return objectIds;
    }
    public static boolean logOutUI(Activity activity, FirebaseUser fbUser){

        new AlertDialog.Builder(activity)
                .setTitle("Confirm Sign Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Confirm", (dialog, which) -> signOut(activity, fbUser))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
        return true;
    }
    private static void signOut(Activity activity, FirebaseUser fbUser) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // 1. Sign out from Firebase
        mAuth.signOut();

        if (fbUser != null) {
            UserPresenceManager.stopTracking(fbUser.getUid());
        }

        Intent intent = new Intent(activity, ActivityLauncher.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
    public static void prepareUserObjectForScreen(Activity activity, User user, ObjectCallBack<User>callBack){
        UserFirebase fbUser;
        Log.d("PrepareUserObject", "User Object: " +user.toString());
        String userType = user.getUserType().toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            fbUser = activity.getIntent().getSerializableExtra(user.getSerializeCode(), UserFirebase.class);
        } else {
            fbUser = (TeacherFirebase) activity.getIntent().getSerializableExtra(user.getSerializeCode());
        }
        if(fbUser == null){
            Log.e(activity.toString(), "Retrieved " + userType + " is null");
            Toast.makeText(activity, "Error loading " + userType + " data", Toast.LENGTH_SHORT).show();
            activity.finish();
        }else{
            Log.d(activity.toString(), "Retrieved " + userType + ": " + fbUser);
        }
        try{
            assert fbUser != null;
            ((FirebaseClass<User>)fbUser).convertToNormal(new ObjectCallBack<>() {
                @Override
                public void onObjectRetrieved(User object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                    callBack.onObjectRetrieved(object);
                }

                @Override
                public void onError(DatabaseError error) {
                    Log.e(activity.toString(), "Error converting " +  userType + ": " + error.getMessage());
                }
            });
        } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }

    }
    public static FirebaseNode getNodeOfID(String objectID){
        final Class<?>[] clazz = new Class[1];
        for(FirebaseNode node : FirebaseNode.values()){
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(node.getPath()).child(objectID);

            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getValue() != null){
                        clazz[0] =snapshot.getValue().getClass();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            if(clazz[0] != null){
                break;
            }
        }
        FirebaseNode fbn = null;
        for(FirebaseNode node : FirebaseNode.values()){
            if(node.getClazz() == clazz[0]){
                fbn = node;
            }
        }
        return fbn;
    }


    public static File uriToFile(Uri uri, Context context) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", ".jpg", context.getCacheDir());
        OutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
        return tempFile;
    }
    public static String getFileName(Context context, Uri uri) {
        String result = null;

        // Case 1: Content URI (content://)
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }

        // Case 2: File URI (file://)
        if (result == null) {
            result = uri.getLastPathSegment();
        }

        return result;
    }

    public static User getUserOfId(String userID) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        UserFirebase [] user = new UserFirebase[1];
        RequireUpdate.retrieveUser(userID, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(Object object) {
                user[0] = (UserFirebase) object;
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
        if(user[0] instanceof StudentFirebase){
            StudentFirebase sf = (StudentFirebase)user[0];
            final Student[] student = new Student[1];
            sf.convertToNormal(new ObjectCallBack<Student>() {
                @Override
                public void onObjectRetrieved(Student object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                    student[0] = object;
                }

                @Override
                public void onError(DatabaseError error) {

                }
            });
            return student[0];
        }else if(user[0] instanceof TeacherFirebase){
            TeacherFirebase tf = (TeacherFirebase)user[0];
            final Teacher[] teacher = new Teacher[1];
            tf.convertToNormal(new ObjectCallBack<Teacher>() {
                @Override
                public void onObjectRetrieved(Teacher object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                    teacher[0] = object;
                }

                @Override
                public void onError(DatabaseError error) {

                }
            });
            return teacher[0];
        } else if (user[0] instanceof ParentFirebase) {
            ParentFirebase pf = (ParentFirebase)user[0];
            final Parent[] parent = new Parent[1];
            pf.convertToNormal(new ObjectCallBack<>() {
                @Override
                public void onObjectRetrieved(Parent object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                    parent[0] = object;
                }

                @Override
                public void onError(DatabaseError error) {

                }
            });
            return parent[0];
        }else if(user[0] instanceof AdminFirebase){
            AdminFirebase af = (AdminFirebase)user[0];
            final Admin[] admin = new Admin[1];
            af.convertToNormal(new ObjectCallBack<>() {
                @Override
                public void onObjectRetrieved(Admin object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                    admin[0] = object;
                }

                @Override
                public void onError(DatabaseError error) {

                }
            });
            return admin[0];
        }
        return null;
    }
    public static void checkIfReferenceExists(DatabaseReference ref, ObjectCallBack<Boolean> callBack){
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    callBack.onObjectRetrieved(snapshot.exists());
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
    public static void handleEmpty(boolean empty, RecyclerView recyclerView, TextView emptyText){
        if(empty){
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        }else{
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    public static void uploadToCloudinary(File file, ObjectCallBack<String>secureUrl){
        Log.d("Tool(UploadToCloudinary)", "File Path being Uploaded: " + file.getAbsolutePath());

        MediaManager.get().upload(file.getAbsolutePath()).callback((new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                Log.d("Tool(UploadToCloudinary)", "Starting to Upload: " + requestId);
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {

            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                try {
                    Log.d("Tool(UploadToCloudinary)", "Upload Successfull!");
                    secureUrl.onObjectRetrieved((String)resultData.get("secure_url"));
                } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {

            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {

            }
        })).dispatch();

    }
    public static void uploadToCloudinary(String path){
        MediaManager.get().upload(path).dispatch();
    }
    public static void setImageCloudinary(Context context, String imageURL, ImageView imageView){
        Glide.with(context)
                .load(imageURL)
                .placeholder(R.drawable.cloudinary_loading_placeholder)
                .error(R.drawable.cloudinary_error)
                .into(imageView);
    }
    public static boolean hasParent(Class<?>clazz){
        return clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class;

    }
    public static Bitmap drawableToBitmap(Drawable drawable){
        Bitmap bitmap;
        if(drawable instanceof BitmapDrawable){
            bitmap = ((BitmapDrawable)drawable).getBitmap();
        }else {
            // If the Drawable is not a BitmapDrawable (e.g., VectorDrawable), create a Bitmap from it
            bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );

            // Create a Canvas to draw the Drawable onto the Bitmap
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return bitmap;
    }
    public static Drawable getDrawableFromId(Context context, int id){
        return ContextCompat.getDrawable(context, id);
    }

    public static boolean isTimeOccupied(LocalTime start, LocalDate date,  Meeting meeting){
        if(date == meeting.getDateOfMeeting()){
            if(start.isAfter(meeting.getStartTimeChange()) && start.isBefore(meeting.getEndTimeChange()) || start.equals(meeting.getStartTimeChange()) || start.equals(meeting.getEndTimeChange())){
                return false;
            }
        }
        return true;
    }
    public static boolean isTimeAvailable(LocalTime start, LocalTime end, LocalTime comparedToStart, LocalTime comparedToEnd){
        return !((start.equals(comparedToStart) || (start.isAfter(comparedToStart) && start.isBefore(comparedToEnd))) && (end.isBefore(comparedToEnd) || end.equals(comparedToEnd) && end.isAfter(comparedToStart)));
    }
    public static boolean isTimeOccupied(LocalTime start, LocalTime end, DayOfWeek day, Schedule schedule){
        if(day == schedule.getDay()){
            LocalTime comparedToStart = schedule.getMeetingStart();
            LocalTime comparedToEnd = schedule.getMeetingEnd();
            return !isTimeAvailable(start, end, comparedToStart, comparedToEnd);
        }
        return false;
    }
    public static boolean isShorter(Duration duration1, Duration duration2){
        return duration1.compareTo(duration2) < 0;
    }
    public static boolean isLonger(Duration duration1, Duration duration2){
        return duration1.compareTo(duration2) > 0;
    }
}
