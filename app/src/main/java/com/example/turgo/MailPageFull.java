package com.example.turgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.turgo.databinding.ActivityMailPageFullBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MailPageFull extends AppCompatActivity {

    FloatingActionButton fab_WriteMail;
    BottomNavigationView nv_MailPage;

    private ActivityMailPageFullBinding binding;
    private User user;
    private Toolbar toolbar;
    boolean inSelectionMode;
    MailSmallAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMailPageFullBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        toolbar = findViewById(R.id.tb_MailTopBar);
        setSupportActionBar(toolbar);
        setSelectionMode(false);

        nv_MailPage = findViewById(R.id.nv_MailPage);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_inbox, R.id.navigation_outbox, R.id.navigation_drafts)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.fcv_MPF_Container);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(nv_MailPage, navController);

        Intent intent = getIntent();
        this.user = (User) intent.getSerializableExtra(User.SERIALIZE_KEY_CODE);

        fab_WriteMail = findViewById(R.id.fab_WriteMail);
        fab_WriteMail.setOnClickListener(view -> {
            Intent intent1 = new Intent(MailPageFull.this, ComposeMail.class);
            intent1.putExtra(User.SERIALIZE_KEY_CODE, user);
            startActivity(intent1);
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mail_topbar_menu, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem backItem = menu.findItem(R.id.mi_MTM_Back);
        MenuItem deleteItem = menu.findItem(R.id.mi_MTM_Delete);

        if (backItem != null) backItem.setVisible(inSelectionMode);
        if (deleteItem != null) deleteItem.setVisible(inSelectionMode);

        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.mi_MTM_Back){
            adapter.exitSelectMode();
            setSelectionMode(false);
            return true;
        }else if(item.getItemId() == R.id.mi_MTM_Delete){
            adapter.deleteSelectedMails();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setSelectionMode(boolean enabled){
        inSelectionMode = enabled;
        if(enabled){
            toolbar.setTitle("Select");
        }else{
            toolbar.setTitle("Mail");
        }
        invalidateMenu();
    }



    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}