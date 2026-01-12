package com.example.turgo;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.google.firebase.database.DatabaseError;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

public class CreateCourse extends AppCompatActivity {
    protected String courseName, courseDescription,  courseIconCloudinary, courseBannerCloudinary;
    protected CourseType courseType;
    Uri courseIcon, courseBanner = null;
    ArrayList<Uri>courseImages = new ArrayList<>();
    ArrayList<String> courseImagesCloudinary = new ArrayList<>();

    protected Teacher teacher;
    protected ArrayList<DayTimeArrangement> dtas = new ArrayList<>();
    protected int hourlyCost, baseCost, monthlyDiscount;
    protected boolean [] groupPrivate, acceptedPaymentMethods;
    protected boolean autoAcceptStudent;
    cc_AddScheduleDTA as = new cc_AddScheduleDTA();
    cc_CourseInformation ci = new cc_CourseInformation();
    cc_Media m = new cc_Media();
    cc_SetTeacher st = new cc_SetTeacher();
    cc_PriceEnrollment pe = new cc_PriceEnrollment();
    Fragment[] phases = {ci, st, as, pe, m};
    private boolean[] fragmentCompletion = new boolean[phases.length];
    Course course = new Course();
    FragmentContainerView fcv_phases;
    Button btn_next, btn_prev;
    int currentPhase;
    Admin admin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        groupPrivate = new boolean[2];
        acceptedPaymentMethods = new boolean[2];
        setContentView(R.layout.activity_create_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        admin = (Admin) getIntent().getSerializableExtra(Admin.SERIALIZE_KEY_CODE);
        currentPhase = 0;
        fcv_phases = findViewById(R.id.fcv_CreateCoursePhases);
        btn_next = findViewById(R.id.btn_CC_next);
        btn_prev = findViewById(R.id.btn_CC_back);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fcv_CreateCoursePhases, phases[0])
                .commit();
        if(currentPhase == 0){
            btn_prev.setVisibility(View.GONE);
        }

