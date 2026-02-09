package com.example.turgo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turgo.databinding.ActivityStudentScreenBinding;
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

public class StudentScreen extends AppCompatActivity{

    private ActivityStudentScreenBinding binding;
    private Student student;
    private FirebaseUser fbUser;
    private NavHostFragment navHostFragment;
    private BottomNavigationView navView;

    private LinearLayout ll_mailEmpty, ll_notifEmpty;
    private Toolbar topAppBar;
    private ArrayList<MailFirebase> inboxFirebase = new ArrayList<>();
    private ArrayList<NotificationFirebase>notifs = new ArrayList<>();
    private MailSmallAdapter mailSmallAdapter;
    private NotifAdapter notifAdapter;
    private BottomNavigationView bottomNav;
    private ArrayList<Mail> inbox = new ArrayList<>();

    private PopupWindow mailPopupWindow, notifPopupWindow;
    private boolean inLoading = false;
    RecyclerView rv_MailDropDown, rv_NotifDropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStudentScreenBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        topAppBar = findViewById(R.id.tb_ss_topAppBar);
        ll_mailEmpty = findViewById(R.id.ll_MDD_EmptyState);
        ll_notifEmpty = findViewById(R.id.ll_NDD_EmptyState);
        bottomNav = findViewById(R.id.nv_ss_BottomNavigation);

        FirebaseAuth  auth = FirebaseAuth.getInstance();
        fbUser = auth.getCurrentUser();

        Student dummy = new Student();
        dummy.setUserType(UserType.STUDENT);

