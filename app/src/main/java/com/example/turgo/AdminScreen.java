package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

import java.util.HashMap;
import java.util.Map;

public class AdminScreen extends AppCompatActivity {
    Button btn_createCourse, btn_manageUsers, btn_manageTasks, btn_signOut, btn_createRoom, btn_manageCourses;
    ImageView iv_arrowCourse, iv_arrowUsers, iv_arrowTaskRoom;
    LinearLayout headerCourse, headerUser, headerTaskRooms,
                contentCourse, contentUser, contentTaskRooms;
    HashMap<Pair<LinearLayout, LinearLayout>, ImageView> headerContent = new HashMap<>();
    Admin admin;
    FirebaseUser fbUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        fbUser = auth.getCurrentUser();

        btn_createCourse = findViewById(R.id.btn_AS_CreateCourse);
        btn_manageTasks = findViewById(R.id.btn_AS_CreateTask);
        btn_manageUsers = findViewById(R.id.btn_AS_CreateUser);
        btn_signOut = findViewById(R.id.btn_AS_SignOut);
        btn_createRoom = findViewById(R.id.btn_AS_CreateRoom);
        btn_manageCourses = findViewById(R.id.btn_AS_ViewAllCourses);
        setAdminActionsEnabled(false);

        headerCourse = findViewById(R.id.header_course);
        headerTaskRooms = findViewById(R.id.header_task);
        headerUser = findViewById(R.id.header_user);

        contentCourse = findViewById(R.id.content_course);
        contentTaskRooms = findViewById(R.id.content_task);
        contentUser = findViewById(R.id.content_user);

        iv_arrowCourse = findViewById(R.id.iv_AS_arrowCourse);
        iv_arrowTaskRoom = findViewById(R.id.iv_AS_arrowTask);
        iv_arrowUsers = findViewById(R.id.iv_AS_arrowUser);



        headerContent.put(Pair.newInstance(headerCourse, contentCourse), iv_arrowCourse);
        headerContent.put(Pair.newInstance(headerTaskRooms, contentTaskRooms), iv_arrowTaskRoom);
        headerContent.put(Pair.newInstance(headerUser, contentUser), iv_arrowUsers);

        for(Map.Entry<Pair<LinearLayout, LinearLayout>, ImageView> hc: headerContent.entrySet()){
            LinearLayout header = hc.getKey().one;
            LinearLayout content = hc.getKey().two;
            ImageView arrow = hc.getValue();
            header.setOnClickListener(view->{
                if(content.getVisibility() == View.GONE){
                    content.setVisibility(View.VISIBLE);
                    arrow.setRotation(180f);

                }else if(content.getVisibility() == View.VISIBLE){
                    content.setVisibility(View.GONE);
                    arrow.setRotation(0f);
                }
            });
        }

        btn_createCourse.setOnClickListener(v -> {
            if (admin == null) return;
            Intent intent = new Intent(this, CreateCourse.class);
            intent.putExtra(Admin.SERIALIZE_KEY_CODE, admin);
            startActivity(intent);

        });
        btn_createRoom.setOnClickListener(v->{
            Intent intent = new Intent(this, AdminCreateRoom.class);
            startActivity(intent);
        });
        btn_manageCourses.setOnClickListener(v->{
            Intent intent = new Intent(this, AdminViewAllCourses.class);
            startActivity(intent);
        });

        btn_signOut.setOnClickListener(v -> Tool.logOutUI(this, fbUser));

        if (fbUser == null) {
            Log.e("AdminScreen", "Firebase user is null");
            finish();
            return;
        }

        AdminRepository ar = new AdminRepository(fbUser.getUid());
        Tool.prepareUserObjectForScreen(ar)
                .addOnSuccessListener(adminObject -> {
                    admin = adminObject;
                    setAdminActionsEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminScreen", "Failed to prepare Admin object", e);
                    finish();
                });
    }

    private void setAdminActionsEnabled(boolean enabled) {
        if (btn_createCourse != null) btn_createCourse.setEnabled(enabled);
        if (btn_manageUsers != null) btn_manageUsers.setEnabled(enabled);
        if (btn_manageTasks != null) btn_manageTasks.setEnabled(enabled);
        if (btn_createRoom != null) btn_createRoom.setEnabled(enabled);
        if (btn_manageCourses != null) btn_manageCourses.setEnabled(enabled);
    }
}
