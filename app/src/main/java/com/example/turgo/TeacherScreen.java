package com.example.turgo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.database.ValueEventListener;

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
    private TextView tv_mailEmpty, tv_notifEmpty;
    private PopupWindow mailPopupWindow, notifPopupWindow;
    RecyclerView rv_MailDropDown, rv_NotifDropdown;
    BottomNavigationView navView;
    FirebaseUser fbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTeacherScreenBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_teacher_screen);

        findViewById(android.R.id.content)
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    View container = findViewById(R.id.nhf_ts_FragmentContainer);
                    Log.d("LAYOUT", "Container real size: " +
                            container.getWidth() + " × " + container.getHeight());
                });
//        setContentView(binding.getRoot());

        // Enable EdgeToEdge
//        EdgeToEdge.enable(this);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        // Get UID from intent (handle both old and new Android versions)
//        String teacherUID;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            teacherFB = getIntent().getSerializableExtra(Teacher.SERIALIZE_KEY_CODE, TeacherFirebase.class);
//        } else {
//            teacherFB = (TeacherFirebase) getIntent().getSerializableExtra(Teacher.SERIALIZE_KEY_CODE);
//        }
//        if (teacherFB == null) {
//            Log.e("TeacherScreen", "Retrieved teacher is null");
//            Toast.makeText(TeacherScreen.this, "Error loading teacher data", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }else{
//            Log.d("TeacherScreen", "Retrieved teacher: " + teacherFB);
//        }
//
//        // Convert to normal Teacher object
//        try {
//            teacherFB.convertToNormal(new ObjectCallBack<>() {
//                @Override
//                public void onObjectRetrieved(Teacher object) {
//                    teacher = object;
//                    if(teacher != null){
//                        Log.d("TeacherScreen", "Teacher: " + teacher);
//                        runOnUiThread(() ->{
//
//                            initializeUI(); // set up navigation etc - teacher IS READY!
//                        });
//                    }else{
//                        Log.e("TeacherScreen", "Error converting teacher");
//                    }
//                }
//
//                @Override
//                public void onError(DatabaseError error) {
//                    Log.e("TeacherScreen", "Error converting teacher: " + error.getMessage());
//                }
//            });
//        } catch (ParseException | InvocationTargetException | NoSuchMethodException |
//                 IllegalAccessException | InstantiationException e) {
//            Log.e("TeacherScreen", "Error converting teacher: " + e.getMessage());
//            throw new RuntimeException(e);
        Teacher dummy = new Teacher();
        dummy.setUserType(UserType.TEACHER);
        Log.d("TeacherScreen", "Teacher Obj" + dummy);
