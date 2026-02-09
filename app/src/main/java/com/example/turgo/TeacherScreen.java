package com.example.turgo;

import android.annotation.SuppressLint;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turgo.databinding.ActivityTeacherScreenBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    private ActivityTeacherScreenBinding binding;

    private MailSmallAdapter mailSmallAdapter;
    private NotifAdapter notifAdapter;

    private ArrayList<NotificationFirebase> notifs = new ArrayList<>();
    private ArrayList<Mail> inbox = new ArrayList<>();

    private Toolbar topAppBar;
    private LinearLayout ll_mailEmpty, ll_notifEmpty;
    private PopupWindow mailPopupWindow, notifPopupWindow;
    private RecyclerView rv_MailDropDown, rv_NotifDropdown;
    private ImageButton notifFullPage;
    private BottomNavigationView bottomNav;
    private FirebaseUser fbUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTeacherScreenBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        topAppBar = findViewById(R.id.tb_ts_topAppBar);
        ll_mailEmpty = findViewById(R.id.ll_MDD_EmptyState);
        ll_notifEmpty = findViewById(R.id.ll_NDD_EmptyState);
        bottomNav = findViewById(R.id.nv_ts_BottomNavigation);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        fbUser = auth.getCurrentUser();

        Teacher dummy = new Teacher();
        dummy.setUserType(UserType.TEACHER);

        Tool.prepareUserObjectForScreen(this, dummy, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(User object) {
                teacher = (Teacher) object;
                if (teacher != null) {
                    prepareActivityUI();
                    loadDashboard();
                } else {
                    Log.e("TeacherScreen", "Teacher conversion failed");
                }
            }

            @Override
            public void onError(DatabaseError error) {
                Log.e("TeacherScreen", "Error retrieving teacher: " + error.getMessage());
            }
        });
    }

    private void prepareActivityUI() {
        initializeUI();
        prepareObjects();

        if (teacher != null && fbUser != null) {
            UserPresenceManager.startTracking(fbUser.getUid());
        }
    }

    private void loadDashboard() {
        Tool.loadFragment(this, getContainerId(), new TeacherDashboard());
    }

    private void initializeUI() {
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(teacher.getFullName());
        }

        setupBottomNavigation(bottomNav);
    }

    private void setupBottomNavigation(BottomNavigationView bnv) {
        bnv.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.dest_teacherDashboard) {
                loadDashboard();
                return true;
            }
            if (itemId == R.id.dest_teacherAllCourse) {
                Tool.loadFragment(this, getContainerId(), new TeacherAllCourse());
                return true;
            }
            if (itemId == R.id.dest_teacherCreateTask) {
                Tool.loadFragment(this, getContainerId(), new TeacherCreateTask());
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topbar_user_menu, menu);
        return true;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @SuppressLint("MissingInflatedId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Log.d("TeacherScreen", "Item Clicked: (" + itemId + ")");

        if (itemId == R.id.action_mail) {
            // Close notifications if open
            if (notifPopupWindow != null && notifPopupWindow.isShowing()) {
                Log.d("TeacherScreen", "(MAIL) Closing notif popup");
                notifPopupWindow.dismiss();
            }

            // Toggle mail popup
            if (mailPopupWindow != null && mailPopupWindow.isShowing()) {
                Log.d("TeacherScreen", "(MAIL) Closing mail popup");
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
                Log.d("TeacherScreen", "(NOTIF) Closing mail popup");
                mailPopupWindow.dismiss();
            }

            // Toggle notif popup
            if (notifPopupWindow != null && notifPopupWindow.isShowing()) {
                Log.d("TeacherScreen", "(NOTIF) Closing notif popup");
                notifPopupWindow.dismiss();
                return true;
            }

            // Create notif popup ✅ FIXED SIZING
            View notifView = getLayoutInflater().inflate(R.layout.notification_drop_down, null);
            rv_NotifDropdown = notifView.findViewById(R.id.rv_NotifDropDown);
            notifAdapter = new NotifAdapter(notifs);
            rv_NotifDropdown.setLayoutManager(new LinearLayoutManager(this));
            rv_NotifDropdown.setAdapter(notifAdapter);


            notifFullPage = notifView.findViewById(R.id.ib_NotifViewAll);
            notifFullPage.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherScreen.this, NotifPageFull.class);
            });
            LinearLayout ll_notifEmpty = notifView.findViewById(R.id.ll_NDD_EmptyState);
            Tool.handleEmpty(notifs.isEmpty(), rv_NotifDropdown, ll_notifEmpty);

            notifPopupWindow = createPopupWindow(notifView, R.id.action_notifications, "Notifications");

        } else if (itemId == R.id.action_signOut) {
            Log.d("TeacherScreen", "(SIGNOUT) Sign out clicked");
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
            Log.d("TeacherScreen", tag + " popup shown at anchor");
        } else {
            Log.e("PopupError", tag + " anchor not found");
        }

        return popup;
    }

    private void setupMailSeeAll(View mailView) {
        Button seeAll = mailView.findViewById(R.id.btn_ViewAllMail);
        if (seeAll != null) {
            seeAll.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherScreen.this, MailPageFull.class);
                intent.putExtra(User.SERIALIZE_KEY_CODE, teacher);
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


    public static int getContainerId() {
        return R.id.nhf_ts_FragmentContainer;
    }

    public void prepareObjects() {
        DatabaseReference teacherRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.TEACHER.getPath())
                .child(teacher.getID());

        teacherRef.child("inboxIDs").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Tool.handleEmpty(snapshot.exists(), rv_MailDropDown, ll_mailEmpty);
                if (!snapshot.exists()) return;

                String mailID = snapshot.getValue(String.class);
                Mail m = new Mail();
                m.retrieveOnce(new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(MailFirebase object) throws ParseException {
                        try {
                            object.convertToNormal(new ObjectCallBack<>() {
                                @Override
                                public void onObjectRetrieved(Mail mail) {
                                    inbox.add(mail);
                                    mailSmallAdapter.addMail(mail);
                                }

                                @Override
                                public void onError(DatabaseError error) { }
                            });
                        } catch (InvocationTargetException | NoSuchMethodException |
                                 IllegalAccessException | InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(DatabaseError error) { }
                }, mailID);
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        teacherRef.child("notitficationIDs").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Tool.handleEmpty(snapshot.exists(), rv_NotifDropdown, ll_notifEmpty);
                if (!snapshot.exists()) return;

                String notifID = snapshot.getValue(String.class);
                Notification<?> n = new Notification<>();
                n.retrieveOnce(new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(NotificationFirebase object) {
                        notifs.add(object);
                        notifAdapter.addNotification(object);
                    }

                    @Override
                    public void onError(DatabaseError error) { }
                }, notifID);
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public Teacher getTeacher() {
        return teacher;
    }
}
