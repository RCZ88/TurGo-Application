package com.example.turgo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
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
    private Toolbar topAppBar;
    private ArrayList<MailFirebase> inboxFirebase = new ArrayList<>();
    private ArrayList<NotificationFirebase>notifs = new ArrayList<>();
    private MailSmallAdapter mailSmallAdapter;
    private NotifAdapter notifAdapter;
    private BottomNavigationView bottomNav;
    private ArrayList<Mail> inbox = new ArrayList<>();
    private PopupWindow mailPopupWindow, notifPopupWindow;
    private TextView tv_mailEmpty, tv_notifEmpty;
    private boolean inLoading = false;
    RecyclerView rv_MailDropDown, rv_NotifDropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStudentScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tv_mailEmpty = findViewById(R.id.tv_MDD_MailEmpty);
        tv_notifEmpty = findViewById(R.id.tv_NDD_NotifEmpty);
        bottomNav = findViewById(R.id.nv_ss_BottomNavigation);

        Class<? extends RequiresDataLoading> fragmentToLoad = (Class<? extends RequiresDataLoading>)getIntent().getSerializableExtra("FragmentToLoad");
        Bundle bundleExtra = getIntent().getExtras();
        int bottomNavMenuId = getIntent().getIntExtra("BottomNavToSelect", -1);
        Log.d("StudentScreen", "BottomNavMenuId Loaded: " + bottomNavMenuId);
        if(fragmentToLoad != null && bundleExtra != null){
            //After Data Loading.
            Log.d("StudentScreen", "FragmentToLoad not Null!");
            student =  (Student) getIntent().getSerializableExtra("FullUserObject");
            prepareActivityUI();
            navigateToFragment(fragmentToLoad, bundleExtra, bottomNavMenuId);
        }else{
            Log.d("StudentScreen", "FragmentToLoad is Null, Redirecting to Dashboard!");
            //load the student once. ui can come on every new activity.
            if(!Tool.boolOf(student)){
                Student dummy = new Student();
                dummy.setUserType(UserType.STUDENT);
                Tool.prepareUserObjectForScreen(this, dummy, new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(User object) {
                        student = (Student) object;
                        Log.d("StudentScreen", "Student Normal Object retrieved: " + student);
                        if (student == null) {
                            Log.d("StudentScreen", "Error Converting StudentFirebase");
                        }else{
                            loadDashboard();
                        }
                    }

                    @Override
                    public void onError(DatabaseError error) {

                    }
                });
            }

        }



    }
    private void prepareActivityUI(){
        runOnUiThread(() -> {
            try {
                initializeUI();
                FirebaseAuth auth = FirebaseAuth.getInstance();
                fbUser = auth.getCurrentUser();
                Log.d("StudentScreen", "fbUser: " + (fbUser != null ? fbUser.getUid() : "NULL"));

                prepareObjects();

                if (student != null && fbUser != null) {
                    UserPresenceManager.startTracking(fbUser.getUid());
                    Log.d("StudentScreen", "Started tracking user presence");
                }

                Log.d("StudentScreen", "========== initializeUI END ==========");
            } catch (ParseException | InvocationTargetException |
                     NoSuchMethodException | IllegalAccessException |
                     InstantiationException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void loadDashboard(){
        Bundle bundle = new Bundle();
        Meeting nextMeeting = student.getNextMeeting();
        bundle.putSerializable("nextMeeting", nextMeeting);
        bundle.putSerializable("student", student);
        DataLoading.loadAndNavigate(this, Student_Dashboard.class, bundle, true, this.getClass(), student);
    }
    private void navigateToFragment(Class<? extends RequiresDataLoading> fragmentClass, Bundle bundle, int bottomNavToSelect){
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            Log.d("NavigateToFragment", "String converted to Fragment Successfully: " + fragment.getClass());
            if(bundle != null){
                fragment.setArguments(bundle);
            }
            int menuId = getMenuIdForFragment(fragmentClass.getName());
            inLoading = true;
            if(menuId != -1){
                Log.d("NavigateToFragment", "View ("+ fragmentClass +") is part of Bottom Nav");
                bottomNav.setSelectedItemId(menuId);

                Log.d("NavigateToFragment", "Menu Id Selected: " + menuId);
            }else{
                Log.d("NavigateToFragment", "View (" + fragmentClass + ") is Not part of Bottom Nav");
                if(bottomNavToSelect != -1){
                    bottomNav.setSelectedItemId(bottomNavToSelect);
                }
            }
            Tool.loadFragment(this, R.id.nhf_ss_FragContainer, fragment);
            inLoading =false;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

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

    private void initializeUI() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        String logTag = student.getUserType().name() + "_SCREEN";

        Log.d(logTag, "========== initializeUI START ==========");
        Log.d(logTag, student.getUserType() + student.getFullName());


        Toolbar topAppBar = findViewById(R.id.tb_ss_topAppBar);

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
            if(inLoading){
                return true;
            }
            int itemId = item.getItemId();
            if(itemId == R.id.dest_studentExploreCourses){
                Bundle bundle = new Bundle();
                bundle.putSerializable(Student.SERIALIZE_KEY_CODE, student);
                DataLoading.loadAndNavigate(this, Student_ExploreCourse.class, bundle, true, this.getClass(), student);
                return true;
            }
            if(itemId == R.id.dest_studentDashboard){
                Log.d("SetupBottomNav", "Student Dashboard Bot Nav Listener Triggered");
                loadDashboard();
                return true;
            }
            if(itemId == R.id.dest_studentMyCourses){
                Bundle bundle = new Bundle();
                bundle.putSerializable(Student.SERIALIZE_KEY_CODE, student);
                DataLoading.loadAndNavigate(this, Student_MyCourses.class, bundle, true, this.getClass(), student);
                return true;
            }
            if(itemId == R.id.dest_studentTaskList){
                Tool.loadFragment(this, R.id.nhf_ss_FragContainer, new Student_TaskList());
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
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
                Intent intent = new Intent(StudentScreen.this, MailPageFull.class);
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
                Tool.handleEmpty(snapshot.exists(), rv_MailDropDown , tv_mailEmpty);
                String mailID = snapshot.getValue(String.class);
                Mail m = new Mail();
                m.retrieveOnce(new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(MailFirebase object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                        inboxFirebase.add(object);
                        //updates the UI and the adapter in general
                        final Mail[] mail = new Mail[1];
                        object.convertToNormal(new ObjectCallBack<Mail>() {
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
                Tool.handleEmpty(snapshot.exists(), rv_NotifDropdown , tv_notifEmpty);
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