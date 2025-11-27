package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public class ActivityLauncher extends AppCompatActivity {
    TextView tv_loadingText;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_launcher);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        String[] items = getResources().getStringArray(R.array.course_options);
//        for(String item : items){
//            try {
//                CourseType courseType = new CourseType(item);
//                courseType.updateDB();
//            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
//                     InstantiationException e) {
//                throw new RuntimeException(e);
//            }
//
//        }
        tv_loadingText = findViewById(R.id.tv_AL_LoadingText);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // Not signed in → go to MainActivity (Sign In/Sign Up)
            tv_loadingText.setText("No User, Redirecting...");
            Log.d("ActivityLauncher", "No user signed in, going to MainActivity");
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // Important: finish so back button doesn't come back here
        } else {
            // Signed in → get user data and go to appropriate screen
            tv_loadingText.setText("Loading your Data...");
            Log.d("ActivityLauncher", "User signed in: " + currentUser.getUid());
            loadUserAndNavigate(currentUser.getUid());
        }
    }
    private void loadUserAndNavigate(String userId) {
        // Show loading if you want
        // setContentView(R.layout.activity_splash); // Optional splash screen
        Intent intent = new Intent();
        UserFirebase user = (UserFirebase) intent.getSerializableExtra("FirebaseObject");
        if(user != null){
            handleActivityTransfer(user);
        }else{
            RequireUpdate.retrieveUser(userId, new ObjectCallBack<>() {
                @Override
                public void onObjectRetrieved(Object object) {
                    handleActivityTransfer((UserFirebase)object);
                }

                @Override
                public void onError(DatabaseError error) {
                    Log.e("ActivityLauncher", "Error loading user: " + error.getMessage());
                    // On error, go to MainActivity
                    Intent intent = new Intent(ActivityLauncher.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }


    }
    public void handleActivityTransfer(UserFirebase userFirebase){
        Intent intent = null;

        if (userFirebase instanceof TeacherFirebase) {
            TeacherFirebase teacherFB = (TeacherFirebase) userFirebase;
            intent = new Intent(ActivityLauncher.this, TeacherScreen.class);
            intent.putExtra(Teacher.SERIALIZE_KEY_CODE, userFirebase);
            Log.d("ActivityLauncher", "Navigating to TeacherScreen");

        } else if (userFirebase instanceof StudentFirebase) {
            StudentFirebase studentFB = (StudentFirebase) userFirebase;
            intent = new Intent(ActivityLauncher.this, StudentScreen.class);
            intent.putExtra(Student.SERIALIZE_KEY_CODE, studentFB);
            Log.d("ActivityLauncher", "Navigating to StudentScreen");

        }else if (userFirebase instanceof AdminFirebase) {
            AdminFirebase adminFB = (AdminFirebase) userFirebase;
            intent = new Intent(ActivityLauncher.this, AdminScreen.class);
            intent.putExtra(Admin.SERIALIZE_KEY_CODE, adminFB);
            Log.d("ActivityLauncher", "Navigating to AdminScreen");
        }
        // else if (userFirebase instanceof ParentFirebase) {
//                    ParentFirebase parentFB = (ParentFirebase) userFirebase;
//                        intent = new Intent(ActivityLauncher.this, ParentScreen.class);
//                    intent.putExtra(Parent.SERIALIZE_KEY_CODE, parentFB);
//                    Log.d("ActivityLauncher", "Navigating to ParentScreen");
//


        if (intent != null) {
            startActivity(intent);
            finish(); // Important: finish so back button doesn't come back here
        } else {
            Log.e("ActivityLauncher", "Unknown user type");
            // If unknown, sign out and go to MainActivity
            FirebaseAuth.getInstance().signOut();
            Intent mainIntent = new Intent(ActivityLauncher.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }
}