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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
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
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tool {
    public static <T> ArrayList<T> streamToArray(Stream<T> stream){
        return stream.collect(Collectors.toCollection(ArrayList::new));
    }

    public static boolean isOverlapping(LocalTime startA, LocalTime endA,
                                        LocalTime startB, LocalTime endB) {
        // Validate inputs
        if (startA.isAfter(endA) || startB.isAfter(endB)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        return startA.isBefore(endB) && startB.isBefore(endA);
    }

    public static boolean hasField(Class<?> clazz, String fieldName) {
        try {
            clazz.getDeclaredField(fieldName);
            return true;  // Field "fieldName" exists on this class
        } catch (NoSuchFieldException e) {
            return false;  // Field "fieldName" does NOT exist
        }
    }
    public static void loadFragment(FragmentActivity activity, int containerId, Fragment fragment){
        activity.getSupportFragmentManager()
                .beginTransaction().replace(containerId, fragment)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }

    public static void loadFragment(FragmentActivity activity, int containerId, Fragment fragment, Bundle bundle) {
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, fragment)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }
    public static String stringifyStartEndTime(LocalTime start, LocalTime end){
        return start.getHour()  + ":" + start.getMinute() + " - " + end.getHour() + ":" + end.getMinute();
    }
    public static boolean boolOf(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof String) {
            return !((String) object).isEmpty();
        }
        if (object instanceof Integer) {
            int val = (int) object;
            return val != 0 && val != -1;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        if (object.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(object) > 0;
        }
        if (object instanceof java.util.Collection) {
            return !((java.util.Collection<?>) object).isEmpty();
        }
        if (object instanceof java.util.Map) {
            return !((java.util.Map<?, ?>) object).isEmpty();
        }
        if (object instanceof Number) {
            return ((Number) object).doubleValue() != 0;
        }
        // Default to true for other non-empty, non-null objects
        return true;
    }
    public static String formatTime24h(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static void run(FragmentActivity activity, String loadingMessage, ThrowingRunnable action, Runnable success, Consumer<Exception> onError){
        LoadingBottomSheet loadingBottomSheet = LoadingBottomSheet.newInstance(loadingMessage);
        loadingBottomSheet.show(activity.getSupportFragmentManager(), "loading");

        final long startTime = System.currentTimeMillis();
        final long MIN_DISPLAY_TIME = 500; // Minimum 500ms display

        new Thread(() -> {
            try {
                action.run();

                // Calculate how long to wait to meet minimum display time
                long elapsed = System.currentTimeMillis() - startTime;
                long delay = Math.max(0, MIN_DISPLAY_TIME - elapsed);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    loadingBottomSheet.dismiss();
                    if (success != null) {
                        success.run();
                    }
                }, delay);

            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    loadingBottomSheet.dismiss();
                    if (onError != null) onError.accept(e);
                });
            }
        }).start();
    }
    public static  void doBackPress(AppCompatActivity activity){
        activity.getOnBackPressedDispatcher().onBackPressed();
    }

    public static int dpToPx(int i, AppCompatActivity aca) {
        return (int) (i* aca.getResources().getDisplayMetrics().density);
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
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

    public static void warn(Activity activity, String title, String message, String positiveButtonText, String negativeButtonText, Runnable onPositive){
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, (dialog, which) -> onPositive.run())
                .setNegativeButton(negativeButtonText, (dialog, which) -> dialog.cancel())
                .show();
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
    public static double safeDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;  // Or whatever default makes sense
        }
        return value;
    }
    public static <U extends User & RequireUpdate<U, ?, R>, R extends RepositoryClass<U, ?>> Task<U> prepareUserObjectForScreen(R repositoryObject){
        TaskCompletionSource<U> tcs = new TaskCompletionSource<>();
        if (repositoryObject == null) {
            tcs.setException(new IllegalArgumentException("Repository object cannot be null"));
            return tcs.getTask();
        }
        AtomicBoolean completed = new AtomicBoolean(false);
        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        Runnable timeoutRunnable = () -> {
            if (completed.compareAndSet(false, true)) {
                tcs.setException(new TimeoutException("Timed out while preparing user object"));
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 15000);

        repositoryObject.loadAsNormal()
                .addOnSuccessListener(user -> {
                    if (!completed.compareAndSet(false, true)) {
                        return;
                    }
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    if (user == null) {
                        tcs.setException(new IllegalStateException("Loaded user is null"));
                        return;
                    }
                    tcs.setResult(user);
                })
                .addOnFailureListener(error -> {
                    if (!completed.compareAndSet(false, true)) {
                        return;
                    }
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    tcs.setException(error);
                });
        return tcs.getTask();
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
        String extension = ".bin";
        String displayName = getFileName(context, uri);
        if (Tool.boolOf(displayName) && displayName.contains(".")) {
            String fromName = displayName.substring(displayName.lastIndexOf('.')).trim();
            if (fromName.length() > 1 && fromName.length() <= 10) {
                extension = fromName;
            }
        } else {
            String mime = context.getContentResolver().getType(uri);
            if (Tool.boolOf(mime)) {
                String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
                if (Tool.boolOf(ext)) {
                    extension = "." + ext;
                }
            }
        }

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("Unable to open URI input stream: " + uri);
            }
            File tempFile = File.createTempFile("upload", extension, context.getCacheDir());
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
            }
            return tempFile;
        }
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

