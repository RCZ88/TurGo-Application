package com.example.turgo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;

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
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Tool {


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
        RequireUpdate.retrieveUser(userID, user);
        if(user[0] instanceof StudentFirebase){
            StudentFirebase sf = (StudentFirebase)user[0];
            return sf.convertToNormal();
        }else if(user[0] instanceof TeacherFirebase){
            TeacherFirebase tf = (TeacherFirebase)user[0];
            return tf.convertToNormal();
        } else if (user[0] instanceof ParentFirebase) {
            ParentFirebase pf = (ParentFirebase)user[0];
            return pf.convertToNormal();
        }else if(user[0] instanceof AdminFirebase){
            AdminFirebase af = (AdminFirebase)user[0];
            return af.convertToNormal();
        }
        return null;
    }
    public static String uploadToCloudinary(File file){
        final String[] secureURL = new String[1];
        MediaManager.get().upload(file.getAbsolutePath()).callback((new com.cloudinary.android.callback.UploadCallback() {
            @Override
            public void onStart(String requestId) {

            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {

            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                secureURL[0] = (String)resultData.get("secure_url");
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {

            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {

            }
        })).dispatch();
        return secureURL[0];
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
