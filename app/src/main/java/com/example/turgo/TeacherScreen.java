package com.example.turgo;

import android.annotation.SuppressLint;
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
import androidx.fragment.app.Fragment;
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
import com.google.firestore.bundle.BundleElement;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class TeacherScreen extends AppCompatActivity {
    private Teacher teacher;
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
    boolean isReturningFromLoading;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTeacherScreenBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_teacher_screen);

        topAppBar = findViewById(R.id.tb_ts_topAppBar);
        Log.d("TeacherScreen", "topAppBar found: " + (topAppBar != null));

        tv_mailEmpty = findViewById(R.id.tv_MDD_MailEmpty);
        tv_notifEmpty = findViewById(R.id.tv_NDD_NotifEmpty);

        findViewById(android.R.id.content)
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    View container = findViewById(R.id.nhf_ts_FragmentContainer);
                    Log.d("LAYOUT", "Container real size: " +
                            container.getWidth() + " × " + container.getHeight());
                });
        Teacher dummy = new Teacher();
        dummy.setUserType(UserType.TEACHER);
        Log.d("TeacherScreen", "Teacher Obj" + dummy);

        AppCompatActivity activity = this;
        Tool.prepareUserObjectForScreen(this, dummy, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(User object) {
                teacher = (Teacher) object;
                if (teacher != null) {
                    Log.d("TeacherScreen", "Teacher: " + teacher);
                    runOnUiThread(() -> {
                        try {
                            initializeUI();
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            fbUser = auth.getCurrentUser();
                            Log.d("TeacherScreen", "fbUser: " + (fbUser != null ? fbUser.getUid() : "NULL"));

                            prepareObjects();

                            if (teacher != null && fbUser != null) {
                                UserPresenceManager.startTracking(fbUser.getUid());
                                Log.d("TeacherScreen", "Started tracking user presence");
                            }

                            String fragmentToLoad = getIntent().getStringExtra("FragmentToLoad");
                            Bundle bundleExtra = getIntent().getExtras();
                            if(fragmentToLoad != null){
                                navigateToFragment(fragmentToLoad, bundleExtra);
                            }else{
                                navigateToFragment("TeacherDashboard", null);
                            }

                            Log.d("TeacherScreen", "========== initializeUI END ==========");
                        }catch(Exception e){

                        }
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

    private int getMenuIdForFragment(String fragmentClassName) {
        switch (fragmentClassName) {
            case "Student_Dashboard": return R.id.dest_studentDashboard;
            case "student_ExploreCourse": return R.id.dest_studentExploreCourses;
            case "Student_MyCourses": return R.id.dest_studentMyCourses;
            case "Student_TaskList": return R.id.dest_studentTaskList;
            case "Profile": return R.id.dest_studentProfile;
            default: return -1;
        }
    }

    private void navigateToFragment(String fragmentClass, Bundle bundle){
        try {
            Fragment fragment = (Fragment) Class.forName("com.example.turgo." + fragmentClass).newInstance();
            if(bundle != null){
                fragment.setArguments(bundle);
            }
            int menuId = getMenuIdForFragment(fragmentClass);
            if(menuId != -1){
                isReturningFromLoading = true;
                navView.setSelectedItemId(menuId);
                isReturningFromLoading = false;
            }
            Tool.loadFragment(this, R.id.nhf_ss_FragContainer, fragment);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }

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

        setupBottomNavigation(navView);


    }

    private void setupBottomNavigation(BottomNavigationView bnv){
        bnv.setOnItemSelectedListener(item ->{
            if(isReturningFromLoading){
                return true;
            }
            int itemId = item.getItemId();
            if(itemId == R.id.dest_teacherDashboard){
                Bundle bundle = new Bundle();
                bundle.putSerializable(Teacher.SERIALIZE_KEY_CODE, teacher);
                DataLoading.loadAndNavigate(this, TeacherDashboard.class, bundle, true, "TeacherScreen");
                return true;
            }
            if(itemId == R.id.dest_teacherAllCourse){
                Tool.loadFragment(this, R.id.nhf_ts_FragmentContainer, new TeacherAllCourse());
                return true;
            }
            if(itemId == R.id.dest_teacherCreateTask){
                Tool.loadFragment(this, R.id.nhf_ts_FragmentContainer, new TeacherCreateTask());
            }
            return false;
        });
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