        Intent intent = getIntent();
        student = (Student)intent.getSerializableExtra("Student_RequireUpdate");
        if(student != null){
            continueAfterStudentNotNull();
            return;
        }
        Tool.prepareUserObjectForScreen(this, dummy, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(User object) {
                student = (Student) object;
                Log.d("StudentScreen", "Student Normal Object retrieved: " + student);
                if (student != null) {
                    continueAfterStudentNotNull();
                }else{
                    loadDashboard();
                }
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });

    }

    private void continueAfterStudentNotNull(){
        prepareActivityUI();
        Intent intent = getIntent();
        if(intent.getBooleanExtra("showCourseJoined", false)){
            Bundle bundle = intent.getBundleExtra("bundleToCourseJoined");
            Tool.loadFragment(this, getContainer(), new CourseJoinedFullPage(), bundle);
            return;
        }
        loadDashboard();
    }
    private void prepareActivityUI() {
        initializeUI();
        prepareObjects();

        if (student != null && fbUser != null) {
            UserPresenceManager.startTracking(fbUser.getUid());
            Log.d("StudentScreen", "Started tracking user presence");
        }
    }
    private void loadDashboard(){
        Tool.loadFragment(this, R.id.nhf_ss_FragContainer, new Student_Dashboard());
    }
    public static int getContainer(){
        return R.id.nhf_ss_FragContainer;
    }

    public static int getMenuIdForFragment(String fragmentClassName) {
        String className = fragmentClassName.substring(fragmentClassName.lastIndexOf(".") + 1);
        switch (className) {
            case "Student_Dashboard": return R.id.dest_studentDashboard;
            case "Student_ExploreCourse": return R.id.dest_studentExploreCourses;
            case "Student_MyCourses": return R.id.dest_studentMyCourses;
            case "Student_TaskList": return R.id.dest_studentTaskList;
            case "Profile": return R.id.dest_studentProfile;
            default: return -1;
        }
    }

    private void initializeUI() {
        String logTag = student.getUserType().name() + "_SCREEN";

        Log.d(logTag, "========== initializeUI START ==========");
        Log.d(logTag, student.getUserType() + student.getFullName());


        topAppBar = findViewById(R.id.tb_ss_topAppBar);
        setSupportActionBar(topAppBar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(student.getFullName());
            Log.d(logTag, "actionBar title set to: " + student.getFullName());
        }else{
            Log.e(logTag, "getSupportActionBar() returned Null!");
        }

        setupBottomNavigation(bottomNav);
    }
    private void setupBottomNavigation(BottomNavigationView bnv){
        bnv.setOnItemSelectedListener(item ->{
            int itemId = item.getItemId();
            if(itemId == R.id.dest_studentExploreCourses){
                Tool.loadFragment(this, StudentScreen.getContainer(), new Student_ExploreCourse());
                return true;
            }
            if(itemId == R.id.dest_studentDashboard){
                Log.d("SetupBottomNav", "Student Dashboard Bot Nav Listener Triggered");
                loadDashboard();
                return true;
            }
            if(itemId == R.id.dest_studentMyCourses){
                Tool.loadFragment(this, StudentScreen.getContainer(), new Student_MyCourses());
                return true;
            }
            if(itemId == R.id.dest_studentTaskList){
                Tool.loadFragment(this, StudentScreen.getContainer(), new Student_TaskList());
                return true;
            }
            return false;
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Log.d("StudentScreen", "Item Clicked: (" + itemId + ")");

        if (itemId == R.id.action_mail) {
            // Close notifications if open
            if (notifPopupWindow != null && notifPopupWindow.isShowing()) {
                Log.d("StudentScreen", "(MAIL) Closing notif popup");
                notifPopupWindow.dismiss();
            }

            // Toggle mail popup
            if (mailPopupWindow != null && mailPopupWindow.isShowing()) {
                Log.d("StudentScreen", "(MAIL) Closing mail popup");
                mailPopupWindow.dismiss();
                return true;
            }

            // Create mail popup ✅ FIXED SIZING
            View mailView = getLayoutInflater().inflate(R.layout.mail_drop_down, null);
            rv_MailDropDown = mailView.findViewById(R.id.rv_MailDropDown);
            mailSmallAdapter = new MailSmallAdapter(fbUser.getUid(), inbox, false);
            rv_MailDropDown.setLayoutManager(new LinearLayoutManager(this));
            rv_MailDropDown.setAdapter(mailSmallAdapter);

            LinearLayout ll_mailEmpty = mailView.findViewById(R.id.ll_MDD_EmptyState);
            Tool.handleEmpty(inbox.isEmpty(), rv_MailDropDown, ll_mailEmpty);

            mailPopupWindow = createPopupWindow(mailView, R.id.action_mail, "Mail");
            setupMailSeeAll(mailView);

        } else if (itemId == R.id.action_notifications) {
            // Close mail if open
            if (mailPopupWindow != null && mailPopupWindow.isShowing()) {
                Log.d("StudentScreen", "(NOTIF) Closing mail popup");
                mailPopupWindow.dismiss();
            }

            // Toggle notif popup
            if (notifPopupWindow != null && notifPopupWindow.isShowing()) {
                Log.d("StudentScreen", "(NOTIF) Closing notif popup");
                notifPopupWindow.dismiss();
                return true;
            }

            // Create notif popup ✅ FIXED SIZING
            View notifView = getLayoutInflater().inflate(R.layout.notification_drop_down, null);
            rv_NotifDropdown = notifView.findViewById(R.id.rv_NotifDropDown);
            notifAdapter = new NotifAdapter(notifs);
            rv_NotifDropdown.setLayoutManager(new LinearLayoutManager(this));
            rv_NotifDropdown.setAdapter(notifAdapter);

            LinearLayout ll_notifEmpty = notifView.findViewById(R.id.ll_NDD_EmptyState);
            Tool.handleEmpty(notifs.isEmpty(), rv_NotifDropdown, ll_notifEmpty);

            notifPopupWindow = createPopupWindow(notifView, R.id.action_notifications, "Notifications");

        } else if (itemId == R.id.action_signOut) {
            Log.d("StudentScreen", "(SIGNOUT) Sign out clicked");
            dismissPopups();
            return Tool.logOutUI(this, fbUser);
        }

        return super.onOptionsItemSelected(item);
    }

    private PopupWindow createPopupWindow(View contentView, int anchorId, String tag) {
        // ✅ FIXED DIMENSIONS - Works with RecyclerView
        PopupWindow popup = new PopupWindow(contentView,
                dpToPx(340), ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setElevation(12f);
        popup.setFocusable(true);
        popup.setOutsideTouchable(true);

        // ✅ MEASURE content first (fixes WRAP_CONTENT bug)
        contentView.measure(
                View.MeasureSpec.makeMeasureSpec(dpToPx(340), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        popup.setHeight(contentView.getMeasuredHeight());

        // Show at anchor
        View anchor = findViewById(anchorId);
        if (anchor != null) {
            popup.showAsDropDown(anchor, -dpToPx(20), dpToPx(8));
            Log.d("StudentScreen", tag + " popup shown at anchor");
        } else {
            Log.e("PopupError", tag + " anchor not found");
        }

        return popup;
    }

    private void setupMailSeeAll(View mailView) {
        Button seeAll = mailView.findViewById(R.id.btn_ViewAllMail);
        if (seeAll != null) {
            seeAll.setOnClickListener(v -> {
                Intent intent = new Intent(StudentScreen.this, MailPageFull.class);
                intent.putExtra(User.SERIALIZE_KEY_CODE, student);
                startActivity(intent);
                if (mailPopupWindow != null) mailPopupWindow.dismiss();
            });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topbar_user_menu, menu);
        return true;
    }

    public void prepareObjects(){
        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.STUDENT.getPath()).child(student.getID());
        studentRef.child("inboxIDs").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String mailID = snapshot.getValue(String.class);
                Mail m = new Mail();
                m.retrieveOnce(new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(MailFirebase object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                        inboxFirebase.add(object);
                        //updates the UI and the adapter in general
                        final Mail[] mail = new Mail[1];
                        object.convertToNormal(new ObjectCallBack<>() {
                            @Override
                            public void onObjectRetrieved(Mail object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                                mail[0] = object;
                            }

                            @Override
                            public void onError(DatabaseError error) {

                            }
                        });
                        inbox.add(mail[0]);
                        mailSmallAdapter.addMail(mail[0]);
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

//    public void syncUser(){
//        RequireUpdate.retrieveUser(student.getID(), new ObjectCallBack<>() {
//            @Override
//            public void onObjectRetrieved(Object object) {
//                student = (StudentFirebase) object;
//            }
//
//            @Override
//            public void onError(DatabaseError error) {
//
//            }
//        });
//    }


    public Student getStudent() {
        Log.d("StudentScreen(getStudent)", "Returning Student: " + student);
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public ActivityStudentScreenBinding getBinding() {
        return binding;
    }

    public void setBinding(ActivityStudentScreenBinding binding) {
        this.binding = binding;
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

//    public StudentFirebase getStudent() {
//        return student;
//    }
//
//    public void setStudent(StudentFirebase student) {
//        this.student = student;
//    }

    public Toolbar getTopAppBar() {
        return topAppBar;
    }

    public void setTopAppBar(Toolbar topAppBar) {
        this.topAppBar = topAppBar;
    }

    public ArrayList<MailFirebase> getInboxFirebase() {
        return inboxFirebase;
    }

    public void setInboxFirebase(ArrayList<MailFirebase> inboxFirebase) {
        this.inboxFirebase = inboxFirebase;
    }

    public ArrayList<NotificationFirebase> getNotifs() {
        return notifs;
    }

    public void setNotifs(ArrayList<NotificationFirebase> notifs) {
        this.notifs = notifs;
    }

    public MailSmallAdapter getMailSmallAdapter() {
        return mailSmallAdapter;
    }

    public void setMailSmallAdapter(MailSmallAdapter mailSmallAdapter) {
        this.mailSmallAdapter = mailSmallAdapter;
    }

    public NotifAdapter getNotifAdapter() {
        return notifAdapter;
    }

    public void setNotifAdapter(NotifAdapter notifAdapter) {
        this.notifAdapter = notifAdapter;
    }
}