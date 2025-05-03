package com.example.turgo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

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
    private Button btn_dropDownMail;

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
            User.getUserDataFromDB(fbUser.getUid(), new ObjectCallBack<User>() {
                @Override
                public void onObjectRetrieved(User user) {
                    Log.d("Firebase", "Retrieved User: " + user.toString());
                    student = (Student) user;
                }

                @Override
                public void onError(DatabaseError error) {
                    Log.e("Firebase", "Error retrieving user: " + error.getMessage());
                }
            });
            String fragmentToShow = getIntent().getStringExtra("ShowFragment");
            if(fragmentToShow.equals("CourseJoinedFullPage")){
                Bundle bundle = new Bundle();
                Intent intent = new Intent();
                Course course = (Course)intent.getSerializableExtra("CourseJoined");
                bundle.putSerializable("Course", course);
                CourseJoinedFullPage cjfp = new CourseJoinedFullPage();
                cjfp.setArguments(bundle);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, cjfp)
                        .addToBackStack(null)
                        .commit();
            }
        }
        btn_dropDownMail.setOnClickListener(view -> {
            View popDownView = getLayoutInflater().inflate(R.layout.mail_drop_down, null);

            PopupWindow popupWindow = new PopupWindow(popDownView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            RecyclerView rv_MailDropDown = popDownView.findViewById(R.id.rv_MailDropDown);
            MailSmallAdapter mailSmallAdapter = new MailSmallAdapter(student.getInbox());
            rv_MailDropDown.setAdapter(mailSmallAdapter);

            popupWindow.showAsDropDown(btn_dropDownMail);

            Button seeAll = findViewById(R.id.btn_mailDropDown);
            seeAll.setOnClickListener(view1 -> {

            });
        });
    }

    public ActivityStudentScreenBinding getBinding() {
        return binding;
    }

    public void setBinding(ActivityStudentScreenBinding binding) {
        this.binding = binding;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public FirebaseUser getFbUser() {
        return fbUser;
    }

    public void setFbUser(FirebaseUser fbUser) {
        this.fbUser = fbUser;
    }

    public NavHostFragment getNavHostFragment() {
        return navHostFragment;
    }

    public void setNavHostFragment(NavHostFragment navHostFragment) {
        this.navHostFragment = navHostFragment;
    }

    public BottomNavigationView getNavView() {
        return navView;
    }

    public void setNavView(BottomNavigationView navView) {
        this.navView = navView;
    }
}