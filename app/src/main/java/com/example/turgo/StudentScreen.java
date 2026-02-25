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
    private StudentScreenData ssd;
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
        NotificationPermissionHelper.requestIfNeeded(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        topAppBar = findViewById(R.id.tb_ss_topAppBar);
        ll_mailEmpty = findViewById(R.id.ll_MDD_EmptyState);
        ll_notifEmpty = findViewById(R.id.ll_NDD_EmptyState);
        bottomNav = findViewById(R.id.nv_ss_BottomNavigation);
        setScreenInteractionEnabled(false);

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
        if (fbUser == null) {
            Log.e("StudentScreen", "Firebase user is null");
            finish();
            return;
        }
        StudentRepository sr = new StudentRepository(fbUser.getUid());
        sr.loadScreenData()
                .addOnSuccessListener(data -> {
                    // Manual LITE hydration: populate only what's needed for this activity and its fragments.
                    // This avoids the recursive overhead of constructClass() while keeping the Student interface.
                    student = new Student();
                    student.setUid(data.uid);
                    student.setFullName(data.fullName);
                    student.setPfpCloudinary(data.pfpCloudinary);
                    student.setInboxIds(data.inboxIds);
                    student.setNotificationsIds(data.notificationIds);
                    student.setCompletionWeekKey(data.completionWeekKey);
                    student.setPercentageCompleted(data.percentageCompleted);
                    student.setScheduleCompletedThisWeekIds(data.scheduleCompletedThisWeekIds);
                    student.setUserType(UserType.STUDENT);

                    if (Tool.boolOf(student.getID())) {
                        PushTokenManager.syncKnownRoleWithLatestToken(student.getID(), "STUDENT");
                    }
                    continueAfterStudentNotNull();

                    // Weekly meeting completion logic
                    String currentWeekKey = Student.getCurrentWeekKey();
                    sr.resetWeeklyMeetingCompletionIfNeeded(currentWeekKey)
                            .addOnSuccessListener(changed -> {
                                if (Boolean.TRUE.equals(changed)) {
                                    student.setScheduleCompletedThisWeek(new ArrayList<>());
                                    student.setScheduleCompletedThisWeekIds(new ArrayList<>());
                                    student.setPercentageCompleted(0);
                                    student.setCompletionWeekKey(currentWeekKey);
                                    Log.d("StudentScreen", "Weekly meeting completion reset for week=" + currentWeekKey);
                                } else if (!Tool.boolOf(student.getCompletionWeekKey())) {
                                    student.setCompletionWeekKey(currentWeekKey);
                                }

                                // If the week matches, the DTO data is already correct.
                                // We don't need to re-calculate from meetingHistory (which isn't loaded).
                                if (!Boolean.TRUE.equals(changed)) {
                                    return;
                                }

                                // Only force an upsert if we just reset (changed == true)
                                ArrayList<String> emptyIds = new ArrayList<>();
                                sr.upsertWeeklyCompletionSnapshot(currentWeekKey, emptyIds, 0.0)
                                        .addOnFailureListener(e -> {
                                            Log.e("StudentScreen", "Failed weekly completion snapshot sync", e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("StudentScreen", "Failed weekly completion reset check", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("StudentScreen", "Failed to load Screen Data", e);
                    student = createFallbackStudent();
                    continueAfterStudentNotNull();
                });

    }

    private Student createFallbackStudent() {
        Student fallback = new Student();
        fallback.setUserType(UserType.STUDENT);
        if (fbUser != null) {
            fallback.setUid(fbUser.getUid());
            fallback.setEmail(fbUser.getEmail());
            String displayName = fbUser.getDisplayName();
            if (!Tool.boolOf(displayName)) {
                displayName = "Student";
            }
            fallback.setFullName(displayName);
        } else {
            fallback.setFullName("Student");
        }
        return fallback;
    }

    private void continueAfterStudentNotNull(){
        setScreenInteractionEnabled(true);
        prepareActivityUI();
        Intent intent = getIntent();
        if(intent.getBooleanExtra("showCourseJoined", false)){
            Bundle bundle = intent.getBundleExtra("bundleToCourseJoined");
            Tool.loadFragment(this, getContainer(), new CourseJoinedFullPage(), bundle);
            return;
        }
        loadDashboard();
    }

    private void setScreenInteractionEnabled(boolean enabled) {
        if (bottomNav != null) {
            bottomNav.setEnabled(enabled);
            bottomNav.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        }
    }
    private void prepareActivityUI() {
        // Initialize UI with lightweight data if available
        if (ssd != null) {
            initializeUIWithSSD();
        }
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
            case "StudentExploreJoined": return R.id.dest_studentMyCourses;
            case "Student_ScheduleMeetings": return R.id.dest_studentExploreCourses;
            case "Student_TaskList": return R.id.dest_studentTaskList;
            case "Profile": return R.id.dest_studentProfile;
            default: return -1;
        }
    }

    private void initializeUIWithSSD() {
        // Initialize UI with lightweight StudentScreenData
        if (ssd == null) {
            return;
        }

        String logTag = UserType.STUDENT.name() + "_SCREEN";
        Log.d(logTag, "========== initializeUIWithSSD START ==========");
        Log.d(logTag, UserType.STUDENT + (ssd.fullName != null ? ssd.fullName : "Student"));

        if (topAppBar == null) {
            topAppBar = findViewById(R.id.tb_ss_topAppBar);
        }
        if (topAppBar != null) {
            setSupportActionBar(topAppBar);
            if(getSupportActionBar() != null){
                getSupportActionBar().setTitle(ssd.fullName != null ? ssd.fullName : "Student");
                Log.d(logTag, "actionBar title set to: " + (ssd.fullName != null ? ssd.fullName : "Student"));
            }else{
                Log.e(logTag, "getSupportActionBar() returned Null!");
            }
        }
    }

    private void initializeUI() {
        String logTag = UserType.STUDENT.name() + "_SCREEN";

        Log.d(logTag, "========== initializeUI START ==========");
        Log.d(logTag, UserType.STUDENT + (ssd != null && ssd.fullName != null ? ssd.fullName : "Student"));

        topAppBar = findViewById(R.id.tb_ss_topAppBar);
        setSupportActionBar(topAppBar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(ssd != null && ssd.fullName != null ? ssd.fullName : "Student");
            Log.d(logTag, "actionBar title set to: " + (ssd != null && ssd.fullName != null ? ssd.fullName : "Student"));
        }else{
            Log.e(logTag, "getSupportActionBar() returned Null!");
        }

        setupBottomNavigation(bottomNav);
    }
    private void setupBottomNavigation(BottomNavigationView bnv){
        bnv.setOnItemSelectedListener(item ->{
            int itemId = item.getItemId();
            if(itemId == R.id.dest_studentExploreCourses){
                Tool.loadFragment(this, StudentScreen.getContainer(), new Student_ScheduleMeetings());
                return true;
            }
            if(itemId == R.id.dest_studentDashboard){
                Log.d("SetupBottomNav", "Student Dashboard Bot Nav Listener Triggered");
                loadDashboard();
                return true;
            }
            if(itemId == R.id.dest_studentMyCourses){
                Tool.loadFragment(this, StudentScreen.getContainer(), new StudentExploreJoined());
                return true;
            }
            if(itemId == R.id.dest_studentTaskList){
                Tool.loadFragment(this, StudentScreen.getContainer(), new Student_TaskList());
                return true;
            }
            if(itemId == R.id.dest_studentProfile){
                // Profile requires the full student object for editing capabilities
                Tool.loadFragment(this, StudentScreen.getContainer(), new Profile(student));
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
            rv_MailDropDown = mailView.findViewById(R.id.rv_MDD_MailDropDown);
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

            ll_notifEmpty = notifView.findViewById(R.id.ll_NDD_EmptyState);
            Tool.handleEmpty(notifs.isEmpty(), rv_NotifDropdown, ll_notifEmpty);
            setupNotifSeeAll(notifView);

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
        Button seeAll = mailView.findViewById(R.id.btn_MDD_ViewAllMail);
        if (seeAll != null) {
            seeAll.setOnClickListener(v -> {
                Intent intent = new Intent(StudentScreen.this, MailPageFull.class);
                // MailPageFull requires the full student object for complete functionality
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
        // Use student object if available, otherwise we can't prepare objects that require full student data
        if (student == null || student.getID() == null) {
            Log.e("StudentScreen", "Cannot prepare objects - student object or ID is null");
            return;
        }

        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.STUDENT.getPath()).child(student.getID());
        studentRef.child("inbox").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String mailID = snapshot.getValue(String.class);
                if (!Tool.boolOf(mailID)) {
                    Log.w("StudentScreen", "Skipping inbox child with empty mailID. key=" + snapshot.getKey());
                    return;
                }
                Mail m = new Mail();
                m.retrieveOnce(new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(MailFirebase object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                        inboxFirebase.add(object);
                        //updates the UI and the adapter in general
                        object.convertToNormal(new ObjectCallBack<>() {
                            @Override
                            public void onObjectRetrieved(Mail object) {
                                addMailToInbox(object);
                            }

                            @Override
                            public void onError(DatabaseError error) {
                                Log.e("StudentScreen", "Mail convertToNormal failed for mailID=" + mailID + ": " + error.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onError(DatabaseError error) {
                        Log.e("StudentScreen", "retrieveOnce failed for mailID=" + mailID + ": " + error.getMessage());
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

        attachNotificationListener(studentRef, "notitficationIDs");
        attachNotificationListener(studentRef, "notificationIDs");
        attachNotificationListener(studentRef, "notifications");
    }

    private void attachNotificationListener(DatabaseReference studentRef, String childPath) {
        studentRef.child(childPath).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String notifID = snapshot.getValue(String.class);
                if (!Tool.boolOf(notifID)) {
                    return;
                }
                Notification<?> n = new Notification<>();
                n.retrieveOnce(new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(NotificationFirebase object) {
                        addNotification(object);
                        LocalNotificationBridge.notifyIfNew(StudentScreen.this, object);
                    }

                    @Override
                    public void onError(DatabaseError error) {
                        Log.e("StudentScreen", "Failed loading notification id=" + notifID + ": " + error.getMessage());
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
                Log.e("StudentScreen", "Notification listener cancelled for path=" + childPath + ": " + error.getMessage());
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

    private void addMailToInbox(Mail mail) {
        if (mail == null) {
            return;
        }
        if (containsMail(mail.getMailID())) {
            return;
        }
        inbox.add(mail);
        if (mailSmallAdapter != null) {
            mailSmallAdapter.notifyItemInserted(inbox.size() - 1);
        }
        if (rv_MailDropDown != null && ll_mailEmpty != null) {
            Tool.handleEmpty(inbox.isEmpty(), rv_MailDropDown, ll_mailEmpty);
        }
    }

    private void setupNotifSeeAll(View notifView) {
        View notifSeeAll = notifView.findViewById(R.id.ib_NotifViewAll);
        if (notifSeeAll != null) {
            notifSeeAll.setOnClickListener(v -> {
                Intent intent = new Intent(StudentScreen.this, NotifPageFull.class);
                startActivity(intent);
                if (notifPopupWindow != null) {
                    notifPopupWindow.dismiss();
                }
            });
        }
    }

    private boolean containsMail(String mailId) {
        if (!Tool.boolOf(mailId)) {
            return false;
        }
        for (Mail existing : inbox) {
            if (existing != null && mailId.equals(existing.getMailID())) {
                return true;
            }
        }
        return false;
    }

    private void addNotification(NotificationFirebase notification) {
        if (notification == null) {
            return;
        }
        if (containsNotification(notification.getID())) {
            return;
        }
        notifs.add(notification);
        if (notifAdapter != null) {
            notifAdapter.notifyItemInserted(notifs.size() - 1);
        }
        if (rv_NotifDropdown != null && ll_notifEmpty != null) {
            Tool.handleEmpty(notifs.isEmpty(), rv_NotifDropdown, ll_notifEmpty);
        }
    }

    private boolean containsNotification(String notificationId) {
        if (!Tool.boolOf(notificationId)) {
            return false;
        }
        for (NotificationFirebase existing : notifs) {
            if (existing != null && notificationId.equals(existing.getID())) {
                return true;
            }
        }
        return false;
    }

    public boolean isInLoading() {
        return inLoading;
    }

    public void setInLoading(boolean inLoading) {
        this.inLoading = inLoading;
    }
}
