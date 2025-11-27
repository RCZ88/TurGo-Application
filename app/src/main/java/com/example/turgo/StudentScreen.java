package com.example.turgo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turgo.databinding.ActivityStudentScreenBinding;
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

public class StudentScreen extends AppCompatActivity{

    private ActivityStudentScreenBinding binding;
    private StudentFirebase student;
    private FirebaseUser fbUser;
    private NavHostFragment navHostFragment;
    private BottomNavigationView navView;
    private Toolbar topAppBar;
    private ArrayList<MailFirebase> inboxFirebase = new ArrayList<>();
    private ArrayList<NotificationFirebase>notifs = new ArrayList<>();
    private MailSmallAdapter mailSmallAdapter;
    private NotifAdapter notifAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStudentScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navView = findViewById(R.id.nv_ss_BottomNavigation);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dest_studentDashboard, R.id.dest_studentMyCourses, R.id.dest_studentExploreCourses, R.id.dest_studentProfile)
                .build();
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nhf_ss_FragContainer);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        FirebaseApp.initializeApp(this);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        fbUser = auth.getCurrentUser();

        student = (StudentFirebase) getIntent().getSerializableExtra(Student.SERIALIZE_KEY_CODE);
        if(student != null){

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
                        .replace(R.id.nhf_ss_FragContainer, cjfp)
                        .addToBackStack(null)
                        .commit();
            }else if(fragmentToShow.equals(PageNames.STUDENT_DASHBOARD.getPageName())){
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nhf_ss_FragContainer, new Student_Dashboard())
                        .addToBackStack(null)
                        .commit();
            }
        }
        syncUser();
        prepareObjects();
        topAppBar = findViewById(R.id.tb_ss_topAppBar);
        topAppBar.setTitle(student.getFullName());
        setSupportActionBar(topAppBar);
        if(student != null){
            UserPresenceManager.startTracking(fbUser.getUid());
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId() == R.id.action_mail){
            View popDownView = getLayoutInflater().inflate(R.layout.mail_drop_down, null);

            PopupWindow popupWindow = new PopupWindow(popDownView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            RecyclerView rv_MailDropDown = popDownView.findViewById(R.id.rv_MailDropDown);
            ArrayList<Mail> inbox;
            try {
                final Student[] studentNormal = new Student[1];
                student.convertToNormal(new ObjectCallBack<Student>() {
                    @Override
                    public void onObjectRetrieved(Student object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                        studentNormal[0] = object;
                    }

                    @Override
                    public void onError(DatabaseError error) {

                    }
                });
                inbox = new ArrayList<>(studentNormal[0].getInbox());
            } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
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
                Intent intent = new Intent(StudentScreen.this, MailPageFull.class);
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
                        object.convertToNormal(new ObjectCallBack<Mail>() {
                            @Override
                            public void onObjectRetrieved(Mail object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                                mail[0] = object;
                            }

                            @Override
                            public void onError(DatabaseError error) {

                            }
                        });
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
//        studentRef.child("notitficationIDs").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Notification<?>n= new Notification<>();
//                for(DataSnapshot ds : snapshot.getChildren()){
//                    n.retrieveOnce(new ObjectCallBack<NotificationFirebase>() {
//                        @Override
//                        public void onObjectRetrieved(NotificationFirebase object) {
//                            notif.add(ds.getValue(NotificationFirebase.class));
//                        }
//
//                        @Override
//                        public void onError(DatabaseError error) {
//
//                        }
//                    }, ds.getValue(String.class));
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

    }

    public void syncUser(){
        RequireUpdate.retrieveUser(student.getID(), new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(Object object) {
                student = (StudentFirebase) object;
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
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

    public StudentFirebase getStudent() {
        return student;
    }

    public void setStudent(StudentFirebase student) {
        this.student = student;
    }

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