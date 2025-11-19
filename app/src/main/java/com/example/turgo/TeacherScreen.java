package com.example.turgo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turgo.databinding.ActivityTeacherScreenBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class TeacherScreen extends AppCompatActivity {
    private Teacher teacher;
    private TeacherFirebase teacherFB;
    private ActivityTeacherScreenBinding binding;
    private MailSmallAdapter mailSmallAdapter;
    private NotifAdapter notifAdapter;
    private ArrayList<NotificationFirebase> notifs = new ArrayList<>();
    private ArrayList<Mail> inbox = new ArrayList<>();
    private Toolbar topAppBar;
    NavHostFragment navHostFragment;
    BottomNavigationView navView;
    FirebaseUser fbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeacherScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        navView = findViewById(R.id.nv_ts_BottomNavigation);

        AppBarConfiguration config = new AppBarConfiguration.Builder(R.id.dest_teacherDashbaord, R.id.dest_teacherCreateTask, R.id.dest_teacherAllCourse, R.id.dest_teacherViewSchedules, R.id.dest_teacherProfile).build();
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, config);
        NavigationUI.setupWithNavController(navView, navController);

        teacherFB = (TeacherFirebase) getIntent().getSerializableExtra(Teacher.getSerializeKeyCode());
        try {
            assert teacherFB != null;
            teacher = teacherFB.convertToNormal();
        } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        FirebaseApp.initializeApp(this);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        fbUser = auth.getCurrentUser();
        if(teacher != null){
            String fragmentToShow = getIntent().getStringExtra("ShowFragment");
            if(fragmentToShow.equals(PageNames.STUDENT_DASHBOARD.getPageName())){
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nhf_ts_FragmentContainer, new TeacherDashboard())
                        .addToBackStack(null)
                        .commit();
            }
        }
        enableUserSync();
        prepareObjects();
        topAppBar = findViewById(R.id.tb_ts_topAppBar);
        topAppBar.setTitle(teacher.getFullName());
        setSupportActionBar(topAppBar);
        if(teacher != null){
            UserPresenceManager.startTracking(fbUser.getUid());
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void enableUserSync(){
        RequireUpdate.retrieveUser(teacher.getID(), new TeacherFirebase[]{teacherFB});
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId() == R.id.action_mail){
            View popDownView = getLayoutInflater().inflate(R.layout.mail_drop_down, null);

            PopupWindow popupWindow = new PopupWindow(popDownView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            RecyclerView rv_MailDropDown = popDownView.findViewById(R.id.rv_MailDropDown);
            mailSmallAdapter = new MailSmallAdapter(fbUser.getUid(), inbox, false);
            rv_MailDropDown.setAdapter(mailSmallAdapter);


            View anchor = findViewById(R.id.action_mail);

            if(anchor != null){
                popupWindow.showAsDropDown(anchor, -50, 10);
            }else{
                Log.e("Dropdown Error", "No Anchor Found!(Null)");
            }

            Button seeAll = popDownView.findViewById(R.id.btn_ViewAllMail);
            seeAll.setOnClickListener(view1 -> {
                Intent intent = new Intent(TeacherScreen.this, MailPageFull.class);
                startActivity(intent);
            });
            return true;
        }else if(item.getItemId() == R.id.action_notifications){
            View popDownView = getLayoutInflater().inflate(R.layout.notification_drop_down, null);

            PopupWindow popupWindow = new PopupWindow(popDownView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            RecyclerView rv_NotifDropDwon = popDownView.findViewById(R.id.rv_NotifDropDown);
            notifAdapter = new NotifAdapter(notifs);
            rv_NotifDropDwon.setAdapter(notifAdapter);

            View anchor = findViewById(R.id.action_notifications);

            if(anchor != null) {
                popupWindow.showAsDropDown(anchor, -50, 10);
            }else{
                Log.e("Dropdown Error", "No Anchor Found!");
            }
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }
    public void prepareObjects(){
        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.STUDENT.getPath()).child(teacher.getID());
        studentRef.child("inboxIDs").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String mailID = snapshot.getValue(String.class);
                Mail m = new Mail();
                m.retrieveOnce(new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(MailFirebase object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                        inbox.add(object.convertToNormal());
                        //updates the UI and the adapter in general
                        mailSmallAdapter.addMail(object.convertToNormal());
                    }

                    @Override
                    public void onError(DatabaseError error) {

                    }
                }, mailID);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        studentRef.child("notitficationIDs").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String notifID = snapshot.getValue(String.class);
                Notification<?> n = new Notification<>();
                n.retrieveOnce(new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(NotificationFirebase object) {
                        notifs.add(object);
                        notifAdapter.addNotification(object);
                    }

                    @Override
                    public void onError(DatabaseError error) {

                    }
                }, notifID);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public Teacher getTeacher() {
        return teacher;
    }
}