//    public static <F, N> void convertFirebaseListToNormal(
//            ArrayList<F> firebaseList,
//            ConvertToNormalCallback<N> finalCallback) {
//
//        if (firebaseList == null || firebaseList.isEmpty()) {
//            finalCallback.onAllConverted(new ArrayList<>());
//            return;
//        }
//
//        ArrayList<N> normalList = new ArrayList<>();
//        AtomicInteger completed = new AtomicInteger(0);
//        AtomicBoolean hasError = new AtomicBoolean(false);
//        int total = firebaseList.size();
//
//        Log.d("ConvertList", "Starting conversion of " + total + " objects");
//
//        for (int i = 0; i < firebaseList.size(); i++) {
//            F firebaseObj = firebaseList.get(i);
//            final int index = i;
//
//            try {
//                // Assuming all Firebase classes have convertToNormal method
//                if (firebaseObj instanceof FirebaseClass) {
//                    ((FirebaseClass<N>) firebaseObj).convertToNormal(new ObjectCallBack<N>() {
//                        @Override
//                        public void onObjectRetrieved(N normalObj) {
//                            synchronized (normalList) {
//                                normalList.add(normalObj);
//                            }
//
//                            int count = completed.incrementAndGet();
//                            Log.d("ConvertList", "Converted " + count + "/" + total);
//
//                            // Check if all done
//                            if (count == total && !hasError.get()) {
//                                Log.d("ConvertList", "✓ All conversions complete!");
//                                finalCallback.onAllConverted(normalList);
//                            }
//                        }
//
//                        @Override
//                        public void onError(DatabaseError error) {
//                            Log.e("ConvertList", "✗ Error converting object " + index + ": " + error.getMessage());
//                            if (!hasError.getAndSet(true)) {
//                                finalCallback.onError(error);
//                            }
//                        }
//                    });
//                }
//            } catch (Exception e) {
//                Log.e("ConvertList", "✗ Exception converting object " + index, e);
//                if (!hasError.getAndSet(true)) {
//                    finalCallback.onError(DatabaseError.fromException(e));
//                }
//            }
//        }
//    }

    // Callback interface
    public interface ConvertToNormalCallback<N> {
        void onAllConverted(ArrayList<N> normalList);
        void onError(DatabaseError error);
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


    public static void handleEmpty(boolean empty, ViewGroup view, TextView textView){
        if(empty){
            view.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }else{
            textView.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        }
    }
    public static void handleEmpty(boolean empty, ViewGroup view, LinearLayout linearLayout){
        if(empty){
            view.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        }else{
            linearLayout.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        }
    }
    public static boolean isPrimitive(Field field){
        return field.getType().isPrimitive() ||
                field.getType() == String.class ||
                field.getType() == Integer.class ||
                field.getType() == Long.class ||
                field.getType() == Boolean.class ||
                field.getType() == Double.class ||
                field.getType() == Float.class;
    }

    public static Class<?> getArrayListElementType(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length > 0) {
                return (Class<?>) typeArgs[0];  // Returns Schedule, Student, etc. [web:201]
            }
        }
        return Object.class;  // Fallback
    }
    public static void uploadToCloudinary(File file, ObjectCallBack<String> secureUrl){

        Log.d("Tool(UploadToCloudinary)", "File Path being Uploaded: " + file.getAbsolutePath());

        MediaManager.get()
                .upload(file.getAbsolutePath())
                .option("resource_type", "raw")   // 🔥 THIS IS THE FIX
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {
                        Log.d("Tool(UploadToCloudinary)", "Starting to Upload: " + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d("Tool(UploadToCloudinary)", "Upload Successful!");

                        try {
                            secureUrl.onObjectRetrieved(
                                    (String) resultData.get("secure_url")
                            );
                        } catch (ParseException | InvocationTargetException |
                                 NoSuchMethodException | IllegalAccessException |
                                 InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        String description = error != null ? error.getDescription() : "unknown error";
                        Exception wrapped = new Exception("Cloudinary upload failed: " + description);
                        secureUrl.onError(DatabaseError.fromException(
                                wrapped
                        ));
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        secureUrl.onError(DatabaseError.fromException(
                                new Exception("Cloudinary upload rescheduled: " +
                                        (error != null ? error.getDescription() : "unknown reason"))
                        ));
                    }
                })
                .dispatch();
    }

    public static void uploadToCloudinary(String path){
        MediaManager.get().upload(path).dispatch();
    }

    public static boolean isConnectivityIssue(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketException
                    || current instanceof SocketTimeoutException
                    || current instanceof ConnectException
                    || current instanceof UnknownHostException) {
                return true;
            }
            String message = current.getMessage();
            if (boolOf(message)) {
                String lower = message.toLowerCase();
                if (lower.contains("connection abort")
                        || lower.contains("software caused connection abort")
                        || lower.contains("failed to connect")
                        || lower.contains("unable to resolve host")
                        || lower.contains("network is unreachable")
                        || lower.contains("timeout")
                        || lower.contains("econn")
                        || lower.contains("no route to host")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    public static boolean isConnectivityIssue(DatabaseError error) {
        if (error == null) {
            return false;
        }
        if (isConnectivityIssue(error.toException())) {
            return true;
        }
        String msg = error.getMessage();
        if (boolOf(msg)) {
            String lower = msg.toLowerCase();
            return lower.contains("connection abort")
                    || lower.contains("failed to connect")
                    || lower.contains("unable to resolve host")
                    || lower.contains("network is unreachable")
                    || lower.contains("timeout");
        }
        return false;
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