//        }
        Tool.prepareUserObjectForScreen(this, dummy, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(User object) {
                teacher = (Teacher) object;
                if (teacher != null) {
                    Log.d("TeacherScreen", "Teacher: " + teacher);
                    runOnUiThread(() -> {
                        initializeUI(); // set up navigation etc - teacher IS READY!
                    });
                } else {
                    Log.e("TeacherScreen", "Error converting teacher");
                }
            }

            @Override
            public void onError(DatabaseError error) {
                Log.e("TeacherScreen", "Error converting teacher: " + error.getMessage());
            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topbar_user_menu, menu);
        return true;
    }

    private void initializeUI() {
        Log.d("TeacherScreen", "========== initializeUI START ==========");
        Log.d("TeacherScreen", "Teacher: " + (teacher != null ? teacher.getFullName() : "NULL"));

        // Setup TOP BAR
        topAppBar = findViewById(R.id.tb_ts_topAppBar);
        Log.d("TeacherScreen", "topAppBar found: " + (topAppBar != null));

        tv_mailEmpty = findViewById(R.id.tv_MDD_MailEmpty);
        tv_notifEmpty = findViewById(R.id.tv_NDD_NotifEmpty);

        setSupportActionBar(topAppBar);
        Log.d("TeacherScreen", "setSupportActionBar done");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(teacher.getFullName());
            Log.d("TeacherScreen", "ActionBar title set to: " + teacher.getFullName());
        } else {
            Log.e("TeacherScreen", "getSupportActionBar() returned NULL!");
        }

        // Setup BOTTOM BAR NAVIGATION
        navView = findViewById(R.id.nv_ts_BottomNavigation);
        Log.d("TeacherScreen", "navView found: " + (navView != null));

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nhf_ts_FragmentContainer);

        Log.d("TeacherScreen", "navHostFragment found: " + (navHostFragment != null));

        if(navHostFragment != null) {
            View navHostView = navHostFragment.getView();
            if (navHostView != null) {
                // Force layout
                navHostView.post(() -> {
                    Log.d("LAYOUT", "Fragment root measured: " +
                            navHostView.getWidth() + " × " + navHostView.getHeight());
                    navHostView.requestLayout();
                    Log.d("TeacherScreen", "Forced NavHostFragment layout");
                });
            }else{
                Log.e("TeacherScreen", "NavHostView is null!");
            }

            NavController navController = navHostFragment.getNavController();
            Log.d("TeacherScreen", "navController obtained: " + (navController != null));

            try {
                Log.d("TeacherScreen", "Setting navigation graph...");
                navController.setGraph(R.navigation.teacher_mobile_navigation);
                Log.d("TeacherScreen", "✓ Navigation graph set successfully");

                Log.d("TeacherScreen", "Current destination: " + navController.getCurrentDestination());

            } catch (Exception e) {
                Log.e("TeacherScreen", "❌ Error setting navigation graph: " + e.getMessage(), e);
            }

            try {
                Log.d("TeacherScreen", "Connecting bottom nav to nav controller...");
                NavigationUI.setupWithNavController(navView, navController);
                Log.d("TeacherScreen", "✓ Bottom nav connected");
            } catch (Exception e) {
                Log.e("TeacherScreen", "❌ Error connecting bottom nav: " + e.getMessage(), e);
            }

        } else {
            Log.e("TeacherScreen", "❌❌❌ NavHostFragment is NULL!");
        }

        // Firebase setup
        FirebaseAuth auth = FirebaseAuth.getInstance();
        fbUser = auth.getCurrentUser();
        Log.d("TeacherScreen", "fbUser: " + (fbUser != null ? fbUser.getUid() : "NULL"));

        prepareObjects();

        if(teacher != null && fbUser != null){
            UserPresenceManager.startTracking(fbUser.getUid());
            Log.d("TeacherScreen", "Started tracking user presence");
        }

        Log.d("TeacherScreen", "========== initializeUI END ==========");
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_mail) {
            // Close notifications popup if open
            if (notifPopupWindow != null && notifPopupWindow.isShowing()) {
                notifPopupWindow.dismiss();
            }

            // Toggle mail popup
            if (mailPopupWindow != null && mailPopupWindow.isShowing()) {
                mailPopupWindow.dismiss();
                return true; // Handled
            }

            // Create and show mail popup
            View popDownView = getLayoutInflater().inflate(R.layout.mail_drop_down, null);
            rv_MailDropDown = popDownView.findViewById(R.id.rv_MailDropDown);
            mailSmallAdapter = new MailSmallAdapter(fbUser.getUid(), inbox, false);
            rv_MailDropDown.setAdapter(mailSmallAdapter);

            mailPopupWindow = new PopupWindow(popDownView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            View anchor = findViewById(R.id.action_mail);
            if (anchor != null) {
                mailPopupWindow.showAsDropDown(anchor, -50, 10);
            } else {
                Log.e("Dropdown Error", "No Anchor Found!(Null)");
            }

            Button seeAll = popDownView.findViewById(R.id.btn_ViewAllMail);
            seeAll.setOnClickListener(view1 -> {
                Intent intent = new Intent(TeacherScreen.this, MailPageFull.class);
                startActivity(intent);
            });

            return true; // Handled

        } else if (itemId == R.id.action_notifications) {
            // Close mail popup if open
            if (mailPopupWindow != null && mailPopupWindow.isShowing()) {
                mailPopupWindow.dismiss();
            }

            // Toggle notif popup
            if (notifPopupWindow != null && notifPopupWindow.isShowing()) {
                notifPopupWindow.dismiss();
                return true; // Handled
            }

            // Create and show notifications popup
            View popDownView = getLayoutInflater().inflate(R.layout.notification_drop_down, null);
            rv_NotifDropdown = popDownView.findViewById(R.id.rv_NotifDropDown);
            notifAdapter = new NotifAdapter(notifs);
            rv_NotifDropdown.setAdapter(notifAdapter);

            notifPopupWindow = new PopupWindow(popDownView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            View anchor = findViewById(R.id.action_notifications);
            if (anchor != null) {
                notifPopupWindow.showAsDropDown(anchor, -50, 10);
            } else {
                Log.e("Dropdown Error", "No Anchor Found!");
            }

            return true; // Handled

        } else if (itemId == R.id.action_signOut) {
            // Dismiss any open popups
            dismissPopups();

            return Tool.logOutUI(this, fbUser);

        } else {
            return super.onOptionsItemSelected(item); // Not handled
        }
    }

    private void dismissPopups() {
        if (mailPopupWindow != null && mailPopupWindow.isShowing()) {
            mailPopupWindow.dismiss();
        }
        if (notifPopupWindow != null && notifPopupWindow.isShowing()) {
            notifPopupWindow.dismiss();
        }
    }

    public void prepareObjects(){
        DatabaseReference teacherRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.TEACHER.getPath()).child(teacher.getID());
//        final boolean[] exist = new boolean[1];
//        Tool.checkIfReferenceExists(teacherRef, new ObjectCallBack<>() {
//            @Override
//            public void onObjectRetrieved(Boolean object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
//                exist[0] = object;
//            }
//
//            @Override
//            public void onError(DatabaseError error) {
//
//            }
//        });
        teacherRef.child("inboxIDs").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Tool.handleEmpty(snapshot.exists(), rv_MailDropDown , tv_mailEmpty);
                if(snapshot.exists()){
                    String mailID = snapshot.getValue(String.class);
                    Mail m = new Mail();
                    m.retrieveOnce(new ObjectCallBack<>() {
                        @Override
                        public void onObjectRetrieved(MailFirebase object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                            final Mail[] mail = new Mail[1];
                            object.convertToNormal(new ObjectCallBack<>() {
                                @Override
                                public void onObjectRetrieved(Mail object) {
                                    mail[0] = object;
                                }

                                @Override
                                public void onError(DatabaseError error) {

                                }
                            });
                            inbox.add(mail[0]);
                            //updates the UI and the adapter in general
                            mailSmallAdapter.addMail(mail[0]);
                        }

                        @Override
                        public void onError(DatabaseError error) {

                        }
                    }, mailID);
                }

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



        teacherRef.child("notitficationIDs").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Tool.handleEmpty(snapshot.exists(), rv_NotifDropdown , tv_notifEmpty);
                if(snapshot.exists()){
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