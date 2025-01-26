package com.example.turgo;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.turgo.databinding.ActivityStudentScreenBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

public class StudentScreen extends AppCompatActivity {

private ActivityStudentScreenBinding binding;
private Student student;
private FirebaseUser fbUser;
private NavHostFragment navHostFragment;
private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStudentScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navView = findViewById(R.id.nv_BottomNavigation);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dest_studentDashboard, R.id.dest_studentMyCourses, R.id.dest_studentExploreCourses, R.id.dest_studentProfile)
                .build();
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        FirebaseApp.initializeApp(this);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        fbUser = auth.getCurrentUser();

        student = (Student) getIntent().getSerializableExtra("Student Object");
        if(student == null){
            User.getUserDataFromDB(fbUser.getUid(), new UserCallback() {
                @Override
                public void onUserRetrieved(User user) {
                    Log.d("Firebase", "Retrieved User: " + user.toString());
                    student = (Student) user;
                }

                @Override
                public void onError(DatabaseError error) {
                    Log.e("Firebase", "Error retrieving user: " + error.getMessage());
                }
            });

        }
    }

}