        btn_next.setOnClickListener(view -> {
            try {
                next();
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                     InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        btn_prev.setOnClickListener(view -> back());
    }
    @SuppressLint("SetTextI18n")
    private void next() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, IOException {
        Log.d("CreateCourse", "CurrentPhase: " + currentPhase);
        if(currentPhase != phases.length-1){
            if(((checkFragmentCompletion)phases[currentPhase]).checkIfCompleted()){
                fragmentCompletion[currentPhase] = true;
                currentPhase++;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fcv_CreateCoursePhases, phases[currentPhase])
                        .commit();
                if(currentPhase == phases.length-1){
                    btn_next.setText("Done");
                }
                if(currentPhase!= 0){
                    btn_prev.setVisibility(View.VISIBLE);
                }
            }

        }else{
            if(((checkFragmentCompletion)phases[currentPhase]).checkIfCompleted()) {
                fragmentCompletion[currentPhase] = true;
                createCourse();
            }
        }
    }
    public void back(){
        Log.d("CreateCourse", "CurrentPhase: " + currentPhase);
        if(currentPhase != 0){
            currentPhase--;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fcv_CreateCoursePhases, phases[currentPhase])
                    .commit();
            if(currentPhase == 0){
                btn_prev.setVisibility(View.GONE);
            }
        }
    }
    private void createCourse() {
        if (courseName.isEmpty() || courseDescription.isEmpty() || courseType == null) {
            Toast.makeText(this, "Please fill in all required course information", Toast.LENGTH_LONG).show();
            return;
        }

        boolean allCompleted = true;
        for (boolean stateOfCompletion : fragmentCompletion) {
            if (!stateOfCompletion) {
                allCompleted = false;
                break;
            }
        }
        Log.d("CreateCourse", "Fragment Completion Status " + Arrays.toString(fragmentCompletion));

        if (!allCompleted) {
            Toast.makeText(this, "Please complete all sections", Toast.LENGTH_LONG).show();
            return;
        }

        // Create custom progress dialog
        UploadProgressDialog progressDialog = new UploadProgressDialog(this);
        progressDialog.setTitle("Creating Course");
        progressDialog.setMessage("Preparing upload...");
        progressDialog.setProgress(0);
        progressDialog.show();

        // Calculate total items to upload
        int totalItems = 0;
        if (courseIcon != null) totalItems++; //only count ones who havent been uplaoded
        if (courseBanner != null) totalItems++;
        if (courseImages != null ) totalItems += courseImages.size();
        totalItems++; // Add 1 for saving to database

        final int totalUploads = totalItems;
        final int[] completedItems = {0};

        // Upload everything in a background thread
        new Thread(() -> {
            try {
                // Helper method to update progress
                Runnable updateProgress = () -> {
                    completedItems[0]++;
                    int progress = (completedItems[0] * 100) / totalUploads;
                    runOnUiThread(() -> progressDialog.setProgress(progress));
                };

                // Upload icon (if exists)
                if (courseIcon != null) {
                    runOnUiThread(() -> progressDialog.setMessage("Uploading course icon..."));

                    final String[] iconUrl = {null};
                    final boolean[] iconDone = {false};

                    Tool.uploadToCloudinary(Tool.uriToFile(courseIcon, this), new ObjectCallBack<String>() {
                        @Override
                        public void onObjectRetrieved(String object) {
                            iconUrl[0] = object;
                            iconDone[0] = true;
                        }

                        @Override
                        public void onError(DatabaseError error) {
                            iconDone[0] = true;
                            Log.e("CreateCourse", "Icon upload failed: " + error);
                        }
                    });

                    // Wait for upload to complete
                    while (!iconDone[0]) {
                        Thread.sleep(100);
                    }

                    if (iconUrl[0] != null) {
                        courseIconCloudinary = iconUrl[0];
                    }

                    updateProgress.run();
                }

                // Upload banner (if exists)
                if (courseBanner != null) {
                    runOnUiThread(() -> progressDialog.setMessage("Uploading course banner..."));

                    final String[] bannerUrl = {null};
                    final boolean[] bannerDone = {false};

                    Tool.uploadToCloudinary(Tool.uriToFile(courseBanner, this), new ObjectCallBack<String>() {
                        @Override
                        public void onObjectRetrieved(String object) {
                            bannerUrl[0] = object;
                            bannerDone[0] = true;
                        }

                        @Override
                        public void onError(DatabaseError error) {
                            bannerDone[0] = true;
                            Log.e("CreateCourse", "Banner upload failed: " + error);
                        }
                    });

                    while (!bannerDone[0]) {
                        Thread.sleep(100);
                    }

                    if (bannerUrl[0] != null) {
                        courseBannerCloudinary = bannerUrl[0];
                    }

                    updateProgress.run();
                }

                // Upload course images (if any)
                if (courseImages != null && !courseImages.isEmpty()) {
                    ArrayList<String> uploadedImages = new ArrayList<>();

                    for (int i = 0; i < courseImages.size(); i++) {
                        Uri uri = courseImages.get(i);
                        final int imageNum = i + 1;

                        runOnUiThread(() -> progressDialog.setMessage(
                                "Uploading image " + imageNum + "/" + courseImages.size() + "..."));

                        final String[] imageUrl = {null};
                        final boolean[] imageDone = {false};

                        Tool.uploadToCloudinary(Tool.uriToFile(uri, this), new ObjectCallBack<String>() {
                            @Override
                            public void onObjectRetrieved(String object) {
                                imageUrl[0] = object;
                                imageDone[0] = true;
                            }

                            @Override
                            public void onError(DatabaseError error) {
                                imageDone[0] = true;
                                Log.e("CreateCourse", "Image upload failed: " + error);
                            }
                        });

                        while (!imageDone[0]) {
                            Thread.sleep(100);
                        }

                        if (imageUrl[0] != null) {
                            uploadedImages.add(imageUrl[0]);
                        }

                        updateProgress.run();
                    }

                    this.courseImagesCloudinary = uploadedImages;
                }

                // All uploads done - save to database
                runOnUiThread(() -> {
                    try {
                        progressDialog.setMessage("Saving course to database...");

                        course.setCourseName(courseName);
                        course.setCourseDescription(courseDescription);
                        course.setCourseType(courseType);
                        course.setBaseCost(baseCost);
                        course.setHourlyCost(hourlyCost);
                        course.setMonthlyDiscountPercentage(monthlyDiscount);
                        course.setAutoAcceptStudent(autoAcceptStudent);
                        ArrayList<Boolean> paymentMethodsArray = new ArrayList<>();
                        for(boolean apm : acceptedPaymentMethods){
                            paymentMethodsArray.add(apm);
                        }
                        Log.d("CreateCourse", "PaymentMethods Array" + paymentMethodsArray);
                        ArrayList<Boolean>groupPrivateArray = new ArrayList<>();
                        for(boolean gp : groupPrivate){
                            groupPrivateArray.add(gp);
                        }
                        Log.d("CreateCourse", "GroupPrivate Array:" + groupPrivateArray);
                        course.setPaymentPer(paymentMethodsArray);
                        course.setPrivateGroup(groupPrivateArray);
                        course.setDayTimeArrangement(dtas);
                        course.setLogo(courseIconCloudinary);
                        course.setBackground(courseBannerCloudinary);
                        course.setImagesCloudinary(this.courseImagesCloudinary);

                        teacher.addCourse(course);
                        teacher.updateDB();
                        Log.d("CreateCourse", "Course Created Object: " + course);
                        course.updateDB();

                        updateProgress.run();
                        progressDialog.setProgress(100);

                        // Dismiss after a short delay to show 100%
                        new android.os.Handler().postDelayed(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Course created successfully!", Toast.LENGTH_LONG).show();
                            finish();
                        }, 500);

                    } catch (Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Error saving course: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("CreateCourse", "Error", e);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("CreateCourse", "Upload error", e);
                });
            }
        }).start();
    }

}