package com.example.turgo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.turgo.databinding.ActivityMailPageFullBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MailPageFull extends AppCompatActivity {

    FloatingActionButton fab_WriteMail;
    BottomNavigationView nv_MailPage;
    TextView tv_pageTitle;
    private ActivityMailPageFullBinding binding;
    private User user;
    private Toolbar toolbar;
    private LinearLayout ll_back;
    boolean inSelectionMode;
    MailSmallAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMailPageFullBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        toolbar = findViewById(R.id.tb_MailTopBar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        setSelectionMode(false);
        ll_back = findViewById(R.id.ll_mpf_goBack);
        tv_pageTitle = findViewById(R.id.tv_mpf_FragmentTitle);

        nv_MailPage = findViewById(R.id.nv_MailPage);

        Intent intent = getIntent();
        this.user = (User) intent.getSerializableExtra(User.SERIALIZE_KEY_CODE);
        MailType whichPage = (MailType) intent.getSerializableExtra("PageToOpen");

        fab_WriteMail = findViewById(R.id.fab_WriteMail);
        fab_WriteMail.setOnClickListener(view -> {
            Intent intent1 = new Intent(MailPageFull.this, ComposeMail.class);
            intent1.putExtra(User.SERIALIZE_KEY_CODE, user);
            startActivity(intent1);
        });

        ll_back.setOnClickListener(v -> {
            Intent i = null;
            if(user instanceof Student){
                i = new Intent(this, StudentScreen.class);
            }else if(user instanceof Teacher){
                i = new Intent(this, TeacherScreen.class);
            }
            if (i != null) {
                startActivity(i);
            } else {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        setupBottomNavigation(nv_MailPage);

        if (whichPage != null) {
            Log.d("MailPageFull", "Which page: " + whichPage.getMailType());
            Bundle bundle = new Bundle();
            bundle.putSerializable(MailType.MAIL_TYPE.getMailType(), whichPage);
            Tool.loadFragment(this, R.id.fcv_MPF_Container, new MailListPage(), bundle);
            if (whichPage == MailType.DRAFT) {
                nv_MailPage.setSelectedItemId(R.id.dest_mailDrafts);
            } else if (whichPage == MailType.OUTBOX) {
                nv_MailPage.setSelectedItemId(R.id.dest_mailOutbox);
            } else {
                nv_MailPage.setSelectedItemId(R.id.dest_mailInbox);
            }
            return;
        }
        nv_MailPage.setSelectedItemId(R.id.dest_mailInbox);
    }

    private void setupBottomNavigation(BottomNavigationView bnv){
        bnv.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Bundle bundle = new Bundle();

            Log.d("BottomNav", "Navigation clicked: " + itemId);
            final String mt = MailType.MAIL_TYPE.getMailType();
            if(itemId == R.id.dest_mailInbox){
                bundle.putSerializable(mt, MailType.INBOX);
            }else if(itemId == R.id.dest_mailDrafts){
                bundle.putSerializable(mt, MailType.DRAFT);
            }else if(itemId == R.id.dest_mailOutbox){
                bundle.putSerializable(mt, MailType.OUTBOX);
            }else{
                Log.w("BottomNav", "Unknown item clicked: " + itemId);
                return false;
            }

            MailListPage page = new MailListPage();
            page.setArguments(bundle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(getContainer(), page)
                    .setReorderingAllowed(true)
                    .commit();

            return true;
        });
    }

    public static int getContainer(){
        return R.id.fcv_MPF_Container;
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
            if (adapter != null) {
                adapter.exitSelectMode();
            }
            setSelectionMode(false);
            return true;
        }else if(item.getItemId() == R.id.mi_MTM_Delete){
            if (adapter != null) {
                adapter.deleteSelectedMails();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setSelectionMode(boolean enabled) {
        inSelectionMode = enabled;

        if (getSupportActionBar() != null) {
            if (enabled) {
                getSupportActionBar().setTitle("Select");
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            } else {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        invalidateOptionsMenu();
    }




    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAdapter(MailSmallAdapter adapter) {
        this.adapter = adapter;
    }
}
