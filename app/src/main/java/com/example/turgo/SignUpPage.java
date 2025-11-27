package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Objects;


public class SignUpPage extends AppCompatActivity {

    public static final int MODSEGMENT1 = 2;
    public static final int AMOUNTOFSEGMENT = 6;

    public UserType userType;
    View fc_signUpStages;
    RTDBManager<User> rtdbManager = new RTDBManager<>();
    double progress;

    ProgressBar pb_signUpProgress;

    Fragment frag_userCategory, frag_userDetails,
            frag_contactInfo, frag_passwordSetup, frag_confirmSelection;


    Button btn_next, btn_previous;


    private int currentSegmentIndex = 0;
    Fragment[]signUpSegments = new Fragment[AMOUNTOFSEGMENT];
    boolean[]completedSegments = new boolean[AMOUNTOFSEGMENT];
    private static final String USER_COLLECTION= "users";


    @SuppressLint({"MissingInflatedId", "CutPasteId", "CommitTransaction"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_page);

        frag_userCategory = new signup_UserCategory();
        frag_userDetails = new signup_userdetails();
        frag_passwordSetup = new signup_PasswordSetup();
        frag_contactInfo = new signup_ContactInformation();
        frag_confirmSelection = new signup_ConfirmData();

        pb_signUpProgress = findViewById(R.id.pb_SignUpProgress);
        pb_signUpProgress.setMax(AMOUNTOFSEGMENT);

        fc_signUpStages = findViewById(R.id.fcv_UserInfo);
        if (fc_signUpStages == null) {
            Log.e("SignUpPage", "FragmentContainerView (fcv_UserInfo) is NULL");
        } else {
            Log.d("SignUpPage", "FragmentContainerView (fcv_UserInfo) initialized successfully");
        }
        btn_next = findViewById(R.id.btn_NextPage);
        btn_previous = findViewById(R.id.btn_PreviousPage);
        btn_previous.setEnabled(false);

        signUpSegments[0] = frag_userCategory;
        signUpSegments[1] = frag_userDetails;
        signUpSegments[2] = new signup__student_selectcourse();
        signUpSegments[3] = frag_contactInfo;
        signUpSegments[4] = frag_passwordSetup;
        signUpSegments[5] = frag_confirmSelection;
        completedSegments[5] = true;
        updateFragment();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ConstraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void updateProgressBar(){
        pb_signUpProgress.setProgress(currentSegmentIndex);
    }

    public void goToNextSegment(View view){
        completedSegments[currentSegmentIndex] = ((checkFragmentCompletion)signUpSegments[currentSegmentIndex]).checkIfCompleted();
        boolean condition1 = currentSegmentIndex < signUpSegments.length;
        boolean condition2 = completedSegments[currentSegmentIndex];
        if(condition1 && condition2){
            currentSegmentIndex++;
            updateProgressBar();
            updateFragment();
        }else{
            Log.e("Next Segment Button", "current Segment is less then segmentIndex: " + condition1 + "current segment Completed: " + condition2);
        }
    }
    public void goToPreviousSegment(View view){
        if(currentSegmentIndex > 0){
            currentSegmentIndex--;
            updateProgressBar();
            updateFragment();
        }
    }

    private void updateFragment(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(fc_signUpStages.getId(), signUpSegments[currentSegmentIndex])
                .commit();
        btn_next.setEnabled(currentSegmentIndex < signUpSegments.length);
        btn_previous.setEnabled(currentSegmentIndex > 0);
    }

    public User createUser(){
        Log.d("Completed Segments", Arrays.toString(completedSegments));
        for(boolean completedSegment : completedSegments){
            if(!completedSegment){
                return null;
            }
        }

        User newUser;
        signup_userdetails fud = ((signup_userdetails)signUpSegments[1]);
        signup_ContactInformation fci = (signup_ContactInformation)signUpSegments[3];

        try {
            String fullName = fud.et_fullName.getText().toString();
            String dateOfBirth = fud.et_dateOfBirth.getText().toString().trim();
            String nickname = fud.et_nickname.getText().toString();
            String email = fci.et_email.getText().toString();
            String phoneNumber = fci.et_phoneNumber.getText().toString();
            String gender = fud.gender;
            Log.d("SignUpPage", "Creating user with: " + fullName + ", " + dateOfBirth + ", " + nickname + ", " + email + ", " + phoneNumber + ", " + gender);
            switch (userType) {
                case STUDENT:
                    newUser = new Student(
                            fullName,
                            gender,
                            dateOfBirth,
                            nickname,
                            email,
                            phoneNumber
                    );

                    signup__student_selectcourse fssc = (signup__student_selectcourse) signUpSegments[2];
                    for (String courseType : fssc.selectedCourses) {
                        ((Student) newUser).addCourseInterest(courseType);
                    }
                    break;

                case TEACHER:
                    newUser = new Teacher(
                            fullName,
                            gender,
                            dateOfBirth,
                            nickname,
                            email,
                            phoneNumber
                    );
                    Log.d("SignUpPage", "Teacher object" + newUser);

                    signup_UserDetails_Teacher fudf = (signup_UserDetails_Teacher) signUpSegments[2];
                    for (String subjectType : fudf.selectedSubjects) {
                        ((Teacher) newUser).addCourseTeach(subjectType);
                    }
                    Log.d("SignUpPage", "Subject Selected: " + fudf.selectedSubjects.toString());


                    break;

                case PARENT:
                    newUser = new Parent(
                            fullName,
                            gender,
                            dateOfBirth,
                            nickname,
                            email,
                            phoneNumber
                    );

                    signup__parent_connecttochild fpcc = (signup__parent_connecttochild) signUpSegments[2];
                    for (Student child : fpcc.childSelectedStudent) {
                        ((Parent) newUser).addChild(child);
                    }
                    break;

                case ADMIN:
                    newUser = new Admin(
                            fullName,
                            gender,
                            dateOfBirth,
                            nickname,
                            email,
                            phoneNumber
                    );
                    // Additional admin logic if needed
                    break;

                default:
                    throw new IllegalArgumentException("Invalid user type: " + userType);
            }
            return newUser;

        } catch (Exception e) {
            Log.e("SIGNUP", "Error parsing input or creating user: " + e.getMessage());
            Toast.makeText(this, "Invalid input. Please check your fields!", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    public void signUp() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {

// Firebase Authentication and Firestore setup
        FirebaseAuth auth = FirebaseAuth.getInstance();
// Create the user in Firebase Authentication

        signup_ConfirmData confirmFragment = (signup_ConfirmData) signUpSegments[5];
        User newUser = confirmFragment.cachedUser;
        auth.createUserWithEmailAndPassword(newUser.getEmail(), Objects.requireNonNull(((signup_PasswordSetup) frag_passwordSetup).pi_password.getText()).toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            newUser.setUid(uid);
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(newUser.getNickname()) // Set the username
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Log.d("FirebaseAuth", "User profile updated successfully.");
                                        }
                                    });


                            try {
                                if(newUser.getUid() != null){
                                    ObjectManager.ADD_USER(newUser);
                                    Log.d("SignUpPage", "ID User: " + newUser.getUid());
                                }else{
                                    Log.e("SignUpPage", "Error: " + "User ID is null");
                                }
                            } catch (InvocationTargetException | InstantiationException |
                                     IllegalAccessException | NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }

                            Intent i = new Intent(SignUpPage.this, SignInPage.class);
                            finish();
                            startActivity(i);
                        }
                    } else {
                        Log.e("SIGNUP", "Error creating user: " + task.getException());
                        Toast.makeText(this, "Account creation failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}