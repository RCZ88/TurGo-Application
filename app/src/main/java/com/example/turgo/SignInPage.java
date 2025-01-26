package com.example.turgo;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignInPage extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 892;
    private static final String TAG = "SIGNINPAGE";

    EditText et_emailOrUsername, et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in_page);
        mAuth = FirebaseAuth.getInstance();

        et_emailOrUsername = findViewById(R.id.pt_EmailOrUsername);
        et_password = findViewById(R.id.pt_Password);
        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Use your web client ID from Firebase
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ConstraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void signInWGoogle(View view){
        if (!isGooglePlayServicesAvailable()) {
            Toast.makeText(this, "Google Play Services not available", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("Google Sign-In", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkIfUserExists(user); // Check if user exists in the database
                    } else {
                        // Sign in failed
                        Log.w("Firebase", "signInWithCredential:failure", task.getException());
                    }
                });
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000).show();
            }
            return false;
        }
        return true;
    }

    private void checkIfUserExists(FirebaseUser firebaseUser) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.enableNetwork().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                try{
                    Log.d("Firestore", "Network enabled.");
                    db.collection("Users").document(firebaseUser.getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // User already exists
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String uid = user.getUid();
                                    selectUserFromDB(uid, user);
                                } else {
                                    // User does not exist
                                    Intent intent = new Intent(SignInPage.this, SignUpPage.class);
                                    intent.putExtra("userId", firebaseUser.getUid());
                                    intent.putExtra("email", firebaseUser.getEmail());
                                    intent.putExtra("name", firebaseUser.getDisplayName());
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(e -> Log.w("Firestore", "Error checking user existence", e));
                }catch(SecurityException se){
                    Log.e("SecurityException", "Error communicating with Google Play Services", se);
                }
            } else {
                Log.e("Firestore", "Failed to enable network", task.getException());
                Toast.makeText(this, "Unable to connect to Firestore. Please check your internet connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateUI(FirebaseUser user, User userData) {
        if (user != null) {
            // User signed in successfully
            Log.d("Firebase User", "User: " + user.getDisplayName() + ", Email: " + user.getEmail());
            Intent i = null;
            switch(userData.getType()){
                case "Student":
                    i = new Intent(SignInPage.this, StudentScreen.class);
                    i.putExtra(Student.SERIALIZE_INTENT_CODE, user);
                    startActivity(i);
                    finish();
                    break;
                case "Teacher":
                case "Admin":
                case "Parent":
            }

            assert i != null;
            i.putExtra("User Object", userData);
            startActivity(i);
        } else {
            // User sign-in failed
            Log.d("Firebase User", "Sign-in failed.");
        }
    }
    public void signIn(View view) {
        String email = "";
        String password = "";
        try {
            email = et_emailOrUsername.getText().toString();
            password = et_password.getText().toString();
            Log.d("Email Inputted", "Email: " + email);
            Log.d("Password Inputted", "Password: " + password);
        } catch (Exception e) {
            Log.d("Error", "Field is not filled Properly!");
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user != null){
                            String uid = user.getUid();
                            selectUserFromDB(uid, user);
                        }

                    } else {
                        // If sign in fails, display a message to the user.
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // Handle invalid credentials
                            Toast.makeText(SignInPage.this, "Invalid credentials. Please check your email/password.", Toast.LENGTH_SHORT).show();
                        } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            // Handle user not found
                            Toast.makeText(SignInPage.this, "User not found. Please sign up first.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Other errors
                            Toast.makeText(SignInPage.this, "Authentication failed. Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void selectUserFromDB(String uid, FirebaseUser FireUser) {
        User.getUserDataFromDB(uid, new UserCallback() {
            @Override
            public void onUserRetrieved(User user) {
                Log.d("Firebase", "Retrieved User: " + user.toString());
                updateUI(FireUser ,user);
            }

            @Override
            public void onError(DatabaseError error) {
                Log.e("Firebase", "Error retrieving user: " + error.getMessage());
            }
        });
    }


}