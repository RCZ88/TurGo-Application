package com.example.turgo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

public class AdminScreen extends AppCompatActivity {
    Button btn_createCourse, btn_manageUsers, btn_manageTasks, btn_signOut;
    Admin admin;
    FirebaseUser fbUser;

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

        Admin dummy = new Admin();
        Log.d("AdminScreen", dummy.toString());
        dummy.setUserType(UserType.ADMIN);
        Tool.prepareUserObjectForScreen(this, dummy , new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(User object) {
                admin = (Admin) object;
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });

        btn_createCourse = findViewById(R.id.btn_AS_CreateCourse);
        btn_manageTasks = findViewById(R.id.btn_AS_CreateTask);
        btn_manageUsers = findViewById(R.id.btn_AS_CreateUser);
        btn_signOut = findViewById(R.id.btn_AS_SignOut);

        btn_createCourse.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateCourse.class);
            intent.putExtra(Admin.SERIALIZE_KEY_CODE, admin);
            startActivity(intent);

        });
        btn_signOut.setOnClickListener(v -> {
            Tool.logOutUI(this, fbUser);
        });
    }
}