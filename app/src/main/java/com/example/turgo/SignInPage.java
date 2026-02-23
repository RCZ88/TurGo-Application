package com.example.turgo;

import android.annotation.SuppressLint;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SignInPage extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 892;
    private static final String TAG = "SIGNINPAGE";
    private boolean isGoogleSignInInProgress = false;

    EditText et_emailOrUsername, et_password;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in_page);
        mAuth = FirebaseAuth.getInstance();

        et_emailOrUsername = findViewById(R.id.pt_EmailOrUsername);
        et_password = findViewById(R.id.tie_SIP_PasswordField);
        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Use your web client ID from Firebase
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cl_SIP_rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void signInWGoogle(View view){
        if (isGoogleSignInInProgress) {
            Toast.makeText(this, "Google sign-in already in progress.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isGooglePlayServicesAvailable()) {
            Toast.makeText(this, "Google Play Services not available", Toast.LENGTH_SHORT).show();
            return;
        }
        isGoogleSignInInProgress = true;
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            isGoogleSignInInProgress = false;
            Log.d(TAG, "Google SignIn activity returned. resultCode=" + resultCode + ", hasData=" + (data != null));
            if (data == null) {
                Log.w(TAG, "Google sign-in returned no data.");
                Toast.makeText(this, "Google sign-in cancelled.", Toast.LENGTH_SHORT).show();
                return;
            }
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account == null || account.getIdToken() == null) {
                    Log.e(TAG, "Google account/idToken is null. account=" + account);
                    Toast.makeText(this, "Google sign-in failed: invalid token.", Toast.LENGTH_LONG).show();
                    return;
                }
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                int code = e.getStatusCode();
                Log.w(TAG, "Google sign in failed. code=" + code, e);
                handleGoogleSignInError(code);
            }
        }
    }

    private void handleGoogleSignInError(int code) {
        if (code == 12501) {
            Toast.makeText(this, "Google sign-in cancelled.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (code == 12502) {
            Toast.makeText(this, "Google sign-in is already running. Please wait and try again.", Toast.LENGTH_LONG).show();
            return;
        }
        if (code == 10) {
            Toast.makeText(this, "Google configuration error (code 10). Check SHA keys and google-services.json.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "DEVELOPER_ERROR (10). Verify package name + SHA-1/SHA-256 in Firebase and download updated google-services.json.");
            return;
        }
        Toast.makeText(this, "Google sign-in failed (" + code + ").", Toast.LENGTH_LONG).show();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            Log.e(TAG, "Firebase auth success but current user is null");
                            Toast.makeText(this, "Sign-in failed: user session missing.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        checkIfUserExists(user); // Check if user exists in the database
                    } else {
                        // Sign in failed
                        Exception e = task.getException();
                        Log.w(TAG, "signInWithCredential:failure", e);
                        Toast.makeText(this, "Firebase login failed: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
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
        String uid = firebaseUser.getUid();
        DatabaseReference roleRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.USER_ID_ROLES.getPath())
                .child(uid);

        roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = null;
                Object rawRoleNode = snapshot.getValue();
                if (rawRoleNode instanceof String) {
                    role = (String) rawRoleNode;
                }
                if (!Tool.boolOf(role) && snapshot.hasChild("role")) {
                    role = snapshot.child("role").getValue(String.class);
                }
                if (Tool.boolOf(role)) {
                    Log.d(TAG, "User role found for uid=" + uid + ", role=" + role);
                    selectUserFromDB(uid, firebaseUser);
                } else {
                    Log.d(TAG, "No existing user role found for uid=" + uid + ". Redirecting to sign-up.");
                    Intent intent = new Intent(SignInPage.this, SignUpPage.class);
                    intent.putExtra("userId", uid);
                    intent.putExtra("email", firebaseUser.getEmail());
                    intent.putExtra("name", firebaseUser.getDisplayName());
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed checking role in RTDB: " + error.getMessage(), error.toException());
                Toast.makeText(SignInPage.this, "Failed to validate account. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void updateUI(FirebaseUser user, UserFirebase userData) {
        if (user != null) {
            // User signed in successfully
            Log.d("Firebase User", "User: " + user.getDisplayName() + ", Email: " + user.getEmail());
            Intent i = new Intent(this, ActivityLauncher.class);
            i.putExtra("FirebaseObject", userData);
            startActivity(i);
            finish();
//            if (userData.getUserType().equals(STUDENT.type())) {
//                i = new Intent(SignInPage.this, StudentScreen.class);
//                i.putExtra(Student.SERIALIZE_KEY_CODE, user);
//                i.putExtra("ShowFragment", PageNames.STUDENT_DASHBOARD);
//                startActivity(i);
//                finish();
//                //TODO: change so it transfers student id instead of its object.
//            }else if(userData.getUserType().equals(TEACHER.type())){
//                i = new Intent(SignInPage.this, TeacherScreen.class);
//                i.putExtra(Teacher.SERIALIZE_KEY_CODE, userData);
//                i.putExtra("ShowFragment", PageNames.TEACHER_DASHBOARD);
//                startActivity(i);
//                finish();
//
//            }
//
//            assert i != null;
//            i.putExtra("User Object", userData);
//            startActivity(i);
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
                        Log.d(TAG, "signInWithEmail& password:success");
                        FirebaseUser user = task.getResult().getUser();
                        if(user != null){
                            String uid = user.getUid();
                            Log.d("Firestore", "User UID from signIn with Password & Email: "+uid);
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
        Log.d("SignInPage(SelectUserFromDB)", "Selecting User: " + uid + " from database.");
        final UserFirebase[] u = {null};
        RequireUpdate.retrieveUser(uid, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(Object object) {
                Log.d("SingInPage(SelectUserFromDB)", "User Found: " + object);
                u[0] = (UserFirebase) object;
                updateUI(FireUser, u[0]);
            }

            @Override
            public void onError(DatabaseError error) {
                Log.d("SingInPage(SelectUserFromDB)", "Error: " + error);

            }
        });

    }

}
