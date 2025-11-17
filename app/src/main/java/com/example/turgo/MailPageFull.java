package com.example.turgo;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.turgo.databinding.ActivityMailPageFullBinding;

import java.util.ArrayList;

public class MailPageFull extends AppCompatActivity {

    private ActivityMailPageFullBinding binding;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMailPageFullBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nv_MailPage);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_inbox, R.id.navigation_outbox)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_mail_page_full);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.nvMailPage, navController);

        Intent intent = getIntent();
        this.user = (User) intent.getSerializableExtra(User.SERIALIZE_KEY_CODE);


    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}