package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

public class MainActivity extends AppCompatActivity {
//    TextView tv_userDisplay, tv_userDetails;
    FirebaseUser currentUser;   
    User userFound;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
//        btn_SignUp = findViewById(R.id.btn_ToSignUp);
//        btn_SignIn = findViewById(R.id.btn_ToSignIn);
//        btn_SignOut = findViewById(R.id.btn_SignOut);
//        tv_userDisplay = findViewById(R.id.tv_User);
//        tv_userDetails = findViewById(R.id.tv_UserString);
//        iv_qrCode = findViewById(R.id.iv_GeneratedQR);

        FirebaseApp.initializeApp(this);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();


        if(hasUser()){
            Log.d("FireBase User", "Firebase User Found");
//            tv_userDisplay.setText(currentUser.getDisplayName());
            userFound = (User) getIntent().getSerializableExtra("User Object");
            if(userFound == null){
                User.getUserDataFromDB(currentUser.getUid(), new ObjectCallBack<User>() {
                    @Override
                    public void onObjectRetrieved(User object) {
                        Log.d("Firebase", "Retrieved User: " + object.toString());
                        userFound = object;
                    }

                    @Override
                    public void onError(DatabaseError error) {
                        Log.e("Firebase", "Error retrieving user: " + error.getMessage());
                    }
                });

            }

        }else{
            Log.d("Auth User", "User Not found!");
//            tv_userDisplay.setText("Guest");
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ConstraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
//    public void generateQR(View view){
//        iv_qrCode.setImageBitmap(QRCmanager.generateQR(currentUser.getUid()));
//    }
    public void toSignUpActivity(View view){
        Intent i = new Intent(this, SignUpPage.class);
        startActivity(i);
    }
    public void toSignInActivity(View view){
        Intent i = new Intent(this, SignInPage.class);
        startActivity(i);
    }
    public void signOut(View view) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // 1. Sign out from Firebase
        mAuth.signOut();

        // 2. Force token refresh (this helps clear cached auth state)
        mAuth.getAccessToken(true);

        // 3. Clear all stored preferences
        clearAuthPreferences();

        // 4. Clear local variables
        currentUser = null;
        userFound = null;

        // 5. Optional: Revoke access
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Auth", "User account deleted.");
                        }
                    });
        }

        // 6. Refresh the activity
        refreshActivity();
    }

    private void clearAuthPreferences() {
        // Clear Firebase Auth preferences
        getSharedPreferences("com.google.firebase.auth", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // Clear WebView data if you're using OAuth
        android.webkit.CookieManager.getInstance().removeAllCookies(null);
        android.webkit.CookieManager.getInstance().flush();

        if (getApplicationContext() != null) {
            android.webkit.WebView webView = new android.webkit.WebView(getApplicationContext());
            webView.clearCache(true);
            webView.clearHistory();
            webView.clearFormData();
        }
    }
    @SuppressLint("UnsafeIntentLaunch")
    public void refreshActivity(){
        Intent i = getIntent();
        finish();
        startActivity(i);
    }
    private void updateUser(){
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Get the current user
        this.currentUser = auth.getCurrentUser();
    }
    private boolean hasUser(){
        return currentUser != null;
    }

}